package javax.usb3.ri.massstorage;

// See Mass_Storage_Specification_Overview_v1.4_2-19-2010.pdf chapter 3
public enum EMassStorageProtocol
{
	/**
	 * USB Mass Storage Class Control/Bulk/Interrupt (CBI) Transport
	 */
	CBI_WITH_COMMAND_COMPLETION_INTERRUPT((byte) 0x00),

	/**
	 * USB Mass Storage Class Control/Bulk/Interrupt (CBI) Transport
	 */
	CBI_WITHOUT_COMMAND_COMPLETION_INTERRUPT((byte) 0x01),

	/**
	 * USB Mass Storage Class Bulk-Only (BBB) Transport
	 */
	BULK_ONLY_TRANSPORT((byte) 0x50),

	/**
	 * Allocated by USB-IF for UAS. UAS is defined outside of USB.
	 */
	UAS((byte) 0x62),

	/**
	 * Specific to device vendor. De facto use.
	 */
	VENDOR_SPECIFIC((byte) 0xFF);

	private final byte byteCode;

	private EMassStorageProtocol(final byte byteCode)
	{
		this.byteCode = byteCode;
	}

	public byte getByteCode()
	{
		return byteCode;
	}
}
