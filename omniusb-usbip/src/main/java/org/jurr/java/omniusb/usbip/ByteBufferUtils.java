package org.jurr.java.omniusb.usbip;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class ByteBufferUtils
{
	private ByteBufferUtils()
	{
	}

	public static String getAsciiString(final ByteBuffer buffer, final int length)
	{
		final byte[] bytes = new byte[length];
		if (buffer.remaining() < length)
		{
			throw new IllegalArgumentException("Buffer too short: " + buffer.remaining() + " < " + length);
		}
		buffer.get(bytes);
		return new String(bytes, StandardCharsets.US_ASCII).trim();
	}

	public static void putAsciiString(final ByteBuffer buffer, final String string, final int length)
	{
		if (string.length() >= length)
		{
			throw new IllegalArgumentException("String (" + (string.length() - length) + ") bytes too long: " + string);
		}
		final byte[] bytes = string.getBytes(StandardCharsets.US_ASCII);
		buffer.put(bytes);
		buffer.position(buffer.position() + length - bytes.length);
	}
}
