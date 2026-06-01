package javax.usb3.mockservices;

import javax.usb3.ri.UsbImplementationService;

public class MockUsbServices extends UsbImplementationService
{
	/**
	 * The implementation description.
	 */
	private static final String IMP_DESCRIPTION = "javax.usb3 mock services";

	/**
	 * The implementation version. This is the Java source code version.
	 */
	private static final String IMP_VERSION = "1.4.x";

	/**
	 * The API version.
	 */
	private static final String API_VERSION = "1.0.0";

	public MockUsbServices()
	{
		// Default constructor declared because of thrown exception by parent
	}

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

	/**
	 * Use this method to connect a mock device to the mock USB hub
	 *
	 * @param mock
	 */
	public void addMock(final IMockUsbDevice mock)
	{
		connectUsbDevice(mock);
	}

	public void removeMock(final IMockUsbDevice mock)
	{
		disconnectUsbDevice(mock);
	}
}
