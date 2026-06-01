package org.jurr.java.omniusb.usbip.domain;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;

import org.jurr.java.omniusb.usbip.domain.UsbIpHeaderBasic.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsbIpSubmitCommand extends UsbIpCommand
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final UsbIpHeaderBasic usbIpHeaderBasic;
	private final TransferFlags transferFlags;
	private final int startFrame;
	private final int numberOfPackets;
	private final int interval;
	private final SetupPacket setupPacket;
	private final byte[] transferBuffer;

	private UsbIpSubmitCommand(final UsbIpHeaderBasic usbIpHeaderBasic, final TransferFlags transferFlags, final int startFrame, final int numberOfPackets, final int interval, final SetupPacket setupPacket, final byte[] transferBuffer)
	{
		this.usbIpHeaderBasic = usbIpHeaderBasic;
		this.transferFlags = transferFlags;
		this.startFrame = startFrame;
		this.numberOfPackets = numberOfPackets;
		this.interval = interval;
		this.setupPacket = setupPacket;
		this.transferBuffer = transferBuffer;
	}

	public UsbIpHeaderBasic getUsbIpHeaderBasic()
	{
		return usbIpHeaderBasic;
	}

	public TransferFlags getTransferFlags()
	{
		return transferFlags;
	}

	// Only for PERIODIC transfers (ISO, INTERRUPT)
	public int getStartFrame()
	{
		return startFrame;
	}

	// size of iso_frame_desc
	public int getNumberOfPackets()
	{
		return numberOfPackets;
	}

	// Only for PERIODIC transfers (ISO, INTERRUPT)
	public int getInterval()
	{
		return interval;
	}

	// setup stage for CTRL (pass a struct usb_ctrlrequest)
	public SetupPacket getSetupPacket()
	{
		return setupPacket;
	}

	public byte[] getTransferBuffer()
	{
		return transferBuffer;
	}

	@Override
	public String toString()
	{
		return "UsbIpSubmitCommand [usbIpHeaderBasic=" + usbIpHeaderBasic + ", transferFlags=" + transferFlags + ", startFrame=" + startFrame + ", numberOfPackets=" + numberOfPackets + ", interval=" + interval + "]";
	}

	public static UsbIpSubmitCommand fromInputStream(final InputStream inputStream) throws IOException
	{
		final byte[] commandBytes = readFromInputStream(inputStream, UsbIpHeaderBasic.BUFFER_SIZE + Integer.BYTES + Integer.BYTES + Integer.BYTES + Integer.BYTES + Integer.BYTES + SetupPacket.BUFFER_SIZE);
		final ByteBuffer headerBuffer = ByteBuffer.wrap(commandBytes);

		final UsbIpHeaderBasic usbIpHeaderBasic = UsbIpHeaderBasic.fromBuffer(headerBuffer);
		final TransferFlags transferFlags = TransferFlags.fromBytes(headerBuffer.getInt());
		final int transferBufferLength = headerBuffer.getInt();
		final int startFrame = headerBuffer.getInt();
		final int numberOfPackets = headerBuffer.getInt();
		final int interval = headerBuffer.getInt();
		final SetupPacket setupPacket = SetupPacket.fromBytes(headerBuffer);
		final byte[] transferBuffer;

		if (usbIpHeaderBasic.getDirection() == Direction.USBIP_DIR_OUT)
		{
			transferBuffer = readFromInputStream(inputStream, transferBufferLength);
		}
		else
		{
			transferBuffer = new byte[transferBufferLength];
		}
		// iso_packet_descriptor??

		return new UsbIpSubmitCommand(usbIpHeaderBasic, transferFlags, startFrame, numberOfPackets, interval, setupPacket, transferBuffer);
	}
}