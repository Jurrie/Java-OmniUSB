package org.jurr.java.omniusb.usbip;

import java.util.HexFormat;

public final class HexFormatUtils
{
	private static final HexFormat HEX_FORMAT = HexFormat.of();

	private HexFormatUtils()
	{
	}

	public static String format(final byte[] data)
	{
		return HEX_FORMAT.formatHex(data);
	}

	public static String format(final byte[] data, final int offset, final int length)
	{
		return HEX_FORMAT.formatHex(data, offset, length);
	}

	public static String format(final byte data)
	{
		return HEX_FORMAT.formatHex(new byte[] { data });
	}

	public static String format(final short data)
	{
		return HEX_FORMAT.formatHex(new byte[] { (byte) (data >> 8), (byte) data });
	}

	public static String format(final int data)
	{
		return HEX_FORMAT.formatHex(new byte[] { (byte) (data >> 24), (byte) (data >> 16), (byte) (data >> 8), (byte) data });
	}

	public static String format(final long data)
	{
		return HEX_FORMAT.formatHex(new byte[] { (byte) (data >> 56), (byte) (data >> 48), (byte) (data >> 40), (byte) (data >> 32), (byte) (data >> 24), (byte) (data >> 16), (byte) (data >> 8), (byte) data });
	}
}
