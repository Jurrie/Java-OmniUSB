package javax.usb3.usb4java;

import java.util.stream.Stream;

import javax.usb3.IUsbInterfaceDescriptor;
import javax.usb3.descriptor.UsbEndpointDescriptor;
import javax.usb3.descriptor.UsbInterfaceDescriptor;
import javax.usb3.enumerated.EUSBClassCode;
import javax.usb3.ri.UsbInterface;

import org.usb4java.EndpointDescriptor;
import org.usb4java.InterfaceDescriptor;

public class Usb4JavaUsbInterface extends UsbInterface
{
	public Usb4JavaUsbInterface(final Usb4JavaUsbConfiguration configuration, final InterfaceDescriptor descriptor)
	{
		super(configuration, convertDescriptor(descriptor));

		for (EndpointDescriptor endpointDescriptor : descriptor.endpoint())
		{
			final Usb4JavaUsbEndpoint endpoint = new Usb4JavaUsbEndpoint(this, endpointDescriptor);
			addUsbEndpoint(endpoint);
		}
	}

	static IUsbInterfaceDescriptor convertDescriptor(final InterfaceDescriptor input)
	{
		return new UsbInterfaceDescriptor(
				input.bInterfaceNumber(),
				input.bAlternateSetting(),
				input.bNumEndpoints(),
				EUSBClassCode.fromByteCode(input.bInterfaceClass()),
				input.bInterfaceSubClass(),
				input.bInterfaceProtocol(),
				input.iInterface(),
				Stream.of(input.endpoint()).map(Usb4JavaUsbEndpoint::convertDescriptor).toArray(UsbEndpointDescriptor[]::new));
	}
}
