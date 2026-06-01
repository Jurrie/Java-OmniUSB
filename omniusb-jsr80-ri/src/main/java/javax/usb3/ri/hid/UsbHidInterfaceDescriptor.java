package javax.usb3.ri.hid;

import javax.usb3.IUsbEndpointDescriptor;
import javax.usb3.descriptor.UsbInterfaceDescriptor;
import javax.usb3.enumerated.EUSBClassCode;

public class UsbHidInterfaceDescriptor extends UsbInterfaceDescriptor
{
	public UsbHidInterfaceDescriptor(final byte bInterfaceNumber, final byte bAlternateSetting, final byte bNumEndpoints, final EHidSubclass bInterfaceSubClass, final EHidProtocol bInterfaceProtocol, final byte iInterface, final IUsbEndpointDescriptor[] endpoint)
	{
		super(bInterfaceNumber, bAlternateSetting, bNumEndpoints, EUSBClassCode.HID_HUMAN_INTERFACE_DEVICE, bInterfaceSubClass.getByteCode(), bInterfaceProtocol.getByteCode(), iInterface, endpoint);
	}
}
