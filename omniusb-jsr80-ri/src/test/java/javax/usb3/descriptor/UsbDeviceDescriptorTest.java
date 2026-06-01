/*
 * Copyright (C) 2013 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information.
 */
// Based on org.usb4java:usb4java-javax:1.3.0#org.usb4java.javax.descriptors.SimpleUsbDeviceDescriptorTest
package javax.usb3.descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import javax.usb3.enumerated.EDescriptorType;
import javax.usb3.enumerated.EUSBClassCode;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link UsbDeviceDescriptor}.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
class UsbDeviceDescriptorTest
{
	/** The test subject. */
	private static UsbDeviceDescriptor descriptor;

	private static final byte LENGTH = (byte) EDescriptorType.DEVICE.getLength();
	private static final byte DESCRIPTOR_TYPE = EDescriptorType.DEVICE.getByteCode();

	/** Value for {@link UsbDeviceDescriptor#bcdUSB()}. */
	private static final short USB = (short) 0xffff;

	/** Value for {@link UsbDeviceDescriptor#bDeviceClass()}. */
	private static final EUSBClassCode DEVICE_CLASS = EUSBClassCode.fromByteCode((byte) 0xfe);

	/** Value for {@link UsbDeviceDescriptor#bDeviceSubClass()}. */
	private static final byte DEVICE_SUB_CLASS = (byte) 0xfc;

	/** Value for {@link UsbDeviceDescriptor#bDeviceProtocol()}. */
	private static final byte DEVICE_PROTOCOL = (byte) 0xfb;

	/** Value for {@link UsbDeviceDescriptor#bMaxPacketSize0()}. */
	private static final byte MAX_PACKET_SIZE0 = (byte) 0xfa;

	/** Value for {@link UsbDeviceDescriptor#idVendor()}. */
	private static final short ID_VENDOR = (short) 0xfffe;

	/** Value for {@link UsbDeviceDescriptor#idProduct()}. */
	private static final short ID_PRODUCT = (short) 0xfffd;

	/** Value for {@link UsbDeviceDescriptor#bcdDevice()}. */
	private static final short DEVICE = (short) 0xfffc;

	/** Value for {@link UsbDeviceDescriptor#iManufacturer()}. */
	private static final byte MANUFACTURER = (byte) 0xf9;

	/** Value for {@link UsbDeviceDescriptor#iProduct()}. */
	private static final byte PRODUCT = (byte) 0xf8;

	/** Value for {@link UsbDeviceDescriptor#iSerialNumber()}. */
	private static final byte SERIAL_NUMBER = (byte) 0xf7;

	/** Value for {@link UsbDeviceDescriptor#bNumConfigurations()}. */
	private static final byte NUM_CONFIGURATIONS = (byte) 0xf6;

	/** A wrong value for equality test. */
	private static final byte WRONG = 0;

	/**
	 * Setup the test subject.
	 */
	@BeforeAll
	static void setUp()
	{
		descriptor = new UsbDeviceDescriptor(
				USB, DEVICE_CLASS, DEVICE_SUB_CLASS,
				DEVICE_PROTOCOL, MAX_PACKET_SIZE0, ID_VENDOR, ID_PRODUCT, DEVICE,
				MANUFACTURER, PRODUCT, SERIAL_NUMBER, NUM_CONFIGURATIONS);
	}

	/**
	 * Tests the {@link UsbDeviceDescriptor#bLength()} method.
	 */
	@Test
	void testLength()
	{
		assertEquals(LENGTH, descriptor.bLength());
	}

	/**
	 * Tests the {@link UsbDeviceDescriptor#bDescriptorType()} method.
	 */
	@Test
	void testDescriptorType()
	{
		assertEquals(DESCRIPTOR_TYPE, descriptor.bDescriptorType());
	}

	/**
	 * Tests the {@link UsbDeviceDescriptor#bcdUSB()} method.
	 */
	@Test
	void testUSB()
	{
		assertEquals(USB, descriptor.bcdUSB());
	}

	/**
	 * Tests the {@link UsbDeviceDescriptor#bDeviceClass()} method.
	 */
	@Test
	void testDeviceClass()
	{
		assertEquals(DEVICE_CLASS.getByteCode(), descriptor.bDeviceClass());
	}

	/**
	 * Tests the {@link UsbDeviceDescriptor#bDeviceSubClass()} method.
	 */
	@Test
	void testDeviceSubClass()
	{
		assertEquals(DEVICE_SUB_CLASS, descriptor.bDeviceSubClass());
	}

