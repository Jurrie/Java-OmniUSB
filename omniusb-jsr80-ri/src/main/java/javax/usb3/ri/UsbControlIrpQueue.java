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

import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.usb3.IUsbConfiguration;
import javax.usb3.IUsbConfigurationDescriptor;
import javax.usb3.IUsbControlIrp;
import javax.usb3.IUsbDeviceDescriptor;
import javax.usb3.IUsbEndpoint;
import javax.usb3.IUsbEndpointDescriptor;
import javax.usb3.IUsbInterface;
import javax.usb3.IUsbInterfaceDescriptor;
import javax.usb3.IUsbIrp;
import javax.usb3.IUsbStringDescriptor;
import javax.usb3.enumerated.EDescriptorType;
import javax.usb3.enumerated.EEndpointDirection;
import javax.usb3.event.UsbDeviceDataEvent;
import javax.usb3.exception.UsbException;
import javax.usb3.request.BMRequestType;
import javax.usb3.request.BRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A concurrent queue manager for USB I/O Request packets.
 * <p>
 * An IrpQueue contains a thread safe FIFO queue and a threaded
 * processUsbIrpQueueor to handle each IRP that is placed into the queue.
 * <p>
 * Developer note: The default operation of an IrpQueue is to support
 * Asynchronous operation (e.g. processUsbIrpQueue in a separate thread.) To
 * implement synchronous IRP queue handling implement a WAIT lock on the
 * {@link IUsbIrp#isComplete() isComplete} method IUsbIrp.isComplete().
 *
 * @author Klaus Reimer
 * @author Jesse Caulfield
 */
public class UsbControlIrpQueue extends AUsbIrpQueue<IUsbControlIrp>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * The USB device listener list.
	 */
	private final UsbDeviceListenerList listeners;

	/**
	 * Constructor.
	 *
	 * @param device The USB device.
	 * @param listeners The USB device listener list.
	 */
	public UsbControlIrpQueue(final AUsbDevice device, final UsbDeviceListenerList listeners)
	{
		super(device);
		this.listeners = listeners;
	}

	/**
	 * Processes the IRP.
	 *
	 * @param irp The IRP to processUsbIrpQueue.
	 * @throws UsbException When processUsbIrpQueueing the IRP fails.
	 */
	@Override
	protected void processIrp(final IUsbControlIrp irp, final ProcessIrpCallback callback) throws UsbException
	{
		processControlIrp(irp, callback);
	}

	/**
	 * Called after IRP has finished. This can be implemented to send events for
	 * example.
	 *
	 * @param irp The IRP which has been finished.
	 */
	@Override
	protected void finishIrp(final IUsbIrp irp)
	{
		listeners.dataEventOccurred(new UsbDeviceDataEvent(getUsbDevice(), (IUsbControlIrp) irp));
	}

	@Override
	protected void doControlTransfer(final IUsbControlIrp irp, final ProcessIrpCallback callback) throws UsbException
	{
		final BMRequestType bmRequestType = new BMRequestType(irp.bmRequestType());
		final BRequest request = new BRequest(irp.bRequest());

		switch (bmRequestType.getType())
		{
		case STANDARD:
			switch (bmRequestType.getRecipient())
			{
			case DEVICE:
				switch (request.getDeviceRequest())
				{
				case GET_DESCRIPTOR:
					switch (EDescriptorType.fromBytecode((byte) (irp.wValue() >> 8)))
					{
					case EDescriptorType.DEVICE:
					{
						LOGGER.info("DEVICE GET_DESCRIPTOR device descriptor request");
						if (bmRequestType.getDirection() != EEndpointDirection.DEVICE_TO_HOST)
						{
							throw new UsbException("GET_DESCRIPTOR only supports DEVICE_TO_HOST");
						}
						final int actualLength = deviceDescriptorToBuffer(irp.getData(), getUsbDevice().getUsbDeviceDescriptor());
						irp.setActualLength(actualLength);
						LOGGER.info("DEVICE GET_DESCRIPTOR device descriptor request complete, sent {} bytes", actualLength);
						callback.onTransferComplete(actualLength);
						return;
					}
					case EDescriptorType.CONFIGURATION:
					{
						byte configurationIndex = (byte) irp.wValue();
						LOGGER.info("DEVICE GET_DESCRIPTOR configuration descriptor request for configuration index {}", configurationIndex);
						if (bmRequestType.getDirection() != EEndpointDirection.DEVICE_TO_HOST)
						{
							throw new UsbException("GET_DESCRIPTOR only supports DEVICE_TO_HOST");
						}

						final int actualLength = configurationDescriptorToBuffer(irp.getData(), getUsbDevice().getUsbConfiguration((byte) (configurationIndex + 1)));
						irp.setActualLength(actualLength);
						LOGGER.info("DEVICE GET_DESCRIPTOR configuration descriptor request complete, sent {} bytes", actualLength);
						callback.onTransferComplete(actualLength);
						return;
					}
					case EDescriptorType.STRING:
					{
						final byte stringIndex = (byte) irp.wValue();
						if (stringIndex == 0x00 && irp.wIndex() == 0x00)
						{
							LOGGER.info("DEVICE GET_DESCRIPTOR string descriptor (supported languages) request");
							if (bmRequestType.getDirection() != EEndpointDirection.DEVICE_TO_HOST)
							{
								throw new UsbException("GET_DESCRIPTOR only supports DEVICE_TO_HOST");
							}
							final int actualLength = languagesStringDescriptorToBuffer(irp.getData(), getUsbDevice().getLanguages());
							irp.setActualLength(actualLength);
							LOGGER.info("DEVICE GET_DESCRIPTOR string descriptor request complete, sent {} bytes", actualLength);
							callback.onTransferComplete(actualLength);
							return;
						}
						else
						{
							LOGGER.info("DEVICE GET_DESCRIPTOR string descriptor request for string index {} and language {} (length: {})", stringIndex, irp.wIndex(), irp.getData().length);
							if (bmRequestType.getDirection() != EEndpointDirection.DEVICE_TO_HOST)
							{
								throw new UsbException("GET_DESCRIPTOR only supports DEVICE_TO_HOST");
							}
							final IUsbStringDescriptor stringDescriptor = getUsbDevice().doGetUsbStringDescriptor(stringIndex);
							final int actualLength = stringDescriptorToBuffer(irp.getData(), stringDescriptor); // TODO: We should support multi-language
							irp.setActualLength(actualLength);
							LOGGER.info("DEVICE GET_DESCRIPTOR string descriptor request complete, sent \"{}\" of {} bytes", stringDescriptor.getString(), actualLength);
							callback.onTransferComplete(actualLength);
							return;
						}
					}
					case DEBUG:
						LOGGER.info("DEVICE GET_DESCRIPTOR debug descriptor request");
						if (bmRequestType.getDirection() != EEndpointDirection.DEVICE_TO_HOST)
						{
							throw new UsbException("GET_DESCRIPTOR only supports DEVICE_TO_HOST");
						}
						irp.setActualLength(0);
						LOGGER.info("DEVICE GET_DESCRIPTOR debug descriptor request complete, sent {} bytes", irp.getActualLength());
						callback.onTransferComplete(0);
						return;
					default:
						throw new UnsupportedOperationException("BMRequestType Standard recipient DEVICE request GET_DESCRIPTOR type " + EDescriptorType.fromBytecode((byte) (irp.wValue() >> 8)) + " not implemented in mock");
					}
				case SET_CONFIGURATION:
				{
					LOGGER.info("DEVICE SET_CONFIGURATION request for configuration index {} (active configuration currently is {})", irp.wValue(), getUsbDevice().getActiveUsbConfigurationNumber());
					getUsbDevice().setActiveUsbConfigurationNumber((byte) irp.wValue());
					callback.onTransferComplete(0);
					return;
				}
				case GET_STATUS:
				{
					LOGGER.info("DEVICE GET_STATUS request");
					if (bmRequestType.getDirection() != EEndpointDirection.DEVICE_TO_HOST)
					{
						throw new UsbException("GET_STATUS only supports DEVICE_TO_HOST");
					}
					final byte[] buffer = irp.getData();
					getUsbDevice().getActiveUsbConfiguration().getUsbConfigurationDescriptor().bmAttributes();
					buffer[1] = 0;
					irp.setActualLength(2);
					LOGGER.info("DEVICE GET_STATUS request complete, sent {} bytes", irp.getActualLength());
					callback.onTransferComplete(2);
					return;
				}
				case GET_CONFIGURATION:
				{
					LOGGER.info("DEVICE GET_CONFIGURATION request");
					if (bmRequestType.getDirection() != EEndpointDirection.DEVICE_TO_HOST)
					{
						throw new UsbException("GET_CONFIGURATION only supports DEVICE_TO_HOST");
					}
					irp.getData()[0] = getUsbDevice().getActiveUsbConfigurationNumber();
					irp.setActualLength(1);
					LOGGER.info("DEVICE GET_CONFIGURATION request complete, sent {} bytes", irp.getActualLength());
					callback.onTransferComplete(1);
					return;
				}
				default:
					throw new UnsupportedOperationException("BMRequestType Standard recipient DEVICE request " + request.getDeviceRequest() + " not implemented in mock");
				}
			case INTERFACE:
			case ENDPOINT:
			case OTHER:
			default:
				throw new UnsupportedOperationException("BMRequestType STANDARD recipient " + bmRequestType.getRecipient() + " not implemented in mock");
			}
		case VENDOR:
		{
			LOGGER.info("Handling VENDOR specific control transfer");
			getUsbDevice().doVendorSpecificControlTransfer(irp);
			callback.onTransferComplete(irp.getActualLength());
			return;
		}
		case CLASS:
			// When you hit this, you need to use a class specific UsbControlIrpQueue extension.
			throw new UnsupportedOperationException("BMRequestType CLASS not implemented in mock");
		default:
			throw new UnsupportedOperationException("Unknown BMRequestType " + bmRequestType.getType() + " not implemented in mock");
		}
	}

	private static int deviceDescriptorToBuffer(final byte[] buffer, final IUsbDeviceDescriptor deviceDescriptor)
	{
		final ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		byteBuffer.put(deviceDescriptor.bLength());
		byteBuffer.put(deviceDescriptor.bDescriptorType());
		byteBuffer.putShort(deviceDescriptor.bcdUSB());
		byteBuffer.put(deviceDescriptor.bDeviceClass());
		byteBuffer.put(deviceDescriptor.bDeviceSubClass());
		byteBuffer.put(deviceDescriptor.bDeviceProtocol());
		byteBuffer.put(deviceDescriptor.bMaxPacketSize0());
		byteBuffer.putShort(deviceDescriptor.idVendor());
		byteBuffer.putShort(deviceDescriptor.idProduct());
		byteBuffer.putShort(deviceDescriptor.bcdDevice());
		byteBuffer.put(deviceDescriptor.iManufacturer());
		byteBuffer.put(deviceDescriptor.iProduct());
		byteBuffer.put(deviceDescriptor.iSerialNumber());
		byteBuffer.put(deviceDescriptor.bNumConfigurations());
		return byteBuffer.position();
	}

	private static int configurationDescriptorToBuffer(final byte[] buffer, final IUsbConfiguration usbConfiguration)
	{
		final ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		final IUsbConfigurationDescriptor usbConfigurationDescriptor = usbConfiguration.getUsbConfigurationDescriptor();
		byteBuffer.put(usbConfigurationDescriptor.bLength());
		byteBuffer.put(usbConfigurationDescriptor.bDescriptorType());
		byteBuffer.putShort(usbConfigurationDescriptor.wTotalLength());
		byteBuffer.put(usbConfigurationDescriptor.bNumInterfaces());
		byteBuffer.put(usbConfigurationDescriptor.bConfigurationValue());
		byteBuffer.put(usbConfigurationDescriptor.iConfiguration());
		byteBuffer.put(usbConfigurationDescriptor.bmAttributes());
		byteBuffer.put(usbConfigurationDescriptor.bMaxPower());

		if (byteBuffer.hasRemaining())
		{
			for (IUsbInterface usbInterface : usbConfiguration.getUsbInterfaces())
			{
				final IUsbInterfaceDescriptor usbInterfaceDescriptor = usbInterface.getUsbInterfaceDescriptor();
				byteBuffer.put(usbInterfaceDescriptor.bLength());
				byteBuffer.put(usbInterfaceDescriptor.bDescriptorType());
				byteBuffer.put(usbInterfaceDescriptor.bInterfaceNumber());
				byteBuffer.put(usbInterfaceDescriptor.bAlternateSetting());
				byteBuffer.put(usbInterfaceDescriptor.bNumEndpoints());
				byteBuffer.put(usbInterfaceDescriptor.bInterfaceClass());
				byteBuffer.put(usbInterfaceDescriptor.bInterfaceSubClass());
				byteBuffer.put(usbInterfaceDescriptor.bInterfaceProtocol());
				byteBuffer.put(usbInterfaceDescriptor.iInterface());

				for (byte[] classSpecificDescriptor : usbInterface.getClassSpecificDescriptors())
				{
					byteBuffer.put(classSpecificDescriptor);
				}

				for (IUsbEndpoint usbEndpoint : usbInterface.getUsbEndpoints())
				{
					final IUsbEndpointDescriptor usbEndpointDescriptor = usbEndpoint.getUsbEndpointDescriptor();
					byteBuffer.put(usbEndpointDescriptor.bLength());
					byteBuffer.put(usbEndpointDescriptor.bDescriptorType());
					byteBuffer.put(usbEndpointDescriptor.bEndpointAddress());
					byteBuffer.put(usbEndpointDescriptor.bmAttributes());
					byteBuffer.putShort(usbEndpointDescriptor.wMaxPacketSize());
					byteBuffer.put(usbEndpointDescriptor.bInterval());

					for (byte[] classSpecificDescriptor : usbEndpoint.getClassSpecificDescriptors())
					{
						byteBuffer.put(classSpecificDescriptor);
					}
				}
			}
		}

		return byteBuffer.position();
	}

	private static int languagesStringDescriptorToBuffer(final byte[] buffer, final short[] languages)
	{
		final ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		byteBuffer.put((byte) (Byte.BYTES + Byte.BYTES + languages.length * Short.BYTES));
		byteBuffer.put(EDescriptorType.STRING.getByteCode());
		for (final short language : languages)
		{
			byteBuffer.putShort(language);
		}
		return byteBuffer.position();
	}

	private static int stringDescriptorToBuffer(final byte[] buffer, final IUsbStringDescriptor stringDescriptor)
	{
		final byte[] stringInBytes = stringDescriptor.bString();
		final ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		byteBuffer.put((byte) (Byte.BYTES + Byte.BYTES + stringInBytes.length));
		byteBuffer.put(stringDescriptor.bDescriptorType());
		byteBuffer.put(stringInBytes, 0, Math.min(byteBuffer.remaining(), stringInBytes.length));
		return byteBuffer.position();
	}
}
