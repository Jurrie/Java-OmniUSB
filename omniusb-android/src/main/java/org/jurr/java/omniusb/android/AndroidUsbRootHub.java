package org.jurr.java.omniusb.android;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.usb3.IUsbDevice;
import javax.usb3.ri.UsbImplementationService;

import android.hardware.usb.UsbDevice;

public abstract class AndroidUsbRootHub extends UsbImplementationService
{
	public Map<AndroidUsbDevice, UsbDevice> getAttachedAndroidUsbDevices()
	{
		final List<IUsbDevice> source = super.getAttachedUsbDevices();
		final Map<AndroidUsbDevice, UsbDevice> result = new HashMap<>(source.size(), 1);
		for (IUsbDevice device : source)
		{
			final AndroidUsbDevice androidUsbDevice = (AndroidUsbDevice) device;
			result.put(androidUsbDevice, androidUsbDevice.getWrappedUsbDevice());
		}
		return result;
	}

	@Override
	public void connectUsbDevice(final IUsbDevice device)
	{
		if (!(device instanceof AndroidUsbDevice))
		{
			throw new IllegalArgumentException("Only AndroidUsbDevice is supported");
		}
		super.connectUsbDevice(device);
	}

	@Override
	public void disconnectUsbDevice(final IUsbDevice device)
	{
		if (!(device instanceof AndroidUsbDevice))
		{
			throw new IllegalArgumentException("Only AndroidUsbDevice is supported");
		}
		super.disconnectUsbDevice(device);
	}

	@Override
	public boolean isUsbDeviceAttached(final IUsbDevice device)
	{
		if (!(device instanceof AndroidUsbDevice))
		{
			throw new IllegalArgumentException("Only AndroidUsbDevice is supported");
		}
		return super.isUsbDeviceAttached(device);
	}
}
