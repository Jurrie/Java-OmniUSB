package org.jurr.java.omniusb.mock;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Scanner;

import javax.usb3.ri.UsbDeviceId;

import org.apache.commons.lang3.SystemUtils;
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

		if (SystemUtils.IS_OS_LINUX)
		{
			LOGGER.info("Do the following command first: sudo modprobe vhci-hcd");
			LOGGER.info("To view USB devices exposed via USB/IP, do: usbip --tcp-port {} list -r localhost", port);
			LOGGER.info("To attach the mock mouse to the Linux kernel, do: sudo usbip --tcp-port {} attach -r localhost -b {}-{} (or simply give the 'm' command)", port, mockMouseDeviceId.getBusNumber(), mockMouseDeviceId.getDeviceAddress());
			LOGGER.info("To attach the mock memory stick to the Linux kernel, do: sudo usbip --tcp-port {} attach -r localhost -b {}-{} (or simply give the 's' command)", port, mockMemoryStickDeviceId.getBusNumber(), mockMemoryStickDeviceId.getDeviceAddress());
			LOGGER.info("To detach from the Linux kernel, do: sudo usbip detach -p 00");
			LOGGER.info("");
			LOGGER.info("The following commands are available:");
			LOGGER.info("  m   - Attach mock mouse to the USB/IP device (requires sudo)");
			LOGGER.info("  s   - Attach mock memory stick to the USB/IP device (requires sudo)");
			LOGGER.info("  q   - Quit the application");
		}
		else if (SystemUtils.IS_OS_WINDOWS)
		{
			LOGGER.info("You should first install USB/IP for Windows. https://github.com/vadimgrn/usbip-win2 is verified to work, but there are others.");
			LOGGER.info("After installing, you can use the GUI to connect to localhost and attach the mock devices.");
			LOGGER.info("Alternatively, you can use the commandline.");
			LOGGER.info("To view USB devices exposed via USB/IP, do: usbip.exe --tcp-port {} list -r localhost", port);
			LOGGER.info("To attach the mock mouse to the Linux kernel, do: usbip.exe --tcp-port {} attach -r localhost -b {}-{} (or simply give the 'm' command)", port, mockMouseDeviceId.getBusNumber(), mockMouseDeviceId.getDeviceAddress());
			LOGGER.info("To attach the mock memory stick to the Linux kernel, do: usbip.exe --tcp-port {} attach -r localhost -b {}-{} (or simply give the 's' command)", port, mockMemoryStickDeviceId.getBusNumber(), mockMemoryStickDeviceId.getDeviceAddress());
			LOGGER.info("To detach from the Linux kernel, do: sudo usbip detach -p 00");
			LOGGER.info("");
			LOGGER.info("The following commands are available:");
			LOGGER.info("  m   - Attach mock mouse to the USB/IP device (requires sudo)");
			LOGGER.info("  s   - Attach mock memory stick to the USB/IP device (requires sudo)");
			LOGGER.info("  q   - Quit the application");
		}

		Thread.currentThread().setName("Main thread");
		boolean running = true;
		try (Scanner scan = new Scanner(System.in))
		{
			while (running)
			{
				final String input = scan.next();
				if (input.equals("m"))
				{
					attachUsbIpToLocalhost(mockMouseDeviceId);
				}
				else if (input.equals("s"))
				{
					attachUsbIpToLocalhost(mockMemoryStickDeviceId);
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

	private void attachUsbIpToLocalhost(final UsbDeviceId mockDeviceId)
	{
		if (SystemUtils.IS_OS_LINUX)
		{
			attachUsbIpToLocalhostOnLinux(mockDeviceId);
		}
		else if (SystemUtils.IS_OS_WINDOWS)
		{
			attachUsbIpToLocalhostOnWindows(mockDeviceId);
		}
		else
		{
			LOGGER.error("Unsupported operating system. Only Linux and Windows are supported. Please attach the USB/IP device manually using the appropriate command for your OS.");
		}
	}

	/**
	 * This function attaches the given USB/IP device to the localhost using the usbip command.
	 * It first tries to do this with sudo, and if that fails, it tries again with pkexec. The reason for this is that sudo will not ask for a password if the user has recently used sudo, while pkexec will always ask for a password.
	 * The pkexec call will also first do a 'modprobe vhci-hcd' to make sure that the vhci-hcd kernel module is loaded, which is required for the usbip attach command to work.
	 *
	 * @param mockDeviceId The USB device ID of the device to attach
	 */
	private void attachUsbIpToLocalhostOnLinux(final UsbDeviceId mockDeviceId)
	{
		final String mockDeviceUsbIpId = mockDeviceId.getBusNumber() + "-" + mockDeviceId.getDeviceAddress();
		try
		{
			final ProcessBuilder processBuilder = new ProcessBuilder("/usr/bin/sudo", "/usr/bin/usbip", "--tcp-port", Integer.toString(port), "attach", "-r", "localhost", "-b", mockDeviceUsbIpId).inheritIO();
			LOGGER.debug(mockDeviceUsbIpId + " attach command: " + String.join(" ", processBuilder.command()));
			try
			{
				final var process = processBuilder.start();
				final var exitValue = process.waitFor();
				if (exitValue == 0)
				{
					LOGGER.info("USB/IP device {} attached successfully.", mockDeviceUsbIpId);
					return;
				}
				else
				{
					LOGGER.debug("sudo usbip attach command failed with exit code {}. Trying again with pkexec.", exitValue);
				}
			}
			catch (IOException e)
			{
				LOGGER.debug("sudo usbip attach command failed with IOException: {}. Trying again with pkexec.", e.getMessage());
			}

			final ProcessBuilder processBuilder2 = new ProcessBuilder("/usr/bin/pkexec", "/bin/bash", "-c", "modprobe vhci-hcd && usbip --tcp-port " + port + " attach -r localhost -b " + mockDeviceUsbIpId).inheritIO();
			LOGGER.debug(mockDeviceUsbIpId + " attach command: " + String.join(" ", processBuilder2.command()));
			try
			{
				final var process = processBuilder2.start();
				final int exitValue = process.waitFor();
				if (exitValue == 0)
				{
					LOGGER.info("USB/IP device {} attached successfully.", mockDeviceUsbIpId);
					return;
				}

				LOGGER.error("Failed to attach USB/IP device {}. Please ensure you have the necessary permissions. Exit code was {}.", mockDeviceUsbIpId, exitValue);
			}
			catch (IOException e)
			{
				LOGGER.error("Error while trying to attach USB/IP device {}: {}", mockDeviceUsbIpId, e.getMessage());
			}
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			LOGGER.error("Thread was interrupted while trying to attach USB/IP device {}: {}", mockDeviceUsbIpId, e.getMessage());
		}
	}

	private void attachUsbIpToLocalhostOnWindows(final UsbDeviceId mockDeviceId)
	{
		final String mockDeviceUsbIpId = mockDeviceId.getBusNumber() + "-" + mockDeviceId.getDeviceAddress();
		try
		{
			final ProcessBuilder processBuilder = new ProcessBuilder("usbip.exe", "--tcp-port", Integer.toString(port), "attach", "-r", "localhost", "-b", mockDeviceUsbIpId).inheritIO();
			LOGGER.debug(mockDeviceUsbIpId + " attach command: " + String.join(" ", processBuilder.command()));
			try
			{
				final var process = processBuilder.start();
				final var exitValue = process.waitFor();
				if (exitValue == 0)
				{
					LOGGER.info("USB/IP device {} attached successfully.", mockDeviceUsbIpId);
					return;
				}
				else
				{
					LOGGER.debug("usbip.exe attach command failed with exit code {}. Trying again with directory C:\\Program Files\\USBip\\", exitValue);
				}
			}
			catch (IOException e)
			{
				LOGGER.debug("usbip.exe attach command failed with IOException: {}. Trying again with directory C:\\Program Files\\USBip\\", e.getMessage());
			}

			final ProcessBuilder processBuilder2 = new ProcessBuilder("C:\\Program Files\\USBip\\usbip.exe", "--tcp-port", Integer.toString(port), "attach", "-r", "localhost", "-b", mockDeviceUsbIpId).inheritIO();
			LOGGER.debug(mockDeviceUsbIpId + " attach command: " + String.join(" ", processBuilder2.command()));
			try
			{
				final var process = processBuilder2.start();
				final var exitValue = process.waitFor();
				if (exitValue == 0)
				{
					LOGGER.info("USB/IP device {} attached successfully.", mockDeviceUsbIpId);
					return;
				}
				else
				{
					LOGGER.debug("usbip.exe attach command failed with exit code {}.", exitValue);
				}

				LOGGER.error("Failed to attach USB/IP device {}. Please ensure you have the necessary permissions and usbip.exe is on your path. Exit code was {}.", mockDeviceUsbIpId, exitValue);
			}
			catch (IOException e)
			{
				LOGGER.error("Error while trying to attach USB/IP device {}: {}", mockDeviceUsbIpId, e.getMessage());
			}
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			LOGGER.error("Thread was interrupted while trying to attach USB/IP device {}: {}", mockDeviceUsbIpId, e.getMessage());
		}
	}
}
