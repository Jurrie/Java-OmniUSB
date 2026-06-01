package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.firmware.scsi;

import java.nio.charset.StandardCharsets;

public class InquiryData
{
	public static enum PeripheralQualifier
	{
		/**
		 * The specified peripheral device type is currently connected to this logical unit.
		 * If the device server is unable to determine whether or not a physical device is currently connected, it also shall use this peripheral qualifier when returning the INQUIRY data.
		 * This peripheral qualifier does not mean that the device is ready for access by the initiator.
		 */
		CONNECTED((byte) 0),

		/**
		 * The device server is capable of supporting the specified peripheral device type on this logical unit.
		 * However, the physical device is not currently connected to this logical unit.
		 */
		NOT_CONNECTED((byte) 1),

		/**
		 * The device server is not capable of supporting a physical device on this logical unit.
		 * For this peripheral qualifier the peripheral device type shall be set to 1Fh to provide compatibility with previous versions of SCSI.
		 * All other peripheral device type values are reserved for this peripheral qualifier.
		 */
		NOT_SUPPORTED((byte) 3);

		private final byte code;

		private PeripheralQualifier(final byte code)
		{
			this.code = code;
		}

		public byte getCode()
		{
			return code;
		}

		public static PeripheralQualifier fromCode(final byte code)
		{
			for (final PeripheralQualifier pc : values())
			{
				if (pc.code == code)
				{
					return pc;
				}
			}
			throw new IllegalArgumentException("Invalid or vendor specific peripheral qualifier code: " + code);
		}
	}

	public static enum PeripheralDeviceType
	{
		/**
		 * Direct-access device (e.g., magnetic disk)
		 */
		DIRECT_ACCESS_DEVICE((byte) 0x00),

		/**
		 * Sequential-access device (e.g., magnetic tape)
		 */
		SEQUENTIAL_ACCESS_DEVICE((byte) 0x01),

		/**
		 * Printer device
		 */
		PRINTER_DEVICE((byte) 0x02),

		/**
		 * Processor device
		 */
		PROCESSOR_DEVICE((byte) 0x03),

		/**
		 * Write-once device (e.g., some optical disks)
		 */
		WRITE_ONCE_DEVICE((byte) 0x04),

		/**
		 * CD-ROM device
		 */
		CD_DVD_DEVICE((byte) 0x05),

		/**
		 * Scanner device
		 */
		SCANNER_DEVICE((byte) 0x06),

		/**
		 * Optical memory device (e.g., some optical disks)
		 */
		OPTICAL_MEMORY_DEVICE((byte) 0x07),

		/**
		 * Medium changer device (e.g., jukeboxes)
		 */
		MEDIUM_CHANGER_DEVICE((byte) 0x08),

		/**
		 * Communications device
		 */
		COMMUNICATIONS_DEVICE((byte) 0x09),

		/**
		 * Defined by ASC IT8 (Graphic arts pre-press devices)
		 */
		GRAPHICS_DEVICE((byte) 0x0A),

		/**
		 * Defined by ASC IT8 (Graphic arts pre-press devices)
		 */
		GRAPHICS_DEVICE_2((byte) 0x0B),

		/**
		 * Storage array controller device (e.g., RAID)
		 */
		STORAGE_ARRAY_CONTROLLER_DEVICE((byte) 0x0C),

		/**
		 * Enclosure services device
		 */
		ENCLOSURE_SERVICES_DEVICE((byte) 0x0D),

		/**
		 * Simplified direct-access device (e.g., magnetic disk)
		 */
		SIMPLIFIED_DIRECT_ACCESS_DEVICE((byte) 0x0E),

		/**
		 * Optical card reader/writer device
		 */
		OPTICAL_CARD_READER_WRITER_DEVICE((byte) 0x0F),

		/**
		 * Object-based Storage Device
		 */
		OBJECT_BASED_STORAGE_DEVICE((byte) 0x11),

		/**
		 * Unknown or no device type
		 */
		UNKNOWN_OR_NO_DEVICE_TYPE((byte) 0x1F);

		private final byte code;

		private PeripheralDeviceType(final byte code)
		{
			this.code = code;
		}

		public byte getCode()
		{
			return code;
		}

		public static PeripheralDeviceType fromCode(final byte code)
		{
			for (final PeripheralDeviceType pdt : values())
			{
				if (pdt.code == code)
				{
					return pdt;
				}
			}
			throw new IllegalArgumentException("Invalid or vendor specific peripheral device type code: " + code);
		}
	}

	private final PeripheralQualifier peripheralQualifier;
	private final PeripheralDeviceType peripheralDeviceType;
	private final boolean removableMedia;
	private final byte version;
	private final boolean asynchronousEventReportingCapability; // AERC
	private final boolean normalACASupported; // NormACA
	private final boolean hierarchicalSupport; // HiSup
	private final byte responseDataFormat;
	private final byte additionalLength;
	private final boolean sccSupported; // SCCS
	private final boolean basicQueuing; // BQue
	private final boolean enclosureServices; // EncServ
	private final boolean multiPort; // MultiP
	private final boolean mediumChanger; // MChngr
	private final boolean relativeAddressing; // RelAdr
	private final boolean linked; // Linked
	private final boolean commandQueuing; // CmdQue
	private final String vendorIdentification;
	private final String productIdentification;
	private final String productRevisionLevel;

