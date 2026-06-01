package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi;

public interface SCSICommand
{
	byte getOperationCode();

	byte getCommandLength();
}
