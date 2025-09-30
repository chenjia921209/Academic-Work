package edu.uwm.cs351;

public class FormatException extends RuntimeException {

	/**
	 * Keep Eclipse happy
	 */
	private static final long serialVersionUID = 1L;

	public FormatException() {
	}

	public FormatException(String message) {
		super(message);
	}

	public FormatException(Throwable cause) {
		super(cause);
	}

	public FormatException(String message, Throwable cause) {
		super(message, cause);
	}

}
