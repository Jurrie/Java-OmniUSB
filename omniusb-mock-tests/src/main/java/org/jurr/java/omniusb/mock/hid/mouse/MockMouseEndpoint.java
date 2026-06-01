package org.jurr.java.omniusb.mock.hid.mouse;

import javax.usb3.IUsbPipe;
import javax.usb3.descriptor.UsbEndpointDescriptor;
import javax.usb3.enumerated.EDataFlowtype;
import javax.usb3.enumerated.EEndpointDirection;
import javax.usb3.request.BEndpointAddress;
import javax.usb3.ri.UsbEndpoint;
import javax.usb3.ri.UsbIrpQueue;
import javax.usb3.ri.UsbPipe;

public class MockMouseEndpoint extends UsbEndpoint
{
	protected MockMouseEndpoint(final MockMouseInterface usbInterface)
	{
		super(usbInterface, new UsbEndpointDescriptor(new BEndpointAddress(1, EEndpointDirection.DEVICE_TO_HOST), EDataFlowtype.INTERRUPT.getByteCode(), (short) 64, (byte) 1));
	}

	@Override
	protected IUsbPipe createUsbPipe()
	{
		return new UsbPipe(this)
		{
			@Override
			protected UsbIrpQueue createIrpQueue()
			{
				return new MockMouseIrpQueue(this);
			}
		};
	}
}
