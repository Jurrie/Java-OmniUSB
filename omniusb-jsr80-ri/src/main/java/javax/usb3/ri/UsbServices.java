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

import java.util.ServiceLoader;

import javax.usb3.IUsbDevice;
import javax.usb3.IUsbServices;
import javax.usb3.event.IUsbServicesListener;
import javax.usb3.event.UsbServicesEvent;
import javax.usb3.exception.UsbException;

/**
 * Implementation of JSR-80 IUsbServices interface.
 * <p>
 * This is instantiated by the USB. The implementation must include a
 * no-parameter constructor.
 *
 * @author Klaus Reimer
 * @author Jesse Caulfield
 */
public class UsbServices implements IUsbServices
{
	/**
	 * The implementation description.
	 */
	private static final String IMP_DESCRIPTION = "javax.usb3 reference implementation";
	/**
	 * The implementation version. This is the Java source code version.
	 */
	private static final String IMP_VERSION = "1.4.x";

	/**
	 * The API version. This is the usb4java JNI source code version.
	 */
	private static final String API_VERSION = "1.0.2";

	/**
	 * The USB services listeners.
	 */
	private final UsbServicesListenerList listeners = new UsbServicesListenerList();

	/**
	 * The virtual USB root hub.
	 */
	private final UsbRootHub rootUsbHub;

	private boolean implementationsInitialized;

	/**
	 * Constructor.
	 *
	 * @throws UsbException When properties could not be loaded.
	 * @throws RuntimeException When the native library corresponding to the host
	 *             operating system fails to load
	 */
	public UsbServices() throws UsbException
	{
		rootUsbHub = new UsbRootHub();

		ServiceLoader.load(UsbImplementationService.class).forEach(rootUsbHub::connectUsbDevice);
		implementationsInitialized = false;
	}

	@Override
	public UsbRootHub getRootUsbHub()
	{
		if (!implementationsInitialized)
		{
			rootUsbHub.getAttachedUsbDevices().stream()
					.map(UsbImplementationService.class::cast)
					.forEach(UsbImplementationService::initialize);
		}

		return rootUsbHub;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addUsbServicesListener(final IUsbServicesListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeUsbServicesListener(final IUsbServicesListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void usbDeviceAttached(final IUsbDevice device)
	{
		listeners.usbDeviceAttached(new UsbServicesEvent(this, device));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void usbDeviceDetached(final IUsbDevice device)
	{
		listeners.usbDeviceDetached(new UsbServicesEvent(this, device));
	}

	@Override
	public String getApiVersion()
	{
		return API_VERSION;
	}

	@Override
	public String getImpVersion()
	{
		return IMP_VERSION;
	}

	@Override
	public String getImpDescription()
	{
		return IMP_DESCRIPTION;
	}
}
