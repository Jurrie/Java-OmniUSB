package org.jurr.java.omniusb.usbip.domain;

import static org.jurr.java.omniusb.usbip.Constants.OP_REP_IMPORT;
import static org.jurr.java.omniusb.usbip.Constants.RESPONSE_BUFFER_DEVICE_DETAILS_SIZE;
import static org.jurr.java.omniusb.usbip.Constants.RESPONSE_BUFFER_HEADER_SIZE;
import static org.jurr.java.omniusb.usbip.Constants.USBIP_RET_SUCCESS;

import java.nio.ByteBuffer;

public class ImportDeviceResponse extends UsbIpManagementResponse
{
	private final UsbDeviceDetails attachedDevice;

	public ImportDeviceResponse(final UsbDeviceDetails attachedDevice)
	{
		super(OP_REP_IMPORT, USBIP_RET_SUCCESS);
		this.attachedDevice = attachedDevice;
	}

	public UsbDeviceDetails getAttachedDevice()
	{
		return attachedDevice;
	}

	public byte[] toBuffer()
	{
		// Calculate required buffer size
		int responseBufferSize = RESPONSE_BUFFER_HEADER_SIZE;
		responseBufferSize += RESPONSE_BUFFER_DEVICE_DETAILS_SIZE;

		final ByteBuffer responseBuffer = ByteBuffer.allocate(responseBufferSize);
		putHeader(responseBuffer);
		putUsbDeviceDetails(responseBuffer, attachedDevice, false);

		return responseBuffer.array();
	}

	public static byte[] error(final int errorCode)
	{
		return new ErrorResponse(OP_REP_IMPORT, errorCode).toBuffer();
	}
}
