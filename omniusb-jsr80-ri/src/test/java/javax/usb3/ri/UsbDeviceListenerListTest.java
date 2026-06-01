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

import javax.usb3.event.UsbDeviceDataEvent;
import javax.usb3.event.UsbDeviceErrorEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link DeviceListenerList} class.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
class UsbDeviceListenerListTest
{
	/** The test subject. */
	private UsbDeviceListenerList list;

	/**
	 * Set up the test.
	 */
	@BeforeEach
	void setUp()
	{
		list = new UsbDeviceListenerList();
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
		final UsbDeviceListenerList a = mock(UsbDeviceListenerList.class);
		list.add(a);
		assertEquals(1, list.getListeners().size());
		assertSame(a, list.getListeners().get(0));

		// Add same listener again
		list.add(a);
		assertEquals(1, list.getListeners().size());

		// Add second listener
		final UsbDeviceListenerList b = mock(UsbDeviceListenerList.class);
		list.add(b);
		assertEquals(2, list.getListeners().size());

		// Remove first listener
		list.remove(a);
		assertEquals(1, list.getListeners().size());
		assertSame(b, list.getListeners().get(0));

		// Add first listener again and check array conversion
		list.add(a);
		assertArrayEquals(new UsbDeviceListenerList[] { b, a }, list.toArray());

		// Clear the list
		list.clear();
		assertSame(0, list.getListeners().size());
	}

	/**
	 * Tests the data event.
	 */
	@Test
	void testDataEvent()
	{
		final UsbDeviceDataEvent event = new UsbDeviceDataEvent(mock(UsbDevice.class), mock(UsbControlIrp.class));
		final UsbDeviceListenerList a = mock(UsbDeviceListenerList.class);
		final UsbDeviceListenerList b = mock(UsbDeviceListenerList.class);
		list.add(a);
		list.add(b);
		list.dataEventOccurred(event);
		verify(a).dataEventOccurred(event);
		verify(b).dataEventOccurred(event);
	}

	/**
	 * Tests the error event.
	 */
	@Test
	void testErrorEvent()
	{
		final UsbDeviceErrorEvent event = new UsbDeviceErrorEvent(mock(UsbDevice.class), mock(UsbControlIrp.class));
		final UsbDeviceListenerList a = mock(UsbDeviceListenerList.class);
		final UsbDeviceListenerList b = mock(UsbDeviceListenerList.class);
		list.add(a);
		list.add(b);
		list.errorEventOccurred(event);
		verify(a).errorEventOccurred(event);
		verify(b).errorEventOccurred(event);
	}

	/**
	 * Tests the detached event.
	 */
	@Test
	void testDetachedEvent()
	{
		final UsbDeviceErrorEvent event = new UsbDeviceErrorEvent(mock(UsbDevice.class), mock(UsbControlIrp.class));
		final UsbDeviceListenerList a = mock(UsbDeviceListenerList.class);
		final UsbDeviceListenerList b = mock(UsbDeviceListenerList.class);
		list.add(a);
		list.add(b);
		list.usbDeviceDetached(event);
		verify(a).usbDeviceDetached(event);
		verify(b).usbDeviceDetached(event);
	}
}
