/*
 * Copyright (C) 2013 Klaus Reimer
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
package javax.usb3.ri;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.usb3.IUsbConfiguration;
import javax.usb3.IUsbControlIrp;
import javax.usb3.IUsbDevice;
import javax.usb3.IUsbDeviceDescriptor;
import javax.usb3.IUsbPort;
import javax.usb3.IUsbServices;
import javax.usb3.IUsbStringDescriptor;
import javax.usb3.UsbHostManager;
import javax.usb3.enumerated.EDevicePortSpeed;
import javax.usb3.event.IUsbDeviceListener;
import javax.usb3.event.UsbDeviceEvent;
import javax.usb3.exception.UsbClaimException;
import javax.usb3.exception.UsbDisconnectedException;
import javax.usb3.exception.UsbException;
import javax.usb3.exception.UsbPlatformException;

/**
 * UsbDevice platform-independent implementation.
 * <p>
 * This implements all required functionality of the IUsbDevice interface plus
 * additional functionality required to interface with the native LIBUSB
 * library.
 * <p>
 * This abstract class is extended by:
 * <ul>
 * <li>UsbHub extends AUsbDevice implements IUsbUsbHub, IUsbPorts</li>
 * <li>UsbDevice extends AUsbDevice</li>
 * </ul>
 * This must be set up before use and/or connection to the topology tree.
 * <ul>
 * <li>The UsbDeviceDescriptor must be set, either in the constructor or by its
 * {@code setUsbDeviceDescriptor(UsbDeviceDescriptor) setter}.</li>
 * <li>The UsbDeviceOs may optionally be set, either in the constructor or by
 * its {@code setUsbDeviceOs(UsbDeviceOs) setter}. If not set, it defaults to a
 * {@code DefaultUsbDeviceOs}.</li>
 * <li>The speed must be set by its {@code setSpeed(Object) setter}.</li>
 * <li>All UsbConfigurations must be
 * {@code addUsbConfiguration(UsbConfiguration) added}.</li>
 * <li>The active config number must be
 * {@code setActiveUsbConfigurationNumber(byte) set}, if this device
 * {@code isConfigured() is configured}.</li>
 * </ul>
 * After setup, this may be connected to the topology tree by using the
 * {@code connect(UsbHub,byte) connect} method. If the connect method is not
 * used, there are additional steps:
 * <ul>
 * <li>Set the parent UsbPort by the
 * {@code setParentUsbPort(UsbPort) setter}.</li>
 * <li>Set this on the UsbPort by its
 * {@code UsbPort#attachUsbDevice(UsbDevice) setter}.</li>
 * </ul>
 *
 * @author Dan Streetman
 * @author Klaus Reimer
 * @author Jesse Caulfield
 */
public abstract class AUsbDevice implements IUsbDeviceWithId, IUsbDevice
{
	/**
	 * The Unique USB Device ID. This encapsulates a USB Device's BUS location to
	 * uniquely identify the device without needing to know or inspect the
	 * internal configuration of the device.
	 */
	private final UsbDeviceId deviceId;
	/**
	 * The parent USB Device ID. Null if no parent exists.
	 */
	private final UsbDeviceId parentId;
	/**
	 * The device speed.
	 */
	private final int speed;
	/**
	 * Mapping from configuration value to configuration.
	 */
	private Map<Byte, IUsbConfiguration> configMapping; // Lazy init
	/**
	 * The USB device listener list.
	 */
	private final UsbDeviceListenerList listeners = new UsbDeviceListenerList();
	/**
	 * The numbers of the currently claimed interface.
	 */
	private final Set<Byte> claimedInterfaceNumbers = new HashSet<>();
	/**
	 * The port this device is connected to.
	 */
	private IUsbPort port;
	/**
	 * The IRP controlIrpQueue.
	 */
	private UsbControlIrpQueue controlIrpQueue;

