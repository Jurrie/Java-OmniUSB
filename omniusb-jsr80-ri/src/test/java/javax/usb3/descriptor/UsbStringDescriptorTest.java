/*
 * Copyright (C) 2013 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information.
 */
// Based on org.usb4java:usb4java-javax:1.3.0#org.usb4java.javax.descriptors.UsbStringDescriptorTest
package javax.usb3.descriptor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import javax.usb3.enumerated.EDescriptorType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link UsbStringDescriptor}.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
class UsbStringDescriptorTest
{
	/** The test subject. */
	private static UsbStringDescriptor descriptor;

	/** Value for {@link UsbStringDescriptor#bString()}. */
	private static final String STRING = "usb4java";

	private static final byte LENGTH = (byte) (STRING.getBytes(StandardCharsets.UTF_16LE).length + 6);
	private static final byte DESCRIPTOR_TYPE = EDescriptorType.STRING.getByteCode();

	/**
	 * Setup the test subject.
	 */
	@BeforeAll
	static void setUp()
	{
		descriptor = new UsbStringDescriptor(STRING);
	}

	/**
	 * Tests the copy constructor.
	 */
	@Test
	void testCopyConstructor()
	{
		final UsbStringDescriptor copy = new UsbStringDescriptor(descriptor);
		assertNotSame(copy, descriptor);
		assertEquals(copy, descriptor);
	}

	/**
	 * Tests the byte buffer constructor.
	 */
	@Test
	void testByteBufferConstructor()
	{
		final ByteBuffer buffer = ByteBuffer.allocate(STRING.length() * 2 + 2);
		buffer.put((byte) (STRING.length() * 2 + 2));
		buffer.put(DESCRIPTOR_TYPE);
		buffer.put(STRING.getBytes(StandardCharsets.UTF_16LE));
		final UsbStringDescriptor testDescriptor = new UsbStringDescriptor(buffer);
		assertEquals((byte) (STRING.length() * 2 + 2), testDescriptor.bLength());
		assertEquals(DESCRIPTOR_TYPE, testDescriptor.bDescriptorType());
		assertEquals(STRING, testDescriptor.getString());
	}

	/**
	 * Tests the {@link UsbStringDescriptor#bLength()} method.
	 */
	@Test
	void testLength()
	{
		assertEquals(LENGTH, descriptor.bLength());
	}

	/**
	 * Tests the {@link UsbStringDescriptor#bDescriptorType()} method.
	 */
	@Test
	void testDescriptorType()
	{
		assertEquals(DESCRIPTOR_TYPE, descriptor.bDescriptorType());
	}

	/**
	 * Tests the {@link UsbStringDescriptor#bString()} method.
	 */
	@Test
	void testString()
	{
		assertArrayEquals(STRING.getBytes(StandardCharsets.UTF_16LE), descriptor.bString());
	}

	/**
	 * Tests the {@link UsbStringDescriptor#getString()} method.
	 */
	@Test
	void testGetString()
	{
		assertEquals(STRING, descriptor.getString());
	}

	/**
	 * Tests the {@link UsbStringDescriptor#hashCode()} method.
	 */
	@Test
	void testHashCode()
	{
		final int code = descriptor.hashCode();
		assertEquals(code, descriptor.hashCode());
		assertEquals(code,
				new UsbStringDescriptor(descriptor).hashCode());
	}

	/**
	 * Tests the {@link UsbStringDescriptor#equals(Object)} method.
	 */
	@Test
	void testEquals()
	{
		assertNotEquals(null, descriptor);
		assertNotEquals(new Object(), descriptor);
		assertEquals(new UsbStringDescriptor(descriptor), descriptor);
		assertNotEquals(new UsbStringDescriptor("wrong"), descriptor);
	}

	/**
	 * Tests the {@link UsbStringDescriptor#toString()} method.
	 *
	 * @throws UnsupportedEncodingException
	 *             When system does not support UTF-16LE encoding.
	 */
	@Test
	void testToString()
	{
		assertEquals(descriptor.getString(), descriptor.toString());
	}
}
