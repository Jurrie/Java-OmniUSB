package org.jurr.java.omniusb.android;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.usb3.IUsbControlIrp;
import javax.usb3.IUsbDevice;
import javax.usb3.IUsbStringDescriptor;
import javax.usb3.UsbHostManager;
import javax.usb3.descriptor.UsbDeviceDescriptor;
import javax.usb3.enumerated.EDescriptorType;
import javax.usb3.enumerated.EUSBClassCode;
import javax.usb3.exception.UsbException;
import javax.usb3.exception.UsbPlatformException;
import javax.usb3.ri.AUsbConfiguration;
import javax.usb3.ri.ProcessIrpCallback;
import javax.usb3.ri.UsbControlIrpQueue;
import javax.usb3.ri.UsbDeviceId;
import javax.usb3.ri.UsbDeviceListenerList;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

/**
 * Bridge between JSR80 UsbDevice and Android UsbDevice.
 * Because the Android UsbDevice does not support configurations (well, it does since API 21), we are faking that this device has 1 configuration.
 */
public class AndroidUsbDevice extends javax.usb3.ri.UsbDevice
{
	private final UsbDevice wrappedUsbDevice;
	private final List<String> stringDescriptors = new ArrayList<>();

	private UsbDeviceConnection connection = null; // Lazy initialized via openDevice()
	private byte activeConfiguration = 0;

	private static final byte STRING_DESCRIPTOR_INDEX_MANUFACTURER = 0;
	private static final byte STRING_DESCRIPTOR_INDEX_PRODUCT = 1;
	private static final byte STRING_DESCRIPTOR_INDEX_SERIAL_NUMBER = 2;

	public AndroidUsbDevice(final UsbDevice usbDevice, final int wrappedUsbDeviceAddress, final UsbDeviceId parentId) throws UsbException
	{
		super(createDescriptorFromAndroidUsbDevice(usbDevice, wrappedUsbDeviceAddress), parentId, 0);
		wrappedUsbDevice = usbDevice;

		stringDescriptors.add(STRING_DESCRIPTOR_INDEX_MANUFACTURER, usbDevice.getManufacturerName());
		stringDescriptors.add(STRING_DESCRIPTOR_INDEX_PRODUCT, usbDevice.getProductName());
		stringDescriptors.add(STRING_DESCRIPTOR_INDEX_SERIAL_NUMBER, getAndroidUsbServices().hasUsbPermission(this) ? usbDevice.getSerialNumber() : "NO PERMISSION");
	}

	protected UsbDevice getWrappedUsbDevice()
	{
		return wrappedUsbDevice;
	}

	protected byte addStringDescriptor(final String string)
	{
		stringDescriptors.add(string);
		return (byte) (stringDescriptors.size() - 1);
	}

	private static UsbDeviceId createDescriptorFromAndroidUsbDevice(final UsbDevice usbDevice, final int deviceAddress)
	{
		final short bcdUSB = 0x210; // Not supported in Android, so fake USB 2.10
		final EUSBClassCode bDeviceClass = EUSBClassCode.fromByteCode((byte) usbDevice.getDeviceClass());
		final byte bDeviceSubClass = (byte) usbDevice.getDeviceSubclass();
		final byte bDeviceProtocol = (byte) usbDevice.getDeviceProtocol();
		final byte bMaxPacketSize0 = 0; // Not supported in Android
		final short idVendor = (short) usbDevice.getVendorId();
		final short idProduct = (short) usbDevice.getProductId();
		final short bcdDevice = (short) usbDevice.getDeviceId();
		final byte iManufacturer = STRING_DESCRIPTOR_INDEX_MANUFACTURER;
		final byte iProduct = STRING_DESCRIPTOR_INDEX_PRODUCT;
		final byte iSerialNumber = STRING_DESCRIPTOR_INDEX_SERIAL_NUMBER;
		final byte bNumConfigurations = (byte) usbDevice.getConfigurationCount();
		final UsbDeviceDescriptor deviceDescriptor = new UsbDeviceDescriptor(bcdUSB, bDeviceClass, bDeviceSubClass, bDeviceProtocol, bMaxPacketSize0, idVendor, idProduct, bcdDevice, iManufacturer, iProduct, iSerialNumber, bNumConfigurations);
		return new UsbDeviceId(0, deviceAddress, 1, deviceDescriptor);
	}

