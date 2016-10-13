///////////////////////////////////////////////////////////////////////////////
// File:    	LoadException.java
// Author:		Josh Ladieu
// Project: 	CPU Sim 3.0
// Date:    	November, 2000
//
// Description:
// An extension of RuntimeException to handle any errors while loading.


///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

package cpusim.util;

import java.util.List;

import cpusim.model.assembler.AssembledInstructionCall;
import cpusim.model.module.RAM;

/**
 * The class represents an exception that occurs when attempting to load a machine
 * language program into RAM and there isn't enough RAM for the program.
 */
public class LoadException extends RuntimeException
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public RAM ram;
    public List<AssembledInstructionCall> instructions;

    public LoadException(String message, RAM memory,
                         List<AssembledInstructionCall> v)
    {
        super(message);
        this.ram = memory;
        this.instructions = v;
    }

}
