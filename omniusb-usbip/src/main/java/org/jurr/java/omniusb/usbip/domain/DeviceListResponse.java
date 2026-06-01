package org.jurr.java.omniusb.usbip.domain;

import static org.jurr.java.omniusb.usbip.Constants.OP_REP_DEVLIST;
import static org.jurr.java.omniusb.usbip.Constants.RESPONSE_BUFFER_DEVICE_DETAILS_SIZE;
import static org.jurr.java.omniusb.usbip.Constants.RESPONSE_BUFFER_HEADER_SIZE;
import static org.jurr.java.omniusb.usbip.Constants.RESPONSE_BUFFER_INTERFACE_DETAILS_SIZE;
import static org.jurr.java.omniusb.usbip.Constants.USBIP_RET_SUCCESS;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.jurr.java.omniusb.usbip.Constants;

public class DeviceListResponse extends UsbIpManagementResponse
{
	private final List<UsbDeviceDetails> devices;

	public DeviceListResponse(final List<UsbDeviceDetails> devices)
	{
		super(Constants.OP_REP_DEVLIST, USBIP_RET_SUCCESS);
		this.devices = devices;
	}

	public List<UsbDeviceDetails> getDevices()
	{
		return devices;
	}

	public byte[] toBuffer()
	{
		// Calculate required buffer size
		int responseBufferSize = RESPONSE_BUFFER_HEADER_SIZE;
		responseBufferSize += Integer.BYTES; // Number of exported devices
		for (UsbDeviceDetails device : devices)
		{
			responseBufferSize += RESPONSE_BUFFER_DEVICE_DETAILS_SIZE;
			responseBufferSize += device.getBNumInterfaces() * RESPONSE_BUFFER_INTERFACE_DETAILS_SIZE;
		}

		final ByteBuffer responseBuffer = ByteBuffer.allocate(responseBufferSize);
		putHeader(responseBuffer);

		responseBuffer.putInt(devices.size()); // Number of exported devices
		for (UsbDeviceDetails device : devices)
		{
			putUsbDeviceDetails(responseBuffer, device, true);
		}

		return responseBuffer.array();
	}

	public static byte[] error(final int errorCode)
	{
		return new ErrorResponse(OP_REP_DEVLIST, errorCode).toBuffer();
	}

	public static DeviceListResponse fromBuffer(final ByteBuffer buffer)
	{
		final short usbIpVersion = buffer.getShort();
		if (usbIpVersion != Constants.USBIP_VERSION)
		{
			throw new IllegalArgumentException("Server is USB/IP version " + usbIpVersion + " but we only support " + Constants.USBIP_VERSION);
		}

		final short replyCode = buffer.getShort();
		if (replyCode != Constants.OP_REP_DEVLIST)
		{
			throw new IllegalArgumentException("Server sent the wrong USB/IP response. Expected " + OP_REP_DEVLIST + " but got " + replyCode);
		}

		final int status = buffer.getInt();
		if (status != USBIP_RET_SUCCESS)
		{
			throw new IllegalArgumentException("Server sent an error status: " + status);
		}

		final int numberOfDevices = buffer.getInt();
		final List<UsbDeviceDetails> devices = new ArrayList<>(numberOfDevices);
		for (int i = 0; i < numberOfDevices; i++)
		{
			devices.add(getUsbDeviceDetails(buffer));
		}

		return new DeviceListResponse(devices);
	}
}
