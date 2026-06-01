package org.jurr.java.omniusb.usbip.domain;

import static org.jurr.java.omniusb.usbip.Constants.RESPONSE_BUFFER_HEADER_SIZE;

import java.nio.ByteBuffer;

class ErrorResponse extends UsbIpManagementResponse
{
	ErrorResponse(final short replyCode, final int errorCode)
	{
		super(replyCode, errorCode);
	}

	ErrorResponse(final int commandCode, final int errorCode)
	{
		super(commandCode, errorCode);
	}

	public byte[] toBuffer()
	{
		final ByteBuffer buffer = ByteBuffer.allocate(RESPONSE_BUFFER_HEADER_SIZE);
		putHeader(buffer);

		return buffer.array();
	}
}
