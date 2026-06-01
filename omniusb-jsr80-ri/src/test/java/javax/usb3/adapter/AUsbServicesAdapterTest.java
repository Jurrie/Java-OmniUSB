/*
 * Copyright (C) 2013 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information.
 */
// Based on org.usb4java:usb4java-javax:1.3.0#org.usb4java.javax.adapter.UsbServiceAdapterTest
package javax.usb3.adapter;

import javax.usb3.event.IUsbServicesListener;
import javax.usb3.event.UsbServicesEvent;

import org.junit.jupiter.api.Test;

/**
 * Test the {@link UsbServicesAdapter} class. There is not really anything to
 * test there. This class just ensures that the class exists and provides
 * the needed methods.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
class AUsbServicesAdapterTest
{
	/**
	 * Ensure the existence of the needed methods.
	 */
	@Test
	void testAbstractMethods()
	{
		final IUsbServicesListener adapter = new AUsbServicesAdapter()
		{
			// Empty
		};
		adapter.usbDeviceAttached((UsbServicesEvent) null);
		adapter.usbDeviceDetached((UsbServicesEvent) null);
	}
}
