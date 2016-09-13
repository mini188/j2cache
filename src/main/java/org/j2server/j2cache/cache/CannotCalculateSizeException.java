package org.j2server.j2cache.cache;

public class CannotCalculateSizeException extends Exception {

	private static final long serialVersionUID = 1938260171577987873L;

	public CannotCalculateSizeException() {
    }

    public CannotCalculateSizeException(Object obj) {
        super("Unable to determine size of " + obj.getClass() + " instance");
    }
}
