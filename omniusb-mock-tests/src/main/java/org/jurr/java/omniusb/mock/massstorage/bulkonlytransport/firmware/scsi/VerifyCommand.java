package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi;

public class VerifyCommand implements RBCCommand
{
	public static final byte OPERATION_CODE = 0x2F;

	private final int logicalBlockAddress;
	private final short verificationLength;

	public VerifyCommand(final byte[] buffer)
	{
		if (buffer.length != getCommandLength())
		{
			throw new IllegalArgumentException("Verify command must be " + getCommandLength() + " bytes long but was " + buffer.length);
		}

		if (buffer[0] != OPERATION_CODE)
		{
			throw new IllegalArgumentException("Invalid command code for Verify command");
		}

		if (buffer[9] != 0)
		{
			throw new IllegalArgumentException("Control byte must be zero for Verify command");
		}

		logicalBlockAddress = (buffer[2] & 0xFF) << 24 | (buffer[3] & 0xFF) << 16 | (buffer[4] & 0xFF) << 8 | buffer[5] & 0xFF;
		verificationLength = (short) ((buffer[7] & 0xFF) << 8 | buffer[8] & 0xFF);
	}

	@Override
	public byte getOperationCode()
	{
		return OPERATION_CODE;
	}

	@Override
	public byte getCommandLength()
	{
		return 10;
	}

	public int getLogicalBlockAddress()
	{
		return logicalBlockAddress;
	}

	public short getTransferLength()
	{
		return verificationLength;
	}
}