	@Override
	protected AUsbConfiguration doGetUsbConfiguration(final byte jsr80ConfigurationIndex) throws UsbPlatformException
	{
		final byte androidApiConfigurationIndex = (byte) (jsr80ConfigurationIndex - 1);
		return new AndroidUsbConfiguration(this, wrappedUsbDevice.getConfiguration(androidApiConfigurationIndex));
	}

	@Override
	protected byte doGetActiveUsbConfiguration() throws UsbPlatformException
	{
		final byte jsr80ActiveConfiguration = (byte) (activeConfiguration + 1);
		return jsr80ActiveConfiguration;
	}

	@Override
	protected void doSetActiveUsbConfigurationNumber(final byte jsr80ConfigurationIndex) throws UsbException
	{
		final byte androidApiConfigurationIndex = (byte) (jsr80ConfigurationIndex - 1);
		openDevice().setConfiguration(wrappedUsbDevice.getConfiguration(androidApiConfigurationIndex));
		activeConfiguration = androidApiConfigurationIndex;
	}

	@Override
	protected void doClaimInterface(byte number, boolean force) throws UsbException
	{
		openDevice().claimInterface(wrappedUsbDevice.getInterface(number), force);
	}

	@Override
	protected void doReleaseInterface(byte number) throws UsbException
	{
		openDevice().releaseInterface(wrappedUsbDevice.getInterface(number));
	}

	@Override
	protected IUsbStringDescriptor doGetUsbStringDescriptor(final byte index) throws UsbException
	{
		final String string = stringDescriptors.get(index);
		return new IUsbStringDescriptor()
		{
			@Override
			public EDescriptorType descriptorType()
			{
				return EDescriptorType.STRING;
			}

			@Override
			public byte bLength()
			{
				return (byte) descriptorType().getLength();
			}

			@Override
			public byte bDescriptorType()
			{
				return descriptorType().getByteCode();
			}

			@Override
			public String getString()
			{
				return string;
			}

			@Override
			public byte[] bString()
			{
				return string.getBytes(StandardCharsets.UTF_16LE);
			}
		};
	}

	@Override
	protected short[] getLanguages() throws UsbException
	{
		// Not supported in Android
		return new short[0];
	}

	@Override
	protected UsbControlIrpQueue createUsbControlIrpQueue(final UsbDeviceListenerList listener)
	{
		return new UsbControlIrpQueue(this, getListeners())
		{
			@Override
			protected void doControlTransfer(final IUsbControlIrp irp, final ProcessIrpCallback callback) throws UsbException
			{
				final int actualLength = openDevice().controlTransfer(irp.bmRequestType(), irp.bRequest(), irp.wValue(), irp.wIndex(), irp.getData(), irp.getLength(), 0); // TODO: Validate if a timeout of 0 means unlimited. Documentation states this for bulk transfers, but not control transfers.
				callback.onTransferComplete(actualLength);
			}
		};
	}

	private static AndroidUsbServices getAndroidUsbServices() throws UsbException
	{
		for (IUsbDevice device : UsbHostManager.getRootUsbHub().getAttachedUsbDevices())
		{
			if (device instanceof AndroidUsbServices androidUsbServices)
			{
				return androidUsbServices;
			}
		}

		throw new UsbException("No AndroidUsbServices found in UsbHostManager");
	}

	protected UsbDeviceConnection openDevice() throws SecurityException, UsbException
	{
		if (connection == null)
		{
			connection = getAndroidUsbServices().openDevice(this);
		}
		return connection;
	}

	@Override
	protected void doVendorSpecificControlTransfer(final IUsbControlIrp irp) throws SecurityException, UsbException
	{
		openDevice().controlTransfer(irp.bmRequestType(), irp.bRequest(), irp.wValue(), irp.wIndex(), irp.getData(), irp.getLength(), 0);
	}
}