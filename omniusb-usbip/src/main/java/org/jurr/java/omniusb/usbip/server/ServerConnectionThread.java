package org.jurr.java.omniusb.usbip.server;

import static org.jurr.java.omniusb.usbip.Constants.USBIP_RET_DEV_BUSY;
import static org.jurr.java.omniusb.usbip.Constants.USBIP_RET_DEV_NOT_FOUND;
import static org.jurr.java.omniusb.usbip.Constants.USBIP_RET_ERROR_STATE;
import static org.jurr.java.omniusb.usbip.Constants.USBIP_RET_FAILED;
import static org.jurr.java.omniusb.usbip.Constants.USBIP_VERSION;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.usb3.IUsbConfiguration;
import javax.usb3.IUsbControlIrp;
import javax.usb3.IUsbDevice;
import javax.usb3.IUsbEndpoint;
import javax.usb3.IUsbHub;
import javax.usb3.IUsbInterface;
import javax.usb3.IUsbPipe;
import javax.usb3.UsbHostManager;
import javax.usb3.enumerated.EDeviceRequest;
import javax.usb3.exception.UsbDisconnectedException;
import javax.usb3.exception.UsbException;
import javax.usb3.exception.UsbNotActiveException;
import javax.usb3.exception.UsbNotOpenException;
import javax.usb3.request.BMRequestType;
import javax.usb3.request.BMRequestType.ERecipient;
import javax.usb3.request.BMRequestType.EType;
import javax.usb3.request.BRequest;
import javax.usb3.ri.IUsbDeviceWithId;
import javax.usb3.ri.UsbIrp;

