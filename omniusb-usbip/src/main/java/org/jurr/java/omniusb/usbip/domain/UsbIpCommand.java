package org.jurr.java.omniusb.usbip.domain;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public abstract class UsbIpCommand
{
	protected static byte[] readFromInputStream(final InputStream in, final int length) throws IOException
	{
		final byte[] buffer = new byte[length];
		int bytesRead = 0;
		while (bytesRead < length)
		{
			final int result = in.read(buffer, bytesRead, length - bytesRead);
			if (result == -1)
			{
				throw new EOFException("End of stream reached while reading USB/IP command: tried to read " + length + " bytes, only read " + bytesRead + " bytes");
			}
			bytesRead += result;
		}
		return buffer;
	}
}
