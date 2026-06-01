package org.jurr.java.omniusb.mock.massstorage.bulkonlytransport;

import javax.usb3.IUsbConfiguration;
import javax.usb3.descriptor.UsbEndpointDescriptor;
import javax.usb3.ri.massstorage.EMassStorageProtocol;
import javax.usb3.ri.massstorage.EMassStorageSubclass;
import javax.usb3.ri.massstorage.UsbMassStorageInterface;
import javax.usb3.ri.massstorage.UsbMassStorageInterfaceDescriptor;

public class MockMemoryStickInterface extends UsbMassStorageInterface
{
	public MockMemoryStickInterface(final IUsbConfiguration configuration)
	{
		super(configuration, new UsbMassStorageInterfaceDescriptor((byte) 0, (byte) 0, (byte) 2, EMassStorageSubclass.SCSI_TRANSPARENT_COMMAND_SET, EMassStorageProtocol.BULK_ONLY_TRANSPORT, (byte) 4, new UsbEndpointDescriptor[0]));

		// We did not pass endpoint descriptors to the parent constructor. So we need to add them ourselves.
		addUsbEndpoint(new MockMemoryStickReadEndpoint(this));
		addUsbEndpoint(new MockMemoryStickWriteEndpoint(this));
	}
}
