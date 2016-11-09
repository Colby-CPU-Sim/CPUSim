/**
 * File: ExecutionException
 * Last Update: August 2013
 */
package cpusim.model;

public class ExecutionException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for new Execution Exception.
	 * 
	 * @param message - Message of Exception.
	 */
    public ExecutionException(String message) {
        super(message);
    }
    
    /**
     * Keeps a cause but adds a new message.
     * @param message
     * @param cause
     */
    public ExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

}
