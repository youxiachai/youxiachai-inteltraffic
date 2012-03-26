package com.dracode.andrdce.ct;

public class CtRuntimeCancelException  extends CtRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1273223116570415161L;

	public CtRuntimeCancelException() {
	super();
    }

    public CtRuntimeCancelException(String message) {
	super(message);
    }

    public CtRuntimeCancelException(String message, Throwable cause) {
        super(message, cause);
    }

    public CtRuntimeCancelException(Throwable cause) {
        super(cause);
    }
}
