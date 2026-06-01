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

import javax.usb3.IUsbControlIrp;
import javax.usb3.IUsbEndpoint;
import javax.usb3.IUsbEndpointDescriptor;
import javax.usb3.IUsbIrp;
import javax.usb3.IUsbPipe;
import javax.usb3.enumerated.EDataFlowtype;
import javax.usb3.enumerated.EEndpointDirection;
import javax.usb3.exception.UsbException;

/**
 * A concurrent queue manager for USB I/O Request packets.
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
public abstract class UsbIrpQueue extends AUsbIrpQueue<IUsbIrp>
{
	/**
	 * The USB pipe.
	 */
	private final IUsbPipe pipe;

	/**
	 * The PIPE end point direction. [IN, OUT]. This is set upon instantiation and
	 * proxied in a class-level field to speed up do/while loops buried within.
	 */
	private final EEndpointDirection endPointDirection;
	/**
	 * The PIPE end point transfer type. This is set upon instantiation and
	 * proxied in a class-level field to speed up do/while loops buried within.
	 */
	private final EDataFlowtype endpointTransferType;
	/**
	 * The PIPE end point descriptor. This is set upon instantiation and proxied
	 * in a class-level field to speed up do/while loops buried within.
	 */
	private final IUsbEndpointDescriptor endpointDescriptor;

	/**
	 * Constructor.
	 *
	 * @param pipe The USB pipe
	 */
	protected UsbIrpQueue(final UsbPipe pipe)
	{
		super(pipe);
		this.pipe = pipe;
		endPointDirection = pipe.getUsbEndpoint().getDirection();
		endpointTransferType = pipe.getUsbEndpoint().getType();
		endpointDescriptor = this.pipe.getUsbEndpoint().getUsbEndpointDescriptor();
	}

	protected IUsbPipe getPipe()
	{
		return pipe;
	}

	protected EEndpointDirection getEndPointDirection()
	{
		return endPointDirection;
	}

	protected EDataFlowtype getEndpointTransferType()
	{
		return endpointTransferType;
	}

	protected IUsbEndpointDescriptor getEndpointDescriptor()
	{
		return endpointDescriptor;
	}

	/**
	 * Processes the IRP.
	 *
	 * @param irp The IRP to processUsbIrpQueue.
	 * @throws UsbException When processUsbIrpQueueing the IRP fails.
	 * @throws InterruptedException When during the processing of the IRP the IRP is aborted
	 */
	@Override
	protected void processIrp(final IUsbIrp irp, final ProcessIrpCallback callback) throws UsbException, InterruptedException
	{
		final IUsbEndpoint endpoint = pipe.getUsbEndpoint();
		if (EDataFlowtype.CONTROL.equals(endpoint.getType()))
		{
			processControlIrp((IUsbControlIrp) irp, callback);
			return;
		}
		switch (endpoint.getDirection())
		{
		case OUT:
		case HOST_TO_DEVICE:
			writeUsbIrp(irp, callback);
			break;
		case IN:
		case DEVICE_TO_HOST:
			readUsbIrp(irp, callback);
			break;

		default:
			throw new UsbException("Invalid direction: " + endpoint.getDirection());
		}
	}

	/**
	 * Called after IRP has finished. This can be implemented to send events for
	 * example.
	 *
	 * @param irp The IRP which has been finished.
	 */
	@Override
	protected void finishIrp(final IUsbIrp irp)
	{
		((UsbPipe) pipe).sendEvent(irp);
	}

	/**
	 * Reads bytes from an interrupt endpoint into the specified I/O Request
	 * Packet.
	 *
	 * @param irp A USB I/O Request Packet (IRP) instance
	 * @throws UsbException if the Device cannot be opened or cannot be read from
	 * @throws InterruptedException When during the processing of the IRP the IRP is aborted
	 */
	protected abstract void readUsbIrp(final IUsbIrp irp, final ProcessIrpCallback callback) throws InterruptedException, UsbException;

	/**
	 * Write an I/O Request Packet to an interrupt endpoint.
	 *
	 * @param irp A USB I/O Request Packet (IRP) instance
	 * @throws UsbException if the Device cannot be opened or cannot be written to
	 * @throws InterruptedException When during the processing of the IRP the IRP is aborted
	 */
	protected abstract void writeUsbIrp(final IUsbIrp irp, final ProcessIrpCallback callback) throws InterruptedException, UsbException;
}
