package javax.usb3.usb4java;

import javax.usb3.exception.UsbException;

import org.usb4java.DeviceHandle;

public interface Usb4JavaAUsbDevice
{
	DeviceHandle open() throws UsbException;

	void close();
}
