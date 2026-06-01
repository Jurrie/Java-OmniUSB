/*
 * Copyright (C) 2014 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information.
 */

package javax.usb3.usb4java.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Constructor;

import javax.usb3.exception.UsbPlatformException;

import org.junit.jupiter.api.Test;
import org.usb4java.LibUsb;

/**
 * Tests the {@link ExceptionUtils} class.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
class Usb4JavaUsbExceptionFactoryTest
{
	/**
	 * Tests the {@link Usb4JavaUsbExceptionFactory#createPlatformException(String, int)}
	 * method.
	 */
	@Test
	void testCreatePlatformException()
	{
		final UsbPlatformException e = Usb4JavaUsbExceptionFactory.createPlatformException("Custom message", LibUsb.ERROR_IO);
		assertEquals("USB error 1: Custom message: Input/Output Error", e.getMessage());
		assertEquals(LibUsb.ERROR_IO, e.getErrorCode());
	}

	/**
	 * Ensure constructor is private.
	 *
	 * @throws Exception
	 *             When constructor test fails.
	 */
	@Test
	void testPrivateConstructor() throws Exception
	{
		assertEquals(0, Usb4JavaUsbExceptionFactory.class.getConstructors().length);
		final Constructor<?> c = Usb4JavaUsbExceptionFactory.class.getDeclaredConstructor();
		c.setAccessible(true);
		c.newInstance();
	}
}
