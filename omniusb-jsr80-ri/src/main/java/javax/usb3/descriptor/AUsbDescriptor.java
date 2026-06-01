/*
 * Copyright (C) 2011 Klaus Reimer
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
// Based on org.usb4java:usb4java-javax:1.3.0#org.usb4java.javax.descriptors.SimpleUsbDescriptor
package javax.usb3.descriptor;

import java.util.Objects;

import javax.usb3.IUsbDescriptor;
import javax.usb3.enumerated.EDescriptorType;

/**
 * 9.5 and 9.6. Abstract USB Standard USB Descriptor Definition. This is a base
 * class to be extended by all Standard USB Descriptor implementations.
 * <p>
 * USB devices report their attributes using descriptors. A descriptor is a data
 * structure with a defined format. Each descriptor begins with a byte-wide
 * field that contains the total number of bytes in the descriptor followed by a
 * byte-wide field that identifies the descriptor type.
 * <p>
 * Using descriptors allows concise storage of the attributes of individual
 * configurations because each configuration may reuse descriptors or portions
 * of descriptors from other configurations that have the same characteristics.
 * In this manner, the descriptors resemble individual data records in a
 * relational database.
 * <p>
 * A device may return class- or vendor-specific descriptors in two ways:
 * <ol>
 * <li>If the class or vendor specific descriptors use the same format as
 * standard descriptors (e.g., start with a length byte and followed by a type
 * byte), they must be returned interleaved with standard descriptors in the
 * configuration information returned by a GetDescriptor(Configuration) request.
 * In this case, the class or vendor-specific descriptors must follow a related
 * standard descriptor they modify or extend.
 * </li>
 * <li>If the class or vendor specific descriptors are independent of
 * configuration information or use a non- standard format, a GetDescriptor()
 * request specifying the class or vendor specific descriptor type and index may
 * be used to retrieve the descriptor from the device. A class or vendor
 * specification will define the appropriate way to retrieve these descriptors.
 * </li>
 * </ol>
 * <p>
 * See 9.5 and 9.6 of USB 3.1 Specification
 *
 * @author Jesse Caulfield
 * @author Klaus Reimer
 */
abstract class AUsbDescriptor implements IUsbDescriptor
{
	/**
	 * The Standard USB descriptor definition enumerated type. This is used to
	 * pre-populate the instance with configurations from the USB specification. *
	 */
	protected final EDescriptorType descriptorType;

	/**
	 * Construct a Standard USB Descriptor Definition for the indicated enumerated
	 * type.
	 *
	 * @param descriptorType An enumerated standard Descriptor Type
	 */
	protected AUsbDescriptor(final EDescriptorType descriptorType)
	{
		this.descriptorType = descriptorType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EDescriptorType descriptorType()
	{
		return descriptorType;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(descriptorType);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		AUsbDescriptor other = (AUsbDescriptor) obj;
		return descriptorType == other.descriptorType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte bDescriptorType()
	{
		return descriptorType.getByteCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte bLength()
	{
		return (byte) descriptorType.getLength();
	}

	@Override
	public String toString()
	{
		return descriptorType.name();
	}
}
