package org.jurr.java.omniusb.android;

import javax.usb3.IUsbInterface;
import javax.usb3.descriptor.UsbConfigurationDescriptor;
import javax.usb3.exception.UsbException;
import javax.usb3.request.BMConfigurationAttributes;

import android.hardware.usb.UsbConfiguration;
import android.hardware.usb.UsbInterface;

public class AndroidUsbConfiguration extends javax.usb3.ri.AUsbConfiguration
{
	private final UsbConfiguration wrappedUsbConfiguration;

	public AndroidUsbConfiguration(final AndroidUsbDevice device, final UsbConfiguration configuration)
	{
		super(device, createDescriptorFromAndroidUsbConfiguration(device, configuration));
		wrappedUsbConfiguration = configuration;

		for (int i = 0; i < configuration.getInterfaceCount(); i++)
		{
			final UsbInterface androidUsbInterface = configuration.getInterface(i);
			final AndroidUsbInterface usbInterface = new AndroidUsbInterface(this, androidUsbInterface);
			addInterface(usbInterface);
		}
	}

	protected UsbConfiguration getWrappedUsbConfiguration()
	{
		return wrappedUsbConfiguration;
	}

	@Override
	public AndroidUsbInterface getUsbInterface(final byte number)
	{
		return (AndroidUsbInterface) super.getUsbInterface(number);
	}

	@Override
	public AndroidUsbDevice getUsbDevice()
	{
		return (AndroidUsbDevice) super.getUsbDevice();
	}

	private static UsbConfigurationDescriptor createDescriptorFromAndroidUsbConfiguration(final AndroidUsbDevice androidUsbDevice, final UsbConfiguration configuration)
	{
		final short wTotalLength = 0; // TODO: Calculate this
		final byte bNumInterfaces = (byte) configuration.getInterfaceCount();
		final byte bConfigurationValue = (byte) configuration.getId();
		final byte iConfiguration = androidUsbDevice.addStringDescriptor(configuration.getName());
		final BMConfigurationAttributes bmAttributes = new BMConfigurationAttributes(configuration.isSelfPowered(), configuration.isRemoteWakeup());
		final byte bMaxPower = (byte) configuration.getMaxPower();
		return new UsbConfigurationDescriptor(wTotalLength, bNumInterfaces, bConfigurationValue, iConfiguration, bmAttributes, bMaxPower);
	}

	@Override
	protected void doSetUsbInterfaceAlternate(final byte number, final IUsbInterface usbInterface) throws UsbException
	{
		throw new UnsupportedOperationException("Not implemented in Android");
	}
}
