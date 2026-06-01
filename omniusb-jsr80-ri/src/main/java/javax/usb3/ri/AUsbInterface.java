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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.usb3.IUsbConfiguration;
import javax.usb3.IUsbEndpoint;
import javax.usb3.IUsbInterface;
import javax.usb3.IUsbInterfaceDescriptor;

/**
 * Abstract Implementation of IUsbInterface.
 * <p>
 * The interface descriptor describes a specific interface within a
 * configuration. A configuration provides one or more interfaces, each with
 * zero or more endpoint descriptors describing a unique set of endpoints within
 * the configuration. When a configuration supports more than one interface, the
 * endpoint descriptors for a particular interface follow the interface
 * descriptor in the data returned by the GetConfiguration() request. An
 * interface descriptor is always returned as part of a configuration
 * descriptor. Interface descriptors cannot be directly accessed with a
 * GetDescriptor() or SetDescriptor() request.
 * <p>
 * An interface may include alternate settings that allow the endpoints and/or
 * their characteristics to be varied after the device has been configured. The
 * default setting for an interface is always alternate setting zero. The
 * SetInterface() request is used to select an alternate setting or to return to
 * the default setting. The GetInterface() request returns the selected
 * alternate setting.
 * <p>
 * Alternate settings allow a portion of the device configuration to be varied
 * while other interfaces remain in operation. If a configuration has alternate
 * settings for one or more of its interfaces, a separate interface descriptor
 * and its associated endpoints are included for each setting.
 * <p>
 * If a device configuration supported a single interface with two alternate
 * settings, the configuration descriptor would be followed by an interface
 * descriptor with the bInterfaceNumber and bAlternateSetting fields set to zero
 * and then the endpoint descriptors for that setting, followed by another
 * interface descriptor and its associated endpoint descriptors. The second
 * interface descriptor’s bInterfaceNumber field would also be set to zero, but
 * the bAlternateSetting field of the second interface descriptor would be set
 * to one. If an interface uses only endpoint zero, no endpoint descriptors
 * follow the interface descriptor. In this case, the bNumEndpoints field must
 * be set to zero. An interface descriptor never includes endpoint zero in the
 * number of endpoints. Table 9-12 shows the
 * <p>
 * See USB 2.0 sec 9.6.5 Interface
 *
 * @author Klaus Reimer
 * @author Jesse Caulfield
 */
public abstract class AUsbInterface implements IUsbInterface
{
	/**
	 * The interface descriptor.
	 */
	private final IUsbInterfaceDescriptor descriptor;
	/**
	 * The configuration this interface belongs to.
	 */
	private final IUsbConfiguration configuration;
	/**
	 * The endpoints of this interface.
	 */
	private final LinkedHashMap<Byte, IUsbEndpoint> endpoints;

	/**
	 * Construct a new UsbInterface.
	 *
	 * @param configuration The USB configuration this interface belongs to.
	 * @param descriptor The USB interface descriptor.
	 */
	protected AUsbInterface(final IUsbConfiguration configuration, final IUsbInterfaceDescriptor descriptor)
	{
		this.descriptor = descriptor;
		this.configuration = configuration;
		endpoints = new LinkedHashMap<>();

		// NOTE: Implementations should call addEndpoint() instead of relying on this code.
		// /**
		// * The USB (virtual) Root hub has no endpoint.
		// */
		// if (descriptor.endpoint() != null)
		// {
		// for (IUsbEndpointDescriptor iUsbEndpointDescriptor : descriptor.endpoint())
		// {
		// endpoints.put(iUsbEndpointDescriptor.endpointAddress().getByteCode(), new UsbEndpoint(this, iUsbEndpointDescriptor));
		// }
		// }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isClaimed()
	{
		return configuration.getUsbDevice().isInterfaceClaimed(descriptor.bInterfaceNumber());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isActive()
	{
		return configuration.getUsbInterface(descriptor.bInterfaceNumber()) == this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNumSettings()
	{
		return configuration.getNumSettings(descriptor.bInterfaceNumber());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IUsbInterface getSetting(final byte number)
	{
		return configuration.getSettings(descriptor.bInterfaceNumber()).get(number & 0xff);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsSetting(final byte number)
	{
		return configuration.getSettings(descriptor.bInterfaceNumber()).containsKey(number & 0xff);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<IUsbInterface> getSettings()
	{
		return Collections.unmodifiableList(new ArrayList<>(configuration.getSettings(descriptor.bInterfaceNumber()).values()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<IUsbEndpoint> getUsbEndpoints()
	{
		// Iterate using endpoints.keys() to maintain the order of the endpoints.
		final List<IUsbEndpoint> result = new ArrayList<>(endpoints.size());
		for (Map.Entry<Byte, IUsbEndpoint> entry : endpoints.entrySet())
		{
			result.add(entry.getValue());
		}
		return Collections.unmodifiableList(result);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IUsbEndpoint getUsbEndpoint(final byte address)
	{
		return endpoints.get(address);
	}

	protected void addUsbEndpoint(final IUsbEndpoint endpoint)
	{
		endpoints.put(endpoint.getUsbEndpointDescriptor().endpointAddress().getByteCode(), endpoint);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsUsbEndpoint(final byte address)
	{
		return endpoints.containsKey(address);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IUsbConfiguration getUsbConfiguration()
	{
		return configuration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IUsbInterfaceDescriptor getUsbInterfaceDescriptor()
	{
		return descriptor;
	}

	/**
	 * Sort order is based upon the interface number in the descriptor.
	 *
	 * @param o the other instance
	 * @return the sort order
	 */
	@Override
	public int compareTo(IUsbInterface o)
	{
		return Integer.compare(descriptor.bInterfaceNumber(), o.getUsbInterfaceDescriptor().bInterfaceNumber());
	}

	@Override
	public String toString()
	{
		return String.format("USB interface %02x", descriptor.bInterfaceNumber());
	}
}