	/**
	 * Tests the {@link UsbDeviceDescriptor#bMaxPacketSize0()} method.
	 */
	@Test
	void testMaxPacketSize0()
	{
		assertEquals(MAX_PACKET_SIZE0, descriptor.bMaxPacketSize0());
	}

	/**
	 * Tests the {@link UsbDeviceDescriptor#idVendor()} method.
	 */
	@Test
	void testIdVendor()
	{
		assertEquals(ID_VENDOR, descriptor.idVendor());
	}

	/**
	 * Tests the {@link UsbDeviceDescriptor#idProduct()} method.
	 */
	@Test
	void testIdProduct()
	{
		assertEquals(ID_PRODUCT, descriptor.idProduct());
	}

	/**
	 * Tests the {@link UsbDeviceDescriptor#bcdDevice()} method.
	 */
	@Test
	void testDevice()
	{
		assertEquals(DEVICE, descriptor.bcdDevice());
	}

	/**
	 * Tests the {@link UsbDeviceDescriptor#iManufacturer()} method.
	 */
	@Test
	void testManufacturer()
	{
		assertEquals(MANUFACTURER, descriptor.iManufacturer());
	}

	/**
	 * Tests the {@link UsbDeviceDescriptor#iProduct()} method.
	 */
	@Test
	void testProduct()
	{
		assertEquals(PRODUCT, descriptor.iProduct());
	}

	/**
	 * Tests the {@link UsbDeviceDescriptor#iSerialNumber()} method.
	 */
	@Test
	void testSerialNumber()
	{
		assertEquals(SERIAL_NUMBER, descriptor.iSerialNumber());
	}

	/**
	 * Tests the {@link UsbDeviceDescriptor#bNumConfigurations()} method.
	 */
	@Test
	void testNumConfigurations()
	{
		assertEquals(NUM_CONFIGURATIONS, descriptor.bNumConfigurations());
	}

	/**
	 * Tests the {@link UsbDeviceDescriptor#hashCode()} method.
	 */
	@Test
	void testHashCode()
	{
		final int code = descriptor.hashCode();
		assertEquals(code, descriptor.hashCode());
		assertEquals(code, new UsbDeviceDescriptor(
				USB, DEVICE_CLASS, DEVICE_SUB_CLASS,
				DEVICE_PROTOCOL, MAX_PACKET_SIZE0, ID_VENDOR, ID_PRODUCT, DEVICE,
				MANUFACTURER, PRODUCT, SERIAL_NUMBER,
				NUM_CONFIGURATIONS).hashCode());
	}

