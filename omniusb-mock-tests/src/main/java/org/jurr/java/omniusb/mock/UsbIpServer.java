package org.jurr.java.omniusb.mock;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Scanner;

import javax.usb3.ri.UsbDeviceId;

import org.jurr.java.omniusb.usbip.server.ServerListenThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsbIpServer
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final int port;
	private ServerListenThread serverThread;

	public static void main(String[] args) throws NumberFormatException, IOException
	{
		if (args.length > 1) // TODO: We need some proper argument parsing here
		{
			LOGGER.error("Usage: UsbIpServer [port] (default port is {}", ServerListenThread.DEFAULT_PORT);
			System.exit(1);
		}

		final int port = args.length > 0 ? Integer.valueOf(args[0]) : ServerListenThread.DEFAULT_PORT;
		new UsbIpServer(port).start();
	}

	public UsbIpServer(final int port) throws IOException
	{
		this.port = port;
		serverThread = new ServerListenThread(port);
	}

	public void start() throws IOException
	{
		final var mockMouseDeviceId = MockInitializer.startMockMouse();
		final var mockMemoryStickDeviceId = MockInitializer.startMockMemoryStick();

		LOGGER.info("Starting USB/IP server...");
		serverThread.start();

		LOGGER.info("Do the following command first: sudo modprobe vhci-hcd");
		LOGGER.info("To view USB devices exposed via USB/IP, do: usbip list -r localhost");

		LOGGER.info("To attach the mock mouse to the Linux kernel, do: sudo usbip attach --tcp-port {} -r localhost -b {}-{} (or simply give the 'm' command)", port, mockMouseDeviceId.getBusNumber(), mockMouseDeviceId.getDeviceAddress());
		LOGGER.info("To attach the mock memory stick to the Linux kernel, do: sudo usbip attach --tcp-port {} -r localhost -b {}-{} (or simply give the 's' command)", port, mockMemoryStickDeviceId.getBusNumber(), mockMemoryStickDeviceId.getDeviceAddress());
		LOGGER.info("To detach from the Linux kernel, do: sudo usbip detach -p 00");
		LOGGER.info("");
		LOGGER.info("The following commands are available:");
		LOGGER.info("  m   - Attach mock mouse to the USB/IP device (requires sudo)");
		LOGGER.info("  s   - Attach mock memory stick to the USB/IP device (requires sudo)");
		LOGGER.info("  q   - Quit the application");

		Thread.currentThread().setName("Main thread");
		boolean running = true;
		try (Scanner scan = new Scanner(System.in))
		{
			while (running)
			{
				final String input = scan.next();
				if (input.equals("m"))
				{
					attachUspIpToLocalhost(mockMouseDeviceId);
				}
				else if (input.equals("s"))
				{
					// final var mockMemoryStickIpAddress = mockMemoryStickDeviceId.getBusNumber() + "-" + mockMemoryStickDeviceId.getDeviceAddress();
					attachUspIpToLocalhost(mockMemoryStickDeviceId);
				}
				else if (input.equals("q"))
				{
					running = false;
				}
				else
				{
					LOGGER.error("Invalid input: '{}'. Expected 'r' to reset or 'q' to stop.", input);
				}
			}
		}

		LOGGER.info("Stopping USB/IP server...");
		serverThread.stopExecuting();
		serverThread.interrupt();

		try
		{
			serverThread.join();
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * This function attaches the given USB/IP device to the localhost using the usbip command.
	 * It first tries to do this with sudo, and if that fails, it tries again with pkexec. The reason for this is that sudo will not ask for a password if the user has recently used sudo, while pkexec will always ask for a password.
	 * The pkexec call will also first do a 'modprobe vhci-hcd' to make sure that the vhci-hcd kernel module is loaded, which is required for the usbip attach command to work.
	 *
	 * @param mockDeviceId The USB device ID of the device to attach
	 */
	private void attachUspIpToLocalhost(final UsbDeviceId mockDeviceId)
	{
		final String mockDeviceUsbIpId = mockDeviceId.getBusNumber() + "-" + mockDeviceId.getDeviceAddress();
		try
		{
			var process = new ProcessBuilder("/usr/bin/sudo", "/usr/bin/usbip", "--tcp-port", Integer.toString(port), "attach", "-r", "localhost", "-b", mockDeviceUsbIpId).start();
			final int exitValue = process.waitFor();
			if (exitValue == 0)
			{
				LOGGER.info("USB/IP device {} attached successfully.", mockDeviceUsbIpId);
				return;
			}

			process = new ProcessBuilder("/usr/bin/pkexec", "/bin/bash", "-c", "modprobe vhci-hcd && usbip --tcp-port " + port + " attach -r localhost -b " + mockDeviceUsbIpId).start();
			final int exitValue2 = process.waitFor();
			if (exitValue2 == 0)
			{
				LOGGER.info("USB/IP device {} attached successfully.", mockDeviceUsbIpId);
				return;
			}

			for (String line : new String(process.getErrorStream().readAllBytes()).split("\n"))
			{
				LOGGER.error(line);
			}
			for (String line : new String(process.getInputStream().readAllBytes()).split("\n"))
			{
				LOGGER.info(line);
			}

			LOGGER.error("Failed to attach USB/IP device {}. Please ensure you have the necessary permissions. Exit code was {}.", mockDeviceUsbIpId, exitValue);
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			LOGGER.error("Thread was interrupted while trying to attach USB/IP device {}: {}", mockDeviceUsbIpId, e.getMessage());
		}
		catch (IOException e)
		{
			LOGGER.error("Error while trying to attach USB/IP device {}: {}", mockDeviceUsbIpId, e.getMessage());
		}
	}
}
