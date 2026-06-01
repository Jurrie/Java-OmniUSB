/*
 * Copyright (C) 2011 Klaus Reimer
 * Copyright (C) 2014 Jesse Caulfield
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package javax.usb3.ri;

import java.util.List;

import javax.usb3.IUsbConfiguration;
import javax.usb3.IUsbControlIrp;
import javax.usb3.IUsbDevice;
import javax.usb3.IUsbEndpoint;
import javax.usb3.IUsbInterface;
import javax.usb3.IUsbIrp;
import javax.usb3.IUsbPipe;
import javax.usb3.event.IUsbPipeListener;
import javax.usb3.event.UsbPipeDataEvent;
import javax.usb3.event.UsbPipeErrorEvent;
import javax.usb3.exception.UsbException;
import javax.usb3.exception.UsbNotActiveException;
import javax.usb3.exception.UsbNotClaimedException;
import javax.usb3.exception.UsbNotOpenException;

/**
 * Implementation of IUsbUsbPipe.
 *
 * @author Klaus Reimer
 * @author Jesse Caulfield
 */
public abstract class UsbPipe implements IUsbPipe, AutoCloseable
{
	/**
	 * The endpoint this pipe belongs to.
	 */
	private final UsbEndpoint endpoint;

	/**
	 * The USB pipe listeners.
	 */
	private final UsbPipeListenerList listeners = new UsbPipeListenerList();

	/**
	 * The USB I/O Request Packet (IRP) queue manager.
	 */
	private UsbIrpQueue irpQueue;

	/**
	 * Construct a new USB Pipe attached to the indicated UsbEndpoint.
	 *
	 * @param endpoint The endpoint this pipe belongs to.
	 */
	protected UsbPipe(final UsbEndpoint endpoint)
	{
		this.endpoint = endpoint;
	}

	protected abstract UsbIrpQueue createIrpQueue();

	/**
	 * Returns the USB device.
	 *
	 * @return The USB device.
	 */
	public IUsbDevice getDevice()
	{
		return endpoint.getUsbInterface().getUsbConfiguration().getUsbDevice();
	}

	/**
	 * Ensures the pipe is active.
	 *
	 * @throws UsbException
	 *
	 * @throws UsbNotActiveException When pipe is not active
	 */
	private void checkActive() throws UsbNotActiveException
	{
		if (!isActive())
		{
			throw new UsbNotActiveException("Pipe is not active.");
		}
	}

	/**
	 * Ensures the interface is active.
	 *
	 * @throws UsbNotClaimedException When interface is not claimed.
	 */
	private void checkClaimed()
	{
		if (!endpoint.getUsbInterface().isClaimed())
		{
			throw new UsbNotClaimedException("Interface is not claimed.");
		}
	}

	/**
	 * Ensures the device is connected.
	 *
	 * @throws UsbDisconnectedException When device has been disconnected.
	 */
	// private void checkConnected() { getDevice().checkConnected(); }
	/**
	 * Ensures the pipe is open.
	 *
	 * @throws UsbNotOpenException When pipe is not open.
	 */
	private void checkOpen() throws UsbNotOpenException
	{
		if (!isOpen())
		{
			throw new UsbNotOpenException("Pipe is not open.");
		}
	}

	/**
	 * @inerit
	 *
	 * @throws UsbException if the Pipe is already open
	 */
	@Override
	public void open() throws UsbException
	{
		checkActive();
		checkClaimed();
		// checkConnected();
		if (isOpen())
		{
			throw new UsbException("Pipe is already open");
		}

		irpQueue = createIrpQueue();
	}

