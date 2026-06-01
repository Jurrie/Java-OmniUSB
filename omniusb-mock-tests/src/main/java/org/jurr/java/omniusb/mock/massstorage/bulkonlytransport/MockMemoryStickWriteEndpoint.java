package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport;

import javax.usb3.IUsbPipe;
import javax.usb3.descriptor.UsbEndpointDescriptor;
import javax.usb3.enumerated.EDataFlowtype;
import javax.usb3.enumerated.EEndpointDirection;
import javax.usb3.request.BEndpointAddress;
import javax.usb3.ri.UsbEndpoint;

public class MockMemoryStickWriteEndpoint extends UsbEndpoint
{
	public MockMemoryStickWriteEndpoint(final MockMemoryStickInterface usbInterface)
	{
		super(usbInterface, new UsbEndpointDescriptor(new BEndpointAddress(3, EEndpointDirection.HOST_TO_DEVICE), EDataFlowtype.BULK.getByteCode(), (short) 512, (byte) 0));
	}

	@Override
	protected IUsbPipe createUsbPipe()
	{
		return new MockMemoryStickUsbPipe(this);
	}
}
