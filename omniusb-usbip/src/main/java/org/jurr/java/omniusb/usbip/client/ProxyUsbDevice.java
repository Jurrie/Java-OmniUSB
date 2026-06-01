package org.jurr.java.omniusb.usbip.client;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.usb3.IUsbConfiguration;
import javax.usb3.IUsbControlIrp;
import javax.usb3.IUsbDevice;
import javax.usb3.IUsbDeviceDescriptor;
import javax.usb3.IUsbPort;
import javax.usb3.IUsbServices;
import javax.usb3.IUsbStringDescriptor;
import javax.usb3.UsbHostManager;
import javax.usb3.descriptor.UsbDeviceDescriptor;
import javax.usb3.enumerated.EDescriptorType;
import javax.usb3.enumerated.EDevicePortSpeed;
import javax.usb3.enumerated.EUSBClassCode;
import javax.usb3.event.IUsbDeviceListener;
import javax.usb3.event.UsbDeviceEvent;
import javax.usb3.exception.UsbDisconnectedException;
import javax.usb3.exception.UsbException;
import javax.usb3.ri.AUsbDevice;
import javax.usb3.ri.IUsbDeviceWithId;
import javax.usb3.ri.UsbDeviceId;
import javax.usb3.ri.UsbDeviceListenerList;
import javax.usb3.utility.ByteUtility;

import org.jurr.java.omniusb.usbip.domain.UsbDeviceDetails;

public class ProxyUsbDevice implements IUsbDeviceWithId, IUsbDeviceDescriptor
{
	private final UsbIpServerConnection parentHub;

	private final String path;
	private final int deviceAddress;
	private final String busId;
	private final int busNumber;
	private final int portNumber;
	private final EDevicePortSpeed portSpeed;
	private final short idVendor;
	private final short idProduct;
	private final short bcdDevice;
	private final byte bDeviceClass;
	private final byte bDeviceSubClass;
	private final byte bDeviceProtocol;
	private final byte bConfigurationValue;
	private final byte bNumConfigurations;
	private final byte bNumInterfaces;
	private final IUsbConfiguration activeUsbConfiguration;

	/**
	 * The port this device is connected to.
	 */
	private IUsbPort port;

	// TODO: This is actually a stub. We need a delegate that chooses between the stub (when not connected) and the 'real' device (when connected).

	/**
	 * The USB device listener list.
	 */
	private final UsbDeviceListenerList listeners = new UsbDeviceListenerList();

	ProxyUsbDevice(final UsbIpServerConnection parentHub, final int deviceAddress, final UsbDeviceDetails usbDeviceDetails)
	{
		this.parentHub = parentHub; // We need this to send out USB/IP commands

		path = usbDeviceDetails.getPath();
		this.deviceAddress = deviceAddress;
		busId = usbDeviceDetails.getBusId();
		busNumber = usbDeviceDetails.getBusNumber();
		portNumber = usbDeviceDetails.getPortNumber();
		portSpeed = usbDeviceDetails.getPortSpeed().toEDevicePortSpeed();
		idVendor = usbDeviceDetails.getIdVendor();
		idProduct = usbDeviceDetails.getIdProduct();
		bcdDevice = usbDeviceDetails.getBcdDevice();
		bDeviceClass = usbDeviceDetails.getBDeviceClass();
		bDeviceSubClass = usbDeviceDetails.getBDeviceSubClass();
		bDeviceProtocol = usbDeviceDetails.getBDeviceProtocol();
		bConfigurationValue = usbDeviceDetails.getBConfigurationValue();
		bNumConfigurations = usbDeviceDetails.getBNumConfigurations();
		bNumInterfaces = usbDeviceDetails.getBNumInterfaces();

		activeUsbConfiguration = new ProxyUsbConfiguration(this, usbDeviceDetails.getInterfaceDetails());
	}

	public boolean isImported()
	{
		return false;
	}

	public String getPath()
	{
		return path;
	}

	public String getBusId()
	{
		return busId;
	}

	public byte getbNumInterfaces()
	{
		return bNumInterfaces;
	}

