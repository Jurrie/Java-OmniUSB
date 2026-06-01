package javax.usb3.usb4java;

import javax.usb3.ri.UsbEndpoint;
import javax.usb3.ri.UsbIrpQueue;
import javax.usb3.ri.UsbPipe;

public class Usb4JavaUsbPipe extends UsbPipe
{
	protected Usb4JavaUsbPipe(final UsbEndpoint endpoint)
	{
		super(endpoint);
	}

	@Override
	protected UsbIrpQueue createIrpQueue()
	{
		return new Usb4JavaUsbIrpQueue(this);
	}
}
