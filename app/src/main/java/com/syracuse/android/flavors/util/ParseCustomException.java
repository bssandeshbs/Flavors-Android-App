package com.syracuse.android.flavors.util;

/**
 * Custom Parse Exception class
 */
public class ParseCustomException extends Exception {
    private static final long serialVersionUID = 1997753363232807009L;

    public ParseCustomException(String message)
    {
        super(message);
    }

    public ParseCustomException(Throwable cause)
    {
        super(cause);
    }

    public ParseCustomException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
