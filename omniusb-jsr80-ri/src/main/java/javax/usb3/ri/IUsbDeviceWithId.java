/*
 * Copyright (C) 1999 - 2001, International Business Machines
 * Corporation. All Rights Reserved. Provided and licensed under the terms and
 * conditions of the Common Public License:
 * http://oss.software.ibm.com/developerworks/opensource/license-cpl.html
 *
 * Copyright (C) 2014 Key Bridge LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package javax.usb3.ri;

import javax.usb3.IUsbDevice;

/**
 * Interface for a USB device.
 * <p>
 * The submission methods contained in this IUsbDevice operate on the device's
 * Default Control Pipe. The device does not have to be
 * {@link #isConfigured() configured} to use the Default Control Pipe.
 * <p>
 * USB devices are divided into device classes such as hub, human interface,
 * printer, imaging, or mass storage device. The hub device class indicates a
 * specially designated USB device that provides additional USB attachment
 * points (refer to Chapter 11). USB devices are required to carry information
 * for self- identification and generic configuration. They are also required at
 * all times to display behavior consistent with defined USB device states.
 * <p>
 * All USB devices are accessed by a USB address that is assigned when the
 * device is attached and enumerated. Each USB device additionally supports one
 * or more pipes through which the host may communicate with the device. All USB
 * devices must support a specially designated pipe at endpoint zero to which
 * the USB device’s USB control pipe will be attached. All USB devices support a
 * common access mechanism for accessing information through this control pipe.
 * <p>
 * Associated with the control pipe at endpoint zero is the information required
 * to completely describe the USB device. This information falls into the
 * following categories:
 * <ul>
 * <li>Standard: This is information whose definition is common to all USB
 * devices and includes items such as vendor identification, device class, and
 * power management capability. Device, configuration, interface, and endpoint
 * descriptions carry configuration-related information about the device.</li>
 * <li>Class: The definition of this information varies, depending on the
 * device class of the USB device.</li>
 * <li>USB Vendor: The vendor of the USB device is free to put any information
 * desired here. The format, however, is not determined by this
 * specification.</li>
 * </ul>
 * <p>
 * Two major divisions of device classes exist: hubs and functions. Only hubs
 * have the ability to provide additional USB attachment points. Functions
 * provide additional capabilities to the host. In this library USB
 * {@code functions} are represented by {@linkplain #IUsbDevice} while Hubs are
 * represented by {@linkplain #IUsbHub}, which extends IUsbDevice.
 * <p>
 * <p>
 * Additionally, each USB device carries USB control and status information.
 *
 * @author Dan Streetman
 * @author Jesse Caulfield
 * @author E. Michael Maximilien
 */
public interface IUsbDeviceWithId extends IUsbDevice
{
	/**
	 * Get the Unique USB Device ID. This encapsulates a USB Device's BUS location
	 * to uniquely identify the device without needing to know or inspect the
	 * internal configuration of the device.
	 *
	 * @return the Unique USB Device ID.
	 */
	public UsbDeviceId getDeviceId();
}
