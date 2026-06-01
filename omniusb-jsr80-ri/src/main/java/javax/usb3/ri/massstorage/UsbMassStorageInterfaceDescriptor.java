package javax.usb3.ri.massstorage;

import javax.usb3.IUsbEndpointDescriptor;
import javax.usb3.descriptor.UsbInterfaceDescriptor;
import javax.usb3.enumerated.EUSBClassCode;

public class UsbMassStorageInterfaceDescriptor extends UsbInterfaceDescriptor
{
	public UsbMassStorageInterfaceDescriptor(final byte bInterfaceNumber, final byte bAlternateSetting, final byte bNumEndpoints, final EMassStorageSubclass bInterfaceSubClass, final EMassStorageProtocol bInterfaceProtocol, final byte iInterface, final IUsbEndpointDescriptor[] endpoint)
	{
		super(bInterfaceNumber, bAlternateSetting, bNumEndpoints, EUSBClassCode.MASS_STORAGE, bInterfaceSubClass.getByteCode(), bInterfaceProtocol.getByteCode(), iInterface, endpoint);
	}
}
