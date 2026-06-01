package javax.usb3.ri.hid;

public enum EHidSubclass
{
	NONE((byte) 0x00), BOOT_INTERFACE((byte) 0x01);

	private final byte byteCode;

	private EHidSubclass(final byte byteCode)
	{
		this.byteCode = byteCode;
	}

	public byte getByteCode()
	{
		return byteCode;
	}
}
