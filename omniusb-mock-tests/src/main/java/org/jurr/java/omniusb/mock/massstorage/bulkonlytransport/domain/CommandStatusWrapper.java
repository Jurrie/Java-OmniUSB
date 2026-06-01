package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.domain;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CommandStatusWrapper
{
	private static final int CSW_SIGNATURE = 0x53425355; // 'USBS' in little-endian

	public enum CSWStatus
	{
		PASSED((byte) 0x00), FAILED((byte) 0x01), PHASE_ERROR((byte) 0x02);

		private final byte code;

		CSWStatus(final byte code)
		{
			this.code = code;
		}

		public byte getCode()
		{
			return code;
		}

		public static CSWStatus fromCode(final byte code)
		{
			for (final CSWStatus status : values())
			{
				if (status.code == code)
				{
					return status;
				}
			}
			throw new IllegalArgumentException("Unknown CSW Status code: " + code);
		}
	}

	private final int dCSWSignature;
	private final int dCSWTag;
	private final int dCSWDataResidue;
	private final CSWStatus bCSWStatus;

	public CommandStatusWrapper(final int dCSWTag, final int dCSWDataResidue, final CSWStatus bCSWStatus)
	{
		this(CSW_SIGNATURE, dCSWTag, dCSWDataResidue, bCSWStatus);
	}

	private CommandStatusWrapper(final int dCSWSignature, final int dCSWTag, final int dCSWDataResidue, final CSWStatus bCSWStatus)
	{
		if (dCSWSignature != CSW_SIGNATURE)
		{
			throw new IllegalArgumentException("Invalid CSW Signature: " + Integer.toHexString(dCSWSignature));
		}

		this.dCSWSignature = dCSWSignature;
		this.dCSWTag = dCSWTag;
		this.dCSWDataResidue = dCSWDataResidue;
		this.bCSWStatus = bCSWStatus;
	}

	protected int getSignature()
	{
		return dCSWSignature;
	}

	public int getTag()
	{
		return dCSWTag;
	}

	public int getDataResidue()
	{
		return dCSWDataResidue;
	}

	public CSWStatus getStatus()
	{
		return bCSWStatus;
	}

	public byte[] toBytes()
	{
		final ByteBuffer buffer = ByteBuffer.allocate(13).order(ByteOrder.LITTLE_ENDIAN);
		buffer.putInt(dCSWSignature);
		buffer.putInt(dCSWTag);
		buffer.putInt(dCSWDataResidue);
		buffer.put(bCSWStatus.getCode());
		return buffer.array();
	}

	protected static CommandStatusWrapper fromBytes(final byte[] data)
	{
		final ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

		final int dCSWSignature = buffer.getInt();
		final int dCSWTag = buffer.getInt();
		final int dCSWDataResidue = buffer.getInt();
		final CSWStatus bCSWStatus = CSWStatus.fromCode(buffer.get());
		return new CommandStatusWrapper(dCSWSignature, dCSWTag, dCSWDataResidue, bCSWStatus);
	}
}
