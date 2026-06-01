package org.jurr.java.omniusb.usbip.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import javax.usb3.IUsbConfiguration;
import javax.usb3.IUsbControlIrp;
import javax.usb3.IUsbDevice;
import javax.usb3.IUsbDeviceDescriptor;
import javax.usb3.IUsbHub;
import javax.usb3.IUsbPort;
import javax.usb3.IUsbPorts;
import javax.usb3.IUsbStringDescriptor;
import javax.usb3.descriptor.UsbDeviceDescriptor;
import javax.usb3.enumerated.EDevicePortSpeed;
import javax.usb3.enumerated.EUSBClassCode;
import javax.usb3.enumerated.EUSBSubclassCode;
import javax.usb3.event.IUsbDeviceListener;
import javax.usb3.exception.UsbDisconnectedException;
import javax.usb3.exception.UsbException;
import javax.usb3.ri.IUsbDeviceWithId;
import javax.usb3.ri.UsbDeviceId;
import javax.usb3.ri.UsbDeviceListenerList;
import javax.usb3.ri.UsbPorts;
import javax.usb3.ri.UsbRootHubConfiguration;

import org.jurr.java.omniusb.usbip.Constants;
import org.jurr.java.omniusb.usbip.domain.DeviceListCommand;
import org.jurr.java.omniusb.usbip.domain.DeviceListResponse;
import org.jurr.java.omniusb.usbip.domain.UsbDeviceDetails;

public class UsbIpServerConnection implements IUsbHub, IUsbPorts, IUsbDeviceWithId
{
	/**
	 * The UsbRootHub (virtual) manufacturer string.
	 */
	private static final String MANUFACTURER = "Javax USB";

	/**
	 * The UsbRootHub (virtual) product string.
	 */
	private static final String PRODUCT = "USB/IP virtual root Hub";

	/**
	 * The UsbRootHub (virtual) serial number.
	 */
	private static final String SERIAL_NUMBER = "1.0.0";

	/**
	 * The USB (virtual) root hub configurations.
	 */
	private final List<IUsbConfiguration> configurations;

	/**
	 * The USB (virtual) root hub device descriptor.
	 */
	private final IUsbDeviceDescriptor deviceDescriptor;

	/**
	 * Container of all USB device listeners.
	 */
	private final UsbDeviceListenerList listeners;

	/**
	 * The USB (virtual) root hub ports.
	 */
	private final UsbPorts ports;

	/**
	 * The port this device is connected to.
	 */
	private IUsbPort port;

	private final Socket socket;

	protected UsbIpServerConnection(UsbDeviceId id, UsbDeviceId parentId, int speed, final String server, final int port) throws IOException
	{
		deviceDescriptor = new UsbDeviceDescriptor((byte) 0x0300,
				EUSBClassCode.HUB,
				EUSBSubclassCode.FULL_SPEED_HUB.getBytecodeSubclass(),
				EUSBSubclassCode.FULL_SPEED_HUB.getBytecodeProtocol(),
				(byte) 0xffff,
				(short) 0x1d6b, // 1d6b Linux Foundation
				(short) 0x0003, // 0003 3.0 root hub
				(byte) 0x00,
				(byte) 1,
				(byte) 2,
				(byte) 3,
				(byte) 1);
		configurations = Arrays.asList(new UsbRootHubConfiguration(this));
		listeners = new UsbDeviceListenerList();
		ports = new UsbPorts(this);

		socket = new Socket(server, port);

		final DeviceListCommand deviceListCommand = new DeviceListCommand(Constants.USBIP_VERSION);
		socket.getOutputStream().write(deviceListCommand.toByteArray());

		final InputStream inputStream = socket.getInputStream();
		final byte[] bytes = new byte[4096 + 48];
		final int bytesRead = inputStream.read(bytes);
		if (bytesRead < 0)
		{
			throw new IOException("End of stream reached");
		}
		final DeviceListResponse deviceListResponse = DeviceListResponse.fromBuffer(ByteBuffer.wrap(bytes, 0, bytesRead));

		for (int i = 0; i < deviceListResponse.getDevices().size(); i++)
		{
			final UsbDeviceDetails usbDeviceDetails = deviceListResponse.getDevices().get(i);
			final ProxyUsbDevice proxyUsbDevice = new ProxyUsbDevice(this, i, usbDeviceDetails);
			connectUsbDevice(proxyUsbDevice);
		}
	}

	public InetAddress getInetAddress()
	{
		return socket.getInetAddress();
	}