	@Override
	public IUsbPort getParentUsbPort() throws UsbDisconnectedException
	{
		isConnected();
		return port;
	}

	@Override
	public void setParentUsbPort(final IUsbPort port)
	{
		if (this.port == null && port == null)
		{
			throw new IllegalStateException("Device already detached");
		}
		if (this.port != null && port != null)
		{
			throw new IllegalStateException("Device already attached");
		}

		this.port = port;

		try
		{
			final IUsbServices services = UsbHostManager.getUsbServices();
			if (port == null)
			{
				listeners.usbDeviceDetached(new UsbDeviceEvent(this));
				services.usbDeviceDetached(this);
			}
			else
			{
				services.usbDeviceAttached(this);
			}
		}
		catch (UsbException | SecurityException usbException)
		{
			Logger.getLogger(AUsbDevice.class.getName()).log(Level.SEVERE, "USB Services error. {0}", usbException.getMessage());
			throw new RuntimeException("Unable to attach USB services: " + usbException);
		}
	}

	@Override
	public boolean isUsbHub()
	{
		// USB hubs are not exposed by USB/IP
		return false;
	}

	@Override
	public String getManufacturerString() throws UsbException, UnsupportedEncodingException, UsbDisconnectedException
	{
		throw new UnsupportedOperationException("This USB/IP device is not yet imported.");
	}

	@Override
	public String getSerialNumberString() throws UsbException, UnsupportedEncodingException, UsbDisconnectedException
	{
		throw new UnsupportedOperationException("This USB/IP device is not yet imported.");
	}

	@Override
	public String getProductString() throws UsbException, UnsupportedEncodingException, UsbDisconnectedException
	{
		throw new UnsupportedOperationException("This USB/IP device is not yet imported.");
	}

	@Override
	public EDevicePortSpeed getSpeed()
	{
		return portSpeed;
	}

	@Override
	public Collection<IUsbConfiguration> getUsbConfigurations()
	{
		return Collections.singletonList(getActiveUsbConfiguration());
	}

	@Override
	public IUsbConfiguration getUsbConfiguration(final byte number)
	{
		if (number == getActiveUsbConfigurationNumber())
		{
			return getActiveUsbConfiguration();
		}
		else
		{
			return null;
		}
	}

	@Override
	public boolean containsUsbConfiguration(final byte number)
	{
		return number == getActiveUsbConfigurationNumber();
	}

	@Override
	public byte getActiveUsbConfigurationNumber()
	{
		return bConfigurationValue;
	}

	@Override
	public void setActiveUsbConfigurationNumber(final byte number) throws UsbException
	{
		throw new UnsupportedOperationException("This USB/IP device is not yet imported.");
	}

	@Override
	public IUsbConfiguration getActiveUsbConfiguration()
	{
		return activeUsbConfiguration;
	}

	@Override
	public boolean isConfigured()
	{
		return false;
	}

	@Override
	public boolean isConnected() throws UsbDisconnectedException
	{
		if (port == null)
		{
			throw new UsbDisconnectedException();
		}
		return true;
	}

	@Override
	public boolean isInterfaceClaimed(final byte number)
	{
		throw new UnsupportedOperationException("This USB/IP device is not yet imported.");
	}

	@Override
	public IUsbStringDescriptor getUsbStringDescriptor(final byte index) throws UsbException, UsbDisconnectedException
	{
		throw new UnsupportedOperationException("This USB/IP device is not yet imported.");
	}

	@Override
	public String getString(final byte index) throws UsbException, UsbDisconnectedException
	{
		throw new UnsupportedOperationException("This USB/IP device is not yet imported.");
	}

	@Override
	public void close()
	{
	}

	@Override
	public void syncSubmit(final IUsbControlIrp irp) throws UsbException, IllegalArgumentException, UsbDisconnectedException
	{
		throw new UnsupportedOperationException("This USB/IP device is not yet imported.");
	}

	@Override
	public void asyncSubmit(final IUsbControlIrp irp) throws UsbException, IllegalArgumentException, UsbDisconnectedException
	{
		throw new UnsupportedOperationException("This USB/IP device is not yet imported.");
	}

