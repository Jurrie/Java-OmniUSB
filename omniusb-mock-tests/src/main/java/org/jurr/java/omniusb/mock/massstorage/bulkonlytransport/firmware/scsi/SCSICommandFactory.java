package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi;

public class SCSICommandFactory
{
	public static SCSICommand fromBytes(final byte[] buffer)
	{
		switch (buffer[0])
		{
		// RBC-2 commands
		case Read10Command.OPERATION_CODE:
			return new Read10Command(buffer);
		case ReadCapacityCommand.OPERATION_CODE:
			return new ReadCapacityCommand(buffer);
		case StartStopUnitCommand.OPERATION_CODE:
			return new StartStopUnitCommand(buffer);
		case SynchronizeCacheCommand.OPERATION_CODE:
			return new SynchronizeCacheCommand(buffer);
		case Write10Command.OPERATION_CODE:
			return new Write10Command(buffer);
		case VerifyCommand.OPERATION_CODE:
			return new VerifyCommand(buffer);

		// SPC-2 commands
		case InquiryCommand.OPERATION_CODE:
			return new InquiryCommand(buffer);
		case ModeSelect6Command.OPERATION_CODE:
			return new ModeSelect6Command(buffer);
		case ModeSense6Command.OPERATION_CODE:
			return new ModeSense6Command(buffer);
		case MediumRemovalCommand.OPERATION_CODE:
			return new MediumRemovalCommand(buffer);
		// case RequestSenseCommand.COMMAND_CODE: // Is optional according to SPC-2 for removable media
		// return new RequestSenseCommand(buffer);
		case TestUnitReadyCommand.OPERATION_CODE:
			return new TestUnitReadyCommand(buffer);
		// case WriteBufferCommand.COMMAND_CODE: // Is optional according to SPC-2 for removable media
		// return new WriteBufferCommand(buffer);
		default:
			throw new IllegalArgumentException("Unsupported SCSI command: " + String.format("0x%02X", buffer[0]));
		}
	}
}
