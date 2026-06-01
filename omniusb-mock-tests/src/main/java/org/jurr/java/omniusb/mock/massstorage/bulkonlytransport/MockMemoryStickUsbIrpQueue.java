package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;

import javax.usb3.IUsbControlIrp;
import javax.usb3.IUsbIrp;
import javax.usb3.enumerated.EEndpointDirection;
import javax.usb3.exception.UsbException;
import javax.usb3.ri.ProcessIrpCallback;
import javax.usb3.ri.UsbIrpQueue;
import javax.usb3.ri.UsbPipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockMemoryStickUsbIrpQueue extends UsbIrpQueue
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected MockMemoryStickUsbIrpQueue(final UsbPipe pipe)
	{
		super(pipe);
	}

	@Override
	protected MockMemoryStickDevice getUsbDevice()
	{
		return (MockMemoryStickDevice) super.getUsbDevice();
	}

	@Override
	protected void readUsbIrp(final IUsbIrp irp, final ProcessIrpCallback callback) throws InterruptedException
	{
		final EEndpointDirection direction = getEndPointDirection();
		if (direction == EEndpointDirection.HOST_TO_DEVICE || direction == EEndpointDirection.OUT)
		{
			throw new UnsupportedOperationException("Endpoint is read-only");
		}

		final ByteBuffer buffer = ByteBuffer.wrap(irp.getData());
		buffer.position(irp.getOffset());
		final int result = getUsbDevice().getFirmware().readFromUsbDevice(buffer);
		buffer.rewind();
		irp.setActualLength(result);

		callback.onTransferComplete(irp.getActualLength());
	}

	@Override
	protected void writeUsbIrp(final IUsbIrp irp, final ProcessIrpCallback callback)
	{
		final EEndpointDirection direction = getEndPointDirection();
		if (direction == EEndpointDirection.DEVICE_TO_HOST || direction == EEndpointDirection.IN)
		{
			throw new UnsupportedOperationException("Endpoint is write-only");
		}

		try
		{
			getUsbDevice().getFirmware().handleCommand(irp.getData());
			irp.setActualLength(irp.getLength());

			callback.onTransferComplete(irp.getActualLength());
		}
		catch (IOException e)
		{
			LOGGER.error("Error handling command sent to mock memory stick device", e);
			irp.setActualLength(0);
			callback.onTransferError("Error handling command sent to mock memory stick device");
		}
	}

	@Override
	protected void doControlTransfer(final IUsbControlIrp irp, final ProcessIrpCallback callback) throws UsbException
	{
		throw new UnsupportedOperationException("Not implemented in mock");
	}
}
