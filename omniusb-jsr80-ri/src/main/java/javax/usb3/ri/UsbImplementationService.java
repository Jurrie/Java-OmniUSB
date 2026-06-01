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

import javax.usb3.IUsbPort;

/**
 * Interface for a javax.usb implementation.
 * <p>
 * This is instantiated by the UsbHostManager. The implementation must include a
 * no-parameter constructor.
 *
 * @author Dan Streetman
 * @author Jesse Caulfield
 * @author E. Michael Maximilien
 */
public abstract class UsbImplementationService extends UsbRootHub
{
	/**
	 * The port this device is connected to.
	 */
	private IUsbPort port;

	@Override
	public void setParentUsbPort(final IUsbPort port)
	{
		this.port = port;
	}

	@Override
	public IUsbPort getParentUsbPort()
	{
		return port;
	}

	/**
	 * Get the (minimum) version number of the javax.usb API that this
	 * IUsbServices implements.
	 * <p>
	 * This should correspond to the output of (some version of) the
	 * {@link javax.usb.Version#getApiVersion() javax.usb.Version}.
	 *
	 * @return the version number of the minimum API version.
	 */
	public abstract String getApiVersion();

	/**
	 * Get the version number of the IUsbServices implementation.
	 * <p>
	 * The format should be <major>.<minor>.<revision>
	 *
	 * @return the version number of the IUsbServices implementation.
	 */
	public abstract String getImpVersion();

	/**
	 * Get a description of this IUsbServices implementation.
	 * <p>
	 * The format is implementation-specific, but should include at least the
	 * following:
	 * <ul>
	 * <li>The company or individual author(s).</li>
	 * <li>The license, or license header.</li>
	 * <li>Contact information.</li>
	 * <li>The minimum or expected version of Java.</li>
	 * <li>The Operating System(s) supported (usually one per
	 * implementation).</li>
	 * <li>Any other useful information.</li>
	 * </ul>
	 *
	 * @return a description of the implementation.
	 */
	public abstract String getImpDescription();

	protected void initialize()
	{
		// Does nothing by default
	}
}
