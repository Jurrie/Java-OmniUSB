package javax.usb3.ri.exception;

import javax.usb3.exception.UsbException;

public class UsbTransferTimeoutException extends UsbException
{
	private static final long serialVersionUID = 1L;

	public UsbTransferTimeoutException()
	{
	}

	public UsbTransferTimeoutException(final String s)
	{
		super(s);
	}
}
