package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi;

public class ModeSense6Command implements SPC2Command
{
	public static final byte OPERATION_CODE = 0x1A;

	public static enum PageControl
	{
		/**
		 * A PC field value of 00b requests that the device server return the current values of the mode parameters.
		 * The current values returned are:
		 * <ol type="a">
		 * <li>the current values of the mode parameters established by the last successful MODE SELECT command;</li>
		 * <li>the saved values of the mode parameters if a MODE SELECT command has not successfully completed since the last power-on or hard reset condition; or</li>
		 * <li>the default values of the mode parameters, if saved values, are not available or not supported.</li>
		 * </ol>
		 */
		CURRENT_VALUES((byte) 0),

		/**
		 * A PC field value of 01b requests that the device server return a mask denoting those mode parameters that are changeable.
		 * In the mask, the fields of the mode parameters that are changeable shall be set to all one bits and the fields of the mode parameters that are non-changeable (i.e., defined by the target) shall be set to all zero bits.
		 * Implementation of changeable page parameters is optional.
		 */
		CHANGEABLE_VALUES((byte) 1),

		/**
		 * A PC field value of 10b requests that the device server return the default values of the mode parameters.
		 * Unsupported parameters shall be set to zero. Default values should be accessible even if the device is not ready.
		 */
		DEFAULT_VALUES((byte) 2),

		/**
		 * A PC field value of 11b requests that the device server return the saved values of the mode parameters.
		 * Implementation of saved page parameters is optional. Mode parameters not supported by the target shall be set to zero.
		 */
		SAVED_VALUES((byte) 3);

		private final byte code;

		private PageControl(final byte code)
		{
			this.code = code;
		}

		public byte getCode()
		{
			return code;
		}

		public static PageControl fromCode(final byte code)
		{
			for (final PageControl pc : values())
			{
				if (pc.code == code)
				{
					return pc;
				}
			}
			throw new IllegalArgumentException("Invalid Page Control code: " + code);
		}
	}

	private final boolean disableBlockDescriptors; // DBD
	private final PageControl pageControl; // PC
	private final byte pageCode;
	private final byte allocationLength;

	public ModeSense6Command(final byte[] buffer)
	{
		if (buffer.length != getCommandLength())
		{
			throw new IllegalArgumentException("Mode Sense(6) command must be " + getCommandLength() + " bytes long but was " + buffer.length);
		}

		if (buffer[0] != OPERATION_CODE)
		{
			throw new IllegalArgumentException("Invalid operation code for Mode Sense(6) command");
		}

		if (buffer[5] != 0)
		{
			throw new IllegalArgumentException("Control byte must be zero for Mode Sense(6) command");
		}

		disableBlockDescriptors = (buffer[1] & 0x08) == 0x08;
		pageControl = PageControl.fromCode((byte) ((buffer[2] & 0xC0) >> 6));
		pageCode = (byte) (buffer[2] & 0x3F);
		allocationLength = buffer[4];
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

	public boolean isDisableBlockDescriptors()
	{
		return disableBlockDescriptors;
	}

	public PageControl getPageControl()
	{
		return pageControl;
	}

	public byte getPageCode()
	{
		return pageCode;
	}

	public byte getAllocationLength()
	{
		return allocationLength;
	}
}
