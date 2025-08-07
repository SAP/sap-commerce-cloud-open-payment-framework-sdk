package de.hybris.platform.opfacceleratoraddon.exception;

/**
 * Custom exception for OPF Accelerator operations.
 * Thrown when an operation within the OPF Accelerator encounters an error.
 */
public class OPFAcceleratorException extends RuntimeException
{
	public OPFAcceleratorException(final String message)
	{
		super(message);
	}

	public OPFAcceleratorException(final String message, final Throwable t)
	{
		super(message, t);
	}
}
