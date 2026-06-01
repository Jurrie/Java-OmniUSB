package org.jurr.java.omniusb.usbip.domain;

public class UnknownCommand extends UsbIpCommand
{
	private final int commandCode;

	public UnknownCommand(final int commandCode)
	{
		this.commandCode = commandCode;
	}

	public int getCommandCode()
	{
		return commandCode;
	}

	public byte[] toErrorResponse(final int errorCode)
	{
		return new ErrorResponse(commandCode, errorCode).toBuffer();
	}
}
