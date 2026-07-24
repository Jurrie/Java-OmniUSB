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
package javax.usb3.usb4java;

import javax.usb3.IUsbConfigurationDescriptor;
import javax.usb3.IUsbDevice;
import javax.usb3.IUsbInterface;
import javax.usb3.descriptor.UsbConfigurationDescriptor;
import javax.usb3.exception.UsbException;
import javax.usb3.request.BMConfigurationAttributes;
import javax.usb3.ri.AUsbConfiguration;
import javax.usb3.usb4java.utility.Usb4JavaUsbExceptionFactory;

import org.usb4java.ConfigDescriptor;
import org.usb4java.InterfaceDescriptor;
import org.usb4java.LibUsb;

/**
 * Implementation of JSR-80 IUsbConfiguration.
 *
 * @author Klaus Reimer
 * @author Jesse Caulfield
 */
public final class Usb4JavaUsbConfiguration extends AUsbConfiguration
{
	/**
	 * Constructor.
	 *
	 * @param device The device this configuration belongs to.
	 * @param descriptor The libusb configuration descriptor.
	 */
	public Usb4JavaUsbConfiguration(final IUsbDevice device, final ConfigDescriptor descriptor)
	{
		super(device, convertDescriptor(device, descriptor));
		for (org.usb4java.Interface jniInterface : descriptor.iface())
		{
			for (InterfaceDescriptor ifDescriptor : jniInterface.altsetting())
			{
				final Usb4JavaUsbInterface usbInterface = new Usb4JavaUsbInterface(this, ifDescriptor);
				addInterface(usbInterface);
			}
		}
	}

	@Override
	protected void doSetUsbInterfaceAlternate(final byte number, final IUsbInterface usbInterface) throws UsbException
	{
		final int result = LibUsb.setInterfaceAltSetting(((Usb4JavaAUsbDevice) getUsbDevice()).open(),
				number,
				usbInterface.getUsbInterfaceDescriptor().bAlternateSetting());
		if (result < 0)
		{
			throw Usb4JavaUsbExceptionFactory.createPlatformException("Unable to set alternate interface", result);
		}
	}

	static IUsbConfigurationDescriptor convertDescriptor(final IUsbDevice device, final ConfigDescriptor input)
	{
		return new UsbConfigurationDescriptor(
				device,
				input.bNumInterfaces(),
				input.bConfigurationValue(),
				input.iConfiguration(),
				new BMConfigurationAttributes(input.bmAttributes()),
				input.bMaxPower());
	}
}
