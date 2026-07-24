/*
 * Copyright (C) 2013 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information.
 */
// Based on org.usb4java:usb4java-javax:1.3.0#org.usb4java.javax.descriptors.SimpleUsbConfigurationDescriptorTest
package javax.usb3.descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import javax.usb3.IUsbControlIrp;
import javax.usb3.IUsbDevice;
import javax.usb3.IUsbInterface;
import javax.usb3.IUsbStringDescriptor;
import javax.usb3.enumerated.EDescriptorType;
import javax.usb3.enumerated.EDevicePortSpeed;
import javax.usb3.enumerated.EUSBClassCode;
import javax.usb3.exception.UsbException;
import javax.usb3.exception.UsbPlatformException;
import javax.usb3.request.BMConfigurationAttributes;
import javax.usb3.ri.AUsbConfiguration;
import javax.usb3.ri.UsbDevice;
import javax.usb3.ri.UsbDeviceId;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link SimpleUsbConfigurationDescriptor}.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
class UsbConfigurationDescriptorTest
{
	/** The test subject. */
	private static UsbConfigurationDescriptor descriptor;

	private static IUsbDevice device;
	private static AUsbConfiguration configuration;

	private static final byte LENGTH = (byte) EDescriptorType.CONFIGURATION.getLength();
	private static final byte DESCRIPTOR_TYPE = EDescriptorType.CONFIGURATION.getByteCode();

	/** Value for {@link SimpleUsbConfigurationDescriptor#bNumInterfaces()}. */
	private static final byte NUM_INTERFACES = (byte) 0xfc;

	/** Value for {@link SimpleUsbConfigurationDescriptor#bConfigurationValue()}. */
	private static final byte CONFIGURATION_VALUE = (byte) 0x01;

	/** Value for {@link SimpleUsbConfigurationDescriptor#iConfiguration()}. */
	private static final byte CONFIGURATION = (byte) 0xfa;

	/** Value for {@link SimpleUsbConfigurationDescriptor#bmAttributes()}. */
	private static final BMConfigurationAttributes ATTRIBUTES = new BMConfigurationAttributes((byte) 0xf9);

	/** Value for {@link SimpleUsbConfigurationDescriptor#bMaxPower()}. */
	private static final byte MAX_POWER = (byte) 0xf8;

	/** A wrong value for equality test. */
	private static final byte WRONG = 0;

	/**
	 * Setup the test subject.
	 *
	 * @throws UsbPlatformException
	 */
	@BeforeAll
	static void setUp() throws UsbPlatformException
	{
		device = createMockUsbDevice();

		descriptor = new UsbConfigurationDescriptor(device,
				NUM_INTERFACES,
				CONFIGURATION_VALUE, CONFIGURATION, ATTRIBUTES, MAX_POWER);

		configuration = createMockUsbConfiguration(device, descriptor);
	}

	private static IUsbDevice createMockUsbDevice() throws UsbPlatformException
	{
		final UsbDeviceId usbDeviceId = new UsbDeviceId(1, 1, 1, new UsbDeviceDescriptor((short) 0x0110, EUSBClassCode.HID_HUMAN_INTERFACE_DEVICE, (byte) 0x00, (byte) 2, (byte) 64, (short) 0xfffe, (short) 0xfffd, (short) 0x0000, (byte) 1, (byte) 2, (byte) 0, (byte) 1));
		return new UsbDevice(usbDeviceId, usbDeviceId, EDevicePortSpeed.FULL.getByteCode())
		{
			@Override
			protected AUsbConfiguration doGetUsbConfiguration(final byte i) throws UsbPlatformException
			{
				return configuration;
			}

			@Override
			protected byte doGetActiveUsbConfiguration() throws UsbPlatformException
			{
				return CONFIGURATION_VALUE;
			}

			@Override
			protected void doSetActiveUsbConfigurationNumber(final byte i) throws UsbException
			{
				// Not implemented in mock
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
				return null;
			}

			@Override
			protected short[] getLanguages() throws UsbException
			{
				return null;
			}

			@Override
			protected void doVendorSpecificControlTransfer(IUsbControlIrp irp) throws UsbException
			{
				// Not implemented in mock
			}
		};
	}

	private static AUsbConfiguration createMockUsbConfiguration(final IUsbDevice device, final UsbConfigurationDescriptor descriptor)
	{
		return new AUsbConfiguration(device, descriptor)
		{
			@Override
			protected void doSetUsbInterfaceAlternate(final byte number, final IUsbInterface usbInterface) throws UsbException
			{
			}
		};
	}

	/**
	 * Tests the {@link SimpleUsbConfigurationDescriptor#bLength()} method.
	 */
	@Test
	void testLength()
	{
		assertEquals(LENGTH, descriptor.bLength());
	}

	/**
	 * Tests the {@link SimpleUsbConfigurationDescriptor#bDescriptorType()}
	 * method.
	 */
	@Test
	void testDescriptorType()
	{
		assertEquals(DESCRIPTOR_TYPE, descriptor.bDescriptorType());
	}