import org.jurr.java.omniusb.usbip.HexFormatUtils;
import org.jurr.java.omniusb.usbip.domain.CommandFactory;
import org.jurr.java.omniusb.usbip.domain.DeviceListCommand;
import org.jurr.java.omniusb.usbip.domain.DeviceListResponse;
import org.jurr.java.omniusb.usbip.domain.ImportDeviceCommand;
import org.jurr.java.omniusb.usbip.domain.ImportDeviceResponse;
import org.jurr.java.omniusb.usbip.domain.SetupPacket;
import org.jurr.java.omniusb.usbip.domain.UnknownCommand;
import org.jurr.java.omniusb.usbip.domain.UsbDeviceDetails;
import org.jurr.java.omniusb.usbip.domain.UsbIpCommand;
import org.jurr.java.omniusb.usbip.domain.UsbIpHeaderBasic;
import org.jurr.java.omniusb.usbip.domain.UsbIpSubmitCommand;
import org.jurr.java.omniusb.usbip.domain.UsbIpSubmitResponse;
import org.jurr.java.omniusb.usbip.domain.UsbIpUnlinkCommand;
import org.jurr.java.omniusb.usbip.domain.UsbIpUnlinkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerConnectionThread extends Thread
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final Socket clientSocket;
	private final Map<Integer, IUsbDeviceWithId> attachedDevices;
	private final List<IUsbInterface> interfacesClaimed;
	private final List<IUsbPipe> pipesOpened;
	private final Map<Integer, ProcessIrpTask> irpsInQueue;
	private final ExecutorService irpExecutorService = Executors.newCachedThreadPool();

	private boolean shouldExit;

	protected ServerConnectionThread(final Socket clientSocket) throws SocketException
	{
		this.clientSocket = clientSocket;
		this.clientSocket.setTcpNoDelay(true);

		attachedDevices = new HashMap<>();
		interfacesClaimed = new ArrayList<>();
		pipesOpened = new ArrayList<>();
		irpsInQueue = Collections.synchronizedMap(new HashMap<>());

		LOGGER.info("Client {} connected", clientSocket.getInetAddress());
	}

	public void stopExecuting() throws IOException
	{
		if (shouldExit)
		{
			return;
		}

		LOGGER.info("Client {} stopping", clientSocket.getInetAddress());

		shouldExit = true;

		for (ProcessIrpTask task : irpsInQueue.values())
		{
			LOGGER.info("Client {}: stopping task #{}", clientSocket.getInetAddress(), task.getSeqNum());
			task.shutdown();
		}

		clientSocket.close();

		pipesOpened.parallelStream().forEach(t -> {
			try
			{
				t.abortAllSubmissions();
				t.close();
			}
			catch (UsbNotActiveException | UsbNotOpenException | UsbDisconnectedException | UsbException e)
			{
				LOGGER.error("Error closing pipe", e);
			}
		});
		pipesOpened.clear();

		interfacesClaimed.parallelStream().forEach(t -> {
			try
			{
				t.release();
			}
			catch (UsbNotActiveException | UsbDisconnectedException | UsbException e)
			{
				LOGGER.error("Error releasing interface", e);
			}
		});
		interfacesClaimed.clear();

		attachedDevices.forEach((i, d) -> {
			d.close();
		});
		attachedDevices.clear();

		try
		{
			irpExecutorService.shutdownNow();
			irpExecutorService.awaitTermination(3, TimeUnit.SECONDS);
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName("USBIP client thread (" + clientSocket.getInetAddress() + ")");

		while (!shouldExit)
		{
			try
			{
				final InputStream inputStream = clientSocket.getInputStream();
				final BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

				try
				{
					handleCommands(bufferedInputStream);
				}
				catch (EOFException e)
				{
					stopExecuting();
					return;
				}
			}
			catch (IOException e)
			{
				if (!shouldExit)
				{
					LOGGER.error("I/O error in client thread", e);
					try
					{
						stopExecuting();
					}
					catch (Exception ignored)
					{
						// Ignore because we are stopping
					}
				}
			}
			catch (Exception e)
			{
				LOGGER.error(e.getClass().getSimpleName() + " in client thread", e);
				try
				{
					stopExecuting();
				}
				catch (Exception ignored)
				{
					// Ignore because we are stopping
				}
			}
		}
		LOGGER.debug("Client thread stopped");
	}

	private void handleCommands(final BufferedInputStream inputStream) throws IOException, UsbException
	{
		while (true)
		{
			final UsbIpCommand command = CommandFactory.getCommand(inputStream);

			switch (command)
			{
			case UsbIpSubmitCommand usbIpSubmitCommand -> doUrbSubmitCommand(usbIpSubmitCommand);
			case UsbIpUnlinkCommand usbIpUnlinkCommand -> doUrbUnlinkCommand(usbIpUnlinkCommand);
			case DeviceListCommand deviceListCommand -> doRequestDeviceListCommand(deviceListCommand);
			case ImportDeviceCommand importDeviceCommand -> doRequestImportDeviceCommand(importDeviceCommand);
			default -> {
				final UnknownCommand unknownCommand = (UnknownCommand) command;
				LOGGER.error("Client {}: unsupported command: {}", clientSocket.getInetAddress(), unknownCommand.getCommandCode());
				clientSocket.getOutputStream().write(unknownCommand.toErrorResponse(USBIP_RET_FAILED));
			}
			}
		}
	}

	private void doUrbSubmitCommand(final UsbIpSubmitCommand command) throws IOException, IllegalArgumentException, UsbDisconnectedException, UsbException
	{
		final UsbIpHeaderBasic usbIpHeaderBasic = command.getUsbIpHeaderBasic();

		LOGGER.debug("Client {} #{}: USBIP_CMD_SUBMIT transferFlags: {}, setupPacket: {} (busId: {}, devNum: {}, direction: {}, endpoint: {})", clientSocket.getInetAddress(), usbIpHeaderBasic.getSeqNum(), command.getTransferFlags(), command.getSetupPacket(), HexFormatUtils.format(usbIpHeaderBasic.getBusId()), HexFormatUtils.format(usbIpHeaderBasic.getDevNum()), usbIpHeaderBasic.getDirection(), usbIpHeaderBasic.getEp());

		final IUsbDeviceWithId usbDevice = attachedDevices.get(usbIpHeaderBasic.getDevId());
		if (usbDevice == null)
		{
			LOGGER.error("Client {} #{}: submit requested for non-attached device with bus ID {}", clientSocket.getInetAddress(), usbIpHeaderBasic.getSeqNum(), usbIpHeaderBasic.getBusId());

			final UsbIpSubmitResponse usbIpSubmitResponse = UsbIpSubmitResponse.errorResponse(usbIpHeaderBasic.getSeqNum(), 1);
			clientSocket.getOutputStream().write(usbIpSubmitResponse.toBuffer());
		}
		else if (usbIpHeaderBasic.getBEndpointAddress().getEndPointNumber() == 0)
		{
			final IUsbControlIrp controlIrp = fromSetupPacket(usbDevice, command.getSetupPacket(), command.getTransferBuffer());

			// TODO: Check for 'setAltInt', claim before, handle after

			final BMRequestType bmRequestType = command.getSetupPacket().getBmRequestType();
			final BRequest request = command.getSetupPacket().getBRequest();

			if (bmRequestType.getType() == EType.STANDARD && bmRequestType.getRecipient() == ERecipient.INTERFACE && request.getDeviceRequest() == EDeviceRequest.SET_INTERFACE)
			{
				// Special handling for SET_INTERFACE to update the active setting in the UsbInterface
				final byte interfaceNumber = (byte) (controlIrp.wIndex() & 0xFF);
				final int alternateSetting = controlIrp.wValue() & 0xFF;

				LOGGER.debug("Processing SET_INTERFACE request - interface number: {}, alternate setting: {}", interfaceNumber, alternateSetting);

				final IUsbConfiguration activeUsbConfiguration = usbDevice.getActiveUsbConfiguration();
				final Map<Integer, IUsbInterface> settings = activeUsbConfiguration.getSettings(interfaceNumber);
				final IUsbInterface iUsbInterface = settings.get(alternateSetting);
				LOGGER.debug("Found interface for alternate setting {}: {}", alternateSetting, iUsbInterface);

				if (!interfacesClaimed.stream().anyMatch(i -> i.getUsbInterfaceDescriptor().bInterfaceNumber() == iUsbInterface.getUsbInterfaceDescriptor().bInterfaceNumber()))
				{
					LOGGER.debug("Client {} #{}: claiming interface {}", clientSocket.getInetAddress(), usbIpHeaderBasic.getSeqNum(), iUsbInterface.getUsbInterfaceDescriptor().bInterfaceNumber());
					iUsbInterface.claim();
					interfacesClaimed.add(iUsbInterface);
					LOGGER.debug("Client {} #{}: interface {} claimed", clientSocket.getInetAddress(), usbIpHeaderBasic.getSeqNum(), iUsbInterface.getUsbInterfaceDescriptor().bInterfaceNumber());
				}

				try
				{
					activeUsbConfiguration.setUsbInterface(interfaceNumber, iUsbInterface);
				}
				catch (UsbException e)
				{
					LOGGER.error("Error setting alternate interface {} for interface number {}", alternateSetting, interfaceNumber, e);
					controlIrp.setUsbException(e);
				}

				LOGGER.debug("SET_INTERFACE request - set interface number {} to alternate setting {}", interfaceNumber, alternateSetting);
			}

			final ProcessIrpTask task = new ProcessIrpTask(usbIpHeaderBasic, clientSocket, usbDevice, controlIrp)
			{
				@Override
				public void onComplete()
				{
					irpsInQueue.remove(getSeqNum());
				}
			};
			irpsInQueue.put(task.getSeqNum(), task);
			irpExecutorService.submit(task);
		}
		else
		{
			final UsbIrp irp = new UsbIrp(command.getTransferBuffer());
			irp.setIsochronousPackets(command.getIsoPackets());

			final IUsbEndpoint usbEndpoint = usbDevice.getActiveUsbConfiguration().getUsbEndpoint(usbIpHeaderBasic.getBEndpointAddress().getByteCode());
			if (usbEndpoint == null)
			{
				LOGGER.error("Client {} #{}: submit requested for non-existing endpoint {} (dir {}) with bus ID {}", clientSocket.getInetAddress(), usbIpHeaderBasic.getSeqNum(), usbIpHeaderBasic.getBEndpointAddress().getEndPointNumber(), usbIpHeaderBasic.getBEndpointAddress().getDirection(), usbIpHeaderBasic.getBusId());

				final UsbIpSubmitResponse usbIpSubmitResponse = UsbIpSubmitResponse.errorResponse(usbIpHeaderBasic.getSeqNum(), 1);
				clientSocket.getOutputStream().write(usbIpSubmitResponse.toBuffer());
				return;
			}

			final IUsbInterface usbInterface = usbEndpoint.getUsbInterface();
			if (!interfacesClaimed.stream().anyMatch(i -> i.getUsbInterfaceDescriptor().bInterfaceNumber() == usbInterface.getUsbInterfaceDescriptor().bInterfaceNumber()))
			{
				usbInterface.claim(); // TODO: This goes south here. The alternative setting is set using LibUsb, but not registered in javax.usb, so we get exception from that here.
				interfacesClaimed.add(usbInterface);
				LOGGER.debug("Client {} #{}: interface {} claimed", clientSocket.getInetAddress(), usbIpHeaderBasic.getSeqNum(), usbInterface.getUsbInterfaceDescriptor().bInterfaceNumber());
			}

			final IUsbPipe usbPipe = usbEndpoint.getUsbPipe();
			if (!pipesOpened.contains(usbPipe))
			{
				usbPipe.open();
				pipesOpened.add(usbPipe);
			}

			final ProcessIrpTask task = new ProcessIrpTask(usbIpHeaderBasic, clientSocket, usbPipe, irp)
			{
				@Override
				public void onComplete()
				{
					irpsInQueue.remove(getSeqNum());
				}
			};
			irpsInQueue.put(task.getSeqNum(), task);
			irpExecutorService.submit(task);
		}
	}

	private static IUsbControlIrp fromSetupPacket(final IUsbDevice usbDevice, final SetupPacket setupPacket, final byte[] transferBuffer)
	{
		if (transferBuffer.length < setupPacket.wLength())
		{
			throw new IllegalArgumentException("Transfer buffer must be at least " + setupPacket.wLength() + " bytes long");
		}

		return usbDevice.createUsbControlIrp(setupPacket.bmRequestType(), setupPacket.bRequest(), setupPacket.wValue(), setupPacket.wIndex(), transferBuffer);
	}

	private void doUrbUnlinkCommand(final UsbIpUnlinkCommand command) throws IOException, UsbException
	{
		final UsbIpHeaderBasic usbIpHeaderBasic = command.getUsbIpHeaderBasic();

		final int seqNumToUnlink = command.getSeqNum();

		final IUsbDeviceWithId usbDevice = attachedDevices.get(usbIpHeaderBasic.getDevId());
		if (usbDevice == null)
		{
			LOGGER.error("Client {} #{}: unlink requested for non-attached device with bus ID {}", clientSocket.getInetAddress(), usbIpHeaderBasic.getSeqNum(), usbIpHeaderBasic.getBusId());

			final UsbIpUnlinkResponse usbIpUnlinkResponse = UsbIpUnlinkResponse.errorResponse(usbIpHeaderBasic.getSeqNum());
			clientSocket.getOutputStream().write(usbIpUnlinkResponse.toBuffer());
		}
		else
		{
			final ProcessIrpTask irpToAbort = irpsInQueue.remove(seqNumToUnlink);
			if (irpToAbort == null)
			{
				LOGGER.error("Client {} #{}: unlink requested for non-existing URB with seqNum {}", clientSocket.getInetAddress(), usbIpHeaderBasic.getSeqNum(), seqNumToUnlink);
				final UsbIpUnlinkResponse usbIpUnlinkResponse = UsbIpUnlinkResponse.errorResponse(usbIpHeaderBasic.getSeqNum());
				clientSocket.getOutputStream().write(usbIpUnlinkResponse.toBuffer());
			}
			else
			{
				irpToAbort.abort(usbIpHeaderBasic.getSeqNum());
			}
		}
	}

	// You trigger this with usbip --tcp-port <port> list -r <server>
	// For example: usbip list -r localhost
	private void doRequestDeviceListCommand(final DeviceListCommand command) throws IOException, SecurityException, UsbException
	{
		if (command.getUsbIpVersion() != USBIP_VERSION)
		{
			clientSocket.getOutputStream().write(DeviceListResponse.error(USBIP_RET_FAILED));
			return;
		}

		final IUsbHub rootUsbHub = UsbHostManager.getUsbServices().getRootUsbHub();
		final List<IUsbDeviceWithId> attachedUsbDevices = new ArrayList<>();
		fillListOfExportableUsbDevices(attachedUsbDevices, rootUsbHub);
		final List<UsbDeviceDetails> usbDeviceDetails = ServerUtils.convertToUsbDeviceDetails(attachedUsbDevices);

		final DeviceListResponse response = new DeviceListResponse(usbDeviceDetails);

		clientSocket.getOutputStream().write(response.toBuffer());
	}

	private void fillListOfExportableUsbDevices(final List<IUsbDeviceWithId> exportableDevices, final IUsbHub usbHub)
	{
		usbHub.getAttachedUsbDevices().forEach(device -> {
			if (device instanceof IUsbHub iUsbHub)
			{
				fillListOfExportableUsbDevices(exportableDevices, iUsbHub);
			}
			else if (device instanceof IUsbDeviceWithId usbDeviceWithId)
			{
				exportableDevices.add(usbDeviceWithId);
			}
			else
			{
				// Skip devices that do not implement IUserDeviceWithId because we cannot give them a unique bus ID
			}
		});
	}

	// You trigger this with sudo usbip --tcp-port <port> attach -r <server> -b <bus ID>
	// For example: sudo usbip attach -r localhost -b 1-1
	// Do not forget to sudo modprobe vhci-hcd first
	private void doRequestImportDeviceCommand(final ImportDeviceCommand command) throws IOException, SecurityException, UsbException
	{
		if (command.getUsbIpVersion() != USBIP_VERSION)
		{
			clientSocket.getOutputStream().write(ImportDeviceResponse.error(USBIP_RET_FAILED));
			return;
		}

		final String requestedBusId = command.getRequestedBusId();

		final IUsbHub rootUsbHub = UsbHostManager.getUsbServices().getRootUsbHub();
		final List<IUsbDeviceWithId> attachedUsbDevices = new ArrayList<>();
		fillListOfExportableUsbDevices(attachedUsbDevices, rootUsbHub);

		IUsbDeviceWithId deviceToAttach = null;
		for (IUsbDeviceWithId usbDevice : attachedUsbDevices)
		{
			if (ServerUtils.getBusId(usbDevice).equals(requestedBusId))
			{
				deviceToAttach = usbDevice;
			}
		}

		if (deviceToAttach == null)
		{
			// Device not found
			LOGGER.error("Client {}: requested attach for device with bus ID {} but it was not found", clientSocket.getInetAddress(), requestedBusId);
			clientSocket.getOutputStream().write(ImportDeviceResponse.error(USBIP_RET_DEV_NOT_FOUND));
			return;
		}

		// Device is found, attach it
		final int devId = deviceToAttach.getDeviceId().getBusNumber() << 16 | deviceToAttach.getDeviceId().getPortNumber();
		if (attachedDevices.containsKey(devId))
		{
			LOGGER.error("Client {}: requested attach for device with bus ID {} but it was already attached", clientSocket.getInetAddress(), requestedBusId);
			clientSocket.getOutputStream().write(ImportDeviceResponse.error(USBIP_RET_DEV_BUSY));
		}
		else if (!attachedDevices.isEmpty())
		{
			LOGGER.error("Client {}: requested attach for device with bus ID {} but another device is already attached", clientSocket.getInetAddress(), requestedBusId);
			clientSocket.getOutputStream().write(ImportDeviceResponse.error(USBIP_RET_ERROR_STATE));
		}
		else
		{
			attachedDevices.put(devId, deviceToAttach);
			// TODO: we should perhaps also lock USB devices?

			LOGGER.info("Client {}: attaching device with bus ID: {}", clientSocket.getInetAddress(), requestedBusId);

			final UsbDeviceDetails usbDeviceDetails = ServerUtils.convertToUsbDeviceDetails(deviceToAttach);

			final ImportDeviceResponse response = new ImportDeviceResponse(usbDeviceDetails);
			clientSocket.getOutputStream().write(response.toBuffer());
		}
	}
}
