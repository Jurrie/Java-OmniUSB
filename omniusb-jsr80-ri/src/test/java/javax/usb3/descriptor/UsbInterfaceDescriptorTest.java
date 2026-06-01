/*
 * Copyright (C) 2013 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information.
 */
// Based on org.usb4java:usb4java-javax:1.3.0#org.usb4java.javax.descriptors.UsbInterfaceDescriptorTest
package javax.usb3.descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import javax.usb3.IUsbEndpointDescriptor;
import javax.usb3.enumerated.EDescriptorType;
import javax.usb3.enumerated.EUSBClassCode;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link UsbInterfaceDescriptor}.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
class UsbInterfaceDescriptorTest
{
	/** The test subject. */
	private static UsbInterfaceDescriptor descriptor;

	private static final byte LENGTH = (byte) EDescriptorType.INTERFACE.getLength();
	private static final byte DESCRIPTOR_TYPE = EDescriptorType.INTERFACE.getByteCode();

	/** Value for {@link UsbInterfaceDescriptor#bInterfaceNumber()}. */
	private static final byte INTERFACE_NUMBER = (byte) 0xfd;

	/** Value for {@link UsbInterfaceDescriptor#bAlternateSetting()}. */
	private static final byte ALTERNATE_SETTING = (byte) 0xfc;

	/** Value for {@link UsbInterfaceDescriptor#bNumEndpoints()}. */
	private static final byte NUM_ENDPOINTS = (byte) 0xfb;

	/** Value for {@link UsbInterfaceDescriptor#bInterfaceClass()}. */
	private static final EUSBClassCode INTERFACE_CLASS = EUSBClassCode.fromByteCode((byte) 0xfe);

	/** Value for {@link UsbInterfaceDescriptor#bInterfaceSubClass()}. */
	private static final byte INTERFACE_SUB_CLASS = (byte) 0xf9;

	/** Value for {@link UsbInterfaceDescriptor#bInterfaceProtocol()}. */
	private static final byte INTERFACE_PROTOCOL = (byte) 0xf8;

	/** Value for {@link UsbInterfaceDescriptor#iInterface()}. */
	private static final byte INTERFACE = (byte) 0xf7;

	/** Value for {@link UsbInterfaceDescriptor#endpoint()}. */
	private static final IUsbEndpointDescriptor[] ENDPOINTS = new IUsbEndpointDescriptor[] {};

	/** A wrong value for equality test. */
	private static final byte WRONG = 0;

	/**
	 * Setup the test subject.
	 */
	@BeforeAll
	static void setUp()
	{
		descriptor = new UsbInterfaceDescriptor(
				INTERFACE_NUMBER, ALTERNATE_SETTING,
				NUM_ENDPOINTS, INTERFACE_CLASS, INTERFACE_SUB_CLASS,
				INTERFACE_PROTOCOL, INTERFACE, ENDPOINTS);
	}

	/**
	 * Tests the {@link UsbInterfaceDescriptor#bLength()} method.
	 */
	@Test
	void testLength()
	{
		assertEquals(LENGTH, descriptor.bLength());
	}

	/**
	 * Tests the {@link UsbInterfaceDescriptor#bDescriptorType()} method.
	 */
	@Test
	void testDescriptorType()
	{
		assertEquals(DESCRIPTOR_TYPE, descriptor.bDescriptorType());
	}

	/**
	 * Tests the {@link UsbInterfaceDescriptor#bInterfaceNumber()} method.
	 */
	@Test
	void testInterfaceNumber()
	{
		assertEquals(INTERFACE_NUMBER, descriptor.bInterfaceNumber());
	}

	/**
	 * Tests the {@link UsbInterfaceDescriptor#bAlternateSetting()}
	 * method.
	 */
	@Test
	void testAlternateSetting()
	{
		assertEquals(ALTERNATE_SETTING, descriptor.bAlternateSetting());
	}

	/**
	 * Tests the {@link UsbInterfaceDescriptor#bNumEndpoints()} method.
	 */
	@Test
	void testNumEndpoints()
	{
		assertEquals(NUM_ENDPOINTS, descriptor.bNumEndpoints());
	}

	/**
	 * Tests the {@link UsbInterfaceDescriptor#bInterfaceClass()} method.
	 */
	@Test
	void testInterfaceClass()
	{
		assertEquals(INTERFACE_CLASS.getByteCode(), descriptor.bInterfaceClass());
	}

	/**
	 * Tests the {@link UsbInterfaceDescriptor#bInterfaceSubClass()}
	 * method.
	 */
	@Test
	void testInterfaceSubClass()
	{
		assertEquals(INTERFACE_SUB_CLASS, descriptor.bInterfaceSubClass());
	}

