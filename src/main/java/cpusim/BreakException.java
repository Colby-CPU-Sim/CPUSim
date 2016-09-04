/**
 * File: BreakException
 * Last update: August 2013
 * Author: Dale Skrien
 */
package cpusim;

import cpusim.model.module.RAM;

public class BreakException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public int breakAddress;
    public RAM breakRAM;
    
    /**
     * Constructor for Break Exception.
     * 
     * @param message - Message of exception.
     * @param addr - Address of RAM.
     * @param memory - the RAM.
     */
    public BreakException(String message, int addr, RAM memory) {
        super(message);
        breakAddress = addr;
        breakRAM = memory; 
    }
}
