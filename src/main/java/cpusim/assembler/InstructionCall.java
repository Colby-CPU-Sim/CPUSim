///////////////////////////////////////////////////////////////////////////////
// File:    	InstructionCall.java
// Type:    	java application file
// Author:		Raymond H. Mazza III and Dale Skrien
// Project: 	CPU Sim 3.0
// Date:    	June, 2000
//
// Description:
//   The instruction calls after parsing but before assembly.  Includes
//   all labels in the assembly code referring to this instruction.


///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides
package cpusim.assembler;

import cpusim.MachineInstruction;
import cpusim.util.SourceLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

///////////////////////////////////////////////////////////////////////////////
// the InstructionCall class

public class InstructionCall
{    //(label)* opcode (operand)*

    //holds the label Tokens in front of the instruction, if any
    //key: original token
    //value: renamed token (token is renamed if it is the same token as another previously
    //declared token)
    public List<Token> labels;
    //a reference to the Instruction that matched the opcode token
    public MachineInstruction machineInstruction;
    //holds all operand Tokens
    public List<Token> operands;
    //holds the comment (if any) on the same line as the instruction
    public String comment;
    //holds the source line info for the instruction
    public SourceLine sourceLine;
    //holds the number of bits per cell of the code store RAM
    private int cellSize;

    //-------------------------------
    // constructor

    public InstructionCall(int s) {
        cellSize = s;
        operands = new ArrayList<Token>();
        labels = new ArrayList<Token>();
        machineInstruction = null; //pseudoinstructions leave this null
        comment = "";
        sourceLine = null;
    }

    //-------------------------------
    // returns the opcode of the instruction being called
    public long getOpcode()
    {
        return machineInstruction.getOpcode();
    }


    //-------------------------------
    //returns the length of the call in bits
    //if the machineInstruction is null, it is assumed that the instruction call
    //is "data" which has its length in cells specified in its first operand
    public int getLength() throws AssemblyException.InvalidOperandError
    {
        if (machineInstruction != null) {
            return machineInstruction.length();
        }
        else {  // .data pseudoinstruction
            int length;
            try {
                length = Integer.parseInt((operands.get(0)).contents);
            } catch (NumberFormatException e) {
                throw new AssemblyException.InvalidOperandError("The number " +
                        (operands.get(0)).contents +
                        " could not be parsed as an integer--" +
                        "\n       It is probably too large",
                        operands.get(0));
            }
            return length * cellSize;
        }
    }

}  //end of class InstructionCall