	/**
	 * Tests the {@link UsbDeviceDescriptor#equals(Object)} method.
	 */
	@Test
	void testEquals()
	{
		assertNotEquals(null, descriptor);
		assertNotEquals(new Object(), descriptor);
		assertEquals(new UsbDeviceDescriptor(
				USB, DEVICE_CLASS, DEVICE_SUB_CLASS,
				DEVICE_PROTOCOL, MAX_PACKET_SIZE0, ID_VENDOR, ID_PRODUCT, DEVICE,
				MANUFACTURER, PRODUCT, SERIAL_NUMBER, NUM_CONFIGURATIONS), descriptor);
		assertNotEquals(new UsbDeviceDescriptor(
				WRONG, DEVICE_CLASS, DEVICE_SUB_CLASS,
				DEVICE_PROTOCOL, MAX_PACKET_SIZE0, ID_VENDOR, ID_PRODUCT, DEVICE,
				MANUFACTURER, PRODUCT, SERIAL_NUMBER, NUM_CONFIGURATIONS), descriptor);
		assertNotEquals(new UsbDeviceDescriptor(
				USB, EUSBClassCode.fromByteCode(WRONG), DEVICE_SUB_CLASS,
				DEVICE_PROTOCOL, MAX_PACKET_SIZE0, ID_VENDOR, ID_PRODUCT, DEVICE,
				MANUFACTURER, PRODUCT, SERIAL_NUMBER, NUM_CONFIGURATIONS), descriptor);
		assertNotEquals(new UsbDeviceDescriptor(
				USB, DEVICE_CLASS, WRONG,
				DEVICE_PROTOCOL, MAX_PACKET_SIZE0, ID_VENDOR, ID_PRODUCT, DEVICE,
				MANUFACTURER, PRODUCT, SERIAL_NUMBER, NUM_CONFIGURATIONS), descriptor);
		assertNotEquals(new UsbDeviceDescriptor(
				USB, DEVICE_CLASS, DEVICE_SUB_CLASS,
				WRONG, MAX_PACKET_SIZE0, ID_VENDOR, ID_PRODUCT, DEVICE,
				MANUFACTURER, PRODUCT, SERIAL_NUMBER, NUM_CONFIGURATIONS), descriptor);
		assertNotEquals(new UsbDeviceDescriptor(
				USB, DEVICE_CLASS, DEVICE_SUB_CLASS,
				DEVICE_PROTOCOL, WRONG, ID_VENDOR, ID_PRODUCT, DEVICE,
				MANUFACTURER, PRODUCT, SERIAL_NUMBER, NUM_CONFIGURATIONS), descriptor);
		assertNotEquals(new UsbDeviceDescriptor(
				USB, DEVICE_CLASS, DEVICE_SUB_CLASS,
				DEVICE_PROTOCOL, MAX_PACKET_SIZE0, WRONG, ID_PRODUCT, DEVICE,
				MANUFACTURER, PRODUCT, SERIAL_NUMBER, NUM_CONFIGURATIONS), descriptor);
		assertNotEquals(new UsbDeviceDescriptor(
				USB, DEVICE_CLASS, DEVICE_SUB_CLASS,
				DEVICE_PROTOCOL, MAX_PACKET_SIZE0, ID_VENDOR, WRONG, DEVICE,
				MANUFACTURER, PRODUCT, SERIAL_NUMBER, NUM_CONFIGURATIONS), descriptor);
		assertNotEquals(new UsbDeviceDescriptor(
				USB, DEVICE_CLASS, DEVICE_SUB_CLASS,
				DEVICE_PROTOCOL, MAX_PACKET_SIZE0, ID_VENDOR, ID_PRODUCT, WRONG,
				MANUFACTURER, PRODUCT, SERIAL_NUMBER, NUM_CONFIGURATIONS), descriptor);
		assertNotEquals(new UsbDeviceDescriptor(
				USB, DEVICE_CLASS, DEVICE_SUB_CLASS,
				DEVICE_PROTOCOL, MAX_PACKET_SIZE0, ID_VENDOR, ID_PRODUCT, DEVICE,
				WRONG, PRODUCT, SERIAL_NUMBER, NUM_CONFIGURATIONS), descriptor);
		assertNotEquals(new UsbDeviceDescriptor(
				USB, DEVICE_CLASS, DEVICE_SUB_CLASS,
				DEVICE_PROTOCOL, MAX_PACKET_SIZE0, ID_VENDOR, ID_PRODUCT, DEVICE,
				MANUFACTURER, WRONG, SERIAL_NUMBER, NUM_CONFIGURATIONS), descriptor);
		assertNotEquals(new UsbDeviceDescriptor(
				USB, DEVICE_CLASS, DEVICE_SUB_CLASS,
				DEVICE_PROTOCOL, MAX_PACKET_SIZE0, ID_VENDOR, ID_PRODUCT, DEVICE,
				MANUFACTURER, PRODUCT, WRONG, NUM_CONFIGURATIONS), descriptor);
		assertNotEquals(new UsbDeviceDescriptor(
				USB, DEVICE_CLASS, DEVICE_SUB_CLASS,
				DEVICE_PROTOCOL, MAX_PACKET_SIZE0, ID_VENDOR, ID_PRODUCT, DEVICE,
				MANUFACTURER, PRODUCT, SERIAL_NUMBER, WRONG), descriptor);
	}

	/**
	 * Tests the {@link UsbDeviceDescriptor#toString()} method.
	 */
	@Test
	void testToString()
	{
		assertEquals(String.format("USB Device Descriptor:%n"
				+ "  bLength                 18%n"
				+ "  bDescriptorType          1%n"
				+ "  bcdUSB               ff.ff%n"
				+ "  bDeviceClass           254 Interface%n"
				+ "  bDeviceSubClass        252%n"
				+ "  bDeviceProtocol        251%n"
				+ "  bMaxPacketSize0        250%n"
				+ "  idVendor            0xfffe%n"
				+ "  idProduct           0xfffd%n"
				+ "  bcdDevice            ff.fc%n"
				+ "  iManufacturer          249%n"
				+ "  iProduct               248%n"
				+ "  iSerial                247%n"
				+ "  bNumConfigurations     246%n"), descriptor.toString());
	}
}
