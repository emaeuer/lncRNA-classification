package de.lncrna.classification.filter;

public class FailedToReceiveIntronException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public FailedToReceiveIntronException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
