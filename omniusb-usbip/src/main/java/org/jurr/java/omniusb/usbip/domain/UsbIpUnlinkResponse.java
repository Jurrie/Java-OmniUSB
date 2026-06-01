package org.jurr.java.omniusb.usbip.domain;

import java.nio.ByteBuffer;

import org.jurr.java.omniusb.usbip.Constants;

public class UsbIpUnlinkResponse
{
	private static final int ECONNRESET = 104;

	private final UsbIpHeaderBasic responseHeader;
	private final int status;

	private UsbIpUnlinkResponse(final int sequenceNumber, final int status)
	{
		responseHeader = UsbIpHeaderBasic.createForResponse(Constants.USBIP_RET_UNLINK, sequenceNumber);
		this.status = status;
	}

	public UsbIpHeaderBasic getResponseHeader()
	{
		return responseHeader;
	}

	public int getStatus()
	{
		return status;
	}

	public byte[] toBuffer()
	{
		final ByteBuffer responseBuffer = ByteBuffer.allocate(UsbIpHeaderBasic.BUFFER_SIZE + Integer.BYTES + 24 * Byte.BYTES);

		responseHeader.toBuffer(responseBuffer);
		responseBuffer.putInt(status);

		return responseBuffer.array();
	}

	public static UsbIpUnlinkResponse errorAlreadyCompletedResponse(final int sequenceNumber)
	{
		return new UsbIpUnlinkResponse(sequenceNumber, 0);
	}

	public static UsbIpUnlinkResponse errorResponse(final int sequenceNumber)
	{
		return new UsbIpUnlinkResponse(sequenceNumber, -1);
	}

	public static UsbIpUnlinkResponse successResponse(final int sequenceNumber)
	{
		return new UsbIpUnlinkResponse(sequenceNumber, -ECONNRESET);
	}
}