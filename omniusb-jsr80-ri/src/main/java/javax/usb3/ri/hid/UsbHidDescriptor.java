package javax.usb3.ri.hid;

public class UsbHidDescriptor
{
	private final EHidDescriptorType descriptorType;
	private final byte[] descriptorData;

	public UsbHidDescriptor(final EHidDescriptorType descriptorType, final byte[] descriptorData)
	{
		this.descriptorType = descriptorType;
		this.descriptorData = descriptorData;
	}

	public EHidDescriptorType getDescriptorType()
	{
		return descriptorType;
	}

	// TODO: We should implement a builder pattern here.
	public byte[] getDescriptorData()
	{
		return descriptorData;
	}
}
