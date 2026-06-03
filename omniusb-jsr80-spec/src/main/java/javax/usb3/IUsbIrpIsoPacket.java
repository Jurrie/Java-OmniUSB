package javax.usb3;

public interface IUsbIrpIsoPacket
{
	int getOffset();

	int getLength();

	int getActualLength();

	int getStatus();
}
