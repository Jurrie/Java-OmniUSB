package javax.usb3.ri.hid;

public enum EHidProtocol
{
	NONE((byte) 0x00), KEYBOARD((byte) 0x01), MOUSE((byte) 0x02);

	private final byte byteCode;

	private EHidProtocol(final byte byteCode)
	{
		this.byteCode = byteCode;
	}

	public byte getByteCode()
	{
		return byteCode;
	}
}
