package javax.usb3.ri.massstorage.bulkonlytransport;

import javax.usb3.IUsbControlIrp;
import javax.usb3.exception.UsbException;
import javax.usb3.request.BMRequestType;
import javax.usb3.request.BRequest;
import javax.usb3.ri.ProcessIrpCallback;
import javax.usb3.ri.UsbControlIrpQueue;
import javax.usb3.ri.UsbDeviceListenerList;

public class UsbMassStorageBulkOnlyTransportControlIrpQueue extends UsbControlIrpQueue
{
	private static final byte BULK_ONLY_MASS_STORAGE_RESET = (byte) 0xFF;
	private static final byte GET_MAX_LUN = (byte) 0xFE;

	public UsbMassStorageBulkOnlyTransportControlIrpQueue(final UsbMassStorageBulkOnlyTransportDevice device, final UsbDeviceListenerList listeners)
	{
		super(device, listeners);
	}

	@Override
	protected UsbMassStorageBulkOnlyTransportDevice getUsbDevice()
	{
		return (UsbMassStorageBulkOnlyTransportDevice) super.getUsbDevice();
	}

	@Override
	protected void doControlTransfer(final IUsbControlIrp irp, final ProcessIrpCallback callback) throws UsbException
	{
		final BMRequestType bmRequestType = new BMRequestType(irp.bmRequestType());
		final BRequest request = new BRequest(irp.bRequest());

		switch (bmRequestType.getType())
		{
		case CLASS: // Handle mass-storage bulk-only transport class requests
			switch (bmRequestType.getRecipient())
			{
			case INTERFACE:
				switch (request.getByteCode())
				{
				case BULK_ONLY_MASS_STORAGE_RESET:
				{
					getUsbDevice().reset();
					irp.setActualLength(0);
					callback.onTransferComplete(irp.getActualLength());
					return;
				}
				case GET_MAX_LUN:
				{
					final byte maxLun = getUsbDevice().getMaxLUN();
					irp.setData(new byte[] { maxLun });
					irp.setActualLength(1);
					callback.onTransferComplete(irp.getActualLength());
					return;
				}
				}
			}
		}

		super.doControlTransfer(irp, callback);
	}
}
