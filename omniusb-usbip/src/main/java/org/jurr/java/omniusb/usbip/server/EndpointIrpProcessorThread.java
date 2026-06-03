package org.jurr.java.omniusb.usbip.server;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.usb3.IUsbEndpoint;
import javax.usb3.exception.UsbDisconnectedException;
import javax.usb3.exception.UsbException;

import org.jurr.java.omniusb.usbip.domain.UsbIpHeaderBasic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndpointIrpProcessorThread extends Thread
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final BlockingQueue<ProcessIrpTask> usbIrpQueue = new ArrayBlockingQueue<>(10);
	private final IUsbEndpoint endpoint;
	private final Socket clientSocket;

	private ProcessIrpTask irpCurrentlyProcessing;

	private boolean shouldExit;

	/**
	 *
	 * @param clientSocket
	 * @param endpoint can be null for control endpoint
	 */
	protected EndpointIrpProcessorThread(final Socket clientSocket, final IUsbEndpoint endpoint)
	{
		super();
		setName("IRP Processor for " + getEndpointDescription());
		this.clientSocket = clientSocket;
		this.endpoint = endpoint;
		// setDaemon(true);
		// usbIrpQueueProcessorThread.start();
	}

	private String getEndpointDescription()
	{
		if (endpoint == null)
		{
			return "control endpoint";
		}
		else
		{
			return "endpoint " + endpoint.getUsbEndpointDescriptor().endpointAddress().getEndPointNumber();
		}
	}

	public void addIrpToQueue(final ProcessIrpTask irpTask) throws InterruptedException
	{
		usbIrpQueue.put(irpTask);
		LOGGER.debug("Client {}@{} #{}: added IRP to queue", clientSocket.getInetAddress(), getEndpointDescription(), irpTask.getSeqNum());
	}

	@Override
	public void run()
	{
		while (!shouldExit)
		{
			try
			{
				irpCurrentlyProcessing = usbIrpQueue.take();
				LOGGER.debug("Client {}@{} #{}: processing IRP from queue", clientSocket.getInetAddress(), getEndpointDescription(), irpCurrentlyProcessing.getSeqNum());
				irpCurrentlyProcessing.run();
				irpCurrentlyProcessing = null;
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
		}
	}

	public void stopExecuting() throws IOException
	{
		if (shouldExit)
		{
			return;
		}

		LOGGER.info("Client {} stopping", clientSocket.getInetAddress());

		shouldExit = true;
		usbIrpQueue.clear();

	}

	public boolean abort(final UsbIpHeaderBasic usbIpHeaderBasic, final int seqNumToUnlink) throws UsbDisconnectedException, UsbException, IOException
	{
		final Optional<ProcessIrpTask> irpToAbort = usbIrpQueue.stream().filter(x -> x.getSeqNum() == seqNumToUnlink).findAny();
		final boolean irpWasNotYetProcessed = !irpToAbort.isEmpty() && usbIrpQueue.remove(irpToAbort.get());
		final boolean irpIsCurrentlyProcessing = irpCurrentlyProcessing != null && irpCurrentlyProcessing.getSeqNum() == seqNumToUnlink;
		if (irpWasNotYetProcessed)
		{
			irpToAbort.get().abort(usbIpHeaderBasic.getSeqNum());
			return true;
		}
		else if (irpIsCurrentlyProcessing)
		{
			irpCurrentlyProcessing.abort(usbIpHeaderBasic.getSeqNum());
			return true;
		}
		else
		{
			return false;
		}
	}
}
