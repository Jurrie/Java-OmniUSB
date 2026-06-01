package org.jurr.java.omniusb.usbip.domain;

import static org.jurr.java.omniusb.usbip.Constants.OP_REQ_DEVLIST;
import static org.jurr.java.omniusb.usbip.Constants.OP_REQ_IMPORT;
import static org.jurr.java.omniusb.usbip.Constants.USBIP_CMD_SUBMIT;
import static org.jurr.java.omniusb.usbip.Constants.USBIP_CMD_UNLINK;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;

public final class CommandFactory
{
	private CommandFactory()
	{
		// Utility class
	}

	@SuppressWarnings("unchecked")
	public static <T extends UsbIpCommand> T getCommand(final BufferedInputStream inputStream) throws IOException
	{
		inputStream.mark(4);
		final byte[] commandCodeBytes = new byte[4];
		if (inputStream.read(commandCodeBytes) != 4)
		{
			throw new EOFException("End of stream reached");
		}
		final int commandCode = (commandCodeBytes[0] & 0xFF) << 24 | (commandCodeBytes[1] & 0xFF) << 16 | (commandCodeBytes[2] & 0xFF) << 8 | commandCodeBytes[3] & 0xFF;
		inputStream.reset();

		switch (commandCode)
		{
		case USBIP_CMD_SUBMIT:
		{
			return (T) UsbIpSubmitCommand.fromInputStream(inputStream);
		}
		case USBIP_CMD_UNLINK:
		{
			return (T) UsbIpUnlinkCommand.fromInputStream(inputStream);
		}
		default:
			final short shortCommandCode = (short) commandCode;
			switch (shortCommandCode)
			{
			case OP_REQ_DEVLIST: // Retrieve the list of exported USB devices.
				return (T) DeviceListCommand.fromInputStream(inputStream);
			case OP_REQ_IMPORT: // Request to import (attach) a remote USB device.
				return (T) ImportDeviceCommand.fromInputStream(inputStream);
			default:
				return (T) new UnknownCommand(commandCode);
			}
		}
	}
}
