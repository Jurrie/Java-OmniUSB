package org.jurr.java.omniusb.usbip.domain;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.usb3.enumerated.EDescriptorType;
import javax.usb3.enumerated.EDeviceRequest;
import javax.usb3.request.BMRequestType;
import javax.usb3.request.BRequest;

import org.jurr.java.omniusb.usbip.HexFormatUtils;

public class SetupPacket
{
	public static final int BUFFER_SIZE = Byte.BYTES + Byte.BYTES + Short.BYTES + Short.BYTES + Short.BYTES;

	private final byte bmRequestType;
	private final byte bRequest;
	private final short wValue;
	private final short wIndex;
	private final short wLength;

	private SetupPacket(final byte bmRequestType, final byte bRequest, final short wValue, final short wIndex, final short wLength)
	{
		this.bmRequestType = bmRequestType;
		this.bRequest = bRequest;
		this.wValue = wValue;
		this.wIndex = wIndex;
		this.wLength = wLength;
	}

	public byte bmRequestType()
	{
		return bmRequestType;
	}

	public BMRequestType getBmRequestType()
	{
		return new BMRequestType(bmRequestType());
	}

	public byte bRequest()
	{
		return bRequest;
	}

	public BRequest getBRequest()
	{
		return new BRequest(bRequest());
	}

	public short wValue()
	{
		return wValue;
	}

	public byte getWValueHigh()
	{
		return (byte) (wValue() >> 8);
	}

	public byte getWValueLow()
	{
		return (byte) wValue();
	}

	public short wIndex()
	{
		return wIndex;
	}

	public short wLength()
	{
		return wLength;
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("SetupPacket [bmRequestType=");
		builder.append(getBmRequestType());
		builder.append(", bRequest=");
		builder.append(getBRequest());

		if (getBRequest().getDeviceRequest() == EDeviceRequest.GET_DESCRIPTOR || getBRequest().getDeviceRequest() == EDeviceRequest.SET_DESCRIPTOR)
		{
			builder.append(", descriptor type=");
			builder.append(EDescriptorType.fromBytecode(getWValueHigh()));
			builder.append(", descriptor index=");
			builder.append(HexFormatUtils.format(getWValueLow()));
		}
		else
		{
			builder.append(", wValue=");
			builder.append(HexFormatUtils.format(wValue));
		}
		builder.append(", wIndex=");
		builder.append(HexFormatUtils.format(wIndex));
		builder.append(", wLength=");
		builder.append(HexFormatUtils.format(wLength));
		builder.append("]");

		return builder.toString();
	}

	public static SetupPacket fromBytes(final ByteBuffer buffer)
	{
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		return new SetupPacket(buffer.get(), buffer.get(), buffer.getShort(), buffer.getShort(), buffer.getShort());
	}
}