package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi;

public class SynchronizeCacheCommand implements RBCCommand
{
	public static final byte OPERATION_CODE = 0x35;

	public SynchronizeCacheCommand(final byte[] buffer)
	{
		if (buffer.length != getCommandLength())
		{
			throw new IllegalArgumentException("Synchronize Cache command must be " + getCommandLength() + " bytes long but was " + buffer.length);
		}

		if (buffer[0] != OPERATION_CODE)
		{
			throw new IllegalArgumentException("Invalid command code for Synchronize Cache command");
		}

		if (buffer[9] != 0)
		{
			throw new IllegalArgumentException("Control byte must be zero for Synchronize Cache command");
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
