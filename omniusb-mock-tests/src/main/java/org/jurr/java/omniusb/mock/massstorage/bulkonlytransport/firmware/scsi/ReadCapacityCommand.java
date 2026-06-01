package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi;

public class ReadCapacityCommand implements RBCCommand
{
	public static final byte OPERATION_CODE = 0x25;

	public ReadCapacityCommand(final byte[] buffer)
	{
		if (buffer.length != getCommandLength())
		{
			throw new IllegalArgumentException("Read Capacity command must be " + getCommandLength() + " bytes long but was " + buffer.length);
		}

		if (buffer[0] != OPERATION_CODE)
		{
			throw new IllegalArgumentException("Invalid command code for Read Capacity command");
		}

		if (buffer[9] != 0)
		{
			throw new IllegalArgumentException("Control byte must be zero for Read Capacity command");
		}
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
}
