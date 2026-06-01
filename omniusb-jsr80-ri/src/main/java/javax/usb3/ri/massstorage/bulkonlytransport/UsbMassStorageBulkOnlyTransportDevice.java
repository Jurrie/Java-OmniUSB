package javax.usb3.ri.massstorage.bulkonlytransport;

import javax.usb3.exception.UsbPlatformException;
import javax.usb3.ri.UsbControlIrpQueue;
import javax.usb3.ri.UsbDevice;
import javax.usb3.ri.UsbDeviceId;
import javax.usb3.ri.UsbDeviceListenerList;

public abstract class UsbMassStorageBulkOnlyTransportDevice extends UsbDevice
{
	public UsbMassStorageBulkOnlyTransportDevice(final UsbDeviceId deviceId, final UsbDeviceId parentId, final int speed) throws UsbPlatformException
	{
		super(deviceId, parentId, speed);
	}

	@Override
	protected UsbControlIrpQueue createUsbControlIrpQueue(final UsbDeviceListenerList listener)
	{
		return new UsbMassStorageBulkOnlyTransportControlIrpQueue(this, getListeners());
	}

	/**
	 * The device may implement several logical units that share common device characteristics.
	 * The host uses bCBWLUN (see 5.1 Command Block Wrapper (CBW)) to designate which logical unit of the device is the destination of the CBW.
	 * The Get Max LUN device request is used to determine the number of logical units supported by the device.
	 * Logical Unit Numbers on the device shall be numbered contiguously starting from LUN 0 to a maximum LUN of 15 (Fh).
	 *
	 * @return the maximum LUN number supported by the device (0 to 15)
	 */
	protected abstract byte getMaxLUN();

	/**
	 * This request is used to reset the mass storage device and its associated interface.
	 * This class-specific request shall ready the device for the next CBW from the host.
	 * The host shall send this request via the default pipe to the device.
	 * The device shall preserve the value of its bulk data toggle bits and endpoint STALL conditions despite the Bulk-Only Mass Storage Reset.
	 * The device shall NAK the status stage of the device request until the Bulk-Only Mass Storage Reset is complete.
	 */
	protected abstract void reset();
}
