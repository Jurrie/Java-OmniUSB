package javax.usb3.usb4java;

import org.usb4java.Context;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

/**
 * This is the event handling thread. libusb doesn't start threads by its own so it is our own responsibility to give libusb time to handle the events in our own thread.
 */
class Usb4JavaLibUsbEventHandlingThread extends Thread
{
	private final Context context;

	private volatile boolean abort;

	Usb4JavaLibUsbEventHandlingThread(final Context context)
	{
		setDaemon(true);
		this.context = context;
	}

	/**
	 * Aborts the event handling thread.
	 */
	public void abort()
	{
		abort = true;
	}

	@Override
	public void run()
	{
		while (!abort)
		{
			// Let libusb handle pending events. This blocks until events
			// have been handled, a hotplug callback has been deregistered
			// or the specified time of 5 seconds (Specified in
			// Microseconds) has passed.
			int result = LibUsb.handleEventsTimeoutCompleted(context, 5 * 1000 * 1000, null);
			if (result != LibUsb.SUCCESS)
			{
				throw new LibUsbException("Unable to handle events", result);
			}
		}
	}
}
