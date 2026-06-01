package org.jurr.java.omniusb.usbip.domain;

import static org.jurr.java.omniusb.usbip.Constants.USBIP_VERSION;

import java.nio.ByteBuffer;

import org.jurr.java.omniusb.usbip.ByteBufferUtils;
import org.jurr.java.omniusb.usbip.domain.UsbDeviceDetails.Speed;
import org.jurr.java.omniusb.usbip.domain.UsbDeviceDetails.UsbInterfaceDetails;

/**
 * Base class for all USB/IP responses that deal with connection / management of devices. I.e. not with actual USB communication.
 */
abstract class UsbIpManagementResponse
{
	private final int commandCode;
	private final int status;

	protected UsbIpManagementResponse(final short replyCode, final int status)
	{
		this(USBIP_VERSION << Short.BYTES * 8 | replyCode, status);
	}

	protected UsbIpManagementResponse(final int commandCode, final int status)
	{
		this.commandCode = commandCode;
		this.status = status;
	}

	public int getCommandCode()
	{
		return commandCode;
	}

	public int getStatus()
	{
		return status;
	}

	protected void putHeader(final ByteBuffer responseBuffer)
	{
		responseBuffer.putInt(commandCode);
		responseBuffer.putInt(status);
	}

	protected void putUsbDeviceDetails(final ByteBuffer responseBuffer, final UsbDeviceDetails usbDevice, final boolean includeInterfaceDetails)
	{
		ByteBufferUtils.putAsciiString(responseBuffer, usbDevice.getPath(), 256);
		ByteBufferUtils.putAsciiString(responseBuffer, usbDevice.getBusId(), 32);
		responseBuffer.putInt(usbDevice.getBusNumber());
		responseBuffer.putInt(usbDevice.getPortNumber());
		responseBuffer.putInt(usbDevice.getPortSpeed().getValue());
		responseBuffer.putShort(usbDevice.getIdVendor());
		responseBuffer.putShort(usbDevice.getIdProduct());
		responseBuffer.putShort(usbDevice.getBcdDevice());
		responseBuffer.put(usbDevice.getBDeviceClass());
		responseBuffer.put(usbDevice.getBDeviceSubClass());
		responseBuffer.put(usbDevice.getBDeviceProtocol());
		responseBuffer.put(usbDevice.getBConfigurationValue());
		responseBuffer.put(usbDevice.getBNumConfigurations());
		responseBuffer.put(usbDevice.getBNumInterfaces());

		if (includeInterfaceDetails)
		{
			for (UsbInterfaceDetails usbInterface : usbDevice.getInterfaceDetails())
			{
				responseBuffer.put(usbInterface.getBInterfaceClass());
				responseBuffer.put(usbInterface.getBInterfaceSubClass());
				responseBuffer.put(usbInterface.getBInterfaceProtocol());
				responseBuffer.put((byte) 0x00); // padding
			}
		}
	}

	protected static UsbDeviceDetails getUsbDeviceDetails(final ByteBuffer buffer)
	{
		final String path = ByteBufferUtils.getAsciiString(buffer, 256);
		final String busId = ByteBufferUtils.getAsciiString(buffer, 32);
		final int busNumber = buffer.getInt();
		final int portNumber = buffer.getInt();
		final Speed portSpeed = Speed.fromValue(buffer.getInt());
		final short idVendor = buffer.getShort();
		final short idProduct = buffer.getShort();
		final short bcdDevice = buffer.getShort();
		final byte bDeviceClass = buffer.get();
		final byte bDeviceSubClass = buffer.get();
		final byte bDeviceProtocol = buffer.get();
		final byte bConfigurationValue = buffer.get();
		final byte bNumConfigurations = buffer.get();
		final byte bNumInterfaces = buffer.get();

		return new UsbDeviceDetails(path, busId, busNumber, portNumber, portSpeed, idVendor, idProduct, bcdDevice, bDeviceClass, bDeviceSubClass, bDeviceProtocol, bConfigurationValue, bNumConfigurations, bNumInterfaces);
	}
}
