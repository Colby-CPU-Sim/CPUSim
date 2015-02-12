///////////////////////////////////////////////////////////////////////////////
// File:    	AssembledInstructionCall.java
// Type:    	java application file
// Author:		Raymond H. Mazza III and Dale Skrien
// Project: 	CPU Sim 3.0
// Date:    	June, 2000
//
// Description:
//   The machine instruction calls are now reduced to one value


///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

package cpusim.assembler;

import cpusim.util.SourceLine;

public class AssembledInstructionCall
{
    private int numBits;        //total size of instruction call
    private long value;			//signed long, the value of the combined fields
    private String comment;		//comment associated with this instr call
    private SourceLine sourceLine;

    public AssembledInstructionCall(int numBits, long value, String comment,
                                    SourceLine sourceLine)
    {
        this.numBits = numBits;
        this.value = value;
        this.comment = comment;
        this.sourceLine = sourceLine;
    }

    public long getValue()
    {
        return value;
    }

    public String getComment()
    {
        return comment;
    }

    public SourceLine getSourceLine()
    {
        return sourceLine;
    }

    /** returns the length of the instruction in bits */
    public int length() {
        return numBits;
    }
} //end of class AssembledInstructionCall
