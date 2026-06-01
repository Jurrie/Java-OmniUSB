package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi;

public class InquiryCommand implements SPC2Command
{
	public static final byte OPERATION_CODE = 0x12;

	private final boolean commandSupportData; // CmdDt
	private final boolean enableVitalProductData; // EVPD
	private final byte pageCode;
	private final byte allocationLength;
	private final byte control;

	public InquiryCommand(final byte[] buffer)
	{
		if (buffer.length != getCommandLength())
		{
			throw new IllegalArgumentException("Inquiry command must be " + getCommandLength() + " bytes long but was " + buffer.length);
		}

		if (buffer[0] != OPERATION_CODE)
		{
			throw new IllegalArgumentException("Invalid operation code for Inquiry command");
		}

		commandSupportData = (buffer[1] & 0x02) == 0x02;
		enableVitalProductData = (buffer[1] & 0x01) == 0x01;
		pageCode = buffer[2];
		allocationLength = buffer[4];
		control = buffer[5];
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

	public boolean isCommandSupportData()
	{
		return commandSupportData;
	}

	public boolean isEnableVitalProductData()
	{
		return enableVitalProductData;
	}

	public byte getPageCode()
	{
		return pageCode;
	}

	public byte getAllocationLength()
	{
		return allocationLength;
	}

	public byte getControl()
	{
		return control;
	}
}
