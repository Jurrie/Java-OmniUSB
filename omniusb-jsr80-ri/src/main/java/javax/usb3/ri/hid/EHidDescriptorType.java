package javax.usb3.ri.hid;

public enum EHidDescriptorType
{
	HID((byte) 0x21), REPORT((byte) 0x22), PHYSICAL((byte) 0x23);

	private final byte byteCode;

	private EHidDescriptorType(final byte byteCode)
	{
		this.byteCode = byteCode;
	}

	public byte getByteCode()
	{
		return byteCode;
	}
}
