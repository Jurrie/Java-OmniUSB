package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi;

public class ModeSelect6Command implements SPC2Command
{
	public static final byte OPERATION_CODE = 0x15;

	private final boolean pageFormat; // PF
	private final boolean savePages; // SP
	private final byte parameterListLength;

	public ModeSelect6Command(final byte[] buffer)
	{
		if (buffer.length != getCommandLength())
		{
			throw new IllegalArgumentException("Mode Select(6) command must be " + getCommandLength() + " bytes long but was " + buffer.length);
		}

		if (buffer[0] != OPERATION_CODE)
		{
			throw new IllegalArgumentException("Invalid operation code for Mode Select(6) command");
		}

		if (buffer[5] != 0)
		{
			throw new IllegalArgumentException("Control byte must be zero for Mode Select(6) command");
		}

		pageFormat = (buffer[1] & 0x10) == 0x10;
		savePages = (buffer[1] & 0x01) == 0x01;
		parameterListLength = buffer[4];
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

	public boolean isPageFormat()
	{
		return pageFormat;
	}

	public boolean isSavePages()
	{
		return savePages;
	}

	public byte getParameterListLength()
	{
		return parameterListLength;
	}
}
