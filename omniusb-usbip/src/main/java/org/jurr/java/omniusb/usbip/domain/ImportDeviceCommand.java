package org.jurr.java.omniusb.usbip.domain;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.jurr.java.omniusb.usbip.ByteBufferUtils;
import org.jurr.java.omniusb.usbip.Constants;

public class ImportDeviceCommand extends UsbIpCommand
{
	public static final int BUFFER_SIZE = Short.BYTES + Short.BYTES + Integer.BYTES + 32;

	private final short usbIpVersion;
	private final String requestedBusId;

	public ImportDeviceCommand(final short usbIpVersion, final String requestedBusId)
	{
		this.usbIpVersion = usbIpVersion;
		this.requestedBusId = requestedBusId;
	}

	public short getUsbIpVersion()
	{
		return usbIpVersion;
	}

	public String getRequestedBusId()
	{
		return requestedBusId;
	}

	public static ImportDeviceCommand fromInputStream(final InputStream inputStream) throws IOException
	{
		final byte[] commandBytes = readFromInputStream(inputStream, BUFFER_SIZE);
		final ByteBuffer buffer = ByteBuffer.wrap(commandBytes);

		final short usbIpVersion = buffer.getShort();
		final short commandCode = buffer.getShort();
		if (commandCode != Constants.OP_REQ_IMPORT)
		{
			throw new IllegalArgumentException("Not a USB/IP Import Device command: " + commandCode);
		}

		buffer.position(buffer.position() + Integer.BYTES); // Skip status
		final String requestedBusId = ByteBufferUtils.getAsciiString(buffer, 32);

		return new ImportDeviceCommand(usbIpVersion, requestedBusId);
	}
}
