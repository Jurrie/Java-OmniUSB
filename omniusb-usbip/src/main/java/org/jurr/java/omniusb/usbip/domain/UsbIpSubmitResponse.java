package org.jurr.java.omniusb.usbip.domain;

import java.nio.ByteBuffer;

import javax.usb3.IUsbIrpIsoPacket;
import javax.usb3.ri.UsbIrpIsoPacket;

import org.jurr.java.omniusb.usbip.Constants;

public class UsbIpSubmitResponse
{
	/*
	 * Documentation says this... private static final int NO_ISO_NUMBER_OF_PACKETS = 0xffffffff;
	 * ...but does:
	 */ private static final int NO_ISO_NUMBER_OF_PACKETS = 0x00000000;

	private final UsbIpHeaderBasic responseHeader;
	private final int status;
	private final int actualLength;
	private final int startFrame;
	private final int errorCount;
	private final byte[] data;
	private final IUsbIrpIsoPacket[] isoPackets;

	private UsbIpSubmitResponse(final int sequenceNumber, final int status, final int actualLength, final int startFrame, final int errorCount, final byte[] data, final IUsbIrpIsoPacket[] isoPackets)
	{
		responseHeader = UsbIpHeaderBasic.createForResponse(Constants.USBIP_RET_SUBMIT, sequenceNumber);
		this.status = status;
		this.actualLength = actualLength;
		this.startFrame = startFrame;
		this.errorCount = errorCount;
		this.data = data;
		this.isoPackets = isoPackets;

	}

	public UsbIpHeaderBasic getResponseHeader()
	{
		return responseHeader;
	}

	public int getStatus()
	{
		return status;
	}

	public int getActualLength()
	{
		return actualLength;
	}

	public int getStartFrame()
	{
		return startFrame;
	}

	public int getNumberOfPackets()
	{
		return isoPackets.length;
	}

	public int getErrorCount()
	{
		return errorCount;
	}

	public byte[] getData()
	{
		return data;
	}

	public byte[] toBuffer()
	{
		final ByteBuffer responseBuffer = ByteBuffer.allocate(UsbIpHeaderBasic.BUFFER_SIZE + Integer.BYTES + Integer.BYTES + Integer.BYTES + Integer.BYTES + Integer.BYTES + 8 + (data == null ? 0 : actualLength) + isoPackets.length * (Integer.BYTES + Integer.BYTES + Integer.BYTES + Integer.BYTES));

		responseHeader.toBuffer(responseBuffer);
		responseBuffer.putInt(status);
		responseBuffer.putInt(actualLength);
		responseBuffer.putInt(startFrame);
		responseBuffer.putInt(isoPackets.length == 0 ? NO_ISO_NUMBER_OF_PACKETS : isoPackets.length);
		responseBuffer.putInt(errorCount);
		responseBuffer.position(responseBuffer.position() + 8); // Padding
		if (data != null)
		{
			responseBuffer.put(data, 0, actualLength);
		}

		for (final IUsbIrpIsoPacket isoPacket : isoPackets)
		{
			responseBuffer.putInt(isoPacket.getOffset());
			responseBuffer.putInt(isoPacket.getLength());
			responseBuffer.putInt(isoPacket.getActualLength());
			responseBuffer.putInt(isoPacket.getStatus());
		}

		return responseBuffer.array();
	}

	public static UsbIpSubmitResponse errorResponse(final int sequenceNumber, final int status)
	{
		return new UsbIpSubmitResponse(sequenceNumber, status, 0, 0, 1, null, new UsbIrpIsoPacket[0]);
	}

	public static UsbIpSubmitResponse errorResponse(final int sequenceNumber, final int status, final int errorCount)
	{
		if (status != 0 && errorCount == 0)
		{
			throw new IllegalArgumentException("status is not 0 but errorCount is 0");
		}

		return new UsbIpSubmitResponse(sequenceNumber, status, 0, 0, 1, null, new IUsbIrpIsoPacket[0]);
	}

	public static UsbIpSubmitResponse successResponse(final int sequenceNumber, final int actualLength)
	{
		return new UsbIpSubmitResponse(sequenceNumber, 0, actualLength, 0, 0, null, new IUsbIrpIsoPacket[0]);
	}

	public static UsbIpSubmitResponse successResponse(final int sequenceNumber, final int actualLength, final byte[] data, final IUsbIrpIsoPacket[] isoPackets)
	{
		return new UsbIpSubmitResponse(sequenceNumber, 0, actualLength, 0, 0, data, isoPackets == null ? new IUsbIrpIsoPacket[0] : isoPackets);
	}
}
