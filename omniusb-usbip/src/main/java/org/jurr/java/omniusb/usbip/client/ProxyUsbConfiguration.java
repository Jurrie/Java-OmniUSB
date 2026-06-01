package org.jurr.java.omniusb.usbip.client;

import java.util.List;

import javax.usb3.IUsbInterface;
import javax.usb3.descriptor.UsbConfigurationDescriptor;
import javax.usb3.exception.UsbException;
import javax.usb3.request.BMConfigurationAttributes;
import javax.usb3.ri.AUsbConfiguration;

import org.jurr.java.omniusb.usbip.domain.UsbDeviceDetails.UsbInterfaceDetails;

class ProxyUsbConfiguration extends AUsbConfiguration
{
	ProxyUsbConfiguration(final ProxyUsbDevice device, final List<UsbInterfaceDetails> usbInterfaceDetails)
	{
		super(device, createUsbConfigurationDescriptor(usbInterfaceDetails));

		for (byte i = 0; i < usbInterfaceDetails.size(); i++)
		{
			addInterface(new ProxyUsbInterface(this, i, usbInterfaceDetails.get(i)));
		}
	}

	private static UsbConfigurationDescriptor createUsbConfigurationDescriptor(final List<UsbInterfaceDetails> usbInterfaceDetails)
	{
		final short wTotalLength = 0; // TODO
		final byte bNumInterfaces = (byte) usbInterfaceDetails.size();

		// The following values are unknown without connecting the USB/IP device
		final byte bConfigurationValue = 1;
		final byte iConfiguration = 0;
		final BMConfigurationAttributes bmAttributes = new BMConfigurationAttributes(false, false);
		final byte bMaxPower = 0;

		return new UsbConfigurationDescriptor(wTotalLength, bNumInterfaces, bConfigurationValue, iConfiguration, bmAttributes, bMaxPower);
	}

	@Override
	protected void doSetUsbInterfaceAlternate(byte number, IUsbInterface usbInterface) throws UsbException
	{
		throw new UnsupportedOperationException("Not implemented in proxy");
	}
}
