package org.jurr.java.omniusb.usbip.domain;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class UsbIpUnlinkCommand extends UsbIpCommand
{
	public static final int BUFFER_SIZE = UsbIpHeaderBasic.BUFFER_SIZE + Integer.BYTES + 24;

	private final UsbIpHeaderBasic usbIpHeaderBasic;
	private final int seqNum;

	private UsbIpUnlinkCommand(final UsbIpHeaderBasic usbIpHeaderBasic, final int seqNum)
	{
		this.usbIpHeaderBasic = usbIpHeaderBasic;
		this.seqNum = seqNum;
	}

	public UsbIpHeaderBasic getUsbIpHeaderBasic()
	{
		return usbIpHeaderBasic;
	}

	public int getSeqNum()
	{
		return seqNum;
	}

	@Override
	public String toString()
	{
		return "UsbIpUnlinkCommand [usbIpHeaderBasic=" + usbIpHeaderBasic + ", seqNum=" + seqNum + "]";
	}

	public static UsbIpUnlinkCommand fromInputStream(final InputStream inputStream) throws IOException
	{
		final byte[] commandBytes = readFromInputStream(inputStream, BUFFER_SIZE);
		final ByteBuffer buffer = ByteBuffer.wrap(commandBytes);

		final UsbIpHeaderBasic usbIpHeaderBasic = UsbIpHeaderBasic.fromBuffer(buffer);
		final int seqNum = buffer.getInt();
		buffer.position(buffer.position() + 24); // padding

		return new UsbIpUnlinkCommand(usbIpHeaderBasic, seqNum);
	}
}