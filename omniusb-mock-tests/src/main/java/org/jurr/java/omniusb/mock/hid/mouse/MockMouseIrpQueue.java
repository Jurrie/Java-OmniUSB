package org.jurr.java.omniusb.mock.hid.mouse;

import java.lang.invoke.MethodHandles;

import javax.usb3.IUsbControlIrp;
import javax.usb3.IUsbIrp;
import javax.usb3.exception.UsbAbortException;
import javax.usb3.exception.UsbException;
import javax.usb3.ri.ProcessIrpCallback;
import javax.usb3.ri.UsbIrpQueue;
import javax.usb3.ri.UsbPipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockMouseIrpQueue extends UsbIrpQueue
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private long lastMoveTime = System.currentTimeMillis();
	private double lastPositionX = 0;
	private double lastPositionY = 0;

	protected MockMouseIrpQueue(final UsbPipe pipe)
	{
		super(pipe);
	}

	@Override
	protected void readUsbIrp(final IUsbIrp irp, final ProcessIrpCallback callback) throws InterruptedException, UsbException
	{
		try
		{
			final long timeToSleep = 1000 / 30 - (System.currentTimeMillis() - lastMoveTime); // 30 frames per second
			Thread.sleep(Math.max(0, timeToSleep));

			lastMoveTime = System.currentTimeMillis();

			// Make a ∞ pattern
			final double currentPositionX = Math.sin(lastMoveTime / 400.0) * 100.0;
			final double currentPositionY = Math.sin(lastMoveTime / 200.0) * 50.0;
			final byte x = (byte) (currentPositionX - lastPositionX);
			final byte y = (byte) (currentPositionY - lastPositionY);
			lastPositionX = currentPositionX;
			lastPositionY = currentPositionY;

			irp.getData()[0] = 0; // No buttons pressed
			irp.getData()[1] = x;
			irp.getData()[2] = y;
			irp.setActualLength(3);

			callback.onTransferComplete(irp.getActualLength());
		}
		catch (final InterruptedException e)
		{
			irp.setUsbException(new UsbAbortException());
			Thread.currentThread().interrupt();
		}
		catch (final Exception e)
		{
			LOGGER.error("Error in MockMouseIrpQueue.readUsbIrp", e);
		}
	}

	@Override
	protected void writeUsbIrp(final IUsbIrp irp, final ProcessIrpCallback callback) throws InterruptedException, UsbException
	{
		throw new UnsupportedOperationException("Endpoint is read-only");
	}

	@Override
	protected void doControlTransfer(final IUsbControlIrp irp, final ProcessIrpCallback callback) throws UsbException
	{
		throw new UnsupportedOperationException("Not implemented in mock");
	}
}
