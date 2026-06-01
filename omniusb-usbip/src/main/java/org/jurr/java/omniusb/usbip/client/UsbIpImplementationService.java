package org.jurr.java.omniusb.usbip.client;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import javax.usb3.ri.UsbImplementationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsbIpImplementationService extends UsbImplementationService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final int DEFAULT_USBIP_PORT = 3240;

	/**
	 * The implementation description.
	 */
	private static final String IMP_DESCRIPTION = "USB/IP client implementation";

	/**
	 * The implementation version. This is the Java source code version.
	 */
	private static final String IMP_VERSION = "1.4.x";

	/**
	 * The API version.
	 */
	private static final String API_VERSION = "1.0.0";

	@Override
	public String getApiVersion()
	{
		return API_VERSION;
	}

	@Override
	public String getImpVersion()
	{
		return IMP_VERSION;
	}

	@Override
	public String getImpDescription()
	{
		return IMP_DESCRIPTION;
	}

	public void connectToUsbIpServer(final String server) throws IOException
	{
		connectToUsbIpServer(server, DEFAULT_USBIP_PORT);
	}

	public void connectToUsbIpServer(final String server, final int port) throws IOException
	{
		final UsbIpServerConnection usbIpServerConnection = new UsbIpServerConnection(getDeviceId(), getDeviceId(), port, server, port);
		connectUsbDevice(usbIpServerConnection);

		LOGGER.info("Connected to USB/IP server {}:{}.", server, port);
	}
}
