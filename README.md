# Java OmniUSB
This project is about USB and Java. It takes [JSR-80](https://jcp.org/en/jsr/detail?id=80) as a base (even though it seems not to progress much) and provides implementations for several platforms.

The platforms supported are:

- Android (no requirement for root)
- Windows / Linux / Mac OS X (all platforms that USB4Java supports)

It also supports [USB/IP](https://wiki.archlinux.org/title/USB/IP), so you can do USB over TCP (for example: access a USB device over the Internet). Currently there is support for the server side of USB/IP. Client side is not yet finished.

There is also support for writing your own USB devices completely in Java. And since USB/IP is supported, you can connect your Java USB device directly to your operating system! See the 'omniusb-mock-tests' module for examples.

## Original authors
This project is a collection of several other projects found on the Internet. I have combined and improved them, but I most certainly did not write it all!

These are the previous authors:
- Dan Streetman
- Jesse Caulfield
- The [USB4Java project](https://github.com/usb4java/usb4java) team
- [Klaus Reimer](https://github.com/kayahr)
- [Luca Longinotti](https://github.com/llongi)
- Attect
- E. Michael Maximilien

I tried to get in touch with these people to see if they are okay with me using their source. If you are one of those people who I couldn't reach, please get in contact.

# How to use Java OmniUSB

Write your application using the JSR-80 specification. You should only need to base your own application on two Maven artifacts:

```
<dependency>
	<groupId>org.jurr.java.omniusb</groupId>
	<artifactId>omniusb-jsr80-spec</artifactId>
	<version><!-- Replace with latest version --></version>
</dependency>

<dependency>
	<groupId>org.jurr.java.omniusb</groupId>
	<artifactId>omniusb-jsr80-ri</artifactId>
	<version><!-- Replace with latest version --></version>
</dependency>
```

After that, you can :

```
// List all USB devices with the given vendor ID and product ID
final List<IUsbDevice> usbDevices UsbHostManager.getUsbDeviceList(USB_VENDOR_ID, USB_PRODUCT_ID);

final IUsbDevice usbDevice = usbDevices.get(0);

// Set an active USB configuration
usbDevice.setActiveUsbConfigurationNumber((byte) 1);

// Retrieve and claim the first interface
usbInterface = usbDevice.getActiveUsbConfiguration().getUsbInterface(0);
usbInterface.claim(); // release() after you are done with it

// Determine read endpoint address and open its pipe
final byte readEndpoint = new BEndpointAddress(2, EEndpointDirection.DEVICE_TO_HOST).getByteCode();
readPipe = usbInterface.getUsbEndpoint(readEndpoint).getUsbPipe();
readPipe.open(); // close() after you are done with it

// Determine write endpoint address and open its pipe
final byte writeEndpoint = new BEndpointAddress(3, EEndpointDirection.HOST_TO_DEVICE).getByteCode();
writePipe = usbInterface.getUsbEndpoint(writeEndpoint).getUsbPipe();
writePipe.open(); // close() after you are done with it

// Write data to your USB device
final byte[] writeBuffer = ...
final int written = writePipe.syncSubmit(writeBuffer);

// Read data from your USB device
final byte[] readBuffer = new byte[2048];
final int read = readPipe.syncSubmit(data);
```

Remember to use only JSR-80 classes. This makes your code usable on all platforms.

## Running the code
To run the code, you'll either need `org.jurr.java.omniusb:omniusb-usb4java` or `org.jurr.java.omniusb:omniusb-android` on your classpath.
These are runtime dependencies: you don't need them when you compile, but you do need **one of** them when you run your application.

## Android support
There is non-root Android support by using the [Android USB host API](https://developer.android.com/develop/connectivity/usb/host).
If you only use JSR-80 classes in your code, then Android support simply means swapping the runtime dependency `org.jurr.java.omniusb:omniusb-usb4java` for `org.jurr.java.omniusb:omniusb-android`.

## Mock support and USB/IP support
In the `omniusb-mock-tests` module there are two pure Java mock devices created:
- A USB mouse doing figure-eights
- A USB memory stick

Both can be attached to your system using [USB/IP](https://wiki.archlinux.org/title/USB/IP).
If you run Linux, you can simply run the `org.jurr.java.omniusb.mock.UsbIpServer` class (or alternatively download the [omniusb-mock-tests-<version>.jar from Central](https://central.sonatype.com/artifact/org.jurr.java.omniusb/omniusb-mock-tests/versions) and run  `java -jar omniusb-mock-tests-<version>.jar`). It's a console application.
Pressing 'm' + 'enter' will attach the mock mouse to your kernel.
Pressing 's' + 'enter' will attach the mock memory stick.
Note that attaching devices using USB/IP requires root. You'll be prompted for your sudo password.

Please note that client-side USB/IP is not yet done. Server-side works.

# Help wanted
If you would like to help out with this project: great! The code currently is half decent, but could be improved in a lot of areas.
The biggest thing that is missing currently is unit tests! And documentation probably also.
