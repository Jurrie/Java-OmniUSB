package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport;

import javax.usb3.IUsbInterface;
import javax.usb3.descriptor.UsbConfigurationDescriptor;
import javax.usb3.exception.UsbException;
import javax.usb3.request.BMConfigurationAttributes;
import javax.usb3.ri.AUsbConfiguration;

public class MockMemoryStickConfiguration extends AUsbConfiguration
{
	protected MockMemoryStickConfiguration(final MockMemoryStickDevice device)
	{
		super(device, new UsbConfigurationDescriptor((short) 32, (byte) 1, (byte) 1, (byte) 4, new BMConfigurationAttributes(false, false), (byte) 0));

		addInterface(new MockMemoryStickInterface(this));
	}

	@Override
	protected void doSetUsbInterfaceAlternate(final byte number, final IUsbInterface usbInterface) throws UsbException
	{
		throw new UnsupportedOperationException("Not implemented in mock");
	}
}
