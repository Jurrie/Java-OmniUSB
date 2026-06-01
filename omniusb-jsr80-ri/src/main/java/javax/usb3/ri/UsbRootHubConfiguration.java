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
package javax.usb3.ri;

import javax.usb3.IUsbDevice;
import javax.usb3.IUsbInterface;
import javax.usb3.descriptor.UsbConfigurationDescriptor;
import javax.usb3.enumerated.EDescriptorType;
import javax.usb3.exception.UsbException;
import javax.usb3.request.BMConfigurationAttributes;

/**
 * Virtual USB configuration used by the virtual USB root hub.
 *
 * @author Klaus Reimer
 * @author Jesse Caulfield
 */
public final class UsbRootHubConfiguration extends AUsbConfiguration
{
	/**
	 * Constructor.
	 *
	 * @param device The device this configuration belongs to.
	 */
	public UsbRootHubConfiguration(final IUsbDevice device)
	{
		super(device, new UsbConfigurationDescriptor(
				(short) (EDescriptorType.CONFIGURATION.getLength() + EDescriptorType.INTERFACE.getLength()), // wTotalLength
				(byte) 1, // bNumInterfaces
				(byte) 1, // bConfigurationValue
				(byte) 0, // iConfiguration
				BMConfigurationAttributes.getInstance(), // (byte) 0x80, // bmAttributes
				(byte) 0)); // bMaxPower
		addInterface(new UsbRootHubInterface(this));
	}

	@Override
	protected void doSetUsbInterfaceAlternate(byte number, IUsbInterface usbInterface) throws UsbException
	{
		if (number != 0)
		{
			throw new IllegalArgumentException("No alternate configurations are supported in Root Hub");
		}
	}
}
