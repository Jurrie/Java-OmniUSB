/*
 * Copyright (C) 2011 Klaus Reimer
 * Copyright (C) 2014 Jesse Caulfield
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package javax.usb3.ri;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.usb3.IUsbControlIrp;
import javax.usb3.IUsbDevice;
import javax.usb3.IUsbIrp;
import javax.usb3.exception.UsbAbortException;
import javax.usb3.exception.UsbException;
import javax.usb3.exception.UsbShortPacketException;
import javax.usb3.exception.UsbStallException;
import javax.usb3.ri.exception.UsbControlRequestNotSupportedException;
import javax.usb3.ri.exception.UsbTransferCancelledException;
import javax.usb3.ri.exception.UsbTransferErrorException;
import javax.usb3.ri.exception.UsbTransferNoDeviceException;
import javax.usb3.ri.exception.UsbTransferOverflowException;
import javax.usb3.ri.exception.UsbTransferTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for a concurrent queue of USB I/O Request packets.
 * <p>
 * An IrpQueue contains a thread safe FIFO queue and a threaded
 * processUsbIrpQueueor to handle each IRP that is placed into the queue.
 * <p>
 * Developer note: The default operation of an IrpQueue is to support
 * Asynchronous operation (e.g. processUsbIrpQueue in a separate thread.) To
 * implement synchronous IRP queue handling implement a WAIT lock on the
 * {@link IUsbIrp.isComplete() isComplete} method IUsbIrp.isComplete().
 *
 * @author Klaus Reimer
 * @author Jesse Caulfield
 * @param <T> The type of IRPs this queue holds.
 */
