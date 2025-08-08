package de.hybris.platform.opfacceleratoraddon.exception;

/**
 * Custom exception for OPF Accelerator operations.
 * Thrown when an operation within the OPF Accelerator encounters an error.
 */
public class OPFRequestValidationException extends RuntimeException
{
    private transient Object validationObject;
    // Constructor with message only
    public OPFRequestValidationException(String message) {
        super(message);
    }

    // Constructor with message and validationObject
    public OPFRequestValidationException(String message, Object validationObject) {
        super(message);
        this.validationObject = validationObject;
    }

    // Getter for the validation object
    public Object getValidationObject() {
        return validationObject;
    }

    // Optionally, you could also implement a setter if needed
    public void setValidationObject(Object validationObject) {
        this.validationObject = validationObject;
    }
}

