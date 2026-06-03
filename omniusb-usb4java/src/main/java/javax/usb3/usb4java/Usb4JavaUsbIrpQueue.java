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
import javax.usb3.IUsbEndpointDescriptor;
import javax.usb3.IUsbIrp;
import javax.usb3.IUsbIrpIsoPacket;
import javax.usb3.enumerated.EEndpointDirection;
import javax.usb3.exception.UsbAbortException;
import javax.usb3.exception.UsbException;
import javax.usb3.request.BEndpointAddress;
import javax.usb3.ri.ProcessIrpCallback;
import javax.usb3.ri.ProcessIrpCallback.WrappingProcessIrpCallback;
import javax.usb3.ri.UsbIrpIsoPacket;
import javax.usb3.ri.UsbIrpQueue;
import javax.usb3.ri.UsbPipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.usb4java.DeviceHandle;
import org.usb4java.IsoPacketDescriptor;
import org.usb4java.LibUsb;
import org.usb4java.Transfer;
import org.usb4java.TransferCallback;

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
public final class Usb4JavaUsbIrpQueue extends UsbIrpQueue
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public Usb4JavaUsbIrpQueue(final UsbPipe pipe)
	{
		super(pipe);
	}

	/**
	 * Reads bytes from an endpoint into the specified USB I/O Request Packet (IRP).
	 *
	 * @param irp A USB I/O Request Packet (IRP) instance.
	 * @param callback The callback that is called with the outcome of the transfer.
	 * @throws UsbException if the Device cannot be opened or cannot be read from
	 */
	@Override
	protected void readUsbIrp(final IUsbIrp irp, final ProcessIrpCallback callback) throws UsbException
	{
		// Open the USB device and returns the USB device handle. If device was already open then the old handle is returned.
		final DeviceHandle deviceHandle = ((Usb4JavaAUsbDevice) getUsbDevice()).open();
		irp.setActualLength(0);
		final ByteBuffer buffer = ByteBuffer.allocateDirect(irp.getLength());
		final ProcessIrpCallback innerCallback = new WrappingProcessIrpCallback(callback)
		{
			@Override
			public void onTransferComplete(final int actualLength)
			{
				buffer.rewind();
				buffer.get(irp.getData(), irp.getOffset(), actualLength);
				irp.setActualLength(actualLength);
				callback.onTransferComplete(irp.getActualLength());
			}
		};
		transfer(irp, deviceHandle, getEndpointDescriptor(), buffer, irp.getNumberOfIsochronousPackets(), innerCallback);
	}

	/**
	 * Write an USB I/O Request Packet (IRP) to an endpoint.
	 *
	 * @param irp A USB I/O Request Packet (IRP) instance.
	 * @param callback The callback that is called with the outcome of the transfer.
	 * @throws UsbException if the Device cannot be opened or cannot be written to
	 */
	@Override
	protected void writeUsbIrp(final IUsbIrp irp, final ProcessIrpCallback callback) throws UsbException
	{
		// Open the USB device and returns the USB device handle. If device was already open then the old handle is returned.
		final DeviceHandle handle = ((Usb4JavaAUsbDevice) getUsbDevice()).open();
		final ByteBuffer buffer = ByteBuffer.allocateDirect(irp.getLength());
		buffer.put(irp.getData(), irp.getOffset(), irp.getLength());
		buffer.rewind();
		final ProcessIrpCallback innerCallback = new WrappingProcessIrpCallback(callback)
		{
			@Override
			public void onTransferComplete(final int actualLength)
			{
				irp.setActualLength(actualLength);
				callback.onTransferComplete(irp.getActualLength());
			}
		};
		transfer(irp, handle, getEndpointDescriptor(), buffer, irp.getNumberOfIsochronousPackets(), innerCallback);
	}

	/**
	 * Transfers data from or to the device.
	 *
	 * @param handle The device handle.
	 * @param descriptor The endpoint descriptor.
	 * @param type The endpoint type.
	 * @param buffer The data buffer.
	 * @param callback The callback that is called with the outcome of the transfer.
	 * @return The number of transferred bytes.
	 * @throws UsbException When data transfer fails.
	 */
	private void transfer(final IUsbIrp irp, final DeviceHandle handle, final IUsbEndpointDescriptor descriptor, final ByteBuffer buffer, final int numIsoPackets, final ProcessIrpCallback callback)
	{
		switch (getEndpointTransferType())
		{
		case BULK:
			transferBulk(irp, handle, descriptor.endpointAddress(), buffer, callback);
			return;
		case INTERRUPT:
			transferInterrupt(irp, handle, descriptor.endpointAddress(), buffer, callback);
			return;
		case CONTROL:
			callback.onTransferError("Unsupported endpoint type: " + getEndpointTransferType() + ": Control transfers require a Control-Type IRP.");
			return;
		case ISOCHRONOUS:
			transferIsochronous(irp, handle, descriptor.endpointAddress(), buffer, numIsoPackets, callback);
			return;
		default:
			throw new AssertionError(getEndpointTransferType().name());
		}
	}

	/**
	 * /**
	 * Transfers isochronous data from or to the device.
	 *
	 * @param handle The device handle.
	 * @param address The endpoint address.
	 * @param buffer The data buffer.
	 * @param callback The callback that is called with the outcome of the transfer.
	 * @throws UsbException When data transfer fails.
	 */
	private void transferIsochronous(final IUsbIrp irp, final DeviceHandle handle, final BEndpointAddress address, final ByteBuffer buffer, final int numIsoPackets, final ProcessIrpCallback callback)
	{
		final TransferCallback usb4JavaCallback = transfer -> {
			final int status = transfer.status();

			// Normally for IN transfers, we may want to retry on timeout. But for isochronous transfers, we don't do so.

			int actualLength = 0;
			if (status == LibUsb.TRANSFER_COMPLETED)
			{
				for (int i = 0; i < transfer.numIsoPackets(); i++)
				{
					final IUsbIrpIsoPacket sentIsoPackets = irp.getIsochronousPackets()[i];
					final IsoPacketDescriptor usb4JavaReturnedPacket = transfer.isoPacketDesc()[i];
					if (usb4JavaReturnedPacket.status() != LibUsb.TRANSFER_COMPLETED)
					{
						LOGGER.debug("Isochronous packet {} transfer completed with status: {} ({})", i, usb4JavaReturnedPacket.status(), LibUsb.strError(usb4JavaReturnedPacket.status()));
					}
					irp.getIsochronousPackets()[i] = new UsbIrpIsoPacket(sentIsoPackets.getOffset(), sentIsoPackets.getLength(), usb4JavaReturnedPacket.actualLength(), usb4JavaReturnedPacket.status());
					actualLength += usb4JavaReturnedPacket.actualLength();
				}
			}

			LibUsb.freeTransfer(transfer);
			transfer = null;

			switch (status)
			{
			// Transfer completed without error. Note that this does not indicate that the entire amount of requested data was transferred.
			case LibUsb.TRANSFER_COMPLETED:
				callback.onTransferComplete(actualLength);
				break;

			// Transfer timed out.
			case LibUsb.TRANSFER_TIMED_OUT:
				if (isAborting() || isClosed() || irpIsAborted(irp))
				{
					callback.onTransferAborted();
				}
				else
				{
					callback.onTransferTimedOut(LibUsb.strError(status));
				}
				break;

			// Transfer failed.
			case LibUsb.TRANSFER_ERROR:
				callback.onTransferError(LibUsb.strError(status));
				break;

			// Transfer was cancelled.
			case LibUsb.TRANSFER_CANCELLED:
				callback.onTransferCancelled(LibUsb.strError(status));
				break;

			// For bulk/interrupt endpoints: halt condition detected (endpoint stalled). For control endpoints: control request not supported.
			case LibUsb.TRANSFER_STALL:
				callback.onTransferStall(LibUsb.strError(status));
				break;

			// Device was disconnected.
			case LibUsb.TRANSFER_NO_DEVICE:
				callback.onTransferNoDevice(LibUsb.strError(status));
				break;

			// Device sent more data than requested.
			case LibUsb.TRANSFER_OVERFLOW:
				callback.onTransferOverflow(LibUsb.strError(status));
				break;
			}
		};

		final Transfer transfer = LibUsb.allocTransfer(numIsoPackets);
		LibUsb.fillIsoTransfer(transfer, handle, address.getByteCode(), buffer, numIsoPackets, usb4JavaCallback, null, UsbServiceInstanceConfiguration.TIMEOUT);
		LibUsb.setIsoPacketLengths(transfer, buffer.capacity() / numIsoPackets);
		int result = LibUsb.submitTransfer(transfer);
		if (result < 0)
		{
			callback.onTransferError(LibUsb.strError(result));
		}
	}

	/**
	 * Transfers bulk data from or to the device.
	 *
	 * @param handle The device handle.
	 * @param address The endpoint address.
	 * @param buffer The data buffer.
	 * @param callback The callback that is called with the outcome of the transfer.
	 * @throws UsbException When data transfer fails.
	 */
	private void transferBulk(final IUsbIrp irp, final DeviceHandle handle, final BEndpointAddress address, final ByteBuffer buffer, final ProcessIrpCallback callback)
	{
		final TransferCallback usb4JavaCallback = transfer -> {
			final int status = transfer.status();
			// For IN transfers, we may want to retry on timeout
			if (status == LibUsb.TRANSFER_TIMED_OUT && !irpIsAborted(irp) && !isAborting() && !isClosed() && EEndpointDirection.DEVICE_TO_HOST.equals(getEndPointDirection()))
			{
				LOGGER.debug("Bulk transfer timed out, retrying");
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
				callback.onTransferComplete(actualLength);
				break;

			// Transfer timed out.
			case LibUsb.TRANSFER_TIMED_OUT:
				if (isAborting() || isClosed() || irpIsAborted(irp))
				{
					callback.onTransferAborted();
				}
				else
				{
					callback.onTransferTimedOut(LibUsb.strError(status));
				}
				break;

			// Transfer failed.
			case LibUsb.TRANSFER_ERROR:
				callback.onTransferError(LibUsb.strError(status));
				break;

			// Transfer was cancelled.
			case LibUsb.TRANSFER_CANCELLED:
				callback.onTransferCancelled(LibUsb.strError(status));
				break;

			// For bulk/interrupt endpoints: halt condition detected (endpoint stalled). For control endpoints: control request not supported.
			case LibUsb.TRANSFER_STALL:
				callback.onTransferStall(LibUsb.strError(status));
				break;

			// Device was disconnected.
			case LibUsb.TRANSFER_NO_DEVICE:
				callback.onTransferNoDevice(LibUsb.strError(status));
				break;

			// Device sent more data than requested.
			case LibUsb.TRANSFER_OVERFLOW:
				callback.onTransferOverflow(LibUsb.strError(status));
				break;
			}
		};

		final Transfer transfer = LibUsb.allocTransfer();
		LibUsb.fillBulkTransfer(transfer, handle, address.getByteCode(), buffer, usb4JavaCallback, null, UsbServiceInstanceConfiguration.TIMEOUT);
		int result = LibUsb.submitTransfer(transfer);
		if (result < 0)
		{
			callback.onTransferError(LibUsb.strError(result));
		}
	}

	/**
	 * Transfers interrupt data from or to the device.
	 *
	 * @param handle The device handle.
	 * @param address The endpoint address.
	 * @param buffer The data buffer.
	 * @param callback The callback that is called with the outcome of the transfer.
	 * @throws UsbException When data transfer fails.
	 */
	private void transferInterrupt(final IUsbIrp irp, final DeviceHandle handle, final BEndpointAddress address, final ByteBuffer buffer, final ProcessIrpCallback callback)
	{
		final TransferCallback usb4JavaCallback = transfer -> {
			final int status = transfer.status();
			// For IN transfers, we may want to retry on timeout
			if (status == LibUsb.TRANSFER_TIMED_OUT && !irpIsAborted(irp) && !isAborting() && !isClosed() && EEndpointDirection.DEVICE_TO_HOST.equals(getEndPointDirection()))
			{
				LOGGER.debug("Interrupt transfer timed out, retrying");
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
				callback.onTransferComplete(actualLength);
				break;

			// Transfer timed out.
			case LibUsb.TRANSFER_TIMED_OUT:
				if (isAborting() || isClosed() || irpIsAborted(irp))
				{
					callback.onTransferAborted();
				}
				else
				{
					callback.onTransferTimedOut(LibUsb.strError(status));
				}
				break;

			// Transfer failed.
			case LibUsb.TRANSFER_ERROR:
				callback.onTransferError(LibUsb.strError(status));
				break;

			// Transfer was cancelled.
			case LibUsb.TRANSFER_CANCELLED:
				callback.onTransferCancelled(LibUsb.strError(status));
				break;

			// For bulk/interrupt endpoints: halt condition detected (endpoint stalled). For control endpoints: control request not supported.
			case LibUsb.TRANSFER_STALL:
				callback.onTransferStall(LibUsb.strError(status));
				break;

			// Device was disconnected.
			case LibUsb.TRANSFER_NO_DEVICE:
				callback.onTransferNoDevice(LibUsb.strError(status));
				break;

			// Device sent more data than requested.
			case LibUsb.TRANSFER_OVERFLOW:
				callback.onTransferOverflow(LibUsb.strError(status));
				break;
			}
		};

		final Transfer transfer = LibUsb.allocTransfer();
		LibUsb.fillInterruptTransfer(transfer, handle, address.getByteCode(), buffer, usb4JavaCallback, null, UsbServiceInstanceConfiguration.TIMEOUT);
		int result = LibUsb.submitTransfer(transfer);
		if (result < 0)
		{
			callback.onTransferError(LibUsb.strError(result));
		}
	}

	@Override
	protected void doControlTransfer(final IUsbControlIrp irp, final ProcessIrpCallback callback) throws UsbException
	{
		throw new IllegalStateException("Control transfers require dedicated control IRP queue.");
	}

	private static boolean irpIsAborted(final IUsbIrp irp)
	{
		return irp.getUsbException() instanceof UsbAbortException;
	}
}