	/**
	 * Tests the {@link SimpleUsbConfigurationDescriptor#wTotalLength()} method.
	 */
	@Test
	void testTotalLength()
	{
		// Since there are no interfaces or endpoints, the total length is the same as the length of the configuration descriptor itself.
		assertEquals(EDescriptorType.CONFIGURATION.getLength(), descriptor.wTotalLength());
	}

	/**
	 * Tests the {@link SimpleUsbConfigurationDescriptor#bNumInterfaces()}
	 * method.
	 */
	@Test
	void testNumInterfaces()
	{
		assertEquals(NUM_INTERFACES, descriptor.bNumInterfaces());
	}

	/**
	 * Tests the {@link SimpleUsbConfigurationDescriptor#bConfigurationValue()}
	 * method.
	 */
	@Test
	void testConfigurationValue()
	{
		assertEquals(CONFIGURATION_VALUE, descriptor.bConfigurationValue());
	}

	/**
	 * Tests the {@link SimpleUsbConfigurationDescriptor#iConfiguration()}
	 * method.
	 */
	@Test
	void testConfiguration()
	{
		assertEquals(CONFIGURATION, descriptor.iConfiguration());
	}

	/**
	 * Tests the {@link SimpleUsbConfigurationDescriptor#bmAttributes()} method.
	 */
	@Test
	void testAttributes()
	{
		assertEquals(ATTRIBUTES.asByte(), descriptor.bmAttributes());
	}

	/**
	 * Tests the {@link SimpleUsbConfigurationDescriptor#bMaxPower()} method.
	 */
	@Test
	void testMaxPower()
	{
		assertEquals(MAX_POWER, descriptor.bMaxPower());
	}

	/**
	 * Tests the {@link SimpleUsbConfigurationDescriptor#hashCode()} method.
	 */
	@Test
	void testHashCode()
	{
		final int code = descriptor.hashCode();
		assertEquals(code, descriptor.hashCode());
		assertEquals(code, new UsbConfigurationDescriptor(
				device, NUM_INTERFACES,
				CONFIGURATION_VALUE, CONFIGURATION, ATTRIBUTES,
				MAX_POWER).hashCode());
	}

	/**
	 * Tests the {@link SimpleUsbConfigurationDescriptor#equals(Object)} method.
	 */
	@Test
	void testEquals()
	{
		assertNotEquals(null, descriptor);
		assertNotEquals(new Object(), descriptor);
		assertEquals(new UsbConfigurationDescriptor(
				device, NUM_INTERFACES,
				CONFIGURATION_VALUE, CONFIGURATION, ATTRIBUTES, MAX_POWER), descriptor);
		assertNotEquals(new UsbConfigurationDescriptor(
				device, WRONG,
				CONFIGURATION_VALUE, CONFIGURATION, ATTRIBUTES, MAX_POWER), descriptor);
		assertNotEquals(new UsbConfigurationDescriptor(
				device, NUM_INTERFACES,
				WRONG, CONFIGURATION, ATTRIBUTES, MAX_POWER), descriptor);
		assertNotEquals(new UsbConfigurationDescriptor(
				device, NUM_INTERFACES,
				CONFIGURATION_VALUE, WRONG, ATTRIBUTES, MAX_POWER), descriptor);
		assertNotEquals(new UsbConfigurationDescriptor(
				device, NUM_INTERFACES,
				CONFIGURATION_VALUE, CONFIGURATION, new BMConfigurationAttributes(WRONG), MAX_POWER), descriptor);
		assertNotEquals(new UsbConfigurationDescriptor(
				device, NUM_INTERFACES,
				CONFIGURATION_VALUE, CONFIGURATION, ATTRIBUTES, WRONG), descriptor);
	}

	/**
	 * Tests the {@link SimpleUsbConfigurationDescriptor#toString()} method.
	 */
	@Test
	void testToString()
	{
		assertEquals(String.format("USB Configuration Descriptor:%n"
				+ "  bLength                  9%n"
				+ "  bDescriptorType          2%n"
				+ "  wTotalLength             9%n"
				+ "  bNumInterfaces         252%n"
				+ "  bConfigurationValue      1%n"
				+ "  iConfiguration         250%n"
				+ "  bmAttributes          0xe0%n"
				+ "    Self Powered%n"
				+ "    Remote Wakeup%n"
				+ "  bMaxPower              496mA%n"), descriptor.toString());
		assertEquals(String.format("USB Configuration Descriptor:%n"
				+ "  bLength                  9%n"
				+ "  bDescriptorType          2%n"
				+ "  wTotalLength             9%n"
				+ "  bNumInterfaces         252%n"
				+ "  bConfigurationValue      1%n"
				+ "  iConfiguration         250%n"
				+ "  bmAttributes          0x80%n"
				+ "    (Bus Powered)%n"
				+ "  bMaxPower              496mA%n"),
				new UsbConfigurationDescriptor(
						device, NUM_INTERFACES,
						CONFIGURATION_VALUE, CONFIGURATION, new BMConfigurationAttributes(false, false), MAX_POWER)
								.toString());
	}
}
