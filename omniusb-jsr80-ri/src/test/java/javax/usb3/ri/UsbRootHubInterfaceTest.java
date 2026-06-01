/*
 * Copyright (C) 2014 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information.
 */

package javax.usb3.ri;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import javax.usb3.IUsbConfiguration;
import javax.usb3.IUsbEndpoint;
import javax.usb3.IUsbInterface;
import javax.usb3.IUsbInterfaceDescriptor;
import javax.usb3.enumerated.EDescriptorType;
import javax.usb3.enumerated.EUSBClassCode;
import javax.usb3.enumerated.EUSBSubclassCode;
import javax.usb3.exception.UsbException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link RootHubInterface} class.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
class UsbRootHubInterfaceTest
{
	/** The USB configuration. */
	private IUsbConfiguration config;

	/** The test subject. */
	private UsbRootHubInterface iface;

	/**
	 * Initialize the test.
	 */
	@BeforeEach
	void init()
	{
		config = mock(IUsbConfiguration.class);
		iface = new UsbRootHubInterface(config);
	}

	/**
	 * Tests the {@link RootHubInterface#claim()} method.
	 *
	 * @throws UsbException
	 *             Excepted exception.
	 */
	@Test
	void testClaim() throws UsbException
	{
		assertThrows(UsbException.class, () -> iface.claim());
	}

	/**
	 * Tests the {@link RootHubInterface#claim(javax.usb.UsbInterfacePolicy)}
	 * method.
	 *
	 * @throws UsbException
	 *             Excepted exception.
	 */
	@Test
	void testClaimWithPolicy() throws UsbException
	{
		assertThrows(UsbException.class, () -> iface.claim(null));
	}

	/**
	 * Tests the {@link RootHubInterface#release()} method.
	 *
	 * @throws UsbException
	 *             Excepted exception.
	 */
	@Test
	void testRelease() throws UsbException
	{
		assertThrows(UsbException.class, () -> iface.release());
	}

	/**
	 * Tests the {@link RootHubInterface#isClaimed()} method.
	 */
	@Test
	void testIsClaimed()
	{
		assertTrue(iface.isClaimed());
	}

	/**
	 * Tests the {@link RootHubInterface#isActive()} method.
	 */
	@Test
	void testIsActive()
	{
		assertTrue(iface.isActive());
	}

	/**
	 * Tests the {@link RootHubInterface#getNumSettings()} method.
	 */
	@Test
	void testGetNumSettings()
	{
		assertEquals(0, iface.getNumSettings());
	}

	/**
	 * Tests the {@link RootHubInterface#getActiveSettingNumber()} method.
	 */
	@Test
	void testGetActiveSettingNumber()
	{
		assertEquals(0, iface.getActiveSettingNumber());
	}

	/**
	 * Tests the {@link RootHubInterface#getActiveSetting()} method.
	 */
	@Test
	void testGetActiveSetting()
	{
		assertSame(iface, iface.getActiveSetting());
	}

	/**
	 * Tests the {@link RootHubInterface#getSetting(byte)} method.
	 */
	@Test
	void testGetSetting()
	{
		assertSame(iface, iface.getSetting((byte) 0));
	}

	/**
	 * Tests the {@link RootHubInterface#containsSetting(byte)} method.
	 */
	@Test
	void testContainsSetting()
	{
		assertFalse(iface.containsSetting((byte) 0));
	}

	/**
	 * Tests the {@link RootHubInterface#getSettings()} method.
	 */
	@Test
	void testGetSettings()
	{
		final List<IUsbInterface> settings = iface.getSettings();
		assertEquals(0, settings.size());
	}

	/**
	 * Tests the {@link RootHubInterface#getUsbEndpoints()} method.
	 */
	@Test
	void testGetUsbEndpoints()
	{
		final List<IUsbEndpoint> endpoints = iface.getUsbEndpoints();
		assertEquals(0, endpoints.size());
	}

	/**
	 * Tests the {@link RootHubInterface#getUsbEndpoint(byte)} method.
	 */
	@Test
	void testGetUsbEndpoint()
	{
		assertNull(iface.getUsbEndpoint((byte) 0));
	}

	/**
	 * Tests the {@link RootHubInterface#containsUsbEndpoint(byte)} method.
	 */
	@Test
	void testContainsUsbEndpoint()
	{
		assertFalse(iface.containsUsbEndpoint((byte) 0));
	}

	/**
	 * Tests the {@link RootHubInterface#getUsbConfiguration()} method.
	 */
	@Test
	void testGetUsbConfiguration()
	{
		assertSame(config, iface.getUsbConfiguration());
	}

	/**
	 * Tests the {@link RootHubInterface#getUsbInterfaceDescriptor()} method.
	 */
	@Test
	void testGetUsbInterfaceDescriptor()
	{
		final IUsbInterfaceDescriptor desc = iface.getUsbInterfaceDescriptor();
		assertEquals(EDescriptorType.INTERFACE.getLength(), desc.bLength());
		assertEquals(EDescriptorType.INTERFACE.getByteCode(), desc.bDescriptorType());
		assertEquals(0, desc.bInterfaceNumber());
		assertEquals(0, desc.bAlternateSetting());
		assertEquals(0, desc.bNumEndpoints());
		assertEquals(EUSBClassCode.HUB.getByteCode(), desc.bInterfaceClass());
		assertEquals(EUSBSubclassCode.INTERFACE_ASSOCIATION_DESCRIPTOR.getBytecodeSubclass(), desc.bInterfaceSubClass());
		assertEquals(EUSBSubclassCode.INTERFACE_ASSOCIATION_DESCRIPTOR.getBytecodeProtocol(), desc.bInterfaceProtocol());
		assertEquals(0, desc.iInterface());
	}

	/**
	 * Tests the {@link RootHubInterface#getInterfaceString()} method.
	 */
	@Test
	void testGetInterfaceString()
	{
		assertNull(iface.getInterfaceString());
	}
}
