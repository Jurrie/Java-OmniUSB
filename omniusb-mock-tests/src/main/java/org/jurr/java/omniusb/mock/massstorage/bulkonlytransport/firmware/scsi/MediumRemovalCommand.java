package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi;

public class MediumRemovalCommand implements SPC2Command
{
	public static final byte OPERATION_CODE = 0x1E;

	public static enum Prevent
	{
		/**
		 * Medium removal shall be allowed from both the data transport element and the attached medium changer (if any).
		 */
		ALLOW_MEDIUM_REMOVAL((byte) 0),

		/**
		 * Medium removal shall be prohibited from the data transport element but allowed from the attached medium changer (if any).
		 */
		ALLOW_FOR_ATTACHED_MEDIUM_CHANGER((byte) 1),

		/**
		 * Medium removal shall be allowed for the data transport element but prohibited for the attached medium changer.
		 */
		ALLOW_FOR_DATA_TRANSPORT_ELEMENT((byte) 2),

		/**
		 * Medium removal shall be prohibited for both the data transport element and the attached medium changer.
		 */
		PREVENT_MEDIUM_REMOVAL((byte) 3);

		private final byte code;

		private Prevent(final byte code)
		{
			this.code = code;
		}

		public byte getCode()
		{
			return code;
		}

		public static Prevent fromCode(final byte code)
		{
			for (final Prevent pa : values())
			{
				if (pa.code == code)
				{
					return pa;
				}
			}
			throw new IllegalArgumentException("Invalid Prevent/Allow code: " + code);
		}
	}

	private final Prevent prevent;

	public MediumRemovalCommand(final byte[] buffer)
	{
		if (buffer.length != getCommandLength())
		{
			throw new IllegalArgumentException("Prevent/allow Medium Removal command must be " + getCommandLength() + " bytes long but was " + buffer.length);
		}

		if (buffer[0] != OPERATION_CODE)
		{
			throw new IllegalArgumentException("Invalid operation code for Prevent/allow Medium Removal command");
		}

		prevent = Prevent.fromCode((byte) (buffer[4] & 0x03));
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

	public Prevent getPrevent()
	{
		return prevent;
	}
}