	/**
	 * Tests the {@link UsbInterfaceDescriptor#bInterfaceProtocol()}
	 * method.
	 */
	@Test
	void testInterfaceProtocol()
	{
		assertEquals(INTERFACE_PROTOCOL, descriptor.bInterfaceProtocol());
	}

	/**
	 * Tests the {@link UsbInterfaceDescriptor#iInterface()} method.
	 */
	@Test
	void testInterface()
	{
		assertEquals(INTERFACE, descriptor.iInterface());
	}

	/**
	 * Tests the {@link UsbInterfaceDescriptor#hashCode()} method.
	 */
	@Test
	void testHashCode()
	{
		final int code = descriptor.hashCode();
		assertEquals(code, descriptor.hashCode());
		assertEquals(code, new UsbInterfaceDescriptor(
				INTERFACE_NUMBER, ALTERNATE_SETTING,
				NUM_ENDPOINTS, INTERFACE_CLASS, INTERFACE_SUB_CLASS,
				INTERFACE_PROTOCOL, INTERFACE, ENDPOINTS).hashCode());
	}

	/**
	 * Tests the {@link UsbInterfaceDescriptor#equals(Object)} method.
	 */
	@Test
	void testEquals()
	{
		assertNotEquals(null, descriptor);
		assertNotEquals(new Object(), descriptor);
		assertEquals(new UsbInterfaceDescriptor(
				INTERFACE_NUMBER, ALTERNATE_SETTING,
				NUM_ENDPOINTS, INTERFACE_CLASS, INTERFACE_SUB_CLASS,
				INTERFACE_PROTOCOL, INTERFACE, ENDPOINTS), descriptor);
		assertNotEquals(new UsbInterfaceDescriptor(
				WRONG, ALTERNATE_SETTING,
				NUM_ENDPOINTS, INTERFACE_CLASS, INTERFACE_SUB_CLASS,
				INTERFACE_PROTOCOL, INTERFACE, ENDPOINTS), descriptor);
		assertNotEquals(new UsbInterfaceDescriptor(
				INTERFACE_NUMBER, WRONG,
				NUM_ENDPOINTS, INTERFACE_CLASS, INTERFACE_SUB_CLASS,
				INTERFACE_PROTOCOL, INTERFACE, ENDPOINTS), descriptor);
		assertNotEquals(new UsbInterfaceDescriptor(
				INTERFACE_NUMBER, ALTERNATE_SETTING,
				WRONG, INTERFACE_CLASS, INTERFACE_SUB_CLASS,
				INTERFACE_PROTOCOL, INTERFACE, ENDPOINTS), descriptor);
		assertNotEquals(new UsbInterfaceDescriptor(
				INTERFACE_NUMBER, ALTERNATE_SETTING,
				NUM_ENDPOINTS, EUSBClassCode.fromByteCode(WRONG), INTERFACE_SUB_CLASS,
				INTERFACE_PROTOCOL, INTERFACE, ENDPOINTS), descriptor);
		assertNotEquals(new UsbInterfaceDescriptor(
				INTERFACE_NUMBER, ALTERNATE_SETTING,
				NUM_ENDPOINTS, INTERFACE_CLASS, WRONG,
				INTERFACE_PROTOCOL, INTERFACE, ENDPOINTS), descriptor);
		assertNotEquals(new UsbInterfaceDescriptor(
				INTERFACE_NUMBER, ALTERNATE_SETTING,
				NUM_ENDPOINTS, INTERFACE_CLASS, INTERFACE_SUB_CLASS,
				WRONG, INTERFACE, ENDPOINTS), descriptor);
		assertNotEquals(new UsbInterfaceDescriptor(
				INTERFACE_NUMBER, ALTERNATE_SETTING,
				NUM_ENDPOINTS, INTERFACE_CLASS, INTERFACE_SUB_CLASS,
				INTERFACE_PROTOCOL, WRONG, ENDPOINTS), descriptor);
	}

	/**
	 * Tests the {@link UsbInterfaceDescriptor#toString()} method.
	 */
	@Test
	void testToString()
	{
		assertEquals(String.format("USB Interface Descriptor:%n"
				+ "  bLength                   9%n"
				+ "  bDescriptorType           4%n"
				+ "  bInterfaceNumber        253%n"
				+ "  bAlternateSetting       252%n"
				+ "  bNumEndpoints           251%n"
				+ "  bInterfaceClass         254 Interface%n"
				+ "  bInterfaceSubClass      249%n"
				+ "  bInterfaceProtocol      248%n"
				+ "  iInterface              247%n"), descriptor.toString());
	}
}
