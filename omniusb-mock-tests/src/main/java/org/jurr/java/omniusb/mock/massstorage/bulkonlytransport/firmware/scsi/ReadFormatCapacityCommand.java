package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi;

public class ReadFormatCapacityCommand implements SPC2Command
{
	public static final byte OPERATION_CODE = 0x23;

	private final short allocationLength;

	public ReadFormatCapacityCommand(final byte[] buffer)
	{
		if (buffer.length != getCommandLength())
		{
			throw new IllegalArgumentException("Read Format Capacity command must be " + getCommandLength() + " bytes long but was " + buffer.length);
		}

		if (buffer[0] != OPERATION_CODE)
		{
			throw new IllegalArgumentException("Invalid command code for Read Format Capacity command");
		}

		allocationLength = (short) ((buffer[7] & 0xFF) << 8 | buffer[8] & 0xFF);

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

	public short getAllocationLength()
	{
		return allocationLength;
	}
}
