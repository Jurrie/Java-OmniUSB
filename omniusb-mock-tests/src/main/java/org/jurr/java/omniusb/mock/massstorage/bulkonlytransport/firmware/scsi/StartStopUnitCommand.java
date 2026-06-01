package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi;

public class StartStopUnitCommand implements RBCCommand
{
	public static final byte OPERATION_CODE = 0x1B;

	public static enum PowerCondition
	{
		/**
		 * No change in power conditions
		 */
		NO_CHANGE((byte) 0),

		/**
		 * Place device in Active state
		 */
		ACTIVE((byte) 1),

		/**
		 * Place device in Idle state
		 */
		IDLE((byte) 2),

		/**
		 * Place device in Standby state
		 */
		STANDBY((byte) 3),

		/**
		 * Place device in Sleep state
		 */
		SLEEP((byte) 5);

		private final byte code;

		private PowerCondition(final byte code)
		{
			this.code = code;
		}

		public byte getCode()
		{
			return code;
		}

		public static PowerCondition fromCode(final byte code)
		{
			for (final PowerCondition pc : values())
			{
				if (pc.code == code)
				{
					return pc;
				}
			}
			throw new IllegalArgumentException("Invalid Power Condition code: " + code);
		}
	}

	private final boolean immediate; // Immed
	private final PowerCondition powerCondition;
	private final boolean loadEject; // LoEj
	private final boolean start; // START

	public StartStopUnitCommand(final byte[] buffer)
	{
		if (buffer.length != getCommandLength())
		{
			throw new IllegalArgumentException("Start/Stop Unit command must be " + getCommandLength() + " bytes long but was " + buffer.length);
		}

		if (buffer[0] != OPERATION_CODE)
		{
			throw new IllegalArgumentException("Invalid command code for Start/Stop Unit command");
		}

		if (buffer[5] != 0)
		{
			throw new IllegalArgumentException("Control byte must be zero for Start/Stop Unit command");
		}

		immediate = (buffer[1] & 0x01) == 0x01;
		powerCondition = PowerCondition.fromCode((byte) (buffer[4] >> 4 & 0x07));
		loadEject = (buffer[4] & 0x02) == 0x02;
		start = (buffer[4] & 0x01) == 0x01;
	}

	@Override
	public byte getOperationCode()
	{
		return OPERATION_CODE;
	}

	@Override
	public byte getCommandLength()
	{
		return 6;
	}

	public boolean isImmediate()
	{
		return immediate;
	}

	public PowerCondition getPowerCondition()
	{
		return powerCondition;
	}

	public boolean isLoadEject()
	{
		return loadEject;
	}

	public boolean isStart()
	{
		return start;
	}
}
