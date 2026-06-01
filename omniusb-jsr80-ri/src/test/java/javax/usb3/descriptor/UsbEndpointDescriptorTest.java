/*
 * Copyright (C) 2013 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information.
 */
// Based on org.usb4java:usb4java-javax:1.3.0#org.usb4java.javax.descriptors.SimpleUsbEndpointDescriptorTest
package javax.usb3.descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import javax.usb3.enumerated.EDescriptorType;
import javax.usb3.enumerated.EEndpointDirection;
import javax.usb3.request.BEndpointAddress;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link UsbEndpointDescriptor}.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
class UsbEndpointDescriptorTest
{
	/** The test subject. */
	private static UsbEndpointDescriptor descriptor;

	private static final byte LENGTH = (byte) EDescriptorType.ENDPOINT.getLength();
	private static final byte DESCRIPTOR_TYPE = EDescriptorType.ENDPOINT.getByteCode();

	/** Value for {@link UsbEndpointDescriptor#bEndpointAddress()}. */
	private static final BEndpointAddress ENDPOINT_ADDRESS = new BEndpointAddress(13, EEndpointDirection.DEVICE_TO_HOST);

	/** Value for {@link UsbEndpointDescriptor#bmAttributes()}. */
	private static final byte ATTRIBUTES = (byte) 0xfc;

	/** Value for {@link UsbEndpointDescriptor#wMaxPacketSize()}. */
	private static final short MAX_PACKET_SIZE = (short) 0xffff;

	/** Value for {@link UsbEndpointDescriptor#bInterval()}. */
	private static final byte INTERVAL = (byte) 0xfb;

	/** A wrong value for equality test. */
	private static final byte WRONG = 0;

	/**
	 * Setup the test subject.
	 */
	@BeforeAll
	static void setUp()
	{
		descriptor = new UsbEndpointDescriptor(
				ENDPOINT_ADDRESS, ATTRIBUTES,
				MAX_PACKET_SIZE, INTERVAL);
	}

	/**
	 * Tests the {@link UsbEndpointDescriptor#bLength()} method.
	 */
	@Test
	void testLength()
	{
		assertEquals(LENGTH, descriptor.bLength());
	}

	/**
	 * Tests the {@link UsbEndpointDescriptor#bDescriptorType()} method.
	 */
	@Test
	void testDescriptorType()
	{
		assertEquals(DESCRIPTOR_TYPE, descriptor.bDescriptorType());
	}

	/**
	 * Tests the {@link UsbEndpointDescriptor#bEndpointAddress()} method.
	 */
	@Test
	void testEndpointAddress()
	{
		assertEquals(ENDPOINT_ADDRESS.getByteCode(), descriptor.bEndpointAddress());
	}

	/**
	 * Tests the {@link UsbEndpointDescriptor#bmAttributes()} method.
	 */
	@Test
	void testAttributes()
	{
		assertEquals(ATTRIBUTES, descriptor.bmAttributes());
	}

	/**
	 * Tests the {@link UsbEndpointDescriptor#wMaxPacketSize()} method.
	 */
	@Test
	void testMaxPacketSize()
	{
		assertEquals(MAX_PACKET_SIZE, descriptor.wMaxPacketSize());
	}

	/**
	 * Tests the {@link UsbEndpointDescriptor#bInterval()} method.
	 */
	@Test
	void testInterval()
	{
		assertEquals(INTERVAL, descriptor.bInterval());
	}

	/**
	 * Tests the {@link UsbEndpointDescriptor#hashCode()} method.
	 */
	@Test
	void testHashCode()
	{
		final int code = descriptor.hashCode();
		assertEquals(code, descriptor.hashCode());
		assertEquals(code, new UsbEndpointDescriptor(
				ENDPOINT_ADDRESS, ATTRIBUTES,
				MAX_PACKET_SIZE, INTERVAL).hashCode());
	}

	/**
	 * Tests the {@link UsbEndpointDescriptor#equals(Object)} method.
	 */
	@Test
	void testEquals()
	{
		assertNotEquals(null, descriptor);
		assertNotEquals(new Object(), descriptor);
		assertEquals(new UsbEndpointDescriptor(
				ENDPOINT_ADDRESS, ATTRIBUTES,
				MAX_PACKET_SIZE, INTERVAL), descriptor);
		assertNotEquals(new UsbEndpointDescriptor(
				new BEndpointAddress(WRONG), ATTRIBUTES,
				MAX_PACKET_SIZE, INTERVAL), descriptor);
		assertNotEquals(new UsbEndpointDescriptor(
				ENDPOINT_ADDRESS, WRONG,
				MAX_PACKET_SIZE, INTERVAL), descriptor);
		assertNotEquals(new UsbEndpointDescriptor(
				ENDPOINT_ADDRESS, ATTRIBUTES,
				WRONG, INTERVAL), descriptor);
		assertNotEquals(new UsbEndpointDescriptor(
				ENDPOINT_ADDRESS, ATTRIBUTES,
				MAX_PACKET_SIZE, WRONG), descriptor);
	}

	/**
	 * Tests the {@link UsbEndpointDescriptor#toString()} method.
	 */
	@Test
	void testToString()
	{
		assertEquals(String.format("USB Endpoint Descriptor:%n"
				+ "  bLength                  7%n"
				+ "  bDescriptorType          5%n"
				+ "  bEndpointAddress      0x8d  EP 13 DEVICE_TO_HOST%n"
				+ "  bmAttributes           252%n"
				+ "    Transfer Type             CONTROL%n"
				+ "    Synch Type                SYNCHRONOUS%n"
				+ "    Usage Type                RESERVED%n"
				+ "    Interrupt Type            RESERVED_%n"
				+ "  wMaxPacketSize       65535%n"
				+ "  bInterval              251%n"), descriptor.toString());
	}
}
