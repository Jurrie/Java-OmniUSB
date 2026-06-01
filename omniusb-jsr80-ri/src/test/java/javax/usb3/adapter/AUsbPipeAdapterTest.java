/*
 * Copyright (C) 2013 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information.
 */
// Based on org.usb4java:usb4java-javax:1.3.0#org.usb4java.javax.adapter.UsbPipeAdapterTest
package javax.usb3.adapter;

import javax.usb3.event.IUsbPipeListener;
import javax.usb3.event.UsbPipeDataEvent;
import javax.usb3.event.UsbPipeErrorEvent;

import org.junit.jupiter.api.Test;

/**
 * Test the {@link AUsbPipeAdapter} class. There is not really anything to
 * test there. This class just ensures that the class exists and provides
 * the needed methods.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
class AUsbPipeAdapterTest
{
	/**
	 * Ensure the existence of the needed methods.
	 */
	@Test
	void testAbstractMethods()
	{
		final IUsbPipeListener adapter = new AUsbPipeAdapter()
		{
			// Empty
		};
		adapter.dataEventOccurred((UsbPipeDataEvent) null);
		adapter.errorEventOccurred((UsbPipeErrorEvent) null);
	}
}
