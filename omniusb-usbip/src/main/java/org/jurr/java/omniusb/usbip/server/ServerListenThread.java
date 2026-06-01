package org.jurr.java.omniusb.usbip.server;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerListenThread extends Thread
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static final int DEFAULT_PORT = 3240;

	private final ServerSocket serverSocket;
	private final List<ServerConnectionThread> clientThreads;

	private boolean shouldExit;

	/**
	 * Utility method to create a ServerListenThread listening on a random port.
	 *
	 * @return a ServerListenThread listening on a random port.
	 * @see {@link #getPort()} for the port number assigned.
	 * @throws IOException
	 */
	public static ServerListenThread createWithRandomPort() throws IOException
	{
		return new ServerListenThread(0);
	}

	/**
	 * Utility method to create a ServerListenThread listening on the default USB/IP port.
	 *
	 * @return a ServerListenThread listening on the default USB/IP port (which is {@value #DEFAULT_PORT}).
	 * @see {@link #DEFAULT_PORT}
	 * @throws IOException
	 */
	public static ServerListenThread createWithDefaultPort() throws IOException
	{
		return new ServerListenThread(DEFAULT_PORT);
	}

	/**
	 * Creates a ServerListenThread listening on the given port.
	 * The default USB/IP port is {@value #DEFAULT_PORT}.
	 * The port may be 0 to get an ephemeral port. In that case, use {@link #getPort()} to get the assigned port.
	 *
	 * @param port the port to listen on
	 * @see {@link #createWithRandomPort()}
	 * @see {@link #createWithDefaultPort()}
	 * @throws IOException
	 */
	public ServerListenThread(final int port) throws IOException
	{
		serverSocket = new ServerSocket(port);
		clientThreads = new ArrayList<>();
		shouldExit = false;
	}

	/**
	 * Gets the port number the server is listening on.
	 * Especially useful when the server was created with port 0 to get an ephemeral port.
	 *
	 * @return the port number the server is listening on
	 */
	public int getPort()
	{
		return serverSocket.getLocalPort();
	}

	public void stopExecuting() throws IOException
	{
		shouldExit = true;
		serverSocket.close();
		for (ServerConnectionThread clientThread : clientThreads)
		{
			clientThread.stopExecuting();
		}
	}

	@Override
	public void run()
	{
		LOGGER.info("Server thread started on {}", serverSocket.getLocalSocketAddress());

		Thread.currentThread().setName("USBIP server thread (" + serverSocket.getLocalSocketAddress() + ")");

		while (!shouldExit)
		{
			try
			{
				final Socket clientSocket = serverSocket.accept();

				final ServerConnectionThread clientThread = new ServerConnectionThread(clientSocket);
				clientThreads.add(clientThread);
				clientThread.start();
			}
			catch (IOException e)
			{
				if (!shouldExit)
				{
					LOGGER.error("Error in server thread", e);
				}
			}
		}
		LOGGER.info("Server thread stopping");
	}
}
