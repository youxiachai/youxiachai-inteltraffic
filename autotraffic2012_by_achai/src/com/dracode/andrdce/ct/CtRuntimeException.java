package com.dracode.andrdce.ct;

public class CtRuntimeException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4746226492222665536L;

	public CtRuntimeException() {
	super();
    }

    public CtRuntimeException(String message) {
	super(message);
    }

    public CtRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public CtRuntimeException(Throwable cause) {
        super(cause);
    }
}
