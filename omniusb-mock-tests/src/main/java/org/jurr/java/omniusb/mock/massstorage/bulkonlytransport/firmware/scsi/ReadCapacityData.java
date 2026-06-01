package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi;

public class ReadCapacityData
{
	private final int returnedLogicalBlockAddress;
	private final int blockLengthInBytes;

	public ReadCapacityData(final int returnedLogicalBlockAddress, final int blockLengthInBytes)
	{
		this.returnedLogicalBlockAddress = returnedLogicalBlockAddress;
		this.blockLengthInBytes = blockLengthInBytes;
	}

	public byte[] toByteArray()
	{
		final byte[] data = new byte[8];

		data[0] = (byte) (returnedLogicalBlockAddress >> 24 & 0xFF);
		data[1] = (byte) (returnedLogicalBlockAddress >> 16 & 0xFF);
		data[2] = (byte) (returnedLogicalBlockAddress >> 8 & 0xFF);
		data[3] = (byte) (returnedLogicalBlockAddress & 0xFF);

		data[4] = (byte) (blockLengthInBytes >> 24 & 0xFF);
		data[5] = (byte) (blockLengthInBytes >> 16 & 0xFF);
		data[6] = (byte) (blockLengthInBytes >> 8 & 0xFF);
		data[7] = (byte) (blockLengthInBytes & 0xFF);

		return data;
	}
}
