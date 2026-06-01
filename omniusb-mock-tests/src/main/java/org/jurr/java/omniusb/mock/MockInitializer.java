package org.jurr.java.omniusb.mock;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.invoke.MethodHandles;

import javax.usb3.IUsbDevice;
import javax.usb3.IUsbPort;
import javax.usb3.UsbHostManager;
import javax.usb3.exception.UsbException;
import javax.usb3.mockservices.MockUsbServices;
import javax.usb3.ri.UsbDeviceId;

import org.jurr.java.omniusb.mock.hid.mouse.MockMouseDevice;
import org.jurr.java.omniusb.mock.massstorage.bulkonlytransport.MockMemoryStickDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MockInitializer
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private MockInitializer()
	{
	}

	public static UsbDeviceId startMockMouse()
	{
		try
		{
			for (IUsbDevice device : UsbHostManager.getRootUsbHub().getAttachedUsbDevices())
			{
				if (device instanceof MockUsbServices mockUsbServices)
				{
					final MockMouseDevice mockMouse = new MockMouseDevice(mockUsbServices.getDeviceId(), 1);
					mockUsbServices.addMock(mockMouse);
					LOGGER.info("Mock Mouse USB device attached to USB hub: {}", mockUsbServices.getDeviceId());

					return mockMouse.getDeviceId();
				}
			}

			throw new RuntimeException("No MockUsbServices found. Is that on your classpath?");
		}
		catch (UsbException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static void stopMockMouse()
	{
		try
		{
			for (var device : UsbHostManager.getRootUsbHub().getAttachedUsbDevices())
			{
				if (device instanceof MockUsbServices mockUsbServices)
				{
					for (var port : mockUsbServices.getUsbPorts())
					{
						final var usbDevice = port.getUsbDevice();
						if (usbDevice instanceof MockMouseDevice mockMouse)
						{
							mockUsbServices.removeMock(mockMouse);
							LOGGER.info("Mock Mouse USB device detached from USB hub: {}", mockUsbServices.getDeviceId());
						}
					}
				}
			}
		}
		catch (UsbException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static UsbDeviceId startMockMemoryStick() throws IOException
	{
		try
		{
			for (var device : UsbHostManager.getRootUsbHub().getAttachedUsbDevices())
			{
				if (device instanceof MockUsbServices mockUsbServices)
				{
					final var mockMemoryStick = new MockMemoryStickDevice(mockUsbServices.getDeviceId(), 2);
					mockUsbServices.addMock(mockMemoryStick);
					LOGGER.info("Mock Memory Stick USB attached to USB hub: {}", mockMemoryStick.getDeviceId());

					final var tempFile = File.createTempFile("JSR80-mockmemorystick", ".bin");
					final var data = new RandomAccessFile(tempFile, "rw");
					data.setLength(0x4000000); // 64 MB
					data.seek(0);
					mockMemoryStick.getFirmware().addLUN(data);
					LOGGER.info("Mock Memory Stick backing storage created at: {}", tempFile.getAbsolutePath());

					return mockMemoryStick.getDeviceId();
				}
			}

			throw new RuntimeException("No MockUsbServices found. Is that on your classpath?");
		}
		catch (UsbException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static void stopMockMemoryStick()
	{
		try
		{
			for (IUsbDevice device : UsbHostManager.getRootUsbHub().getAttachedUsbDevices())
			{
				if (device instanceof MockUsbServices mockUsbServices)
				{
					for (IUsbPort port : mockUsbServices.getUsbPorts())
					{
						final IUsbDevice usbDevice = port.getUsbDevice();
						if (usbDevice instanceof MockMemoryStickDevice mockMemoryStick)
						{
							mockUsbServices.removeMock(mockMemoryStick);
							LOGGER.info("Mock MemoryStick USB device detached from USB hub: {}", mockUsbServices.getDeviceId());
						}
					}
				}
			}
		}
		catch (UsbException e)
		{
			throw new RuntimeException(e);
		}
	}
}
