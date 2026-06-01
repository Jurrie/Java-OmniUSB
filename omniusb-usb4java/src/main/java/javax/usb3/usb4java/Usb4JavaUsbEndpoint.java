package javax.usb3.usb4java;

import javax.usb3.IUsbEndpointDescriptor;
import javax.usb3.IUsbPipe;
import javax.usb3.descriptor.UsbEndpointDescriptor;
import javax.usb3.request.BEndpointAddress;
import javax.usb3.ri.UsbEndpoint;

import org.usb4java.EndpointDescriptor;

public class Usb4JavaUsbEndpoint extends UsbEndpoint
{
	public Usb4JavaUsbEndpoint(final Usb4JavaUsbInterface usbInterface, final EndpointDescriptor descriptor)
	{
		super(usbInterface, convertDescriptor(descriptor));
	}

	@Override
	protected IUsbPipe createUsbPipe()
	{
		return new Usb4JavaUsbPipe(this);
	}

	static IUsbEndpointDescriptor convertDescriptor(final EndpointDescriptor input)
	{
		return new UsbEndpointDescriptor(new BEndpointAddress(input.bEndpointAddress()), input.bmAttributes(), input.wMaxPacketSize(), input.bInterval());
	}
}
