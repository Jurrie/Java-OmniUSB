package org.jurr.java.omniusb.android;

import javax.usb3.IUsbEndpointDescriptor;
import javax.usb3.descriptor.UsbEndpointDescriptor;
import javax.usb3.descriptor.UsbInterfaceDescriptor;
import javax.usb3.enumerated.EUSBClassCode;

import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;

public class AndroidUsbInterface extends javax.usb3.ri.UsbInterface
{
	private UsbInterface wrappedUsbInterface;

	public AndroidUsbInterface(final AndroidUsbConfiguration configuration, final UsbInterface usbInterface)
	{
		super(configuration, createDescriptorFromAndroidUsbInterface(configuration, usbInterface));
		wrappedUsbInterface = usbInterface;

		// We did not pass endpoint descriptors to the parent constructor. So we need to add them ourselves.
		for (int i = 0; i < usbInterface.getEndpointCount(); i++)
		{
			final UsbEndpoint androidUsbEndpoint = usbInterface.getEndpoint(i);
			final AndroidUsbEndpoint usbEndpoint = new AndroidUsbEndpoint(this, androidUsbEndpoint);
			addUsbEndpoint(usbEndpoint);
		}
	}

	protected UsbInterface getWrappedUsbInterface()
	{
		return wrappedUsbInterface;
	}

	private static UsbInterfaceDescriptor createDescriptorFromAndroidUsbInterface(final AndroidUsbConfiguration configuration, final UsbInterface usbInterface)
	{
		final byte bInterfaceNumber = (byte) usbInterface.getId();
		final byte bAlternateSetting = (byte) usbInterface.getAlternateSetting();
		final byte bNumEndpoints = (byte) usbInterface.getEndpointCount();
		final EUSBClassCode bInterfaceClass = EUSBClassCode.fromByteCode((byte) usbInterface.getInterfaceClass());
		final byte bInterfaceSubClass = (byte) usbInterface.getInterfaceSubclass();
		final byte bInterfaceProtocol = (byte) usbInterface.getInterfaceProtocol();
		final byte iInterface = configuration.getUsbDevice().addStringDescriptor(usbInterface.getName());
		final IUsbEndpointDescriptor[] endpoints = new UsbEndpointDescriptor[0]; // We will add endpoints in the constructor later
		return new UsbInterfaceDescriptor(bInterfaceNumber, bAlternateSetting, bNumEndpoints, bInterfaceClass, bInterfaceSubClass, bInterfaceProtocol, iInterface, endpoints);
	}
}
