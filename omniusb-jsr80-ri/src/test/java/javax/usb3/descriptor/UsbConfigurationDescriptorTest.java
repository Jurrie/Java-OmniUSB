/*
 * Copyright (C) 2013 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information.
 */
// Based on org.usb4java:usb4java-javax:1.3.0#org.usb4java.javax.descriptors.SimpleUsbConfigurationDescriptorTest
package javax.usb3.descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import javax.usb3.enumerated.EDescriptorType;
import javax.usb3.request.BMConfigurationAttributes;

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

	private static final byte LENGTH = (byte) EDescriptorType.CONFIGURATION.getLength();
	private static final byte DESCRIPTOR_TYPE = EDescriptorType.CONFIGURATION.getByteCode();

	/** Value for {@link SimpleUsbConfigurationDescriptor#wTotalLength()}. */
	private static final short TOTAL_LENGTH = (short) 0xffff;

	/** Value for {@link SimpleUsbConfigurationDescriptor#bNumInterfaces()}. */
	private static final byte NUM_INTERFACES = (byte) 0xfc;

	/**
	 * Value for {@link SimpleUsbConfigurationDescriptor#bConfigurationValue()}.
	 */
	private static final byte CONFIGURATION_VALUE = (byte) 0xfb;

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
	 */
	@BeforeAll
	static void setUp()
	{
		descriptor = new UsbConfigurationDescriptor(
				TOTAL_LENGTH, NUM_INTERFACES,
				CONFIGURATION_VALUE, CONFIGURATION, ATTRIBUTES, MAX_POWER);
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
		assertEquals(TOTAL_LENGTH, descriptor.wTotalLength());
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
				TOTAL_LENGTH, NUM_INTERFACES,
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
				TOTAL_LENGTH, NUM_INTERFACES,
				CONFIGURATION_VALUE, CONFIGURATION, ATTRIBUTES, MAX_POWER), descriptor);
		assertNotEquals(new UsbConfigurationDescriptor(
				WRONG, NUM_INTERFACES,
				CONFIGURATION_VALUE, CONFIGURATION, ATTRIBUTES, MAX_POWER), descriptor);
		assertNotEquals(new UsbConfigurationDescriptor(
				TOTAL_LENGTH, WRONG,
				CONFIGURATION_VALUE, CONFIGURATION, ATTRIBUTES, MAX_POWER), descriptor);
		assertNotEquals(new UsbConfigurationDescriptor(
				TOTAL_LENGTH, NUM_INTERFACES,
				WRONG, CONFIGURATION, ATTRIBUTES, MAX_POWER), descriptor);
		assertNotEquals(new UsbConfigurationDescriptor(
				TOTAL_LENGTH, NUM_INTERFACES,
				CONFIGURATION_VALUE, WRONG, ATTRIBUTES, MAX_POWER), descriptor);
		assertNotEquals(new UsbConfigurationDescriptor(
				TOTAL_LENGTH, NUM_INTERFACES,
				CONFIGURATION_VALUE, CONFIGURATION, new BMConfigurationAttributes(WRONG), MAX_POWER), descriptor);
		assertNotEquals(new UsbConfigurationDescriptor(
				TOTAL_LENGTH, NUM_INTERFACES,
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
				+ "  wTotalLength         65535%n"
				+ "  bNumInterfaces         252%n"
				+ "  bConfigurationValue    251%n"
				+ "  iConfiguration         250%n"
				+ "  bmAttributes          0xe0%n"
				+ "    Self Powered%n"
				+ "    Remote Wakeup%n"
				+ "  bMaxPower              496mA%n"), descriptor.toString());
		assertEquals(String.format("USB Configuration Descriptor:%n"
				+ "  bLength                  9%n"
				+ "  bDescriptorType          2%n"
				+ "  wTotalLength         65535%n"
				+ "  bNumInterfaces         252%n"
				+ "  bConfigurationValue    251%n"
				+ "  iConfiguration         250%n"
				+ "  bmAttributes          0x80%n"
				+ "    (Bus Powered)%n"
				+ "  bMaxPower              496mA%n"),
				new UsbConfigurationDescriptor(
						TOTAL_LENGTH, NUM_INTERFACES,
						CONFIGURATION_VALUE, CONFIGURATION, new BMConfigurationAttributes(false, false), MAX_POWER)
								.toString());
	}
}
