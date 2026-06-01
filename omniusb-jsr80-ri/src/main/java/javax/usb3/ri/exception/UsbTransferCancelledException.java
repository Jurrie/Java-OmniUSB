package javax.usb3.ri.exception;

import javax.usb3.exception.UsbException;

public class UsbTransferCancelledException extends UsbException
{
	private static final long serialVersionUID = 1L;

	public UsbTransferCancelledException()
	{
	}

	public UsbTransferCancelledException(final String s)
	{
		super(s);
	}
}