	/**
	 * Get the Virtual USB Root HUB Device ID.
	 * <p>
	 * The Virtual USB Root HUB is hard coded to attach at bus zero, port zero,
	 * device zero.
	 * <p>
	 * Device ID encapsulates a USB Device's location to uniquely identify the
	 * device without needing to know or inspect the internal configuration of the
	 * device.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public UsbDeviceId getDeviceId()
	{
		return new UsbDeviceId(0, 0, 0, deviceDescriptor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IUsbPort getParentUsbPort()
	{
		return port;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setParentUsbPort(final IUsbPort port)
	{
		this.port = port;
	}

	@Override
	public boolean isUsbHub()
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return {@value #MANUFACTURER}
	 */
	@Override
	public String getManufacturerString()
	{
		return MANUFACTURER;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return {@value #SERIAL_NUMBER}
	 */
	@Override
	public String getSerialNumberString()
	{
		return SERIAL_NUMBER;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return {@value #PRODUCT}
	 */
	@Override
	public String getProductString()
	{
		return PRODUCT;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return {@linkplain javax.usb3.enumerated.EDevicePortSpeed#HIGH}
	 */
	@Override
	public EDevicePortSpeed getSpeed()
	{
		return EDevicePortSpeed.HIGH;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<IUsbConfiguration> getUsbConfigurations()
	{
		return configurations;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IUsbConfiguration getUsbConfiguration(final byte number)
	{
		if (number != 1)
		{
			return null;
		}
		return configurations.get(0);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return 1
	 */
	@Override
	public boolean containsUsbConfiguration(final byte number)
	{
		return number == 1;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return 1
	 */
	@Override
	public byte getActiveUsbConfigurationNumber()
	{
		return 1;
	}

	@Override
	public void setActiveUsbConfigurationNumber(final byte number) throws UsbException
	{
		if (number != 1)
		{
			throw new UsbException("Invalid configuration number: " + number);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IUsbConfiguration getActiveUsbConfiguration()
	{
		return configurations.get(0);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return TRUE
	 */
	@Override
	public boolean isConfigured()
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IUsbDeviceDescriptor getUsbDeviceDescriptor()
	{
		return deviceDescriptor;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Can't get USB string descriptor from a virtual device
	 */
	@Deprecated
	@Override
	public IUsbStringDescriptor getUsbStringDescriptor(final byte index) throws UsbException
	{
		throw new UsbException("Can't get USB string descriptor from a virtual device");
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Can't get string from a virtual device
	 */
	@Deprecated
	@Override
	public String getString(final byte index) throws UsbException
	{
		throw new UsbException("Can't get string from a virtual device");
	}

	@Override
	public void close()
	{
		ports.getAttachedUsbDevices().forEach(IUsbDevice::close);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Can't syncSubmit a virtual device
	 */
	@Deprecated
	@Override
	public void syncSubmit(final IUsbControlIrp irp) throws UsbException
	{
		throw new UsbException("Can't syncSubmit a virtual device");
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Can't asyncSubmit a virtual device
	 */
	@Deprecated
	@Override
	public void asyncSubmit(final IUsbControlIrp irp) throws UsbException
	{
		throw new UsbException("Can't asyncSubmit a virtual device");
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Can't syncSubmit a virtual device
	 */
	@Deprecated
	@Override
	public void syncSubmit(final List<IUsbControlIrp> list) throws UsbException
	{
		throw new UsbException("Can't syncSubmit a virtual device");
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Can't asyncSubmit a virtual device
	 */
	@Deprecated
	@Override
	public void asyncSubmit(final List<IUsbControlIrp> list) throws UsbException
	{
		throw new UsbException("Can't asyncSubmit a virtual device");
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Can't abort a virtual device (because we can not submit to it)
	 */
	@Deprecated
	@Override
	public void abortSubmission(final IUsbControlIrp irp) throws UsbException
	{
		throw new UsbException("Can't abort a virtual device");
	}

	@Override
	public IUsbControlIrp createUsbControlIrp(final byte bmRequestType, final byte bRequest, final short wValue, final short wIndex)
	{
		// TODO: Do we need this method, or just throw a not implemented exception?
		return null;
	}

	@Override
	public IUsbControlIrp createUsbControlIrp(final byte bmRequestType, final byte bRequest, final short wValue, final short wIndex, final byte[] data)
	{
		// TODO: Do we need this method, or just throw a not implemented exception?
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addUsbDeviceListener(final IUsbDeviceListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeUsbDeviceListener(final IUsbDeviceListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte getNumberOfPorts()
	{
		return ports.getNumberOfPorts();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<IUsbPort> getUsbPorts()
	{
		return ports.getUsbPorts();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IUsbPort getUsbPort(final byte number)
	{
		return ports.getUsbPort(number);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<IUsbDevice> getAttachedUsbDevices()
	{
		return ports.getAttachedUsbDevices();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isUsbDeviceAttached(final IUsbDevice device)
	{
		return ports.isUsbDeviceAttached(device);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRootUsbHub()
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return TRUE
	 */
	@Override
	public boolean isConnected() throws UsbDisconnectedException
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return TRUE
	 */
	@Override
	public boolean isInterfaceClaimed(byte number)
	{
		return true;
	}

	@Override
	public void connectUsbDevice(final IUsbDevice device)
	{
		ports.connectUsbDevice(device);
	}

	@Override
	public void disconnectUsbDevice(final IUsbDevice device)
	{
		ports.disconnectUsbDevice(device);
	}

	/**
	 * Virtual Root Hub is always first.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(IUsbDevice o)
	{
		return -1;
	}

	@Override
	public String toString()
	{
		return getManufacturerString()
				+ " " + getProductString()
				+ " (" + getNumberOfPorts() + " ports)"
				+ " @" + getInetAddress();
	}
}
