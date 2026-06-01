/*
 * Copyright (C) 2011 Klaus Reimer
 * Copyright (C) 2014 Jesse Caulfield
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package javax.usb3.usb4java;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.usb3.IUsbControlIrp;
import javax.usb3.IUsbStringDescriptor;
import javax.usb3.descriptor.UsbStringDescriptor;
import javax.usb3.enumerated.EDescriptorType;
import javax.usb3.exception.UsbDisconnectedException;
import javax.usb3.exception.UsbException;
import javax.usb3.exception.UsbPlatformException;
import javax.usb3.ri.UsbControlIrpQueue;
import javax.usb3.ri.UsbDeviceId;
import javax.usb3.ri.UsbDeviceListenerList;
import javax.usb3.ri.UsbHub;
import javax.usb3.usb4java.utility.Usb4JavaUsbExceptionFactory;

import org.usb4java.ConfigDescriptor;
import org.usb4java.Device;
import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;

/**
 * UsbHub implementation.
 * <p>
 * This must be set up before use and/or connection to the topology tree. To set
 * <p>
 * The port numbering is 1-based, not 0-based.
 * <p>
 * Hubs are a type of USB device that provide additional attachment points to
 * the USB.
 *
 * @author Klaus Reimer
 * @author Jesse Caulfield
 */
public final class Usb4JavaUsbHub extends UsbHub implements Usb4JavaAUsbDevice
{
	/**
	 * The device handle. Null if not open.
	 */
	private DeviceHandle handle;

	/**
	 * The USB device deviceManager.
	 */
	private final Usb4JavaUsbDeviceManager deviceManager;

	private final Device device;

	/**
	 * If kernel driver was detached when interface was claimed.
	 */
	private boolean detachedKernelDriver;

	/**
	 * Constructs a new USB hub device. This creates a hub with a initial number
	 * of ports.
	 *
	 * @param deviceManager The USB device manager which is responsible for this *
	 *            device.
	 * @param id THe device id. Must not be null.
	 * @param parentId The parent id. may be null if this device has no
	 *            parent.
	 * @param speed The device speed.
	 * @param device The libusb device. This reference is only valid during
	 *            the constructor execution, so don't store it in a
	 *            property or something like that.
	 * @throws UsbPlatformException When device configuration could not be read.
	 */
	public Usb4JavaUsbHub(final Usb4JavaUsbDeviceManager deviceManager, final UsbDeviceId id, final UsbDeviceId parentId, final int speed, final Device device) throws UsbPlatformException
	{
		super(id, parentId, speed);
		this.deviceManager = deviceManager;
		this.device = device;
	}

	// TODO: Stuff below is duplicated in Usb4JavaUsbDevice.

	/**
	 * Opens the USB device and returns the USB device handle. If device was
	 * already open then the old handle is returned.
	 *
	 * @return The USB device handle.
	 * @throws UsbException When USB device could not be opened.
	 */
	@Override
	public final DeviceHandle open() throws UsbException
	{
		if (handle == null)
		{
			final Device device = deviceManager.getLibUsbDevice(getDeviceId());
			try
			{
				final DeviceHandle deviceHandle = new DeviceHandle();
				final int result = LibUsb.open(device, deviceHandle);
				if (result < 0)
				{
					throw Usb4JavaUsbExceptionFactory.createPlatformException("Can't open device " + getDeviceId(), result);
				}
				handle = deviceHandle;
			}
			finally
			{
				deviceManager.releaseDevice(device);
			}
		}
		return handle;
	}

	/**
	 * Closes the device. If device is not open then nothing is done.
	 */
	@Override
	public final void close()
	{
		if (handle != null)
		{
			LibUsb.close(handle);
			handle = null;
		}
	}

	@Override
	protected Usb4JavaUsbConfiguration doGetUsbConfiguration(final byte i) throws UsbPlatformException
	{
		final ConfigDescriptor configDescriptor = new ConfigDescriptor();
		final int result = LibUsb.getConfigDescriptor(device, i, configDescriptor);
		if (result < 0)
		{
			throw Usb4JavaUsbExceptionFactory.createPlatformException("Unable to get configuation " + i + " for device " + getDeviceId(), result);
		}
		try
		{
			return new Usb4JavaUsbConfiguration(this, configDescriptor);
		}
		finally
		{
			LibUsb.freeConfigDescriptor(configDescriptor);
		}
	}

