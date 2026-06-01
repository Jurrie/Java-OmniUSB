/*
 * Copyright (C) 2013 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information.
 */

package javax.usb3.ri;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.usb3.event.UsbServicesEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link ServicesListenerList} class.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
class UsbServicesListenerListTest
{
	/** The test subject. */
	private UsbServicesListenerList list;

	/**
	 * Set up the test.
	 */
	@BeforeEach
	void setUp()
	{
		list = new UsbServicesListenerList();
	}

	/**
	 * Tests the list functionality.
	 */
	@Test
	void testList()
	{
		// Must be initially empty
		assertEquals(0, list.getListeners().size());

		// Add first listener
		final UsbServicesListenerList a = mock(UsbServicesListenerList.class);
		list.add(a);
		assertEquals(1, list.getListeners().size());
		assertSame(a, list.getListeners().get(0));

		// Add same listener again
		list.add(a);
		assertEquals(1, list.getListeners().size());

		// Add second listener
		final UsbServicesListenerList b = mock(UsbServicesListenerList.class);
		list.add(b);
		assertEquals(2, list.getListeners().size());

		// Remove first listener
		list.remove(a);
		assertEquals(1, list.getListeners().size());
		assertSame(b, list.getListeners().get(0));

		// Add first listener again and check array conversion
		list.add(a);
		assertArrayEquals(new UsbServicesListenerList[] { b, a }, list.toArray());

		// Clear the list
		list.clear();
		assertSame(0, list.getListeners().size());
	}

	/**
	 * Tests the detached event.
	 */
	@Test
	void testDetachedEvent()
	{
		final UsbServicesEvent event = new UsbServicesEvent(mock(UsbServices.class), mock(UsbDevice.class));
		final UsbServicesListenerList a = mock(UsbServicesListenerList.class);
		final UsbServicesListenerList b = mock(UsbServicesListenerList.class);
		list.add(a);
		list.add(b);
		list.usbDeviceDetached(event);
		verify(a).usbDeviceDetached(event);
		verify(b).usbDeviceDetached(event);
	}

	/**
	 * Tests the attached event.
	 */
	@Test
	void testAttachedEvent()
	{
		final UsbServicesEvent event = new UsbServicesEvent(mock(UsbServices.class), mock(UsbDevice.class));
		final UsbServicesListenerList a = mock(UsbServicesListenerList.class);
		final UsbServicesListenerList b = mock(UsbServicesListenerList.class);
		list.add(a);
		list.add(b);
		list.usbDeviceAttached(event);
		verify(a).usbDeviceAttached(event);
		verify(b).usbDeviceAttached(event);
	}
}
