package javax.usb3.ri.massstorage;

public enum EMassStorageSubclass
{
	/**
	 * SCSI command set not reported. De facto use.
	 */
	SCSI_COMMAND_SET_NOT_REPORTED((byte) 0x00),

	/**
	 * Allocated by USB-IF for RBC. RBC is defined outside of USB.
	 */
	RBC((byte) 0x01),

	/**
	 * (ATAPI) Allocated by USB-IF for MMC-5. MMC-5 is defined outside of USB.
	 */
	MMC_5((byte) 0x02),

	/**
	 * Specifies how to interface Floppy Disk Drives to USB.
	 */
	UFI((byte) 0x04),

	/**
	 * Allocated by USB-IF for SCSI. SCSI standards are defined outside of USB.
	 */
	SCSI_TRANSPARENT_COMMAND_SET((byte) 0x06),

	/**
	 * LSDFS specifies how host has to negotiate access before trying SCSI.
	 */
	LSD_FS((byte) 0x07),

	/**
	 * Allocated by USB-IF for IEEE 1667. IEEE 1667 is defined outside of USB.
	 */
	IEEE_1667((byte) 0x08),

	/**
	 * Specific to device vendor De facto use.
	 */
	VENDOR_SPECIFIC((byte) 0xFF);

	private final byte byteCode;

	private EMassStorageSubclass(final byte byteCode)
	{
		this.byteCode = byteCode;
	}

	public byte getByteCode()
	{
		return byteCode;
	}
}
