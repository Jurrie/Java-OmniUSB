package javax.usb3.ri.massstorage;

import javax.usb3.IUsbConfiguration;
import javax.usb3.ri.UsbInterface;

public abstract class UsbMassStorageInterface extends UsbInterface
{
	public UsbMassStorageInterface(final IUsbConfiguration configuration, final UsbMassStorageInterfaceDescriptor descriptor)
	{
		super(configuration, descriptor);
	}
}
