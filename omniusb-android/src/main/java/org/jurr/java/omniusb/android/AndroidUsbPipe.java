package org.jurr.java.omniusb.android;

import javax.usb3.ri.UsbIrpQueue;

public class AndroidUsbPipe extends javax.usb3.ri.UsbPipe
{
	public AndroidUsbPipe(final AndroidUsbEndpoint endpoint)
	{
		super(endpoint);
	}

	@Override
	protected UsbIrpQueue createIrpQueue()
	{
		return new AndroidUsbIrpQueue(this);
	}

	@Override
	public AndroidUsbDevice getDevice()
	{
		return (AndroidUsbDevice) super.getDevice();
	}
}
