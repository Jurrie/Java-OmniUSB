package javax.usb3.ri;

import javax.usb3.IUsbIrpIsoPacket;

public class UsbIrpIsoPacket implements IUsbIrpIsoPacket
{
	/*
	 * https://github.com/torvalds/linux/blob/master/drivers/usb/usbip/usbip_common.h#L222C1-L227C12
	 * struct usbip_iso_packet_descriptor {
	 * __u32 offset;
	 * __u32 length; // expected length
	 * __u32 actual_length;
	 * __u32 status;
	 * } __packed;
	 */

	private final int offset;
	private final int length;
	private final int actualLength;
	private final int status;

	public UsbIrpIsoPacket(final int offset, final int length, final int actualLength, final int status)
	{
		this.offset = offset;
		this.length = length;
		this.actualLength = actualLength;
		this.status = status;
	}

	@Override
	public int getOffset()
	{
		return offset;
	}

	@Override
	public int getLength()
	{
		return length;
	}

	@Override
	public int getActualLength()
	{
		return actualLength;
	}

	@Override
	public int getStatus()
	{
		return status;
	}
}
