package javax.usb3.ri.hid;

import javax.usb3.exception.UsbPlatformException;
import javax.usb3.ri.UsbControlIrpQueue;
import javax.usb3.ri.UsbDevice;
import javax.usb3.ri.UsbDeviceId;
import javax.usb3.ri.UsbDeviceListenerList;

public abstract class UsbHidDevice extends UsbDevice
{
	public UsbHidDevice(final UsbDeviceId deviceId, final UsbDeviceId parentId, final int speed) throws UsbPlatformException
	{
		super(deviceId, parentId, speed);
	}

	@Override
	protected UsbControlIrpQueue createUsbControlIrpQueue(final UsbDeviceListenerList listener)
	{
		return new UsbHidControlIrpQueue(this, getListeners());
	}
}