	/**
	 * Construct a new device.
	 *
	 * @param deviceManager The USB device deviceManager which is responsible for
	 *            this device.
	 * @param deviceId The device deviceId. Must not be null.
	 * @param parentId The parent device deviceId. May be null if this device
	 *            has no parent (Because it is a root device).
	 * @param speed The device speed code. This is the native (OS)
	 *            negotiated connection speed for the device.
	 * @param device The libusb native device reference. This reference is
	 *            only valdeviceId during the constructor execution, so
	 *            don't store it in a property or something like that.
	 * @throws UsbPlatformException When device configuration could not be
	 *             read.
	 * @throws IllegalArgumentException if the DeviceManager or DeviceId are null
	 */
	protected AUsbDevice(final UsbDeviceId deviceId, final UsbDeviceId parentId, final int speed) throws UsbPlatformException
	{
		if (deviceId == null)
		{
			throw new IllegalArgumentException("DeviceId is required.");
		}
		this.deviceId = deviceId;
		this.parentId = parentId;
		this.speed = speed;
	}

	private Map<Byte, IUsbConfiguration> getConfigMapping()
	{
		if (configMapping == null)
		{
			configMapping = new HashMap<>();

			/**
			 * Read the device configurations
			 */
			final byte numConfigurations = deviceId.getDeviceDescriptor().bNumConfigurations();
			for (byte i = 1; i <= numConfigurations; i += 1) // Configuration 0 means "unconfigured", so we skip that
			{
				AUsbConfiguration usbConfiguration;
				try
				{
					usbConfiguration = doGetUsbConfiguration(i);
				}
				catch (UsbPlatformException e)
				{
					throw new RuntimeException("Error while getting USB configuration", e);
				}
				configMapping.put(i, usbConfiguration);
			}
		}
		return configMapping;
	}

	protected UsbControlIrpQueue getControlIrpQueue()
	{
		if (controlIrpQueue == null)
		{
			controlIrpQueue = createUsbControlIrpQueue(listeners);
		}
		return controlIrpQueue;
	}

	protected UsbControlIrpQueue createUsbControlIrpQueue(final UsbDeviceListenerList listener)
	{
		return new UsbControlIrpQueue(this, listener);
	}

	/**
	 * @param i the INDEX of the configuration to retrieve (so NOT bConfigurationValue!)
	 */
	protected abstract AUsbConfiguration doGetUsbConfiguration(byte i) throws UsbPlatformException;

	/**
	 * @return NULL when there is no active configuration
	 * @throws UsbPlatformException
	 */
	protected abstract byte doGetActiveUsbConfiguration() throws UsbPlatformException;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final UsbDeviceId getDeviceId()
	{
		return deviceId;
	}

	/**
	 * Returns the parent USB device Id.
	 *
	 * @return The parent device id or null of there is no parent.
	 */
	public final UsbDeviceId getParentDeviceId()
	{
		return parentId;
	}

