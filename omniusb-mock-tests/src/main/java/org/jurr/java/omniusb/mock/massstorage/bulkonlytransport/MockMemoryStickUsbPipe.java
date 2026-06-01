package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport;

import javax.usb3.ri.UsbEndpoint;
import javax.usb3.ri.UsbIrpQueue;
import javax.usb3.ri.UsbPipe;

public class MockMemoryStickUsbPipe extends UsbPipe
{
	protected MockMemoryStickUsbPipe(final UsbEndpoint endpoint)
	{
		super(endpoint);
	}

	@Override
	protected UsbIrpQueue createIrpQueue()
	{
		return new MockMemoryStickUsbIrpQueue(this);
	}
}
