/*
 * Copyright (C) 2013 Klaus Reimer
 * Copyright (C) 2014 Jesse Caulfield
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package javax.usb3.descriptor;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.usb3.IUsbStringDescriptor;
import javax.usb3.enumerated.EDescriptorType;

/**
 * 9.6.9 String Descriptor implementation.
 * <p>
 * Devices report their attributes using descriptors. A descriptor is a data
 * structure with a defined format.
 * <p>
 * String descriptors are optional. If a device does not support string
 * descriptors, all references to string descriptors within device,
 * configuration, and interface descriptors must be reset to zero.
 * <p>
 * String descriptors use UNICODE encodings. When requesting a string
 * descriptor, the requester specifies the desired language using a sixteen- bit
 * language ID (LANGID) defined by the USB-IF. A USB device may omit all string
 * descriptors. USB devices that omit all string descriptors must not return an
 * array of LANGID codes.
 * <p>
 * String index zero for all languages returns a string descriptor that contains
 * an array of 2-byte LANGID codes supported by the device. e.g. To get a list
 * of supported languages first request the String Descriptor as index zero.
 *
 * @author Klaus Reimer
 * @author Jesse Caulfield
 */
public final class UsbStringDescriptor extends AUsbDescriptor implements IUsbStringDescriptor
{
	/**
	 * The descriptor length.
	 */
	private byte bLength;

	/**
	 * The string data in UTF-16LE encoding.
	 */
	private final byte[] bString;

	/**
	 * Constructs a new String descriptor by reading the descriptor data from the
	 * specified byte buffer.
	 *
	 * @param data The descriptor data as a byte buffer.
	 */
	public UsbStringDescriptor(final ByteBuffer data)
	{
		super(EDescriptorType.STRING);
		bLength = data.get(0); // TODO: This is not Android API 33 safe
		/**
		 * Instantiate the String array.
		 */
		bString = new byte[bLength - 2];
		/**
		 * Set the ByteBuffer position to the data.
		 */
		data.position(2);
		/**
		 * Copy bytes from the 'data' buffer into the 'bString' destination array.
		 */
		data.get(bString);
	}

	/**
	 * Constructs a new string descriptor with the specified data.
	 *
	 * @param string The string.
	 */
	public UsbStringDescriptor(final String string)
	{
		super(EDescriptorType.STRING);
		bString = string.getBytes(StandardCharsets.UTF_16LE);
		bLength = (byte) (bString.length + 6);
	}

	/**
	 * Copy constructor.
	 *
	 * @param descriptor The descriptor from which to copy the data.
	 */
	public UsbStringDescriptor(final IUsbStringDescriptor descriptor)
	{
		super(EDescriptorType.STRING);
		bLength = descriptor.bLength();
		bString = descriptor.bString().clone();
	}

	@Override
	public byte bLength()
	{
		return bLength;
	}

	/**
	 * Get this descriptor's UNICODE encoded string.
	 * <p>
	 * Modifications to the returned byte[] will not affect the StringDescriptor's
	 * bString (i.e. a copy of the bString is returned).
	 *
	 * @return This descriptor's UNICODE encoded string.
	 */
	@Override
	public byte[] bString()
	{
		return bString.clone();
	}

	/**
	 * Get this descriptor's translated String.
	 * <p>
	 * This is the String translation of the {@link #bString() bString}. The
	 * translation is done using the best available Unicode encoding that this JVM
	 * provides. USB strings are 16-bit little-endian; if no 16-bit little-endian
	 * encoding is available, and the string can be converted to 8-bit (all high
	 * bytes are zero), then 8-bit encoding is used. If no encoding is available,
	 * an UnsupportedEncodingException is thrown.
	 * <p>
	 * For information about Unicode see
	 * <a href="http://www.unicode.org/">the Unicode website</a>.
	 *
	 * @return This descriptor's String.
	 */
	@Override
	public String getString()
	{
		return new String(bString, StandardCharsets.UTF_16LE);
	}

	@Override
	public int hashCode()
	{
		int hash = 5;
		hash += 11 * super.hashCode();
		hash += 11 * hash + Arrays.hashCode(bString);
		return hash;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		return hashCode() == obj.hashCode();
	}

	@Override
	public String toString()
	{
		return new String(bString, StandardCharsets.UTF_16LE);
	}
}
