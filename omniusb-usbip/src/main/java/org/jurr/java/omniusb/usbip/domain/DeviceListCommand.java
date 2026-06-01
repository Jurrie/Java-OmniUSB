package org.jurr.java.omniusb.usbip.domain;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.jurr.java.omniusb.usbip.Constants;

public class DeviceListCommand extends UsbIpCommand
{
	public static final int BUFFER_SIZE = Short.BYTES + Short.BYTES + Integer.BYTES;

	private final short usbIpVersion;

	public DeviceListCommand(final short usbIpVersion)
	{
		this.usbIpVersion = usbIpVersion;
	}

	public short getUsbIpVersion()
	{
		return usbIpVersion;
	}

	public byte[] toByteArray()
	{
		final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		buffer.putShort(usbIpVersion);
		buffer.putShort(Constants.OP_REQ_DEVLIST);
		// Padding is 4 bytes
		return buffer.array();
	}

	public static DeviceListCommand fromInputStream(final InputStream inputStream) throws IOException
	{
		final byte[] commandBytes = readFromInputStream(inputStream, BUFFER_SIZE);
		final ByteBuffer buffer = ByteBuffer.wrap(commandBytes);

		final short usbIpVersion = buffer.getShort();
		final short commandCode = buffer.getShort();
		if (commandCode != Constants.OP_REQ_DEVLIST)
		{
			throw new IllegalArgumentException("Not a USB/IP Request Device List command: " + commandCode);
		}

		// Skip the status
		buffer.position(buffer.position() + Integer.BYTES);

		return new DeviceListCommand(usbIpVersion);
	}
}
