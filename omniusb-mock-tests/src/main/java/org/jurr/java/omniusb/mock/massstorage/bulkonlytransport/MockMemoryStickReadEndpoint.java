package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport;

import javax.usb3.IUsbPipe;
import javax.usb3.descriptor.UsbEndpointDescriptor;
import javax.usb3.enumerated.EDataFlowtype;
import javax.usb3.enumerated.EEndpointDirection;
import javax.usb3.request.BEndpointAddress;
import javax.usb3.ri.UsbEndpoint;

public class MockMemoryStickReadEndpoint extends UsbEndpoint
{
	public MockMemoryStickReadEndpoint(final MockMemoryStickInterface usbInterface)
	{
		super(usbInterface, new UsbEndpointDescriptor(new BEndpointAddress(2, EEndpointDirection.DEVICE_TO_HOST), EDataFlowtype.BULK.getByteCode(), (short) 512, (byte) 0));
	}

	@Override
	protected IUsbPipe createUsbPipe()
	{
		return new MockMemoryStickUsbPipe(this);
	}
}
