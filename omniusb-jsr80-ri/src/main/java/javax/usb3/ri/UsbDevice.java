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

import javax.usb3.exception.UsbPlatformException;

/**
 * A basic (non-hub) USB device implementation. USB devices present a standard USB interface.
 *
 * @author Klaus Reimer
 * @author Jesse Caulfield
 */
public abstract class UsbDevice extends AUsbDevice
{
	/**
	 * Constructs a new (non-hub) USB device.
	 *
	 * @param deviceId The device id. Must not be null.
	 * @param parentId The parent device id. May be null if this device has
	 *            no parent (Because it is a root device).
	 * @param speed The device USB port speed.
	 * @throws UsbPlatformException When device configuration could not be read.
	 */
	protected UsbDevice(
			final UsbDeviceId deviceId,
			final UsbDeviceId parentId,
			final int speed) throws UsbPlatformException
	{
		super(deviceId, parentId, speed);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return FALSE. UsbDevice instances are never UsbHubs. If the device is a
	 *         hub it will be identified as a UsbHub implementation.
	 */
	@Override
	public boolean isUsbHub()
	{
		return false;
	}
}