	@Override
	public void syncSubmit(final List<IUsbControlIrp> list) throws UsbException, IllegalArgumentException, UsbDisconnectedException
	{
		throw new UnsupportedOperationException("This USB/IP device is not yet imported.");
	}

	@Override
	public void asyncSubmit(final List<IUsbControlIrp> list) throws UsbException, IllegalArgumentException, UsbDisconnectedException
	{
		throw new UnsupportedOperationException("This USB/IP device is not yet imported.");
	}

	@Override
	public void abortSubmission(final IUsbControlIrp irp) throws UsbDisconnectedException, UsbException
	{
		throw new UnsupportedOperationException("This USB/IP device is not yet imported.");
	}

	@Override
	public IUsbControlIrp createUsbControlIrp(final byte bmRequestType, final byte bRequest, final short wValue, final short wIndex)
	{
		throw new UnsupportedOperationException("This USB/IP device is not yet imported.");
	}

	@Override
	public IUsbControlIrp createUsbControlIrp(final byte bmRequestType, final byte bRequest, final short wValue, final short wIndex, final byte[] data)
	{
		throw new UnsupportedOperationException("This USB/IP device is not yet imported.");
	}

	@Override
	public void addUsbDeviceListener(final IUsbDeviceListener listener)
	{
		listeners.add(listener);
	}

	@Override
	public void removeUsbDeviceListener(final IUsbDeviceListener listener)
	{
		listeners.remove(listener);
	}

	@Override
	public int compareTo(final IUsbDevice o)
	{
		throw new UnsupportedOperationException("This USB/IP device is not yet imported.");
	}

	@Override
	public byte bLength()
	{
		throw new UnsupportedOperationException("This USB/IP device is not yet imported.");
	}

	@Override
	public EDescriptorType descriptorType()
	{
		throw new UnsupportedOperationException("This USB/IP device is not yet imported.");
	}

	@Override
	public byte bDescriptorType()
	{
		throw new UnsupportedOperationException("This USB/IP device is not yet imported.");
	}

	@Override
	public short bcdUSB()
	{
		return 0;
	}

	@Override
	public EUSBClassCode deviceClass()
	{
		return EUSBClassCode.fromByteCode(bDeviceClass);
	}

	@Override
	public byte bDeviceClass()
	{
		return bDeviceClass;
	}

	@Override
	public byte bDeviceSubClass()
	{
		return bDeviceSubClass;
	}

	@Override
	public byte bDeviceProtocol()
	{
		return bDeviceProtocol;
	}

	@Override
	public byte bMaxPacketSize0()
	{
		return 0;
	}

	@Override
	public short idVendor()
	{
		return idVendor;
	}

	@Override
	public short idProduct()
	{
		return idProduct;
	}

	@Override
	public short bcdDevice()
	{
		return bcdDevice;
	}

	@Override
	public byte iManufacturer()
	{
		return 0;
	}

	@Override
	public byte iProduct()
	{
		return 0;
	}

	@Override
	public byte iSerialNumber()
	{
		return 0;
	}

	@Override
	public byte bNumConfigurations()
	{
		return bNumConfigurations;
	}

	@Override
	public UsbDeviceId getDeviceId()
	{
		return new UsbDeviceId(busNumber, deviceAddress, portNumber, getUsbDeviceDescriptor());
	}

	@Override
	public IUsbDeviceDescriptor getUsbDeviceDescriptor()
	{
		return new UsbDeviceDescriptor(bcdUSB(), deviceClass(), bDeviceSubClass, bDeviceProtocol, bMaxPacketSize0(), idVendor(), idProduct(), bcdDevice(), iManufacturer(), iProduct(), iSerialNumber(), bNumConfigurations);
	}

	@Override
	public String toString()
	{
		return "USB/IP client " + ByteUtility.toHexString(idVendor) + ":" + ByteUtility.toHexString(idProduct) + " (not connected) @" + parentHub.getInetAddress() + "/" + path;
	}
}