	/**
	 * Check and ensures the device is connected.
	 *
	 * @return TRUE if the device is connected.
	 * @throws UsbDisconnectedException When device is disconnected.
	 */
	@Override
	public final boolean isConnected() throws UsbDisconnectedException
	{
		if (port == null)
		{
			throw new UsbDisconnectedException();
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final IUsbPort getParentUsbPort()
	{
		isConnected();
		return port;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setParentUsbPort(final IUsbPort port)
	{
		if (this.port == null && port == null)
		{
			throw new IllegalStateException("Device already detached");
		}
		if (this.port != null && port != null)
		{
			throw new IllegalStateException("Device already attached");
		}

		// Disconnect client devices
		if (port == null && isUsbHub())
		{
			final UsbHub hub = (UsbHub) this;
			for (final IUsbDevice device : hub.getAttachedUsbDevices())
			{
				hub.disconnectUsbDevice(device);
			}
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
			throw new RuntimeException("Unable to attach USB services: " + usbException);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getManufacturerString() throws UsbException, UnsupportedEncodingException
	{
		isConnected();
		final byte index = getUsbDeviceDescriptor().iManufacturer();
		return getString(index);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getSerialNumberString() throws UsbException, UnsupportedEncodingException
	{
		isConnected();
		final byte index = getUsbDeviceDescriptor().iSerialNumber();
		return getString(index);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getProductString() throws UsbException, UnsupportedEncodingException
	{
		isConnected();
		final byte index = getUsbDeviceDescriptor().iProduct();
		return getString(index);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final EDevicePortSpeed getSpeed()
	{
		return EDevicePortSpeed.speedSupported((short) speed);
	}

	protected UsbDeviceListenerList getListeners()
	{
		return listeners;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Collection<IUsbConfiguration> getUsbConfigurations()
	{
		return Collections.unmodifiableCollection(getConfigMapping().values());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final IUsbConfiguration getUsbConfiguration(final byte number)
	{
		return getConfigMapping().get(number);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean containsUsbConfiguration(final byte number)
	{
		return getConfigMapping().containsKey(number);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final byte getActiveUsbConfigurationNumber()
	{
		try
		{
			return doGetActiveUsbConfiguration();
		}
		catch (UsbPlatformException e)
		{
			throw new RuntimeException("Error while getting active USB configuration", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setActiveUsbConfigurationNumber(final byte number) throws UsbException
	{
		if (number != doGetActiveUsbConfiguration())
		{
			if (!claimedInterfaceNumbers.isEmpty())
			{
				throw new UsbException("Cannot change configuration while an interface is still claimed");
			}

			doSetActiveUsbConfigurationNumber(number);
		}
	}

	/**
	 * @param i the INDEX of the configuration to retrieve (so NOT bConfigurationValue!)
	 */
	protected abstract void doSetActiveUsbConfigurationNumber(byte i) throws UsbException;

	/**
	 * Claim the specified interface.
	 *
	 * @param number The number of the interface to claim.
	 * @param force If possible, try to force the claim.
	 * @throws UsbException When the interface cannot not be claimed.
	 */
	public final void claimInterface(final byte number, final boolean force) throws UsbException
	{
		if (claimedInterfaceNumbers.contains(number))
		{
			throw new UsbClaimException("An interface is already claimed");
		}

		doClaimInterface(number, force);

		claimedInterfaceNumbers.add(number);
	}

	protected abstract void doClaimInterface(byte number, boolean force) throws UsbException;

	/**
	 * Release a claimed interface.
	 *
	 * @param number The number of the interface to release.
	 * @throws UsbException When the interface claim cannot be released or the
	 *             interface is not claimed.
	 */
	public final void releaseInterface(final byte number) throws UsbException
	{
		if (claimedInterfaceNumbers.isEmpty())
		{
			throw new UsbClaimException("No interface is claimed");
		}
		if (!claimedInterfaceNumbers.contains(number))
		{
			throw new UsbClaimException("Interface not claimed");
		}

		doReleaseInterface(number);

		claimedInterfaceNumbers.remove(number);
	}

	protected abstract void doReleaseInterface(byte number) throws UsbException;

	/**
	 * @inherit.
	 */
	@Override
	public final boolean isInterfaceClaimed(final byte number)
	{
		return claimedInterfaceNumbers.contains(number);
	}

	/**
	 * Get the active IUsbConfiguration.
	 * <p>
	 * If this device is Not Configured, this returns null.
	 *
	 * @return The active IUsbConfiguration, or null.
	 */
	@Override
	public final IUsbConfiguration getActiveUsbConfiguration()
	{
		return getUsbConfiguration(getActiveUsbConfigurationNumber());
	}

	/**
	 * If this IUsbDevice is configured.
	 * <p>
	 * This returns true if the device is in the configured state as shown in the
	 * USB 1.1 specification table 9.1.
	 *
	 * @return If this is in the Configured state.
	 */
	@Override
	public final boolean isConfigured()
	{
		return getActiveUsbConfigurationNumber() != 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final IUsbDeviceDescriptor getUsbDeviceDescriptor()
	{
		return deviceId.getDeviceDescriptor();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final IUsbStringDescriptor getUsbStringDescriptor(final byte index) throws UsbException
	{
		isConnected();
		return doGetUsbStringDescriptor(index);
	}

	protected abstract IUsbStringDescriptor doGetUsbStringDescriptor(byte index) throws UsbException;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getString(final byte index) throws UsbException
	{
		return getUsbStringDescriptor(index).getString();
	}

	/**
	 * Returns the languages the specified device supports.
	 *
	 * @return Array with supported language codes. Never null. May be empty.
	 * @throws UsbException When string descriptor languages could not be read.
	 */
	protected abstract short[] getLanguages() throws UsbException;

	@Override
	public void close()
	{
		if (controlIrpQueue != null) // It could be null if we never called getControlIrpQueue()
		{
			try
			{
				controlIrpQueue.close();
				controlIrpQueue.join();
			}
			catch (InterruptedException e)
			{
				// We are already closing
				Thread.currentThread().interrupt();
			}
			finally
			{
				controlIrpQueue = null;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void syncSubmit(final IUsbControlIrp irp) throws UsbException
	{
		if (irp == null)
		{
			throw new IllegalArgumentException("irp must not be null");
		}
		isConnected();
		getControlIrpQueue().add(irp);
		irp.waitUntilComplete();
		if (irp.isUsbException())
		{
			throw irp.getUsbException();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void asyncSubmit(final IUsbControlIrp irp)
	{
		if (irp == null)
		{
			throw new IllegalArgumentException("irp must not be null");
		}
		isConnected();
		getControlIrpQueue().add(irp);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void syncSubmit(final List<IUsbControlIrp> list) throws UsbException
	{
		if (list == null)
		{
			throw new IllegalArgumentException("list must not be null");
		}
		isConnected();
		for (final IUsbControlIrp item : list)
		{
			syncSubmit(item);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void asyncSubmit(final List<IUsbControlIrp> list)
	{
		if (list == null)
		{
			throw new IllegalArgumentException("list must not be null");
		}
		isConnected();
		for (final IUsbControlIrp item : list)
		{
			asyncSubmit(item);
		}
	}

	@Override
	public void abortSubmission(final IUsbControlIrp irp) throws UsbDisconnectedException
	{
		isConnected();
		getControlIrpQueue().abortSubmission(irp);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final IUsbControlIrp createUsbControlIrp(final byte bmRequestType, final byte bRequest, final short wValue, final short wIndex)
	{
		return new UsbControlIrp(bmRequestType, bRequest, wValue, wIndex);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IUsbControlIrp createUsbControlIrp(byte bmRequestType, byte bRequest, short wValue, short wIndex, byte[] data)
	{
		final UsbControlIrp usbControlIrp = new UsbControlIrp(bmRequestType, bRequest, wValue, wIndex);
		usbControlIrp.setData(data);
		return usbControlIrp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void addUsbDeviceListener(final IUsbDeviceListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void removeUsbDeviceListener(final IUsbDeviceListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * Hash code is based upon the DeviceId.
	 *
	 * @return object hash code
	 */
	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 79 * hash + Objects.hashCode(deviceId);
		return hash;
	}

	/**
	 * Equals is based upon the DeviceId.
	 *
	 * @param obj the other object to test
	 * @return TRUE if the other object has the same DeviceId.
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (obj == null)
		{
			return false;
		}

		if (obj instanceof IUsbDeviceWithId usbDeviceWithId)
		{
			return Objects.equals(deviceId, usbDeviceWithId.getDeviceId());
		}

		throw new IllegalArgumentException("Reference implementation only works with IUsbDevice instances that implement IUsbDeviceWithId");
	}

	/**
	 * Sort on device ID.
	 *
	 * @param o the other instance
	 * @return the sort order
	 */
	@Override
	public int compareTo(final IUsbDevice o)
	{
		if (o instanceof IUsbDeviceWithId usbDeviceWithId)
		{
			return deviceId == null ? +1 : deviceId.compareTo(usbDeviceWithId.getDeviceId());
		}

		throw new IllegalArgumentException("Reference implementation only works with IUsbDevice instances that implement IUsbDeviceWithId");
	}

	@Override
	public final String toString()
	{
		return deviceId.toString();
	}

	protected abstract void doVendorSpecificControlTransfer(IUsbControlIrp irp) throws UsbException;
}
