package org.jurr.java.omniusb.usbip.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.usb3.IUsbConfiguration;
import javax.usb3.IUsbDeviceDescriptor;
import javax.usb3.IUsbInterface;
import javax.usb3.IUsbInterfaceDescriptor;
import javax.usb3.enumerated.EDevicePortSpeed;
import javax.usb3.ri.IUsbDeviceWithId;
import javax.usb3.ri.UsbDeviceId;

import org.jurr.java.omniusb.usbip.domain.UsbDeviceDetails;
import org.jurr.java.omniusb.usbip.domain.UsbDeviceDetails.Speed;
import org.jurr.java.omniusb.usbip.domain.UsbDeviceDetails.UsbInterfaceDetails;

final class ServerUtils
{
	private ServerUtils()
	{
	}

	/**
	 * Uniform way to get a string representation of a bus ID for a USB device.
	 *
	 * @param usbDevice the USB device to get the bus ID for
	 * @return the bus ID as a string
	 */
	static String getBusId(final IUsbDeviceWithId usbDevice)
	{
		final UsbDeviceId usbDeviceId = usbDevice.getDeviceId();
		return usbDeviceId.getBusNumber() + "-" + usbDeviceId.getDeviceAddress();
	}

	static List<UsbDeviceDetails> convertToUsbDeviceDetails(final List<IUsbDeviceWithId> usbDevices)
	{
		return usbDevices.stream().map(ServerUtils::convertToUsbDeviceDetails).toList();
	}

	static UsbDeviceDetails convertToUsbDeviceDetails(final IUsbDeviceWithId usbDevice)
	{
		final String busId = ServerUtils.getBusId(usbDevice);
		final String path = "java://" + busId;
		final int busNumber = usbDevice.getDeviceId().getBusNumber();
		final int portNumber = usbDevice.getDeviceId().getPortNumber();
		final Speed portSpeed = Speed.fromEDevicePortSpeed(EDevicePortSpeed.HIGH); // TODO: Get actual speed
		final IUsbDeviceDescriptor usbDeviceDescriptor = usbDevice.getUsbDeviceDescriptor();
		final short idVendor = usbDeviceDescriptor.idVendor();
		final short idProduct = usbDeviceDescriptor.idProduct();
		final short bcdDevice = usbDeviceDescriptor.bcdDevice();
		final byte bDeviceClass = usbDeviceDescriptor.bDeviceClass();
		final byte bDeviceSubClass = usbDeviceDescriptor.bDeviceSubClass();
		final byte bDeviceProtocol = usbDeviceDescriptor.bDeviceProtocol();
		final byte bConfigurationValue = usbDevice.getActiveUsbConfigurationNumber();
		final byte bNumConfigurations = usbDeviceDescriptor.bNumConfigurations();

		final List<UsbInterfaceDetails> interfaceDetails = new ArrayList<>();
		for (byte configurationIndex = 1; configurationIndex <= bNumConfigurations; configurationIndex++)
		{
			final IUsbConfiguration usbConfiguration = usbDevice.getUsbConfiguration(configurationIndex);
			if (usbConfiguration == null)
			{
				continue;
			}
			final Collection<IUsbInterface> usbInterfaces = usbConfiguration.getUsbInterfaces();
			for (IUsbInterface usbInterface : usbInterfaces)
			{
				final IUsbInterfaceDescriptor usbInterfaceDescriptor = usbInterface.getUsbInterfaceDescriptor();
				final byte bInterfaceClass = usbInterfaceDescriptor.bInterfaceClass();
				final byte bInterfaceSubClass = usbInterfaceDescriptor.bInterfaceSubClass();
				final byte bInterfaceProtocol = usbInterfaceDescriptor.bInterfaceProtocol();

				interfaceDetails.add(new UsbInterfaceDetails(bInterfaceClass, bInterfaceSubClass, bInterfaceProtocol));
			}
		}

		return new UsbDeviceDetails(path, busId, busNumber, portNumber, portSpeed, idVendor, idProduct, bcdDevice, bDeviceClass, bDeviceSubClass, bDeviceProtocol, bConfigurationValue, bNumConfigurations, interfaceDetails);
	}
}
