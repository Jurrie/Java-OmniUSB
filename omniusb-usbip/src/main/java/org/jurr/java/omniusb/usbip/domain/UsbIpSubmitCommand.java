package org.jurr.java.omniusb.usbip.domain;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;

import javax.usb3.IUsbIrpIsoPacket;
import javax.usb3.ri.UsbIrpIsoPacket;

import org.jurr.java.omniusb.usbip.domain.UsbIpHeaderBasic.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsbIpSubmitCommand extends UsbIpCommand
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final UsbIpHeaderBasic usbIpHeaderBasic;
	private final TransferFlags transferFlags;
	private final int startFrame;
	private final int interval;
	private final SetupPacket setupPacket;
	private final byte[] transferBuffer;
	private final IUsbIrpIsoPacket[] isoPackets;

	private UsbIpSubmitCommand(final UsbIpHeaderBasic usbIpHeaderBasic, final TransferFlags transferFlags, final int startFrame, final int interval, final SetupPacket setupPacket, final byte[] transferBuffer, final IUsbIrpIsoPacket[] isoPackets)
	{
		this.usbIpHeaderBasic = usbIpHeaderBasic;
		this.transferFlags = transferFlags;
		this.startFrame = startFrame;
		this.interval = interval;
		this.setupPacket = setupPacket;
		this.transferBuffer = transferBuffer;
		this.isoPackets = isoPackets;
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
		return isoPackets.length;
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

	public IUsbIrpIsoPacket[] getIsoPackets()
	{
		return isoPackets;
	}

	@Override
	public String toString()
	{
		return "UsbIpSubmitCommand [usbIpHeaderBasic=" + usbIpHeaderBasic + ", transferFlags=" + transferFlags + ", startFrame=" + startFrame + ", numberOfPackets=" + getNumberOfPackets() + ", interval=" + interval + "]";
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

		final byte[] isoPacketBytes = readFromInputStream(inputStream, numberOfPackets * Integer.BYTES * 4);
		final ByteBuffer buffer = ByteBuffer.wrap(isoPacketBytes);
		final IUsbIrpIsoPacket[] isoPackets = new IUsbIrpIsoPacket[numberOfPackets];
		for (int i = 0; i < numberOfPackets; i++)
		{
			final int offset = buffer.getInt();
			final int length = buffer.getInt();
			final int actualLength = buffer.getInt();
			final int status = buffer.getInt();

			isoPackets[i] = new UsbIrpIsoPacket(offset, length, actualLength, status);
		}

		return new UsbIpSubmitCommand(usbIpHeaderBasic, transferFlags, startFrame, interval, setupPacket, transferBuffer, isoPackets);
	}
}