package org.jurr.java.omniusb.mock.hid.mouse;

import javax.usb3.IUsbInterface;
import javax.usb3.descriptor.UsbConfigurationDescriptor;
import javax.usb3.exception.UsbException;
import javax.usb3.request.BMConfigurationAttributes;
import javax.usb3.ri.AUsbConfiguration;

public class MockMouseConfiguration extends AUsbConfiguration
{
	protected MockMouseConfiguration(final MockMouseDevice device)
	{
		super(device, new UsbConfigurationDescriptor((short) 34, (byte) 1, (byte) 1, (byte) 3, new BMConfigurationAttributes(false, false), (byte) 0));

		addInterface(new MockMouseInterface(this));
	}

	@Override
	protected void doSetUsbInterfaceAlternate(final byte number, final IUsbInterface usbInterface) throws UsbException
	{
		throw new UnsupportedOperationException("Not implemented in mock");
	}
}
