package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HexFormat;

import org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.domain.CommandBlockWrapper;
import org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.domain.CommandStatusWrapper;
import org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.domain.CommandBlockWrapper.CBWFlags;
import org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.domain.CommandStatusWrapper.CSWStatus;
import org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi.InquiryCommand;
import org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi.InquiryData;
import org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi.MediumRemovalCommand;
import org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi.ModeSense6Command;
import org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi.Read10Command;
import org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi.ReadCapacityCommand;
import org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi.ReadCapacityData;
import org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi.SCSICommand;
import org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi.SCSICommandFactory;
import org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi.StartStopUnitCommand;
import org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi.TestUnitReadyCommand;
import org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi.Write10Command;
import org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi.InquiryData.PeripheralDeviceType;
import org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi.InquiryData.PeripheralQualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockMemoryStickFirmware
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ByteBuffer responseBuffer = ByteBuffer.allocate(0xDF2000); // Max we saw was 0x1fe00 × 0x70 = 0xDF2000 bytes

	private final ArrayList<RandomAccessFile> luns;
	private CommandBlockWrapper cbwCommandInProgress = null;

	public MockMemoryStickFirmware()
	{
		luns = new ArrayList<>();
	}

	public boolean addLUN(final RandomAccessFile data)
	{
		if (luns.size() >= 0xFF)
		{
			return false;
		}
		return luns.add(data);
	}

	/**
	 * Get the number of logical units supported by the device.
	 *
	 * @return the number of LUNs (we return 1 if there is one LUN at index 0, 2 if there are LUNs at index 0 and 1, etc.)
	 */
	public byte getLUNCount()
	{
		return (byte) luns.size();
	}

	public RandomAccessFile setLUN(final byte index, final RandomAccessFile data)
	{
		return luns.set(index, data);
	}

	public void reset()
	{
		// No implementation for now
	}

	public int getBlockSize(final byte lun)
	{
		return 512;
	}

	public int readFromUsbDevice(final ByteBuffer buffer)
	{
		synchronized (responseBuffer)
		{
			final int length = Math.min(responseBuffer.position(), buffer.remaining());
			buffer.put(buffer.position(), responseBuffer, 0, length);

			// There is 'length' data read from responseBuffer. Remove the first 'length' bytes from it.
			final int oldPos = responseBuffer.position();
			responseBuffer.position(length);
			responseBuffer.compact();
			responseBuffer.position(oldPos - length);

			return length;
		}
	}

	public void handleCommand(final byte[] data) throws IOException
	{
		if (cbwCommandInProgress != null)
		{
			final SCSICommand scsiCommandInProgress = SCSICommandFactory.fromBytes(cbwCommandInProgress.getCommandBlock());
			if (scsiCommandInProgress instanceof Write10Command write10CommandInProgress)
			{
				// There is a write command in progress; write the data to storage
				final int blockSize = getBlockSize(cbwCommandInProgress.getLUN());
				final RandomAccessFile storage = luns.get(cbwCommandInProgress.getLUN());
				final int startPosition = write10CommandInProgress.getLogicalBlockAddress() * blockSize;
				final int length = Math.min(write10CommandInProgress.getTransferLength() * blockSize, data.length);
				storage.seek(startPosition);
				storage.write(data);

				final CommandStatusWrapper csw = new CommandStatusWrapper(cbwCommandInProgress.getTag(), cbwCommandInProgress.getDataTransferLength() - length, CSWStatus.PASSED);
				sendResponse(csw.toBytes());

				cbwCommandInProgress = null;
				return;
			}
			else
			{
				throw new IllegalStateException("There is an unknown SCSI command in progress: " + scsiCommandInProgress.getClass());
			}
		}

		final CommandBlockWrapper cbw = CommandBlockWrapper.fromBytes(data);
		try
		{
			final int handledData = handleCommand(cbw);

			if (cbwCommandInProgress != null)
			{
				// Still waiting for data to be written, this will be the next byte array read from USB
				return;
			}
			else
			{
				final CommandStatusWrapper csw = new CommandStatusWrapper(cbw.getTag(), cbw.getDataTransferLength() - handledData, CSWStatus.PASSED);
				sendResponse(csw.toBytes());
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Error handling SCSI command", e);

			final CommandStatusWrapper csw = new CommandStatusWrapper(cbw.getTag(), 0, CSWStatus.FAILED);
			sendResponse(csw.toBytes());
		}
	}

	// Return the number of bytes processed (for data-out) or the number of bytes sent (for data-in)
	private int handleCommand(final CommandBlockWrapper cbw) throws IOException
	{
		final byte[] commandBlock = cbw.getCommandBlock();
		final int dataTransferLength = cbw.getDataTransferLength();
		final byte lun = cbw.getLUN();

		LOGGER.debug("M (SCSI)← {}", HexFormat.of().formatHex(commandBlock));

		final SCSICommand command = SCSICommandFactory.fromBytes(commandBlock);
		byte[] response = null;

		switch (command)
		{
		case InquiryCommand inquiryCommand:
			response = doInquiryCommand(inquiryCommand, lun);
			break;
		case TestUnitReadyCommand testUnitReadyCommand:
			doTestUnitReadyCommand(testUnitReadyCommand);
			break;
		case ReadCapacityCommand readCapacityCommand:
			response = doReadCapacityCommand(readCapacityCommand, lun);
			break;
		case Read10Command read10Command:
			response = doRead10Command(read10Command, dataTransferLength, lun);
			break;
		case ModeSense6Command modeSense6Command:
			response = doModeSense6Command(modeSense6Command, lun);
			break;
		case MediumRemovalCommand mediumRemovalCommand:
			doMediumRemovalCommand(mediumRemovalCommand, lun);
			break;
		case Write10Command write10Command:
			doWrite10Command(cbw, write10Command, dataTransferLength, lun);
			break;
		case StartStopUnitCommand startStopUnitCommand:
			doStartStopUnitCommand(startStopUnitCommand, lun);
			break;
		default:
			throw new UnsupportedOperationException("Unsupported SCSI command: " + command.getClass());
		}

		if (dataTransferLength == 0)
		{
			return 0;
		}

		final CBWFlags flags = cbw.getFlags();
		if (response == null)
		{
			if (flags.isDataOut())
			{
				return commandBlock.length;
			}
			else
			{
				throw new IllegalStateException("No response generated for data-out command");
			}
		}
		else
		{
			sendResponse(response);
			LOGGER.debug("M (SCSI)→ {}", HexFormat.of().formatHex(response));

			if (flags.isDataIn())
			{
				return response.length;
			}
			else
			{
				throw new IllegalStateException("Response generated for data-in command");
			}
		}
	}

	private void sendResponse(final byte[] response)
	{
		synchronized (responseBuffer)
		{
			responseBuffer.put(response);
			responseBuffer.notifyAll();
		}
	}

	private byte[] doInquiryCommand(final InquiryCommand command, final byte lun)
	{
		return new InquiryData(PeripheralQualifier.CONNECTED, PeripheralDeviceType.DIRECT_ACCESS_DEVICE, true, (byte) 0x04, false, false, false, (byte) 2, (byte) 0x1f, false, false, false, false, false, false, false, false, "JSR-80", "Mock Memorystick", "1.0").toByteArray();
	}

	private void doTestUnitReadyCommand(final TestUnitReadyCommand command)
	{
		// Nothing to do, we were _born_ ready!
	}

	private byte[] doReadCapacityCommand(final ReadCapacityCommand readCapacityCommand, final byte lun) throws IOException
	{
		final int blockSize = getBlockSize(lun);
		final RandomAccessFile storage = luns.get(lun);
		return new ReadCapacityData((int) (storage.length() / blockSize - 1), blockSize).toByteArray();
	}

	private byte[] doRead10Command(final Read10Command read10Command, final int dataTransferLength, final byte lun) throws IOException
	{
		final int blockSize = getBlockSize(lun);
		final RandomAccessFile storage = luns.get(lun);
		final int startPosition = read10Command.getLogicalBlockAddress() * blockSize;
		final int length = Math.min(read10Command.getTransferLength() * blockSize, dataTransferLength);
		final byte[] result = new byte[length];
		storage.seek(startPosition);
		if (storage.read(result) != length)
		{
			throw new IOException("Could not read enough data from storage");
		}
		return result;
	}

	private void doWrite10Command(final CommandBlockWrapper cbw, final Write10Command write10Command, final int dataTransferLength, final byte lun)
	{
		cbwCommandInProgress = cbw;
	}

	private byte[] doModeSense6Command(final ModeSense6Command modeSense6Command, final byte lun)
	{
		return new byte[] { 0x03, 0x00, 0x00, 0x00 }; // Return the minimum response: no pages supported
	}

	private void doMediumRemovalCommand(final MediumRemovalCommand mediumRemovalCommand, final byte lun)
	{
		// No implementation for now
	}

	private void doStartStopUnitCommand(final StartStopUnitCommand startStopUnitCommand, final byte lun)
	{
		// No implementation for now
	}
}
