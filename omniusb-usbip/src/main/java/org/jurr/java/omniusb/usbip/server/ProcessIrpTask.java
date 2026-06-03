package org.jurr.java.omniusb.usbip.server;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.Socket;

import javax.usb3.IUsbControlIrp;
import javax.usb3.IUsbDevice;
import javax.usb3.IUsbIrp;
import javax.usb3.IUsbPipe;
import javax.usb3.enumerated.EEndpointDirection;
import javax.usb3.exception.UsbAbortException;
import javax.usb3.exception.UsbDisconnectedException;
import javax.usb3.exception.UsbException;
import javax.usb3.exception.UsbNotActiveException;
import javax.usb3.exception.UsbPlatformException;

import org.jurr.java.omniusb.usbip.domain.UsbIpHeaderBasic;
import org.jurr.java.omniusb.usbip.domain.UsbIpSubmitResponse;
import org.jurr.java.omniusb.usbip.domain.UsbIpUnlinkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessIrpTask implements Runnable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final UsbIpHeaderBasic header;
	private final Socket clientSocket;
	private final IUsbDevice device;
	private final IUsbPipe pipe;
	private final IUsbIrp irp;

	// Use this constructor for control IRPs
	protected ProcessIrpTask(final UsbIpHeaderBasic header, final Socket clientSocket, final IUsbDevice device, final IUsbControlIrp controlIrp)
	{
		this.header = header;
		this.clientSocket = clientSocket;
		this.device = device;
		pipe = null;
		irp = controlIrp;
	}

	// Use this constructor for normal IRPs
	protected ProcessIrpTask(final UsbIpHeaderBasic header, final Socket clientSocket, final IUsbPipe pipe, final IUsbIrp irp)
	{
		this.header = header;
		this.clientSocket = clientSocket;
		device = null;
		this.pipe = pipe;
		this.irp = irp;
	}

	public int getSeqNum()
	{
		return header.getSeqNum();
	}

	/**
	 * This gets called when this task completes. Either by exception or by success.
	 */
	protected void onComplete()
	{
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName("Future task for Client " + clientSocket.getInetAddress() + " #" + getSeqNum());
		try
		{
			LOGGER.debug("Client {} #{}: Submitting IRP...", clientSocket.getInetAddress(), getSeqNum());
			if (irp instanceof IUsbControlIrp controlIrp)
			{
				device.asyncSubmit(controlIrp);
			}
			else
			{
				pipe.asyncSubmit(irp);
			}
			LOGGER.debug("Client {} #{}: Waiting for IRP to complete...", clientSocket.getInetAddress(), getSeqNum());

			while (!irp.isComplete())
			{
				irp.waitUntilComplete();
			}
			LOGGER.debug("Client {} #{}: IRP is complete, preparing UsbIpSubmitResponse...", clientSocket.getInetAddress(), getSeqNum());

			synchronized (irp)
			{
				final UsbIpSubmitResponse usbIpSubmitResponse;
				if (irp.isUsbException())
				{
					if (irp.getUsbException() instanceof UsbAbortException)
					{
						// If we aborted, just log and return
						LOGGER.debug("Client {} #{}: IRP was aborted, not sending UsbIpSubmitResponse (UsbIpUnlinkResponse should already have been sent by abort(int) call)", clientSocket.getInetAddress(), getSeqNum());
						return;
					}

					LOGGER.info("Client {} #{}: IRP was USB exception {}", clientSocket.getInetAddress(), getSeqNum(), irp.getUsbException().getLocalizedMessage());

					if (irp.getUsbException() instanceof UsbPlatformException usbPlatformException)
					{
						usbIpSubmitResponse = UsbIpSubmitResponse.errorResponse(getSeqNum(), usbPlatformException.getErrorCode(), 1);
					}
					else
					{
						usbIpSubmitResponse = UsbIpSubmitResponse.errorResponse(getSeqNum(), -1, 1);
					}
				}
				else
				{
					LOGGER.debug("Client {} #{}: IRP completed successfully, actual length {}", clientSocket.getInetAddress(), getSeqNum(), irp.getActualLength());
					usbIpSubmitResponse = UsbIpSubmitResponse.successResponse(getSeqNum(), irp.getActualLength(), header.getEEndpointDirection() == EEndpointDirection.DEVICE_TO_HOST ? irp.getData() : null, irp.getIsochronousPackets());
				}
				clientSocket.getOutputStream().write(usbIpSubmitResponse.toBuffer());
			}
		}
		catch (UsbAbortException e)
		{
			LOGGER.debug("Client {} #{}: IRP was aborted", clientSocket.getInetAddress(), getSeqNum());
		}
		catch (UsbNotActiveException | UsbDisconnectedException | UsbException | IOException e)
		{
			LOGGER.error("Client " + clientSocket.getInetAddress() + " #" + getSeqNum() + ": error processing URB", e);
		}
		finally
		{
			onComplete();
			Thread.currentThread().setName("Future task for Client " + clientSocket.getInetAddress() + " #" + getSeqNum() + " [DONE]");
		}
	}

	protected void shutdown()
	{
		try
		{
			if (pipe == null)
			{
				device.abortSubmission((IUsbControlIrp) irp);
			}
			else
			{
				pipe.abortSubmission(irp);
			}

			irp.complete(); // To cancel the waitUntilComplete
		}
		catch (UsbException e)
		{
			// Ignore because we are stopping
		}
	}

	protected void abort(final int seqNumOfUnlinkPacket) throws UsbDisconnectedException, UsbException, IOException
	{
		synchronized (irp)
		{
			if (irp.isComplete())
			{
				LOGGER.debug("Client {} #{}: unlink requested for URB with seqNum {}, but IRP is already complete", clientSocket.getInetAddress(), seqNumOfUnlinkPacket, header.getSeqNum());
				final UsbIpUnlinkResponse usbIpUnlinkResponse = UsbIpUnlinkResponse.errorAlreadyCompletedResponse(seqNumOfUnlinkPacket);
				clientSocket.getOutputStream().write(usbIpUnlinkResponse.toBuffer());
				return;
			}

			if (pipe == null)
			{
				LOGGER.debug("Client {} #{}: unlink requested for control URB with seqNum {}", clientSocket.getInetAddress(), seqNumOfUnlinkPacket, header.getSeqNum());
				device.abortSubmission((IUsbControlIrp) irp);
			}
			else
			{
				LOGGER.debug("Client {} #{}: unlink requested for URB with seqNum {}", clientSocket.getInetAddress(), seqNumOfUnlinkPacket, header.getSeqNum());
				pipe.abortSubmission(irp);
			}

			if (irp.getUsbException() instanceof UsbAbortException)
			{
				final UsbIpUnlinkResponse usbIpUnlinkResponse = UsbIpUnlinkResponse.successResponse(seqNumOfUnlinkPacket);
				clientSocket.getOutputStream().write(usbIpUnlinkResponse.toBuffer());
			}
			else
			{
				LOGGER.error("Client {} #{}: IRP abort requested for {}, but IRP is not in aborted state (was {})", clientSocket.getInetAddress(), seqNumOfUnlinkPacket, header.getSeqNum(), irp.getUsbException() != null ? irp.getUsbException().getClass().getSimpleName() : "not completed");
				final UsbIpUnlinkResponse usbIpUnlinkResponse = UsbIpUnlinkResponse.errorResponse(seqNumOfUnlinkPacket);
				clientSocket.getOutputStream().write(usbIpUnlinkResponse.toBuffer());
			}
		}
	}
}
