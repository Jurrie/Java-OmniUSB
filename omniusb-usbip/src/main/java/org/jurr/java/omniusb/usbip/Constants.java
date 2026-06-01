package org.jurr.java.omniusb.usbip;

public final class Constants
{
	public static final short USBIP_VERSION = 0x111;

	// These are request and replies that are prepended with the USBIP_VERSION
	public static final short OP_REQ_DEVLIST = (short) 0x8005;
	public static final short OP_REP_DEVLIST = (short) 0x0005;
	public static final short OP_REQ_IMPORT = (short) 0x8003;
	public static final short OP_REP_IMPORT = (short) 0x0003;

	// These are requests and replies that are NOT prepended with the USBIP_VERSION
	// Defined in drivers/usb/usbip/usbip_common.h
	public static final int USBIP_CMD_SUBMIT = 0x00000001;
	public static final int USBIP_CMD_UNLINK = 0x00000002;
	public static final int USBIP_RET_SUBMIT = 0x00000003;
	public static final int USBIP_RET_UNLINK = 0x00000004;

	public static final int RESPONSE_BUFFER_HEADER_SIZE = Short.BYTES /* USBIP version */ + Short.BYTES /* command or reply code */ + Integer.BYTES /* status (error) code */;
	public static final int RESPONSE_BUFFER_DEVICE_DETAILS_SIZE = 256 /* Path */ + 32 /* Busid */ + Integer.BYTES /* busnum */ + Integer.BYTES /* devnum */ + Integer.BYTES /* speed */ + Short.BYTES /* idVendor */ + Short.BYTES /* idProduct */ + Short.BYTES /* bcdDevice */ + Byte.BYTES /* bDeviceClass */ + Byte.BYTES /* bDeviceSubClass */ + Byte.BYTES /* bDeviceProtocol */ + Byte.BYTES /* bConfigurationValue */ + Byte.BYTES /* bNumConfigurations */ + Byte.BYTES /* bNumInterfaces */;
	public static final int RESPONSE_BUFFER_INTERFACE_DETAILS_SIZE = Byte.BYTES /* bInterfaceClass */ + Byte.BYTES /* bInterfaceSubClass */ + Byte.BYTES /* bInterfaceProtocol */ + Byte.BYTES /* padding */;

	public static final short USBIP_RET_SUCCESS = 0x00;
	public static final short USBIP_RET_FAILED = 0x01; // Client says "request failed"
	public static final short USBIP_RET_DEV_BUSY = 0x02; // Client says "device is busy (exported)"
	public static final short USBIP_RET_ERROR_STATE = 0x03; // Client says "device in error state"
	public static final short USBIP_RET_DEV_NOT_FOUND = 0x04; // Client says "device not found"
	// public static final short USBIP_RET_UNEXPECTED = 0x05; // Client says "unexpected response"

	private Constants()
	{
	}
}