	/**
	 * @inerit
	 *
	 * @throws UsbException if the Pipe is already closed or the Pipe is still
	 *             busy
	 */
	@Override
	public void close() throws UsbException
	{
		checkActive();
		checkClaimed();
		// checkConnected();
		if (!isOpen())
		{
			throw new UsbException("Pipe is already closed");
		}
		if (irpQueue.isBusy())
		{
			throw new UsbException("Pipe is still busy");
		}

		try
		{
			irpQueue.close();
			irpQueue.join();
		}
		catch (InterruptedException e)
		{
			// We are already closing
			Thread.currentThread().interrupt();
		}
		finally
		{
			irpQueue = null;
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws UsbException
	 */
	@Override
	public boolean isActive()
	{
		final IUsbInterface iface = endpoint.getUsbInterface();
		final IUsbConfiguration config = iface.getUsbConfiguration();
		return iface.isActive() && config.isActive();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isOpen()
	{
		return irpQueue != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IUsbEndpoint getUsbEndpoint()
	{
		return endpoint;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int syncSubmit(final byte[] data) throws UsbException
	{
		final IUsbIrp irp = asyncSubmit(data);
		irp.waitUntilComplete();
		if (irp.isUsbException())
		{
			throw irp.getUsbException();
		}
		return irp.getActualLength();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws UsbException
	 *
	 * @throws IllegalArgumentException if data is null
	 */
	@Override
	public IUsbIrp asyncSubmit(final byte[] data) throws UsbException
	{
		if (data == null)
		{
			throw new IllegalArgumentException("USB I/O Request Packet (IRP) data must not be null");
		}
		final IUsbIrp irp = createUsbIrp();
		irp.setAcceptShortPacket(true);
		irp.setData(data);
		asyncSubmit(irp);
		return irp;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if IRP is null
	 */
	@Override
	public void syncSubmit(final IUsbIrp irp) throws UsbException
	{
		if (irp == null)
		{
			throw new IllegalArgumentException("USB I/O Request Packet (IRP) must not be null");
		}
		asyncSubmit(irp);
		irp.waitUntilComplete();
		if (irp.isUsbException())
		{
			throw irp.getUsbException();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws UsbException
	 *
	 * @throws IllegalArgumentException if IRP is null
	 */
	@Override
	public void asyncSubmit(final IUsbIrp irp) throws UsbException
	{
		if (irp == null)
		{
			throw new IllegalArgumentException("USB I/O Request Packet (IRP) must not be null");
		}
		checkActive();
		// checkConnected();
		checkOpen();
		irpQueue.add(irp);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if IRP is null
	 */
	@Override
	public void syncSubmit(final List<IUsbIrp> list) throws UsbException
	{
		for (final IUsbIrp irp : list)
		{
			syncSubmit(irp);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws UsbException
	 */
	@Override
	public void asyncSubmit(final List<IUsbIrp> list) throws UsbException
	{
		for (final IUsbIrp irp : list)
		{
			asyncSubmit(irp);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws UsbException
	 */
	@Override
	public void abortAllSubmissions() throws UsbNotActiveException, UsbNotOpenException
	{
		checkActive();
		// checkConnected();
		checkOpen();
		irpQueue.abortAllSubmissions();
	}

	@Override
	public void abortSubmission(final IUsbIrp irp) throws UsbNotActiveException, UsbNotOpenException
	{
		checkActive();
		// checkConnected();
		checkOpen();
		irpQueue.abortSubmission(irp);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IUsbIrp createUsbIrp()
	{
		return new UsbIrp();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IUsbControlIrp createUsbControlIrp(final byte bmRequestType,
			final byte bRequest,
			final short wValue,
			final short wIndex)
	{
		return new UsbControlIrp(bmRequestType, bRequest, wValue, wIndex);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addUsbPipeListener(final IUsbPipeListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeUsbPipeListener(final IUsbPipeListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * Sends event to all event listeners.
	 *
	 * @param irp Then request package
	 */
	public void sendEvent(final IUsbIrp irp)
	{
		if (irp.isUsbException())
		{
			listeners.errorEventOccurred(new UsbPipeErrorEvent(this, irp));
		}
		else
		{
			listeners.dataEventOccurred(new UsbPipeDataEvent(this, irp));
		}
	}

	@Override
	public String toString()
	{
		return String.format("USB pipe of endpoint %s", endpoint.getUsbEndpointDescriptor());
	}
}
