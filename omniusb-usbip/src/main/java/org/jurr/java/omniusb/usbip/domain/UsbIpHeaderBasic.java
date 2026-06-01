package org.jurr.java.omniusb.usbip.domain;

import java.nio.ByteBuffer;

import javax.usb3.enumerated.EEndpointDirection;
import javax.usb3.request.BEndpointAddress;

public class UsbIpHeaderBasic
{
	public static final int BUFFER_SIZE = Integer.BYTES + Integer.BYTES + Integer.BYTES + Integer.BYTES + Integer.BYTES;

	enum Direction
	{
		// Defined in drivers/usb/usbip/usbip_common.h
		USBIP_DIR_OUT(0x00), USBIP_DIR_IN(0x01);

		private final int value;

		private Direction(final int value)
		{
			this.value = value;
		}

		public int getValue()
		{
			return value;
		}

		public static Direction fromValue(final int value)
		{
			for (Direction direction : values())
			{
				if (direction.getValue() == value)
				{
					return direction;
				}
			}
			throw new IllegalArgumentException("Unknown direction: " + value);
		}
	}

	private final int command;
	private final int seqNum;
	private final int devId;
	private final Direction direction;
	private final int ep;

	private UsbIpHeaderBasic(final int command, final int seqNum, final int devId, final int direction, final int ep)
	{
		this.command = command;
		this.seqNum = seqNum;
		this.devId = devId;
		this.direction = Direction.fromValue(direction);
		this.ep = ep;
	}

	public int getCommand()
	{
		return command;
	}

	public int getSeqNum()
	{
		return seqNum;
	}

	public int getDevId()
	{
		return devId;
	}

	public short getBusId()
	{
		return (short) (devId >> 16);
	}

	public short getDevNum()
	{
		return (short) (devId & 0xFF);
	}

	public Direction getDirection()
	{
		return direction;
	}

	public EEndpointDirection getEEndpointDirection()
	{
		return direction == Direction.USBIP_DIR_OUT ? EEndpointDirection.HOST_TO_DEVICE : EEndpointDirection.DEVICE_TO_HOST;
	}

	public int getEp()
	{
		return ep;
	}

	public BEndpointAddress getBEndpointAddress()
	{
		return new BEndpointAddress(ep, getEEndpointDirection());
	}

	public static UsbIpHeaderBasic fromBuffer(final ByteBuffer buffer)
	{
		final int command = buffer.getInt();
		final int seqNum = buffer.getInt();
		final int devId = buffer.getInt();
		final int direction = buffer.getInt();
		final int ep = buffer.getInt();
		return new UsbIpHeaderBasic(command, seqNum, devId, direction, ep);
	}

	public static UsbIpHeaderBasic createForResponse(final int command, final int seqNum)
	{
		return new UsbIpHeaderBasic(command, seqNum, 0, 0, 0);
	}

	public void toBuffer(final ByteBuffer buffer)
	{
		buffer.putInt(command);
		buffer.putInt(seqNum);
		buffer.putInt(devId);
		buffer.putInt(direction.getValue());
		buffer.putInt(ep);
	}
}
