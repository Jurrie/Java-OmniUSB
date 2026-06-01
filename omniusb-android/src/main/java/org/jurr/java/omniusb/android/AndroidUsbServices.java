package org.jurr.java.omniusb.android;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.usb3.exception.UsbException;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class AndroidUsbServices extends AndroidUsbRootHub
{
	/**
	 * The implementation description.
	 */
	private static final String IMP_DESCRIPTION = "Android bridge to javax.usb3";

	/**
	 * The implementation version. This is the Java source code version.
	 */
	private static final String IMP_VERSION = "1.4.x";

	/**
	 * The API version. This is the Android dependency version.
	 */
	private static final String API_VERSION = "4.1.1.4";

	private UsbManager usbManager;
	private Context mainActivity;

	public AndroidUsbServices()
	{
		// Default constructor declared because of thrown exception by parent.

		// Actual implementation is in start(UsbManager).
	}

	public void start(final Context mainActivity) throws UsbException
	{
		this.mainActivity = mainActivity;

		usbManager = (UsbManager) mainActivity.getSystemService(Context.USB_SERVICE);

		for (UsbDevice device : usbManager.getDeviceList().values())
		{
			connectUsbDevice(new AndroidUsbDevice(device, 0, getDeviceId()));
		}

		final Map<AndroidUsbDevice, UsbDevice> previouslyAttachedDevices = getAttachedAndroidUsbDevices();
		final Collection<UsbDevice> currentlyAttachedDevices = usbManager.getDeviceList().values();
		for (Entry<AndroidUsbDevice, UsbDevice> entry : previouslyAttachedDevices.entrySet())
		{
			if (!currentlyAttachedDevices.contains(entry.getValue()))
			{
				disconnectUsbDevice(entry.getKey());
			}
		}

		for (UsbDevice device : currentlyAttachedDevices)
		{
			if (!previouslyAttachedDevices.values().contains(device))
			{
				connectUsbDevice(new AndroidUsbDevice(device, 0, getDeviceId()));
			}
		}

		// TODO: How do we get notified in Android when a device is attached or detached? We should register for intents android.hardware.usb.action.USB_DEVICE_ATTACHED and android.hardware.usb.action.USB_DEVICE_DETACHED
	}

	private void assertStarted()
	{
		if (usbManager == null)
		{
			throw new IllegalStateException("UsbManager not initialized - please call start(Context) first.");
		}
		if (mainActivity == null)
		{
			throw new IllegalStateException("Main activity not set - please call start(Context) first.");
		}
	}

	protected UsbDeviceConnection openDevice(final AndroidUsbDevice usbDevice)
	{
		assertStarted();

		if (!hasUsbPermission(usbDevice))
		{
			requestUsbPermission(usbDevice);
		}

		return usbManager.openDevice(usbDevice.getWrappedUsbDevice());
	}

	protected boolean hasUsbPermission(final AndroidUsbDevice usbDevice)
	{
		assertStarted();
		final UsbDevice wrappedUsbDevice = usbDevice.getWrappedUsbDevice();
		return usbManager.hasPermission(wrappedUsbDevice);
	}

	protected void requestUsbPermission(final AndroidUsbDevice usbDevice)
	{
		assertStarted();
		final String ACTION_USB_PERMISSION = mainActivity.getPackageName() + ".USB_PERMISSION";

		final BroadcastReceiver usbPermissionReceiver = new BroadcastReceiver()
		{
			// TODO: This BroadcastReceiver is never called - why?
			// If it is called, then we can use wait()/notify() to block until the permission is granted or denied.
			// Or perhaps use lambda callbacks for onPermissionGranted() and onPermissionDenied()?

			@Override
			public void onReceive(final Context context, final Intent intent)
			{
				synchronized (this)
				{
					String action = intent.getAction();
					if (ACTION_USB_PERMISSION.equals(action))
					{
						mainActivity.unregisterReceiver(this);

						final UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

						if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
						{
							if (device != null)
							{
								// call method to set up device communication
								Log.i(AndroidUsbServices.class.getSimpleName(), "PERMISSION GRANTED! connection = usbManager.openDevice(wrappedUsbDevice);");
							}
							else
							{
								Log.e(AndroidUsbServices.class.getSimpleName(), "PERMISSION WAS GRANTED, BUT DEVICE WAS NULL?!");
							}
						}
						else
						{
							Log.e(AndroidUsbServices.class.getSimpleName(), "PERMISSION DENIED!");
						}
					}
				}
			}
		};
		mainActivity.registerReceiver(usbPermissionReceiver, new IntentFilter(ACTION_USB_PERMISSION));

		final PendingIntent permissionIntent = PendingIntent.getBroadcast(mainActivity, 0, new Intent(ACTION_USB_PERMISSION).setPackage(mainActivity.getPackageName()), /* PendingIntent.FLAG_IMMUTABLE = */ /* 1 << 26 *//* FLAG_MUTABLE = */ 1 << 25);
		usbManager.requestPermission(usbDevice.getWrappedUsbDevice(), permissionIntent);
	}

	@Override
	public String getApiVersion()
	{
		return API_VERSION;
	}

	@Override
	public String getImpVersion()
	{
		return IMP_VERSION;
	}

	@Override
	public String getImpDescription()
	{
		return IMP_DESCRIPTION;
	}
}
