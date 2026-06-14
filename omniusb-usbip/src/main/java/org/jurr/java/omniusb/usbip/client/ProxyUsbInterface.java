package org.jurr.java.omniusb.usbip.client;

import java.io.UnsupportedEncodingException;

import javax.usb3.IUsbConfiguration;
import javax.usb3.IUsbEndpointDescriptor;
import javax.usb3.IUsbInterface;
import javax.usb3.IUsbInterfacePolicy;
import javax.usb3.descriptor.UsbInterfaceDescriptor;
import javax.usb3.enumerated.EUSBClassCode;
import javax.usb3.exception.UsbClaimException;
import javax.usb3.exception.UsbDisconnectedException;
import javax.usb3.exception.UsbException;
import javax.usb3.exception.UsbNotActiveException;
import javax.usb3.ri.AUsbInterface;

import org.jurr.java.omniusb.usbip.domain.UsbDeviceDetails.UsbInterfaceDetails;

class ProxyUsbInterface extends AUsbInterface
{
	protected ProxyUsbInterface(final IUsbConfiguration configuration, final byte bInterfaceNumber, final UsbInterfaceDetails usbInterfaceDetails)
	{
		super(configuration, createUsbInterfaceDescriptor(bInterfaceNumber, usbInterfaceDetails));
	}

	private static UsbInterfaceDescriptor createUsbInterfaceDescriptor(final byte bInterfaceNumber, final UsbInterfaceDetails usbInterfaceDetails)
	{
		final EUSBClassCode bInterfaceClass = EUSBClassCode.fromByteCode(usbInterfaceDetails.getBInterfaceClass());
		final byte bInterfaceSubClass = usbInterfaceDetails.getBInterfaceSubClass();
		final byte bInterfaceProtocol = usbInterfaceDetails.getBInterfaceProtocol();

		// The following values are unknown without connecting the USB/IP device
		final byte bAlternateSetting = 0; // TODO
		final byte bNumEndpoints = 0; // TODO
		final byte iInterface = 0; // TODO
		final IUsbEndpointDescriptor[] endpoints = new IUsbEndpointDescriptor[0];

		return new UsbInterfaceDescriptor(bInterfaceNumber, bAlternateSetting, bNumEndpoints, bInterfaceClass, bInterfaceSubClass, bInterfaceProtocol, iInterface, endpoints);
	}

	@Override
	public void claim() throws UsbClaimException, UsbException, UsbNotActiveException, UsbDisconnectedException
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void claim(final IUsbInterfacePolicy policy) throws UsbClaimException, UsbException, UsbNotActiveException, UsbDisconnectedException
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void release() throws UsbClaimException, UsbException, UsbNotActiveException, UsbDisconnectedException
	{
		// TODO Auto-generated method stub
	}

	@Override
	public byte getActiveSettingNumber() throws UsbNotActiveException
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IUsbInterface getActiveSetting() throws UsbNotActiveException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInterfaceString() throws UsbException, UnsupportedEncodingException, UsbDisconnectedException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte getAlternativeSetting()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAlternativeSetting(byte wValue)
	{
		// TODO Auto-generated method stub
	}
}
