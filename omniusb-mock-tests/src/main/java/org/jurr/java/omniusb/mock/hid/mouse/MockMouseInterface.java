package org.jurr.java.omniusb.mock.hid.mouse;

import javax.usb3.descriptor.UsbEndpointDescriptor;
import javax.usb3.ri.hid.EHidDescriptorType;
import javax.usb3.ri.hid.EHidProtocol;
import javax.usb3.ri.hid.EHidSubclass;
import javax.usb3.ri.hid.UsbHidDescriptor;
import javax.usb3.ri.hid.UsbHidInterface;
import javax.usb3.ri.hid.UsbHidInterfaceDescriptor;

public class MockMouseInterface extends UsbHidInterface
{
	public MockMouseInterface(final MockMouseConfiguration configuration)
	{
		super(configuration, new UsbHidInterfaceDescriptor((byte) 0, (byte) 0, (byte) 1, EHidSubclass.BOOT_INTERFACE, EHidProtocol.MOUSE, (byte) 4, new UsbEndpointDescriptor[0]));

		// We did not pass endpoint descriptors to the parent constructor. So we need to add them ourselves.
		addUsbEndpoint(new MockMouseEndpoint(this));
	}

	@Override
	protected byte[] getHIDDescriptor()
	{
		final byte[] result = new byte[6 + getNumberOfHidDescriptors() * 3];
		result[0] = 0x09; // bLength
		result[1] = EHidDescriptorType.HID.getByteCode(); // bDescriptorType (HID)
		result[2] = 0x11; // bcdHID 1.11
		result[3] = 0x01;
		result[4] = getHidCountryCode(); // bCountryCode
		result[5] = getNumberOfHidDescriptors(); // bNumDescriptors
		for (int i = 0; i < getNumberOfHidDescriptors(); i++)
		{
			final UsbHidDescriptor hidDescriptor = getHidDescriptor((byte) i);
			result[6 + i * 3] = hidDescriptor.getDescriptorType().getByteCode(); // bDescriptorType
			final byte[] descriptorData = hidDescriptor.getDescriptorData();
			result[7 + i * 3] = (byte) (descriptorData.length & 0xff); // wDescriptorLength LSB
			result[8 + i * 3] = (byte) (descriptorData.length >> 8 & 0xff); // wDescriptorLength MSB
		}

		return result;
	}

	@Override
	protected byte getNumberOfHidDescriptors()
	{
		return 1;
	}

	@Override
	protected UsbHidDescriptor getHidDescriptor(final byte hidDescriptorNumber)
	{
		if (hidDescriptorNumber != 0)
		{
			throw new IllegalArgumentException("Only report descriptor number 0 is supported in this mock");
		}

		// Report Descriptor for a mouse - see hid1_11.pdf page 61
		final byte[] descriptorData = new byte[] {
				0x05, 0x01, // ________ Usage Page (Generic Desktop),
				0x09, 0x02, // _________Usage (Mouse),
				(byte) 0xa1, 0x01, // __ Collection (Application),
				0x09, 0x01, // __________ Usage (Pointer),
				(byte) 0xa1, 0x00, // ___ Collection (Physical),
				(byte) 0x95, 0x03, // _____ Report Count (3),
				0x75, 0x01, // ____________ Report Size (1),
				0x05, 0x09, // ____________ Usage Page (Buttons),
				0x19, 0x01, // ____________ Usage Minimum (1),
				0x29, 0x03, // ____________ Usage Maximum (3),
				0x15, 0x00, // ____________ Logical Minimum (0),
				0x25, 0x01, // ____________ Logical Maximum (1),
				(byte) 0x81, 0x02, // _____ Input (Data, Variable, Absolute),
				(byte) 0x95, 0x01, // _____ Report Count (1),
				0x75, 0x05, // ____________ Report Size (5),
				(byte) 0x81, 0x01, // _____ Input (Constant),
				0x75, 0x08, // ____________ Report Size (8),
				(byte) 0x95, 0x02, // _____ Report Count (2),
				0x05, 0x01, // ____________ Usage Page (Generic Desktop),
				0x09, 0x30, // ____________ Usage (X),
				0x09, 0x31, // ____________ Usage (Y),
				0x15, (byte) 0x81, // _____ Logical Minimum (-127),
				0x25, 0x7f, // ____________ Logical Maximum (127),
				(byte) 0x81, 0x06, // _____ Input (Data, Variable, Relative),
				(byte) 0xc0, // _________ End Collection,
				(byte) 0xc0 // ________ End Collection
		};

		return new UsbHidDescriptor(EHidDescriptorType.REPORT, descriptorData);
	}
}