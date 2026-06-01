package org.jurr.java.omniusb.usbip.domain;

class TransferFlags
{
	public static final TransferFlags EMPTY = new TransferFlags(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false)
	{
		@Override
		public String toString()
		{
			return "EMPTY";
		}
	};

	// From /usr/src/linux-headers-6.8.0-41/include/uapi/linux/usbip.h
	private static final int MASK_SHORT_NOT_OK = 0x00000001;
	private static final int MASK_ISO_ASAP = 0x00000002;
	private static final int MASK_NO_TRANSFER_DMA_MAP = 0x00000004;
	private static final int MASK_ZERO_PACKET = 0x00000040;
	private static final int MASK_NO_INTERRUPT = 0x00000080;
	private static final int MASK_FREE_BUFFER = 0x00000100;
	private static final int MASK_DIR_IN = 0x00000200;
	private static final int MASK_DMA_MAP_SINGLE = 0x00010000;
	private static final int MASK_DMA_MAP_PAGE = 0x00020000;
	private static final int MASK_DMA_MAP_SG = 0x00040000;
	private static final int MASK_MAP_LOCAL = 0x00080000;
	private static final int MASK_SETUP_MAP_SINGLE = 0x00100000;
	private static final int MASK_SETUP_MAP_LOCAL = 0x00200000;
	private static final int MASK_DMA_SG_COMBINED = 0x00400000;
	private static final int MASK_ALIGNED_TEMP_BUFFER = 0x00800000;

	private final boolean shortNotOk;
	private final boolean isoAsap;
	private final boolean noTransferDmaMap;
	private final boolean zeroPacket;
	private final boolean noInterrupt;
	private final boolean freeBuffer;
	private final boolean dirIn;
	private final boolean dmaMapSingle;
	private final boolean dmaMapPage;
	private final boolean dmaMapSg;
	private final boolean mapLocal;
	private final boolean setupMapSingle;
	private final boolean setupMapLocal;
	private final boolean dmaSgCombined;
	private final boolean alignedTempBuffer;

	private TransferFlags(final boolean shortNotOk, final boolean isoAsap, final boolean noTransferDmaMap, final boolean zeroPacket, final boolean noInterrupt, final boolean freeBuffer, final boolean dirIn, final boolean dmaMapSingle, final boolean dmaMapPage, final boolean dmaMapSg, final boolean mapLocal, final boolean setupMapSingle, final boolean setupMapLocal, final boolean dmaSgCombined, final boolean alignedTempBuffer)
	{
		this.shortNotOk = shortNotOk;
		this.isoAsap = isoAsap;
		this.noTransferDmaMap = noTransferDmaMap;
		this.zeroPacket = zeroPacket;
		this.noInterrupt = noInterrupt;
		this.freeBuffer = freeBuffer;
		this.dirIn = dirIn;
		this.dmaMapSingle = dmaMapSingle;
		this.dmaMapPage = dmaMapPage;
		this.dmaMapSg = dmaMapSg;
		this.mapLocal = mapLocal;
		this.setupMapSingle = setupMapSingle;
		this.setupMapLocal = setupMapLocal;
		this.dmaSgCombined = dmaSgCombined;
		this.alignedTempBuffer = alignedTempBuffer;
	}

	public boolean isShortNotOk()
	{
		return shortNotOk;
	}

	public boolean isIsoAsap()
	{
		return isoAsap;
	}

	public boolean isNoTransferDmaMap()
	{
		return noTransferDmaMap;
	}

	public boolean isZeroPacket()
	{
		return zeroPacket;
	}

	public boolean isNoInterrupt()
	{
		return noInterrupt;
	}

	public boolean isFreeBuffer()
	{
		return freeBuffer;
	}

	public boolean isDirIn()
	{
		return dirIn;
	}

	public boolean isDirOut()
	{
		return !isDirIn();
	}

	public boolean isDmaMapSingle()
	{
		return dmaMapSingle;
	}

	public boolean isDmaMapPage()
	{
		return dmaMapPage;
	}

	public boolean isDmaMapSg()
	{
		return dmaMapSg;
	}

	public boolean isMapLocal()
	{
		return mapLocal;
	}

	public boolean isSetupMapSingle()
	{
		return setupMapSingle;
	}

	public boolean isSetupMapLocal()
	{
		return setupMapLocal;
	}

	public boolean isDmaSgCombined()
	{
		return dmaSgCombined;
	}

	public boolean isAlignedTempBuffer()
	{
		return alignedTempBuffer;
	}

	@Override
	public String toString()
	{
		return "TransferFlags [shortNotOk=" + shortNotOk + ", isoAsap=" + isoAsap + ", noTransferDmaMap=" + noTransferDmaMap + ", zeroPacket=" + zeroPacket + ", noInterrupt=" + noInterrupt + ", freeBuffer=" + freeBuffer + ", dirIn=" + dirIn + ", dmaMapSingle=" + dmaMapSingle + ", dmaMapPage=" + dmaMapPage + ", dmaMapSg=" + dmaMapSg + ", mapLocal=" + mapLocal + ", setupMapSingle=" + setupMapSingle + ", setupMapLocal=" + setupMapLocal + ", dmaSgCombined=" + dmaSgCombined + ", alignedTempBuffer=" + alignedTempBuffer + "]";
	}

	public static TransferFlags fromBytes(final int buffer)
	{
		// Hmm... this might not be handy....
		// if (buffer == 0)
		// {
		// return EMPTY;
		// }

		return new TransferFlags(
				(buffer & MASK_SHORT_NOT_OK) == MASK_SHORT_NOT_OK,
				(buffer & MASK_ISO_ASAP) == MASK_ISO_ASAP,
				(buffer & MASK_NO_TRANSFER_DMA_MAP) == MASK_NO_TRANSFER_DMA_MAP,
				(buffer & MASK_ZERO_PACKET) == MASK_ZERO_PACKET,
				(buffer & MASK_NO_INTERRUPT) == MASK_NO_INTERRUPT,
				(buffer & MASK_FREE_BUFFER) == MASK_FREE_BUFFER,
				(buffer & MASK_DIR_IN) == MASK_DIR_IN,
				(buffer & MASK_DMA_MAP_SINGLE) == MASK_DMA_MAP_SINGLE,
				(buffer & MASK_DMA_MAP_PAGE) == MASK_DMA_MAP_PAGE,
				(buffer & MASK_DMA_MAP_SG) == MASK_DMA_MAP_SG,
				(buffer & MASK_MAP_LOCAL) == MASK_MAP_LOCAL,
				(buffer & MASK_SETUP_MAP_SINGLE) == MASK_SETUP_MAP_SINGLE,
				(buffer & MASK_SETUP_MAP_LOCAL) == MASK_SETUP_MAP_LOCAL,
				(buffer & MASK_DMA_SG_COMBINED) == MASK_DMA_SG_COMBINED,
				(buffer & MASK_ALIGNED_TEMP_BUFFER) == MASK_ALIGNED_TEMP_BUFFER);
	}
}