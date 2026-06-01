package org.jurr.java.omniusb.usbip.domain;

import java.util.Collections;
import java.util.List;

import javax.usb3.enumerated.EDevicePortSpeed;

public class UsbDeviceDetails
{
	public enum Speed
	{
		UNKNOWN(0), LOW(1), FULL(2), HIGH(3), Wireless(4), SUPER(5), SuperPlus(6);

		private final int value;

		private Speed(int value)
		{
			this.value = value;
		}

		public int getValue()
		{
			return value;
		}

		public static Speed fromValue(final int value)
		{
			for (Speed speed : Speed.values())
			{
				if (speed.getValue() == value)
				{
					return speed;
				}
			}
			return UNKNOWN;
		}

		public static Speed fromEDevicePortSpeed(final EDevicePortSpeed eSpeed)
		{
			switch (eSpeed)
			{
			case LOW:
				return LOW;
			case FULL:
				return FULL;
			case HIGH:
				return HIGH;
			case SUPER:
				return SUPER;
			default:
				return UNKNOWN;
			}
		}

		public EDevicePortSpeed toEDevicePortSpeed()
		{
			switch (this)
			{
			case LOW:
				return EDevicePortSpeed.LOW;
			case FULL:
				return EDevicePortSpeed.FULL;
			case HIGH:
				return EDevicePortSpeed.HIGH;
			case SUPER:
				return EDevicePortSpeed.SUPER;
			default:
				return null;
			}
		}
	}

	private final String path;
	private final String busId;
	private final int busNumber;
	private final int portNumber;
	private final Speed portSpeed;
	private final short idVendor;
	private final short idProduct;
	private final short bcdDevice;
	private final byte bDeviceClass;
	private final byte bDeviceSubClass;
	private final byte bDeviceProtocol;
	private final byte bConfigurationValue;
	private final byte bNumConfigurations;
	private final byte bNumInterfaces;
	private final List<UsbInterfaceDetails> interfaceDetails;

	public UsbDeviceDetails(final String path, final String busId, final int busNumber, final int portNumber, final Speed portSpeed, final short idVendor, final short idProduct, final short bcdDevice, final byte bDeviceClass, final byte bDeviceSubClass, final byte bDeviceProtocol, final byte bConfigurationValue, final byte bNumConfigurations, final byte bNumInterfaces)
	{
		this.path = path;
		this.busId = busId;
		this.busNumber = busNumber;
		this.portNumber = portNumber;
		this.portSpeed = portSpeed;
		this.idVendor = idVendor;
		this.idProduct = idProduct;
		this.bcdDevice = bcdDevice;
		this.bDeviceClass = bDeviceClass;
		this.bDeviceSubClass = bDeviceSubClass;
		this.bDeviceProtocol = bDeviceProtocol;
		this.bConfigurationValue = bConfigurationValue;
		this.bNumConfigurations = bNumConfigurations;
		this.bNumInterfaces = bNumInterfaces;
		interfaceDetails = null;
	}

	public UsbDeviceDetails(final String path, final String busId, final int busNumber, final int portNumber, final Speed portSpeed, final short idVendor, final short idProduct, final short bcdDevice, final byte bDeviceClass, final byte bDeviceSubClass, final byte bDeviceProtocol, final byte bConfigurationValue, final byte bNumConfigurations, final List<UsbInterfaceDetails> interfaceDetails)
	{
		this.path = path;
		this.busId = busId;
		this.busNumber = busNumber;
		this.portNumber = portNumber;
		this.portSpeed = portSpeed;
		this.idVendor = idVendor;
		this.idProduct = idProduct;
		this.bcdDevice = bcdDevice;
		this.bDeviceClass = bDeviceClass;
		this.bDeviceSubClass = bDeviceSubClass;
		this.bDeviceProtocol = bDeviceProtocol;
		this.bConfigurationValue = bConfigurationValue;
		this.bNumConfigurations = bNumConfigurations;
		bNumInterfaces = (byte) interfaceDetails.size();
		this.interfaceDetails = interfaceDetails;
	}

	public String getPath()
	{
		return path;
	}

	public String getBusId()
	{
		return busId;
	}

	public int getBusNumber()
	{
		return busNumber;
	}

	public int getPortNumber()
	{
		return portNumber;
	}

	public Speed getPortSpeed()
	{
		return portSpeed;
	}

	public short getIdVendor()
	{
		return idVendor;
	}

	public short getIdProduct()
	{
		return idProduct;
	}

	public short getBcdDevice()
	{
		return bcdDevice;
	}

	public byte getBDeviceClass()
	{
		return bDeviceClass;
	}

	public byte getBDeviceSubClass()
	{
		return bDeviceSubClass;
	}

	public byte getBDeviceProtocol()
	{
		return bDeviceProtocol;
	}

	public byte getBConfigurationValue()
	{
		return bConfigurationValue;
	}

	public byte getBNumConfigurations()
	{
		return bNumConfigurations;
	}

	public byte getBNumInterfaces()
	{
		return bNumInterfaces;
	}

	public boolean hasInterfaceDetails()
	{
		return interfaceDetails != null;
	}

	public List<UsbInterfaceDetails> getInterfaceDetails()
	{
		if (interfaceDetails == null)
		{
			throw new IllegalStateException("No interface details available");
		}
		return Collections.unmodifiableList(interfaceDetails);
	}

	public static class UsbInterfaceDetails
	{
		private final byte bInterfaceClass;
		private final byte bInterfaceSubClass;
		private final byte bInterfaceProtocol;

		public UsbInterfaceDetails(final byte bInterfaceClass, final byte bInterfaceSubClass, final byte bInterfaceProtocol)
		{
			this.bInterfaceClass = bInterfaceClass;
			this.bInterfaceSubClass = bInterfaceSubClass;
			this.bInterfaceProtocol = bInterfaceProtocol;
		}

		public byte getBInterfaceClass()
		{
			return bInterfaceClass;
		}

		public byte getBInterfaceSubClass()
		{
			return bInterfaceSubClass;
		}

		public byte getBInterfaceProtocol()
		{
			return bInterfaceProtocol;
		}
	}
}
