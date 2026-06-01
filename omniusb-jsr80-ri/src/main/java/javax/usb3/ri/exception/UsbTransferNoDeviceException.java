package javax.usb3.ri.exception;

import javax.usb3.exception.UsbException;

public class UsbTransferNoDeviceException extends UsbException
{
	private static final long serialVersionUID = 1L;

	public UsbTransferNoDeviceException()
	{
	}

	public UsbTransferNoDeviceException(final String s)
	{
		super(s);
	}
}
