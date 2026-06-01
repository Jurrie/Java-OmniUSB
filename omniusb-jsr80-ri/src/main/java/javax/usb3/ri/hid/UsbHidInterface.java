package javax.usb3.ri.hid;

import java.util.Collections;
import java.util.List;

import javax.usb3.IUsbConfiguration;
import javax.usb3.ri.UsbInterface;

public abstract class UsbHidInterface extends UsbInterface
{
	public UsbHidInterface(final IUsbConfiguration configuration, final UsbHidInterfaceDescriptor descriptor)
	{
		super(configuration, descriptor);
	}

	@Override
	public List<byte[]> getClassSpecificDescriptors()
	{
		return Collections.singletonList(getHIDDescriptor());
	}

	public byte getHidCountryCode()
	{
		return 0x00; // Not localized
	}

	protected abstract byte[] getHIDDescriptor();

	protected abstract byte getNumberOfHidDescriptors();

	protected abstract UsbHidDescriptor getHidDescriptor(byte hidDescriptorNumber);
}
