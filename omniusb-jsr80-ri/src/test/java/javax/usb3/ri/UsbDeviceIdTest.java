/*
 * Copyright (C) 2013 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information.
 */

package javax.usb3.ri;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.usb3.descriptor.UsbDeviceDescriptor;
import javax.usb3.enumerated.EUSBClassCode;

import org.junit.jupiter.api.Test;

/**
 * Tests the {@link DeviceId} class.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
class UsbDeviceIdTest
{
	/** A zero byte used in the dummy descriptor. */
	private static final byte ZERO = 0;

	/** A dummy device descriptor. */
	private static final UsbDeviceDescriptor DESCRIPTOR = new UsbDeviceDescriptor(ZERO, EUSBClassCode.fromByteCode(ZERO), ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);

	/**
	 * Tests the constructor without a descriptor.
	 */
	@Test
	void testConstructorWithoutDescriptor()
	{
		assertThrows(IllegalArgumentException.class, () -> new UsbDeviceId(0, 1, 2, null));
	}

	/**
	 * Tests the {@link DeviceId#getBusNumber()} method.
	 */
	@Test
	void testGetBusNumber()
	{
		assertEquals(1, new UsbDeviceId(1, 2, 3, DESCRIPTOR).getBusNumber());
	}

	/**
	 * Tests the {@link DeviceId#getDeviceAddress()} method.
	 */
	@Test
	void testGetDeviceAddress()
	{
		assertEquals(2, new UsbDeviceId(1, 2, 3, DESCRIPTOR).getDeviceAddress());
	}

	/**
	 * Tests the {@link DeviceId#getPortNumber()} method.
	 */
	@Test
	void testGetPortNumber()
	{
		assertEquals(3, new UsbDeviceId(1, 2, 3, DESCRIPTOR).getPortNumber());
	}

	/**
	 * Tests the {@link DeviceId#getDeviceDescriptor()} method.
	 */
	@Test
	void testGetDeviceDescriptor()
	{
		assertSame(DESCRIPTOR, new UsbDeviceId(1, 2, 3, DESCRIPTOR).getDeviceDescriptor());
	}

	/**
	 * Tests the {@link DeviceId#hashCode()} method.
	 */
	@Test
	void testHashCode()
	{
		final int code = new UsbDeviceId(1, 2, 3, DESCRIPTOR).hashCode();
		assertEquals(code, new UsbDeviceId(1, 2, 3, DESCRIPTOR).hashCode());
	}

	/**
	 * Tests the {@link DeviceId#equals(Object)} method.
	 */
	@Test
	void testEquals()
	{
		final UsbDeviceId subject = new UsbDeviceId(1, 2, 3, DESCRIPTOR);
		final UsbDeviceId equal = new UsbDeviceId(1, 2, 3, DESCRIPTOR);
		final UsbDeviceId other = new UsbDeviceId(2, 3, 4, DESCRIPTOR);
		assertNotEquals(null, subject);
		assertNotEquals(new Object(), subject);
		assertEquals(equal, subject);
		assertNotEquals(other, subject);
	}
}
