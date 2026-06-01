package javax.usb3.ri.exception;

import javax.usb3.exception.UsbException;

public class UsbControlRequestNotSupportedException extends UsbException
{
	private static final long serialVersionUID = 1L;

	public UsbControlRequestNotSupportedException()
	{
	}

	public UsbControlRequestNotSupportedException(final String s)
	{
		super(s);
	}
}
