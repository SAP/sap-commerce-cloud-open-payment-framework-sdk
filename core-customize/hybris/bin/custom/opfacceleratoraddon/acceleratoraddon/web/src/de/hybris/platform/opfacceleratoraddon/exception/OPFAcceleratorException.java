/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.opfacceleratoraddon.exception;

/**
 * Custom exception for OPF Accelerator operations.
 * Thrown when an operation within the OPF Accelerator encounters an error.
 */
public class OPFAcceleratorException extends RuntimeException
{
	/**
	 * Constructs a new OPFAcceleratorException with the specified detail message.
	 *
	 * @param message The detail message explaining the reason for the exception.
	 */
	public OPFAcceleratorException(final String message)
	{
		super(message);
	}

	/**
	 * Constructs a new OPFAcceleratorException with the specified detail message and cause.
	 *
	 * @param message The detail message explaining the reason for the exception.
	 * @param t The cause of the exception, which can be used to retrieve the original exception.
	 */
	public OPFAcceleratorException(final String message, final Throwable t)
	{
		super(message, t);
	}
}
