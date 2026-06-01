package org.jurr.java.omniusb.android;

import javax.usb3.IUsbPipe;
import javax.usb3.descriptor.UsbEndpointDescriptor;
import javax.usb3.request.BEndpointAddress;

import android.hardware.usb.UsbEndpoint;

public class AndroidUsbEndpoint extends javax.usb3.ri.UsbEndpoint
{
	private UsbEndpoint wrappedUsbEndpoint;

	public AndroidUsbEndpoint(final AndroidUsbInterface usbInterface, final UsbEndpoint usbEndpoint)
	{
		super(usbInterface, createDescriptorFromAndroidUsbEndpoint(usbEndpoint));
		wrappedUsbEndpoint = usbEndpoint;
	}

	protected UsbEndpoint getWrappedUsbEndpoint()
	{
		return wrappedUsbEndpoint;
	}

	@Override
	protected IUsbPipe createUsbPipe()
	{
		return new AndroidUsbPipe(this);
	}

	private static UsbEndpointDescriptor createDescriptorFromAndroidUsbEndpoint(final UsbEndpoint usbEndpoint)
	{
		final BEndpointAddress bEndpointAddress = BEndpointAddress.getInstance((byte) usbEndpoint.getAddress());
		final byte bmAttributes = (byte) usbEndpoint.getAttributes();
		final short wMaxPacketSize = (short) usbEndpoint.getMaxPacketSize();
		final byte bInterval = (byte) usbEndpoint.getInterval();
		return new UsbEndpointDescriptor(bEndpointAddress, bmAttributes, wMaxPacketSize, bInterval);
	}
}
