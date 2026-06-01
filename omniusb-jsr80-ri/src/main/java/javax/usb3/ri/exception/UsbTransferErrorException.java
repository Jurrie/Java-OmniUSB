package javax.usb3.ri.exception;

import javax.usb3.exception.UsbException;

public class UsbTransferErrorException extends UsbException
{
	private static final long serialVersionUID = 1L;

	public UsbTransferErrorException()
	{
	}

	public UsbTransferErrorException(final String s)
	{
		super(s);
	}
}
