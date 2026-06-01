// Based on org.usb4java:usb4java-javax:1.3.0#org.usb4java.javax.adapter.UsbDeviceAdapterTest
package javax.usb3.adapter;

import javax.usb3.event.IUsbDeviceListener;
import javax.usb3.event.UsbDeviceDataEvent;
import javax.usb3.event.UsbDeviceErrorEvent;
import javax.usb3.event.UsbDeviceEvent;

import org.junit.jupiter.api.Test;

/**
 * Test the {@link AUsbDeviceAdapter} class. There is not really anything to
 * test there. This class just ensures that the class exists and provides
 * the needed methods.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
class AUsbDeviceAdapterTest
{
	/**
	 * Ensure the existence of the needed methods.
	 */
	@Test
	void testAbstractMethods()
	{
		final IUsbDeviceListener adapter = new AUsbDeviceAdapter()
		{
			// Empty
		};
		adapter.usbDeviceDetached((UsbDeviceEvent) null);
		adapter.dataEventOccurred((UsbDeviceDataEvent) null);
		adapter.errorEventOccurred((UsbDeviceErrorEvent) null);
	}
}
