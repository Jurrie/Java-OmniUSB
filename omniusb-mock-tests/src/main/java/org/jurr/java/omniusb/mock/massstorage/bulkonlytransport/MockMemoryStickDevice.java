package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.usb3.IUsbControlIrp;
import javax.usb3.IUsbStringDescriptor;
import javax.usb3.descriptor.UsbDeviceDescriptor;
import javax.usb3.enumerated.EDescriptorType;
import javax.usb3.enumerated.EDevicePortSpeed;
import javax.usb3.enumerated.EUSBClassCode;
import javax.usb3.exception.UsbException;
import javax.usb3.exception.UsbPlatformException;
import javax.usb3.mockservices.IMockUsbDevice;
import javax.usb3.ri.AUsbConfiguration;
import javax.usb3.ri.UsbDeviceId;
import javax.usb3.ri.massstorage.bulkonlytransport.UsbMassStorageBulkOnlyTransportDevice;

import org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.MockMemoryStickFirmware;

public class MockMemoryStickDevice extends UsbMassStorageBulkOnlyTransportDevice implements IMockUsbDevice
{
	public static final short USB_VENDOR_ID = 0x1d6b; // Linux Foundation
	public static final short USB_PRODUCT_ID = 0x00f2; // Unknown - I hope the Linux Foundation doesn't mind :)

	private final List<String> stringDescriptors = new ArrayList<>();

	private final MockMemoryStickFirmware firmware;

	public MockMemoryStickDevice(final UsbDeviceId parentId, final int deviceAddress) throws UsbPlatformException
	{
		super(new UsbDeviceId(1, deviceAddress, 1, new UsbDeviceDescriptor((short) 0x0110, EUSBClassCode.MASS_STORAGE, (byte) 0x00, (byte) 0x00, (byte) 64, USB_VENDOR_ID, USB_PRODUCT_ID, (short) 0x0000, (byte) 1, (byte) 2, (byte) 3, (byte) 1)), parentId, EDevicePortSpeed.FULL.getByteCode());
		addStringDescriptor("Javax USB");
		addStringDescriptor("Mock Memory Stick device");
		addStringDescriptor("0123456789ABCDEF"); // usbmassbulk_10.pdf 4.1.1: The serial number shall contain at least 12 valid digits, represented as a UNICODE string. The last 12 digits of the serial number shall be unique to each USB idVendor and idProduct pair.
		addStringDescriptor("Mock Memory Stick configuration");
		addStringDescriptor("Mock Memory Stick interface");

		firmware = new MockMemoryStickFirmware();
	}

	@Override
	protected AUsbConfiguration doGetUsbConfiguration(final byte i) throws UsbPlatformException
	{
		if (i != 1)
		{
			throw new IllegalArgumentException("Only configuration 1 is supported in mock");
		}

		return new MockMemoryStickConfiguration(this);
	}

	@Override
	protected byte doGetActiveUsbConfiguration() throws UsbPlatformException
	{
		return 1;
	}

	@Override
	protected void doSetActiveUsbConfigurationNumber(final byte i) throws UsbException
	{
		if (i != 1)
		{
			throw new IllegalArgumentException("Only configuration 1 is supported in mock");
		}
	}

	@Override
	protected void doClaimInterface(final byte number, final boolean force) throws UsbException
	{
		// Not implemented in mock
	}

	@Override
	protected void doReleaseInterface(final byte number) throws UsbException
	{
		// Not implemented in mock
	}

	@Override
	protected IUsbStringDescriptor doGetUsbStringDescriptor(final byte index) throws UsbException
	{
		final int indexAsInt = index & 0xff;

		final String string = stringDescriptors.size() > indexAsInt ? stringDescriptors.get(indexAsInt) : "";
		return new IUsbStringDescriptor()
		{
			@Override
			public EDescriptorType descriptorType()
			{
				return EDescriptorType.STRING;
			}

			@Override
			public byte bLength()
			{
				return (byte) descriptorType().getLength();
			}

			@Override
			public byte bDescriptorType()
			{
				return descriptorType().getByteCode();
			}

			@Override
			public String getString()
			{
				return string;
			}

			@Override
			public byte[] bString()
			{
				return string.getBytes(StandardCharsets.UTF_16LE);
			}
		};
	}

	protected byte addStringDescriptor(final String string)
	{
		stringDescriptors.add(string);
		return (byte) (stringDescriptors.size() - 1);
	}

	@Override
	protected short[] getLanguages() throws UsbException
	{
		return new short[] { 0x0409 }; // We only support English - United States in this mock
	}

	@Override
	protected void doVendorSpecificControlTransfer(final IUsbControlIrp irp) throws UsbException
	{
		throw new UsbException("There are no vendor specific control transfers for this mock memory stick");
	}

	public MockMemoryStickFirmware getFirmware()
	{
		return firmware;
	}

	@Override
	protected byte getMaxLUN()
	{
		return (byte) (firmware.getLUNCount() - 1);
	}

	@Override
	protected void reset()
	{
		firmware.reset();
	}
}
