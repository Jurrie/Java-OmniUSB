/*
 * Copyright (C) 1999 - 2001, International Business Machines Corporation.
 * All Rights Reserved. Provided and licensed under the terms and
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
package javax.usb3.exception;

/**
 * Exception indicating a platform-specific UsbException.
 * <p>
 * This indicates an error occurred that is specific to the operating system or
 * platform. This provides access to the specific
 * {@link #getErrorCode() error code} and/or
 * {@link #getPlatformException() platform Exception}.
 *
 * @author Dan Streetman
 * @author Jesse Caulfield
 */
public class UsbPlatformException extends UsbException
{
	private static final long serialVersionUID = 1L;

	private final Exception platformException;
	private final int errorCode;

	/**
	 * Constructor.
	 */
	public UsbPlatformException()
	{
		super();
		platformException = null;
		errorCode = 0;
	}

	/**
	 * Constructor.
	 *
	 * @param s The detail message.
	 */
	public UsbPlatformException(final String s)
	{
		super(s);
		platformException = null;
		errorCode = 0;
	}

	/**
	 * Constructor.
	 *
	 * @param e The error code.
	 */
	public UsbPlatformException(final int e)
	{
		super();
		platformException = null;
		errorCode = e;
	}

	/**
	 * Constructor.
	 *
	 * @param pE The platform Exception.
	 */
	public UsbPlatformException(final Exception pE)
	{
		super();
		platformException = pE;
		errorCode = 0;
	}

	/**
	 * Constructor.
	 *
	 * @param s The detail message.
	 * @param e The error code.
	 */
	public UsbPlatformException(final String s, final int e)
	{
		super(s);
		platformException = null;
		errorCode = e;
	}

	/**
	 * Constructor.
	 *
	 * @param s The detail message.
	 * @param pE The platform Exception.
	 */
	public UsbPlatformException(final String s, final Exception pE)
	{
		super(s);
		platformException = pE;
		errorCode = 0;
	}

	/**
	 * Constructor.
	 *
	 * @param e The error code.
	 * @param pE The platform Exception.
	 */
	public UsbPlatformException(final int e, final Exception pE)
	{
		super();
		platformException = pE;
		errorCode = e;
	}

	/**
	 * Constructor.
	 *
	 * @param s The detail message.
	 * @param e The error code.
	 * @param pE The platform Exception.
	 */
	public UsbPlatformException(final String s, final int e, final Exception pE)
	{
		super(s);
		platformException = pE;
		errorCode = e;
	}

	/**
	 * Get the platform Exception.
	 *
	 * @return The platform Exception, or null.
	 */
	public Exception getPlatformException()
	{
		return platformException;
	}

	/**
	 * Get the platform error code.
	 *
	 * @return The platform error code.
	 */
	public int getErrorCode()
	{
		return errorCode;
	}
}
