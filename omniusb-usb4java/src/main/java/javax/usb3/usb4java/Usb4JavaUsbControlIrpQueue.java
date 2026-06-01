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
package javax.usb3.usb4java;

import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;

import javax.usb3.IUsbControlIrp;
import javax.usb3.IUsbIrp;
import javax.usb3.exception.UsbException;
import javax.usb3.ri.AUsbDevice;
import javax.usb3.ri.ProcessIrpCallback;
import javax.usb3.ri.UsbControlIrpQueue;
import javax.usb3.ri.UsbDeviceListenerList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;
import org.usb4java.Transfer;
import org.usb4java.TransferCallback;

/**
 * Abstract base class for a concurrent queue of USB I/O Request packets.
 * <p>
 * An IrpQueue contains a thread safe FIFO queue and a threaded
 * processUsbIrpQueueor to handle each IRP that is placed into the queue.
 * <p>
 * Developer note: The default operation of an IrpQueue is to support
 * Asynchronous operation (e.g. processUsbIrpQueue in a separate thread.) To
 * implement synchronous IRP queue handling implement a WAIT lock on the
 * {@link IUsbIrp#isComplete() isComplete} method IUsbIrp.isComplete().
 *
 * @author Klaus Reimer
 * @author Jesse Caulfield
 */
public class Usb4JavaUsbControlIrpQueue extends UsbControlIrpQueue
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected Usb4JavaUsbControlIrpQueue(final AUsbDevice usbDevice, final UsbDeviceListenerList listeners)
	{
		super(usbDevice, listeners);
	}

	@Override
	protected void doControlTransfer(final IUsbControlIrp irp, final ProcessIrpCallback outerCallback) throws UsbException
	{
		final ByteBuffer buffer = ByteBuffer.allocateDirect(irp.getLength() + LibUsb.CONTROL_SETUP_SIZE);
		LibUsb.fillControlSetup(buffer, irp.bmRequestType(), irp.bRequest(), irp.wValue(), irp.wIndex(), (short) irp.getLength());
		buffer.put(LibUsb.CONTROL_SETUP_SIZE, irp.getData(), irp.getOffset(), irp.getLength());
		buffer.rewind();

		final TransferCallback usb4JavaCallback = transfer -> {
			final int status = transfer.status();
			// For IN transfers, we may want to retry on timeout
			if (status == LibUsb.TRANSFER_TIMED_OUT && !isAborting() && !isClosed())
			{
				LOGGER.debug("Control transfer timed out, retrying");
				LibUsb.submitTransfer(transfer);
				return;
			}

			final int actualLength = transfer.actualLength();
			LibUsb.freeTransfer(transfer);
			transfer = null;

			switch (status)
			{
			// Transfer completed without error. Note that this does not indicate that the entire amount of requested data was transferred.
			case LibUsb.TRANSFER_COMPLETED:
				buffer.get(LibUsb.CONTROL_SETUP_SIZE, irp.getData(), irp.getOffset(), actualLength);
				irp.setActualLength(actualLength);
				outerCallback.onTransferComplete(actualLength);
				break;

			// Transfer timed out.
			case LibUsb.TRANSFER_TIMED_OUT:
				LOGGER.debug("Control transfer timed out");
				if (isAborting() || isClosed())
				{
					outerCallback.onTransferAborted();
				}
				else
				{
					outerCallback.onTransferTimedOut(LibUsb.strError(status));
				}
				break;

			// Transfer failed.
			case LibUsb.TRANSFER_ERROR:
				LOGGER.debug("Control transfer error");
				outerCallback.onTransferError(LibUsb.strError(status));
				break;

			// Transfer was cancelled.
			case LibUsb.TRANSFER_CANCELLED:
				LOGGER.debug("Control transfer cancelled");
				outerCallback.onTransferCancelled(LibUsb.strError(status));
				break;

			// For bulk/interrupt endpoints: halt condition detected (endpoint stalled). For control endpoints: control request not supported.
			case LibUsb.TRANSFER_STALL:
				LOGGER.debug("Control transfer stalled");
				outerCallback.onControlRequestNotSupported(LibUsb.strError(status));
				break;

			// Device was disconnected.
			case LibUsb.TRANSFER_NO_DEVICE:
				LOGGER.debug("Control transfer no device");
				outerCallback.onTransferNoDevice(LibUsb.strError(status));
				break;

			// Device sent more data than requested.
			case LibUsb.TRANSFER_OVERFLOW:
				LOGGER.debug("Control transfer overflow");
				outerCallback.onTransferOverflow(LibUsb.strError(status));
				break;
			}
		};

		final DeviceHandle deviceHandle = ((Usb4JavaAUsbDevice) getUsbDevice()).open();
		final Transfer transfer = LibUsb.allocTransfer();
		LibUsb.fillControlTransfer(transfer, deviceHandle, buffer, usb4JavaCallback, null, UsbServiceInstanceConfiguration.TIMEOUT);
		int result = LibUsb.submitTransfer(transfer);
		if (result < 0)
		{
			outerCallback.onTransferError(LibUsb.strError(result));
		}
	}
}