public abstract class AUsbIrpQueue<T extends IUsbIrp> implements AutoCloseable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * The queued USB IRP packets. These are placed in a ConcurrentLinkedQueue: an
	 * unbounded thread-safe {@link java.util.Queue queue} based on linked nodes.
	 * This queue orders elements FIFO (first-in-first-out). The <em>head</em> of
	 * the queue is that element that has been on the queue the longest time. The
	 * <em>tail</em> of the queue is that element that has been on the queue the
	 * shortest time. New elements are inserted at the tail of the queue, and the
	 * queue retrieval operations obtain elements at the head of the queue.
	 */
	private final BlockingQueue<T> usbIrpQueue = new ArrayBlockingQueue<>(10);

	private T irpCurrentlyProcessing;

	/**
	 * The queue processUsbIrpQueueor thread.
	 */
	private Thread usbIrpQueueProcessorThread;

	/**
	 * If queue is currently aborting. Aborting means that we should drop IRPs submitted, but keep processing new IRPs. (I.e. don't stop the thread.)
	 */
	private volatile boolean aborting;

	/**
	 * If the queue is closed. Closed means that we should stop the thread.
	 */
	private boolean closed;

	/**
	 * The USB device instance upon which the QUEUE is to be processed. This is
	 * either a UsbHub or UsbDevice implementation.
	 */
	private final AUsbDevice usbDevice;

	/**
	 * Constructor.
	 *
	 * @param usbDevice The USB usbDevice. Must not be null.
	 * @param usbEndpoint The USB endpoint. If NULL, this is the control endpoint.
	 */
	protected AUsbIrpQueue(final IUsbDevice usbDevice)
	{
		if (usbDevice == null)
		{
			throw new IllegalArgumentException("USB device must be set");
		}
		this.usbDevice = (AUsbDevice) usbDevice;

		usbIrpQueueProcessorThread = new Thread(this::processUsbIrpQueue, "IRP Processor ep0");
		/**
		 * Developer note: Mark this thread as a daemon thread. A daemon thread in
		 * Java is one that doesn't prevent the JVM from exiting. When the JVM
		 * halts any remaining daemon threads are abandoned: finally blocks are
		 * not executed, stacks are not unwound - JVM just exits.
		 */
		usbIrpQueueProcessorThread.setDaemon(true);
		/**
		 * Start the thread. This will begin processing all IRPs in the queue in a
		 * separate thread and immediately return (e.g. asynchronously).
		 */
		usbIrpQueueProcessorThread.start();
	}

	protected AUsbIrpQueue(final UsbPipe pipe)
	{
		this(pipe.getDevice());

		usbIrpQueueProcessorThread.setName("IRP Processor ep" + pipe.getUsbEndpoint().getUsbEndpointDescriptor().endpointAddress().getEndPointNumber() + "(" + pipe.getUsbEndpoint().getUsbEndpointDescriptor().endpointAddress().getDirection() + ")");
	}

	@Override
	public void close()
	{
		closed = true;
		usbIrpQueue.clear();
		usbIrpQueueProcessorThread.interrupt();
	}

	public boolean isClosed()
	{
		return closed;
	}

	public void join() throws InterruptedException
	{
		usbIrpQueueProcessorThread.join();
	}

	protected AUsbDevice getUsbDevice()
	{
		return usbDevice;
	}

	/**
	 * Queues the specified control IRP for processUsbIrpQueueing.
	 *
	 * @param irp The control IRP to queue.
	 */
	public final void add(final T irp)
	{
		/**
		 * Add the USB IRP to the queue.
		 */
		try
		{
			usbIrpQueue.put(irp);
		}
		catch (final InterruptedException e)
		{
			irpCurrentlyProcessing.setUsbException(new UsbAbortException());
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Internal method to processUsbIrpQueue all IRPs in the FIFO queue. This
	 * method returns after all IRP objects in the queue have been
	 * processUsbIrpQueueed.
	 * <p>
	 * This method should be called from within a separate thread to enable
	 * asynchronous operation.
	 */
	private void processUsbIrpQueue()
	{
		while (!closed)
		{
			try
			{
				irpCurrentlyProcessing = usbIrpQueue.take();
				processIrp(irpCurrentlyProcessing, new ProcessIrpCallback()
				{
					@Override
					public void onTransferComplete(final int actualLength)
					{
						if (actualLength < irpCurrentlyProcessing.getLength() && !irpCurrentlyProcessing.getAcceptShortPacket())
						{
							LOGGER.debug("IRP transfer short packet detected, raising UsbShortPacketException");
							irpCurrentlyProcessing.setUsbException(new UsbShortPacketException());
						}
						else
						{
							irpCurrentlyProcessing.complete();
						}

						finishIrp(irpCurrentlyProcessing);
					}

					@Override
					public void onTransferTimedOut(final String strError)
					{
						irpCurrentlyProcessing.setUsbException(new UsbTransferTimeoutException(strError));
					}

					@Override
					public void onTransferStall(final String strError)
					{
						irpCurrentlyProcessing.setUsbException(new UsbStallException(strError));
					}

					@Override
					public void onControlRequestNotSupported(final String strError)
					{
						irpCurrentlyProcessing.setUsbException(new UsbControlRequestNotSupportedException(strError));
					}

					@Override
					public void onTransferOverflow(final String strError)
					{
						irpCurrentlyProcessing.setUsbException(new UsbTransferOverflowException(strError));
					}

					@Override
					public void onTransferNoDevice(final String strError)
					{
						irpCurrentlyProcessing.setUsbException(new UsbTransferNoDeviceException(strError));
					}

					@Override
					public void onTransferError(final String strError)
					{
						irpCurrentlyProcessing.setUsbException(new UsbTransferErrorException(strError));
					}

					@Override
					public void onTransferCancelled(final String strError)
					{
						irpCurrentlyProcessing.setUsbException(new UsbTransferCancelledException(strError));
					}

					@Override
					public void onTransferAborted()
					{
						irpCurrentlyProcessing.setUsbException(new UsbAbortException());
					}
				});
			}
			catch (final InterruptedException e)
			{
				irpCurrentlyProcessing.setUsbException(new UsbAbortException());
			}
			catch (final UsbException e)
			{
				irpCurrentlyProcessing.setUsbException(e);
			}
		}
	}

	/**
	 * Processes the IRP.
	 *
	 * @param irp The IRP to processUsbIrpQueue.
	 * @throws UsbException When processUsbIrpQueueing the IRP fails.
	 * @throws InterruptedException When during the processing of the IRP the IRP is aborted.
	 */
	protected abstract void processIrp(final T irp, final ProcessIrpCallback callback) throws UsbException, InterruptedException;

	/**
	 * Called after IRP has finished. This can be implemented to send events for
	 * example.
	 *
	 * @param irp The IRP which has been finished.
	 */
	protected abstract void finishIrp(final IUsbIrp irp);

	/**
	 * Aborts all queued IRPs. The IRP which is currently processUsbIrpQueueed
	 * can't be aborted. This method returns as soon as no more IRPs are in the
	 * queue and no more are processUsbIrpQueueed.
	 */
	public final void abortAllSubmissions()
	{
		aborting = true;
		usbIrpQueue.clear();
		usbIrpQueueProcessorThread.interrupt();
		aborting = false;
	}

	public final void abortSubmission(final T irp)
	{
		irp.setUsbException(new UsbAbortException());

		final boolean irpWasNotYetProcessed = usbIrpQueue.remove(irp);
		if (irpWasNotYetProcessed)
		{
			return;
		}

		if (irp == irpCurrentlyProcessing)
		{
			usbIrpQueueProcessorThread.interrupt();
		}
	}

	/**
	 * Checks if queue is busy. A busy queue is a queue which is currently
	 * processUsbIrpQueueing IRPs or which still has IRPs in the queue.
	 *
	 * @return True if queue is busy, false if not.
	 */
	public final boolean isBusy()
	{
		return !usbIrpQueue.isEmpty();
	}

	/**
	 * Processes a control IRP. This method is places here (in the Abstract
	 * IrpQueue) so that it may support bother the ControlIrpQueue and the
	 * (Pipe-scoped) IrpQueue.
	 *
	 * @param irp The IRP to processUsbIrpQueue.
	 * @throws UsbException When processUsbIrpQueueing the IRP fails.
	 */
	protected final void processControlIrp(final IUsbControlIrp irp, final ProcessIrpCallback callback) throws UsbException
	{
		doControlTransfer(irp, callback);
	}

	protected abstract void doControlTransfer(final IUsbControlIrp irp, final ProcessIrpCallback callback) throws UsbException;

	/**
	 * Checks if this queue is currently aborting.
	 *
	 * @return True if queue is aborting, false if not.
	 */
	protected final boolean isAborting()
	{
		return aborting;
	}
}
