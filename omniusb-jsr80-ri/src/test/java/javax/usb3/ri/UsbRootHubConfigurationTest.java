/*
 * Copyright (C) 2013 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information.
 */

package javax.usb3.ri;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.usb3.IUsbInterface;
import javax.usb3.exception.UsbException;
import javax.usb3.exception.UsbPlatformException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link RootHubConfiguration} class.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
class UsbRootHubConfigurationTest
{
	/** The test subject. */
	private UsbRootHubConfiguration config;

	/**
	 * Initialize the test.
	 */
	@BeforeEach
	void init() throws UsbPlatformException
	{
		final UsbDevice mock = mock(UsbDevice.class);
		when(mock.doGetActiveUsbConfiguration()).thenReturn((byte) 1);
		when(mock.getActiveUsbConfigurationNumber()).thenCallRealMethod();
		config = new UsbRootHubConfiguration(mock);
	}

	/**
	 * Tests the {@link RootHubConfiguration#isActive()} method.
	 */
	@Test
	void testIsActive()
	{
		assertTrue(config.isActive());
	}

	/**
	 * Tests the {@link RootHubConfiguration#getUsbInterfaces()} method.
	 */
	@Test
	void testGetUsbInterfaces()
	{
		final List<IUsbInterface> ifaces = config.getUsbInterfaces();
		assertEquals(1, ifaces.size());
		assertNotNull(ifaces.get(0));
	}

	/**
	 * Tests the {@link RootHubConfiguration#getUsbInterface(byte)} method.
	 */
	@Test
	void testGetUsbInterface()
	{
		assertNotNull(config.getUsbInterface((byte) 0));
		assertNull(config.getUsbInterface((byte) 1));
	}

	/**
	 * Tests the {@link RootHubConfiguration#containsUsbInterface(byte)} method.
	 */
	@Test
	void testContainsUsbInterface()
	{
		assertTrue(config.containsUsbInterface((byte) 0));
		assertFalse(config.containsUsbInterface((byte) 1));
	}

	/**
	 * Tests the {@link RootHubConfiguration#getUsbDevice()} method.
	 */
	@Test
	void testGetUsbDevice()
	{
		assertNotNull(config.getUsbDevice());
	}

	/**
	 * Tests the {@link RootHubConfiguration#getUsbConfigurationDescriptor()} method.
	 */
	@Test
	void testGetUsbConfigurationDescriptor()
	{
		assertNotNull(config.getUsbConfigurationDescriptor());
	}

	/**
	 * Tests the {@link RootHubConfiguration#getConfigurationString()} method.
	 */
	@Test
	void testGetConfigurationString() throws UsbException
	{
		assertNull(config.getConfigurationString());
	}
}