	public InquiryData(
			final PeripheralQualifier peripheralQualifier,
			final PeripheralDeviceType peripheralDeviceType,
			final boolean removableMedia,
			final byte version,
			final boolean asynchronousEventReportingCapability,
			final boolean normalACASupported,
			final boolean hierarchicalSupport,
			final byte responseDataFormat,
			final byte additionalLength,
			final boolean sccSupported,
			final boolean basicQueuing,
			final boolean enclosureServices,
			final boolean multiPort,
			final boolean mediumChanger,
			final boolean relativeAddressing,
			final boolean linked,
			final boolean commandQueuing,
			final String vendorIdentification,
			final String productIdentification,
			final String productRevisionLevel)
	{
		this.peripheralQualifier = peripheralQualifier;
		this.peripheralDeviceType = peripheralDeviceType;
		this.removableMedia = removableMedia;
		this.version = version;
		this.asynchronousEventReportingCapability = asynchronousEventReportingCapability;
		this.normalACASupported = normalACASupported;
		this.hierarchicalSupport = hierarchicalSupport;
		this.responseDataFormat = responseDataFormat;
		this.additionalLength = additionalLength;
		this.sccSupported = sccSupported;
		this.basicQueuing = basicQueuing;
		this.enclosureServices = enclosureServices;
		this.multiPort = multiPort;
		this.mediumChanger = mediumChanger;
		this.relativeAddressing = relativeAddressing;
		this.linked = linked;
		this.commandQueuing = commandQueuing;
		this.vendorIdentification = vendorIdentification;
		this.productIdentification = productIdentification;
		this.productRevisionLevel = productRevisionLevel;
	}

	public PeripheralQualifier getPeripheralQualifier()
	{
		return peripheralQualifier;
	}

	public PeripheralDeviceType getPeripheralDeviceType()
	{
		return peripheralDeviceType;
	}

	public boolean isRemovableMedia()
	{
		return removableMedia;
	}

	public byte getVersion()
	{
		return version;
	}

	public boolean isAsynchronousEventReportingCapability()
	{
		return asynchronousEventReportingCapability;
	}

	public boolean isNormalACASupported()
	{
		return normalACASupported;
	}

	public boolean isHierarchicalSupport()
	{
		return hierarchicalSupport;
	}

	public byte getResponseDataFormat()
	{
		return responseDataFormat;
	}

	public byte getAdditionalLength()
	{
		return additionalLength;
	}

	public boolean isSccSupported()
	{
		return sccSupported;
	}

	public boolean isBasicQueuing()
	{
		return basicQueuing;
	}

	public boolean isEnclosureServices()
	{
		return enclosureServices;
	}

	public boolean isMultiPort()
	{
		return multiPort;
	}

	public boolean isMediumChanger()
	{
		return mediumChanger;
	}

	public boolean isRelativeAddressing()
	{
		return relativeAddressing;
	}

	public boolean isLinked()
	{
		return linked;
	}

	public boolean isCommandQueuing()
	{
		return commandQueuing;
	}

	public String getVendorIdentification()
	{
		return vendorIdentification;
	}

	public String getProductIdentification()
	{
		return productIdentification;
	}

	public String getProductRevisionLevel()
	{
		return productRevisionLevel;
	}

	public byte[] toByteArray()
	{
		final byte[] data = new byte[36];

		data[0] = (byte) (peripheralQualifier.getCode() << 5 | peripheralDeviceType.getCode() & 0x1F);
		data[1] = (byte) (removableMedia ? 0x80 : 0x00);
		data[2] = version;
		data[3] = (byte) ((asynchronousEventReportingCapability ? 0x20 : 0x00)
				| (normalACASupported ? 0x10 : 0x00)
				| (hierarchicalSupport ? 0x08 : 0x00)
				| responseDataFormat & 0x03);
		data[4] = additionalLength;
		data[5] = (byte) (sccSupported ? 0x80 : 0x00);
		data[6] = (byte) ((basicQueuing ? 0x20 : 0x00)
				| (enclosureServices ? 0x10 : 0x00)
				| (multiPort ? 0x08 : 0x00)
				| (mediumChanger ? 0x04 : 0x00)
				| (relativeAddressing ? 0x02 : 0x00));
		data[7] = (byte) ((linked ? 0x02 : 0x00)
				| (commandQueuing ? 0x01 : 0x00));

		System.arraycopy(fixLengthStringBytes(vendorIdentification, 8), 0, data, 8, 8);
		System.arraycopy(fixLengthStringBytes(productIdentification, 16), 0, data, 16, 16);
		System.arraycopy(fixLengthStringBytes(productRevisionLevel, 4), 0, data, 32, 4);

		return data;
	}

	private byte[] fixLengthStringBytes(final String inputString, final int length)
	{
		final byte[] bytes = new byte[length];

		final byte[] strBytes;
		if (inputString.length() < length)
		{
			strBytes = (inputString + " ".repeat(length - inputString.length())).getBytes(StandardCharsets.US_ASCII);
		}
		else
		{
			strBytes = inputString.substring(0, length).getBytes(StandardCharsets.US_ASCII);
		}

		System.arraycopy(strBytes, 0, bytes, 0, Math.min(strBytes.length, length));

		return bytes;
	}
}
