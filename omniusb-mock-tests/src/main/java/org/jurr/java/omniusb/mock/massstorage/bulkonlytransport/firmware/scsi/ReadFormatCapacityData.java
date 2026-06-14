package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi;

public class ReadFormatCapacityData
{
	private final int numberOfBlocks;
	private final int blockLengthInBytes;

	public ReadFormatCapacityData(final int numberOfBlocks, final int blockLengthInBytes)
	{
		this.numberOfBlocks = numberOfBlocks;
		this.blockLengthInBytes = blockLengthInBytes;
	}

	public byte[] toByteArray(final short allocationLength)
	{
		final byte[] capacityListHeader = new byte[4];
		capacityListHeader[0] = 0; // Reserved
		capacityListHeader[1] = 0; // Reserved
		capacityListHeader[2] = 0; // Reserved
		capacityListHeader[3] = 16; // Capacity List Length - 8 bytes for one capacity descriptor

		final byte[] maximumCapacityDescriptor = new byte[8];
		maximumCapacityDescriptor[0] = (byte) (numberOfBlocks >> 24 & 0xFF);
		maximumCapacityDescriptor[1] = (byte) (numberOfBlocks >> 16 & 0xFF);
		maximumCapacityDescriptor[2] = (byte) (numberOfBlocks >> 8 & 0xFF);
		maximumCapacityDescriptor[3] = (byte) (numberOfBlocks & 0xFF);
		maximumCapacityDescriptor[4] = (byte) 0x02; // Descriptor Type Code - 0x02 for "Formatted Media - Current media capacity"
		maximumCapacityDescriptor[5] = (byte) (blockLengthInBytes >> 16 & 0xFF);
		maximumCapacityDescriptor[6] = (byte) (blockLengthInBytes >> 8 & 0xFF);
		maximumCapacityDescriptor[7] = (byte) (blockLengthInBytes & 0xFF);

		final byte[] formattableCapacityDescriptor = new byte[8];
		formattableCapacityDescriptor[0] = (byte) (numberOfBlocks >> 24 & 0xFF);
		formattableCapacityDescriptor[1] = (byte) (numberOfBlocks >> 16 & 0xFF);
		formattableCapacityDescriptor[2] = (byte) (numberOfBlocks >> 8 & 0xFF);
		formattableCapacityDescriptor[3] = (byte) (numberOfBlocks & 0xFF);
		formattableCapacityDescriptor[4] = 0; // Reserved
		formattableCapacityDescriptor[5] = (byte) (blockLengthInBytes >> 16 & 0xFF);
		formattableCapacityDescriptor[6] = (byte) (blockLengthInBytes >> 8 & 0xFF);
		formattableCapacityDescriptor[7] = (byte) (blockLengthInBytes & 0xFF);

		final byte[] data = new byte[allocationLength];
		System.arraycopy(capacityListHeader, 0, data, 0, Math.min(capacityListHeader.length, allocationLength));
		if (allocationLength > 4)
		{
			System.arraycopy(maximumCapacityDescriptor, 0, data, 4, Math.min(maximumCapacityDescriptor.length, allocationLength - 4));
		}
		if (allocationLength > 12)
		{
			System.arraycopy(formattableCapacityDescriptor, 0, data, 12, Math.min(formattableCapacityDescriptor.length, allocationLength - 12));
		}
		return data;
	}
}
