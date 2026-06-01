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
package javax.usb3.ri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.usb3.IUsbConfiguration;
import javax.usb3.IUsbConfigurationDescriptor;
import javax.usb3.IUsbDevice;
import javax.usb3.IUsbEndpoint;
import javax.usb3.IUsbInterface;
import javax.usb3.exception.UsbException;

/**
 * Implementation of JSR-80 IUsbConfiguration.
 *
 * @author Klaus Reimer
 * @author Jesse Caulfield
 */
public abstract class AUsbConfiguration implements IUsbConfiguration
{
	/**
	 * The configurationDescriptor.
	 */
	private final IUsbConfigurationDescriptor descriptor;

	/**
	 * The USB device this configuration belongs to.
	 */
	private final IUsbDevice device;

	/**
	 * The interfaces.
	 * <p>
	 * This is a map of the interface number to a sub-map of alternate settings
	 * which maps setting numbers to actual interfaces.
	 */
	private final Map<Integer, Map<Integer, AUsbInterface>> interfaces = new HashMap<>(); // TODO: Should be Map<Byte, Map<Byte, AUsbInterace>>?

	/**
	 * This map contains the active USB interfaces.
	 */
	private final Map<Integer, AUsbInterface> activeSettings = new HashMap<>(); // TODO: Should be Map<Byte, AUsbInterface>?

	/**
	 * Constructor.
	 *
	 * @param device The device this configuration belongs to.
	 * @param descriptor The libusb configuration descriptor.
	 */
	protected AUsbConfiguration(final IUsbDevice device, final IUsbConfigurationDescriptor descriptor)
	{
		this.device = device;
		this.descriptor = descriptor;
	}

	protected void addInterface(final AUsbInterface usbInterface)
	{
		// TODO: Should be bytes?
		final int interfaceNumber = usbInterface.getUsbInterfaceDescriptor().bInterfaceNumber() & 0xff;
		final int settingNumber = usbInterface.getUsbInterfaceDescriptor().bAlternateSetting() & 0xff;

		/**
		 * If we have no active setting for current interface number yet or the
		 * alternate setting number is 0 (which marks the default alternate
		 * setting) then set current interface as the active setting.
		 */
		if (!getActiveSettings().containsKey(interfaceNumber) || settingNumber == 0)
		{
			getActiveSettings().put(interfaceNumber, usbInterface);
		}
		/**
		 * Add the interface to the settings list
		 */
		Map<Integer, AUsbInterface> settings = getInterfaces().get(interfaceNumber);
		if (settings == null)
		{
			settings = new HashMap<>();
			getInterfaces().put(interfaceNumber, settings);
		}
		settings.put(settingNumber, usbInterface);
	}

	protected Map<Integer, AUsbInterface> getActiveSettings()
	{
		return activeSettings;
	}

	protected Map<Integer, Map<Integer, AUsbInterface>> getInterfaces()
	{
		return interfaces;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isActive()
	{
		return device.getActiveUsbConfigurationNumber() == descriptor.bConfigurationValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<IUsbInterface> getUsbInterfaces()
	{
		return Collections.unmodifiableList(new ArrayList<>(activeSettings.values()));
	}

	@Override
	public IUsbEndpoint getUsbEndpoint(byte address)
	{
		for (IUsbInterface usbInterface : getUsbInterfaces())
		{
			for (IUsbEndpoint usbEndpoint : usbInterface.getUsbEndpoints())
			{
				if (usbEndpoint.getUsbEndpointDescriptor().bEndpointAddress() == address)
				{
					return usbEndpoint;
				}
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Integer, IUsbInterface> getSettings(final byte number)
	{
		final Map<Integer, AUsbInterface> map = interfaces.get(number & 0xff);
		final Map<Integer, IUsbInterface> result = new HashMap<>(map.size(), 1);
		for (final Map.Entry<Integer, AUsbInterface> entry : map.entrySet())
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNumSettings(final byte number)
	{
		return interfaces.get(number & 0xff).size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AUsbInterface getUsbInterface(final byte number)
	{
		return activeSettings.get(number & 0xff);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUsbInterface(final byte number, final IUsbInterface usbInterface) throws UsbException
	{
		if (activeSettings.get(number & 0xff) != usbInterface)
		{
			doSetUsbInterfaceAlternate(number, usbInterface);

			activeSettings.put(number & 0xff, (AUsbInterface) usbInterface);
		}
	}

	protected abstract void doSetUsbInterfaceAlternate(byte number, IUsbInterface usbInterface) throws UsbException;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsUsbInterface(final byte number)
	{
		return activeSettings.containsKey(number & 0xff);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IUsbDevice getUsbDevice()
	{
		return device;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IUsbConfigurationDescriptor getUsbConfigurationDescriptor()
	{
		return descriptor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getConfigurationString() throws UsbException
	{
		/**
		 * Ensure the device is still connected.
		 */
		device.isConnected();
		final byte iConfiguration = descriptor.iConfiguration();
		if (iConfiguration == 0)
		{
			return null;
		}
		return device.getString(iConfiguration);
	}

	@Override
	public String toString()
	{
		return String.format("USB configuration %02x", descriptor.bConfigurationValue());
	}
}
