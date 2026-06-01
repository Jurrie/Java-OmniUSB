package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi;

public class TestUnitReadyCommand implements SPC2Command
{
	public static final byte OPERATION_CODE = 0x00;

	// TODO: The content of this command is not yet implemented

	public TestUnitReadyCommand(final byte[] buffer)
	{
		if (buffer.length != getCommandLength())
		{
			throw new IllegalArgumentException("Test Unit Ready command must be " + getCommandLength() + " bytes long but was " + buffer.length);
		}

		if (buffer[0] != OPERATION_CODE)
		{
			throw new IllegalArgumentException("Invalid command code for Test Unit Ready command");
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
		return 6;
	}
}
