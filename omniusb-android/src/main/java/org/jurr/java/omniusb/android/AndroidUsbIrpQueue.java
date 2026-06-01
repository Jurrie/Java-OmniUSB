package org.jurr.java.omniusb.android;

import javax.usb3.IUsbControlIrp;
import javax.usb3.IUsbIrp;
import javax.usb3.exception.UsbException;
import javax.usb3.ri.ProcessIrpCallback;
import javax.usb3.ri.UsbIrpQueue;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;

public class AndroidUsbIrpQueue extends UsbIrpQueue
{
	protected AndroidUsbIrpQueue(final AndroidUsbPipe pipe)
	{
		super(pipe);
	}

	@Override
	protected AndroidUsbPipe getPipe()
	{
		return (AndroidUsbPipe) super.getPipe();
	}

	@Override
	protected AndroidUsbDevice getUsbDevice()
	{
		return (AndroidUsbDevice) super.getUsbDevice();
	}

	protected AndroidUsbEndpoint getUsbEndpoint()
	{
		return (AndroidUsbEndpoint) getPipe().getUsbEndpoint();
	}

	@Override
	protected void readUsbIrp(final IUsbIrp irp, final ProcessIrpCallback callback) throws UsbException
	{
		final int size = irp.getLength();
		final byte[] buffer = new byte[size];
		final int result = transfer(buffer);
		System.arraycopy(buffer, 0, irp.getData(), irp.getOffset(), result);
		irp.setActualLength(result);
		callback.onTransferComplete(irp.getActualLength());
	}

	@Override
	protected void writeUsbIrp(final IUsbIrp irp, final ProcessIrpCallback callback) throws UsbException
	{
		final int size = irp.getLength();
		final byte[] buffer = new byte[size];
		System.arraycopy(irp.getData(), irp.getOffset(), buffer, 0, size);
		final int result = transfer(buffer);
		irp.setActualLength(result);
		callback.onTransferComplete(irp.getActualLength());
	}

	/**
	 * Transfers data from or to the device.
	 *
	 * @param handle The device handle.
	 * @param descriptor The endpoint descriptor.
	 * @param type The endpoint type.
	 * @param buffer The data buffer.
	 * @return The number of transferred bytes.
	 * @throws UsbException When data transfer fails.
	 */
	private int transfer(final byte[] buffer) throws UsbException
	{
		final UsbDeviceConnection handle = getUsbDevice().openDevice();
		final UsbEndpoint wrappedUsbEndpoint = getUsbEndpoint().getWrappedUsbEndpoint();
		switch (getEndpointTransferType())
		{
		case BULK:
			final int result = handle.bulkTransfer(wrappedUsbEndpoint, buffer, buffer.length, 0);
			if (result < 0)
			{
				throw new UsbException("Bulk transfer failed with error code: " + result);
			}
			return result;
		case INTERRUPT:
			throw new UsbException("Interrupt transfer type not supported in Android.");
		case CONTROL:
			throw new UsbException("Unsupported endpoint type: " + getEndpointTransferType() + ": Control transfers require a Control-Type IRP.");
		case ISOCHRONOUS:
			throw new UsbException("Asynchronous (non-blocking) transfer type not supported in Android.");
		default:
			throw new AssertionError(getEndpointTransferType().name());
		}
	}

	@Override
	protected void doControlTransfer(final IUsbControlIrp irp, final ProcessIrpCallback callback) throws UsbException
	{
		// This is only used in the control IRP queue variant. Bad design actually...
		// TODO: Improve design.
		throw new UnsupportedOperationException("Not implemented in Android");
	}
}
