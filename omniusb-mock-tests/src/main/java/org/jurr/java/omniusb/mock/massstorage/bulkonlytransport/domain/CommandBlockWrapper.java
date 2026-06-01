package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.domain;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CommandBlockWrapper
{
	private static final int CBW_SIGNATURE = 0x43425355; // 'USBC' in little-endian

	public static class CBWFlags
	{
		public static final byte DATA_OUT = 0x00;
		public static final byte DATA_IN = (byte) 0x80;

		private final byte value;

		private CBWFlags(final byte value)
		{
			this.value = value;
		}

		public boolean isDataIn()
		{
			return (value & DATA_IN) == DATA_IN;
		}

		public boolean isDataOut()
		{
			return (value & DATA_IN) == DATA_OUT;
		}
	}

	private final int dCBWSignature;
	private final int dCBWTag;
	private final int dCBWDataTransferLength;
	private final CBWFlags bmCBWFlags;
	private final byte bCBWLUN;
	private final byte bCBWCBLength;
	private final byte[] cBWCB;

	protected CommandBlockWrapper(final int dCBWSignature, final int dCBWTag, final int dCBWDataTransferLength, final CBWFlags bmCBWFlags, final byte bCBWLUN, final byte bCBWCBLength, final byte[] cBWCB)
	{
		if (dCBWSignature != CBW_SIGNATURE)
		{
			throw new IllegalArgumentException("Invalid CBW Signature: " + Integer.toHexString(dCBWSignature));
		}

		if (bCBWCBLength > 16)
		{
			throw new IllegalArgumentException("CBWCB must be at most 16 bytes long");
		}

		if (bCBWCBLength > cBWCB.length)
		{
			throw new IllegalArgumentException("CBWCB can not be shorter than bCBWCBLength");
		}

		this.dCBWSignature = dCBWSignature;
		this.dCBWTag = dCBWTag;
		this.dCBWDataTransferLength = dCBWDataTransferLength;
		this.bmCBWFlags = bmCBWFlags;
		this.bCBWLUN = bCBWLUN;
		this.bCBWCBLength = bCBWCBLength;

		if (bCBWCBLength < cBWCB.length)
		{
			// Trim the array to the actual length
			this.cBWCB = new byte[bCBWCBLength];
			System.arraycopy(cBWCB, 0, this.cBWCB, 0, bCBWCBLength);
		}
		else
		{
			this.cBWCB = cBWCB;
		}
	}

	protected int getSignature()
	{
		return dCBWSignature;
	}

	public int getTag()
	{
		return dCBWTag;
	}

	public int getDataTransferLength()
	{
		return dCBWDataTransferLength;
	}

	public CBWFlags getFlags()
	{
		return bmCBWFlags;
	}

	public byte getLUN()
	{
		return bCBWLUN;
	}

	protected byte getCommandBlockLength()
	{
		return bCBWCBLength;
	}

	public byte[] getCommandBlock()
	{
		return cBWCB;
	}

	public static CommandBlockWrapper fromBytes(final byte[] data)
	{
		final ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

		final int dCBWSignature = buffer.getInt();
		final int dCBWTag = buffer.getInt();
		final int dCBWDataTransferLength = buffer.getInt();
		final CBWFlags bmCBWFlags = new CBWFlags(buffer.get());
		final byte bCBWLUN = buffer.get();
		final byte bCBWCBLength = buffer.get();
		final byte[] cbwCB = new byte[bCBWCBLength];
		buffer.get(cbwCB);
		return new CommandBlockWrapper(dCBWSignature, dCBWTag, dCBWDataTransferLength, bmCBWFlags, bCBWLUN, bCBWCBLength, cbwCB);
	}
}
