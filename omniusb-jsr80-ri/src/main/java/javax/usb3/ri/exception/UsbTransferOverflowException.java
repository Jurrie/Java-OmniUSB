package javax.usb3.ri.exception;

import javax.usb3.exception.UsbException;

public class UsbTransferOverflowException extends UsbException
{
	private static final long serialVersionUID = 1L;

	public UsbTransferOverflowException()
	{
	}

	public UsbTransferOverflowException(final String s)
	{
		super(s);
	}
}
