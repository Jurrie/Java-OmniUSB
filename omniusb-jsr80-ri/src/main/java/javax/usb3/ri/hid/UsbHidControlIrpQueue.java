package javax.usb3.ri.hid;

import java.lang.invoke.MethodHandles;

import javax.usb3.IUsbControlIrp;
import javax.usb3.IUsbInterface;
import javax.usb3.exception.UsbException;
import javax.usb3.request.BMRequestType;
import javax.usb3.request.BRequest;
import javax.usb3.ri.AUsbDevice;
import javax.usb3.ri.ProcessIrpCallback;
import javax.usb3.ri.UsbControlIrpQueue;
import javax.usb3.ri.UsbDeviceListenerList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsbHidControlIrpQueue extends UsbControlIrpQueue
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final byte SET_IDLE = 0x0A;
	private static final byte REPORT_DESCRIPTOR_TYPE = 0x22;

	public UsbHidControlIrpQueue(final AUsbDevice device, final UsbDeviceListenerList listeners)
	{
		super(device, listeners);
	}

	@Override
	protected void doControlTransfer(IUsbControlIrp irp, final ProcessIrpCallback callback) throws UsbException
	{
		final BMRequestType bmRequestType = new BMRequestType(irp.bmRequestType());
		final BRequest request = new BRequest(irp.bRequest());

		switch (bmRequestType.getType())
		{
		case STANDARD:
			switch (bmRequestType.getRecipient())
			{
			case INTERFACE:
				switch (request.getDeviceRequest())
				{
				case GET_DESCRIPTOR:
					switch ((byte) (irp.wValue() >> 8))
					{
					case REPORT_DESCRIPTOR_TYPE:
					{
						LOGGER.info("INTERFACE GET_HID_REPORT request received for REPORT_DESCRIPTOR_TYPE");

						final byte interfaceNumber = (byte) (irp.wValue() & 0xff);
						final byte reportDescriptorNumber = (byte) irp.wIndex();

						LOGGER.info("Interface Number: {}, Report Descriptor Number: {}", interfaceNumber, reportDescriptorNumber);

						final IUsbInterface usbInterface = getUsbDevice().getActiveUsbConfiguration().getUsbInterface(interfaceNumber);
						if (!(usbInterface instanceof UsbHidInterface usbHidInterface))
						{
							throw new UsbException("Requested interface is not a HID interface");
						}

						final byte[] reportDescriptor = usbHidInterface.getHidDescriptor(reportDescriptorNumber).getDescriptorData();
						irp.setData(reportDescriptor);
						irp.setActualLength(reportDescriptor.length);

						callback.onTransferComplete(irp.getActualLength());
						return;
					}
					}
				}
			}
			break;
		case CLASS: // Handle HID class requests
			switch (bmRequestType.getRecipient())
			{
			case INTERFACE:
				switch (request.getDeviceRequest().getByteCode())
				{
				case SET_IDLE:
				{
					LOGGER.info("INTERFACE SET_IDLE request received, duration: {}, report ID: {}", irp.wValue() >> 8, irp.wValue() & 0xff);
					// TODO: Implement idle handling

					// https://blog.mshafeeq.com/index.php/2024/11/17/implementing-usb-as-a-custom-hid-device-using-stm32-part-2/
					// wValue: 00, 00 - High byte: Idle duration (0 for infinite). Low byte: Report ID (0 for all).
					// wIndex: 00, 00 - Interface number (usually 0 for single interface devices).
					// wLength: 00, 00 - No data expected
					callback.onTransferComplete(0);
					return;
				}
				}
			}
		}

		super.doControlTransfer(irp, callback);
	}
}