	@Override
	protected byte doGetActiveUsbConfiguration() throws UsbPlatformException
	{
		final ConfigDescriptor configDescriptor = new ConfigDescriptor();
		final int result = LibUsb.getActiveConfigDescriptor(device, configDescriptor);
		/**
		 * ERROR_NOT_FOUND is returned when device is in unconfigured state. On OSX
		 * it may return INVALID_PARAM in this case because of a bug in libusb.
		 */
		if (result == LibUsb.ERROR_NOT_FOUND || result == LibUsb.ERROR_INVALID_PARAM)
		{
			return 0;
		}
		else if (result < 0)
		{
			throw Usb4JavaUsbExceptionFactory.createPlatformException("Unable to read active config descriptor from device " + getDeviceId(), result);
		}
		else
		{
			final byte activeConfiguration = configDescriptor.bConfigurationValue();
			LibUsb.freeConfigDescriptor(configDescriptor);
			return activeConfiguration;
		}
	}

	@Override
	protected void doSetActiveUsbConfigurationNumber(final byte number) throws UsbException
	{
		final int result = LibUsb.setConfiguration(open(), number & 0xff);
		if (result < 0)
		{
			throw Usb4JavaUsbExceptionFactory.createPlatformException("Unable to set configuration", result);
		}
	}

	@Override
	protected void doClaimInterface(final byte number, final boolean force) throws UsbException
	{
		final DeviceHandle deviceHandle = open();
		/**
		 * Detach existing driver from the device if requested and libusb supports
		 * it.
		 */
		if (force)
		{
			int result = LibUsb.kernelDriverActive(deviceHandle, number);
			if (result == LibUsb.ERROR_NO_DEVICE)
			{
				throw new UsbDisconnectedException();
			}
			if (result == 1)
			{
				result = LibUsb.detachKernelDriver(deviceHandle, number);
				if (result < 0)
				{
					throw Usb4JavaUsbExceptionFactory.createPlatformException("Unable to detach kernel driver", result);
				}
				detachedKernelDriver = true;
			}
		}

		final int result = LibUsb.claimInterface(deviceHandle, number & 0xff);
		if (result < 0)
		{
			throw Usb4JavaUsbExceptionFactory.createPlatformException("Unable to claim interface", result);
		}

	}

	@Override
	protected void doReleaseInterface(byte number) throws UsbException
	{
		final DeviceHandle deviceHandle = open();
		int result = LibUsb.releaseInterface(deviceHandle, number & 0xff);
		if (result < 0)
		{
			throw Usb4JavaUsbExceptionFactory.createPlatformException("Unable to release interface", result);
		}

		if (detachedKernelDriver)
		{
			result = LibUsb.attachKernelDriver(deviceHandle, number & 0xff);
			if (result < 0)
			{
				throw Usb4JavaUsbExceptionFactory.createPlatformException("Uanble to re-attach kernel driver", result);
			}
		}

	}

	@Override
	protected IUsbStringDescriptor doGetUsbStringDescriptor(byte index) throws UsbException
	{
		final short[] languages = getLanguages();
		final DeviceHandle deviceHandle = open();
		final short langId = languages.length == 0 ? 0 : languages[0];
		final ByteBuffer data = ByteBuffer.allocateDirect(256);
		final int result = LibUsb.getStringDescriptor(deviceHandle, index, langId, data);
		if (result < 0)
		{
			throw Usb4JavaUsbExceptionFactory.createPlatformException("Unable to get string descriptor " + index + " from device " + this, result);
		}
		return new UsbStringDescriptor(data);
	}

	@Override
	protected short[] getLanguages() throws UsbException
	{
		final DeviceHandle deviceHandle = open();
		final ByteBuffer buffer = ByteBuffer.allocateDirect(256);
		final int result = LibUsb.getDescriptor(deviceHandle, EDescriptorType.STRING.getByteCode(), (byte) 0, buffer);
		if (result < 0)
		{
			throw Usb4JavaUsbExceptionFactory.createPlatformException("Unable to get string descriptor languages", result);
		}
		if (result < 2)
		{
			throw new UsbException("Received illegal descriptor length: " + result);
		}
		final short[] languages = new short[(result - 2) / 2];
		if (languages.length == 0)
		{
			return languages;
		}
		buffer.position(2);
		buffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(languages);
		return languages;
	}

	@Override
	protected UsbControlIrpQueue createUsbControlIrpQueue(final UsbDeviceListenerList listener)
	{
		return new Usb4JavaUsbControlIrpQueue(this, listener);
	}

	@Override
	protected void doVendorSpecificControlTransfer(final IUsbControlIrp irp)
	{
		throw new IllegalStateException("Vendor specific control transfer not supported on this platform");
	}
}