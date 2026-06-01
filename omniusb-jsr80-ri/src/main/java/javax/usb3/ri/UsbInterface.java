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

import java.io.UnsupportedEncodingException;

import javax.usb3.IUsbConfiguration;
import javax.usb3.IUsbInterface;
import javax.usb3.IUsbInterfaceDescriptor;
import javax.usb3.IUsbInterfacePolicy;
import javax.usb3.exception.UsbDisconnectedException;
import javax.usb3.exception.UsbException;
import javax.usb3.exception.UsbNotActiveException;

/**
 * Implementation of IUsbUsbInterface.
 *
 * @author Jesse Caulfield
 */
public class UsbInterface extends AUsbInterface
{
	/**
	 * Constructor.
	 *
	 * @param configuration The USB configuration this interface belongs to.
	 * @param descriptor The libusb interface descriptor.
	 */
	public UsbInterface(final IUsbConfiguration configuration, final IUsbInterfaceDescriptor descriptor)
	{
		super(configuration, descriptor);
	}

	/**
	 * Ensures this setting and configuration is active.
	 *
	 * @throws UsbException
	 *
	 * @throws UsbNotActiveException When the setting or the configuration is not
	 *             active.
	 */
	private void checkActive() throws UsbException
	{
		if (!getUsbConfiguration().isActive())
		{
			throw new UsbNotActiveException("Configuration is not active");
		}
		if (!isActive())
		{
			throw new UsbNotActiveException("Setting is not active");
		}
	}

	/**
	 * Ensures that the device is connected.
	 *
	 * @throws UsbDisconnectedException When device has been disconnected.
	 */
	private void checkConnected()
	{
		getUsbConfiguration().getUsbDevice().isConnected();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void claim() throws UsbException
	{
		claim(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void claim(final IUsbInterfacePolicy policy) throws UsbException
	{
		checkActive();
		checkConnected();
		final AUsbDevice device = (AUsbDevice) getUsbConfiguration().getUsbDevice();
		device.claimInterface(getUsbInterfaceDescriptor().bInterfaceNumber(), policy != null && policy.forceClaim(this));
		getUsbConfiguration().setUsbInterface(getUsbInterfaceDescriptor().bInterfaceNumber(), this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() throws UsbException
	{
		checkActive();
		checkConnected();
		((AUsbDevice) getUsbConfiguration().getUsbDevice()).releaseInterface(getUsbInterfaceDescriptor().bInterfaceNumber());
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws UsbException
	 */
	@Override
	public byte getActiveSettingNumber() throws UsbNotActiveException
	{
		try
		{
			checkActive();
		}
		catch (UsbException e)
		{
			throw new UsbNotActiveException(e.getLocalizedMessage());
		}
		return getUsbConfiguration()
				.getUsbInterface(getUsbInterfaceDescriptor().bInterfaceNumber())
				.getUsbInterfaceDescriptor().bAlternateSetting();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws UsbException
	 */
	@Override
	public IUsbInterface getActiveSetting() throws UsbNotActiveException
	{
		try
		{
			checkActive();
		}
		catch (UsbException e)
		{
			throw new UsbNotActiveException(e.getLocalizedMessage());
		}
		return getUsbConfiguration().getUsbInterface(getUsbInterfaceDescriptor().bInterfaceNumber());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getInterfaceString() throws UsbException,
			UnsupportedEncodingException
	{
		checkConnected();
		final byte iInterface = getUsbInterfaceDescriptor().iInterface();
		if (iInterface == 0)
		{
			return null;
		}
		return getUsbConfiguration().getUsbDevice().getString(iInterface);
	}

	@Override
	public String toString()
	{
		return String.format("USB interface %02x",
				getUsbInterfaceDescriptor().bInterfaceNumber());
	}
}
