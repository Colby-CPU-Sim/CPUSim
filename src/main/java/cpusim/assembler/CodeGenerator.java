///////////////////////////////////////////////////////////////////////////////
// File:    	CodeGenerator.java
// Type:    	java application file
// Author:		Raymond H. Mazza III and Dale Skrien
// Project: 	CPU Sim 3.0
// Date:    	June, 2000
//
// Description:
//   The class that converts the parsed instructions into code.

/*
 * Michael Goldenberg, Ben Borchard, and Jinghui Yu made the following changes in 11/11/13
 * 
 * 1.) Modified the handleNormalInstruction method so that it put translated the operands
 * of the instructionsCall into the proper order as dictated by the order of the 
 * assembly fields and the instruction fields
 * 
 */


package cpusim.assembler;

import cpusim.model.Machine;
import cpusim.model.MachineInstruction;
import cpusim.util.Convert;

import java.util.ArrayList;
import java.util.List;

///////////////////////////////////////////////////////////////////////////////
// the CodeGenerator class

public class CodeGenerator
{
    private Machine machine;

    //-------------------------------
    // constructor
    public CodeGenerator(Machine machine) {
        this.machine = machine;
    }

    /**
     * This method converts InstructionCalls into AssembledInstructionCalls.
     *
     * @param instructionsWithNoVars List of InstructionCalls
     * @return List of AssembledInstructionCalls
     * @throws AssemblyException if something is illegal
     */
    public List<AssembledInstructionCall> generateCode(List<InstructionCall>
                                                               instructionsWithNoVars)
            throws AssemblyException {
        List<AssembledInstructionCall> assembledInstrCalls = new ArrayList<>();

        for (InstructionCall instrCall : instructionsWithNoVars) {
            //only need to look at the MachineInstruction and Operands parts
            //of the InstructionCall
            if (instrCall.machineInstruction != null) {
                handleNormalInstruction(instrCall, assembledInstrCalls);
            }
            else { //.data or .ascii pseudo-instruction
                handleData(instrCall, assembledInstrCalls);
            }
        }
        return assembledInstrCalls;
    }


    //the first operand of the data call specifies how many cells are to be
    //filled with the given data.  This operand is followed by either:
    //(a) one long value that will be stored using the given number of cells.
    //(b) a long value between 1 and 8 indicating cell size followed by
    //    a bracketed list of operands with length equal to the
    //    number of cells specified in the first operand divided by the cell size.
    //    Each of these values must fit in a cell of the given size.
    //    The list items are optionally separated by commas.
    //(c) a bracketed list of operands with length equal to the
    //    number of cells specified in the first operand.  These values are
    //    optionally separated by commas.
    //    Note that this case is just a special case of the preceding case
    //    where the cell size is omitted and assumed to be 1 cell.
    //examples:  .data 1 7 ; one 1-cell field, with value 7
    //			 .data 5 -1; one 5-cell field with (2's complement) value -1,
    //			 .data 5 0 ; one 5-cell field containing the value 0
    //           .data 5 5 [0] ; same as above
    //			 .data 3 [4 2 7] ; three cells with values 4, 2, and 7
    //			 .data 3 1 [4 2 7] ; same thing as above
    //			 .data 6 2 [4 2 7] ; three 2-cell values of 4, 2, 7
    private void handleData(InstructionCall instructionCall,
                            List<AssembledInstructionCall> instructionCallList) throws
            AssemblyException.InvalidOperandError, AssemblyException.MemoryError,
            AssemblyException.SyntaxError, AssemblyException.ValueError {
        int cellSize = machine.getCodeStore().getCellSize(); //num bits per cell
        List operands = instructionCall.operands;
        int numberOfCells = (int) getLong((Token) operands.get(0));

        if (numberOfCells <= 0) {
            throw new AssemblyException.InvalidOperandError("The first operand must be " +
                    "" + "" + "" + "" + "" + "greater than 0 for the data " +
                    "pseudo-instruction", (Token) operands.get(0));
        }

        if (operands.size() == 2) {
            if (numberOfCells * cellSize > 64) {
                throw new AssemblyException.MemoryError("A data pseudo-instruction" +
                        "" + " can use at most 64 bits to store one value", (Token)
                        operands.get(0));
            }
            else if (numberOfCells * cellSize < 64) {
                // if numberOfCells*cellSize == 64, the long value has to fit so
                // there is no need to test.
                // Otherwise, test size of value and throw an exception if it won't fit.
                // Allow the bits as unsigned or signed.
                long valueToStore = getLong((Token) operands.get(1));
                toBinaryString(valueToStore, numberOfCells * cellSize, false, true, (Token)
                        operands.get(1), false);
                /*
                //NOTE: The follow code was removed when the RAM cell size was
                //      allowed to be other than 8 bits.  Now, any data
                //      directive must refer to at most 64 bits of data total.
                //first add all the cells of data before the cells containing
                // the last 64 bits, since those cells will be all 0's or all 1's.
                int numSpecialCells = (64+cellSize-1)/cellSize;
                for (int i = 0; i < numberOfCells - numSpecialCells; i++)
                    instructionCallList.add(new AssembledInstructionCall(cellSize,
                            (valueToStore >= 0 ? 0 : -1),
                            (i == 0 ? instructionCall.comment : ""),
                            instructionCall.sourceLine));
                */
            }
            instructionCallList.add(new AssembledInstructionCall(numberOfCells *
                    cellSize, getLong((Token) operands.get(1)), instructionCall
                    .comment, instructionCall.sourceLine));

        }
        else { // the pseudo-instruction had some values inside brackets
            int numCellsPerValue = (int) getLong((Token) operands.get(1));
            if (numCellsPerValue * cellSize > 64) {
                throw new AssemblyException.MemoryError("A data pseudo-instruction" +
                        "" + " can use at most 64 bits to store one value", (Token)
                        operands.get(1));
            }
            if (numCellsPerValue <= 0) {
                throw new AssemblyException.InvalidOperandError("The second operand " +
                        "must be greater than 0 for this data pseudo-instruction",
                        (Token) operands.get(1));
            }
            else if (numberOfCells % numCellsPerValue != 0) {
                throw new AssemblyException.InvalidOperandError("The number of cells "
                        + "(the first operand) must be divisible by the number of " +
                        "cells per integer (the second operand if provided, " +
                        "otherwise 1)", (Token) operands.get(0));
            }
            //            else if (numberOfCells / numCellsPerValue != operands.size()
            // - 2) {
            //                throw new AssemblyException.InvalidOperandError("Error:
            // The number of "
            //                        + "cells (the first operand) must equal the
            // number of " +
            //                        "cells per integer (the second operand) times the
            // number of " +
            //                        "integers inside the brackets", (Token) operands
            // .get(0));
            //            }
            int numValuesGenerated = 0;
            int nextIndex = 2;
            while (numValuesGenerated < numberOfCells / numCellsPerValue) {
                long specificValue = getLong((Token) operands.get(nextIndex));
                //test that the value fits in cell size otherwise throw exception
                //allow the bits as unsigned or signed.
                toBinaryString(specificValue, cellSize * numCellsPerValue, false, true,
                        (Token) operands.get(nextIndex), false);

                instructionCallList.add(new AssembledInstructionCall(numCellsPerValue *
                        cellSize, specificValue, (numValuesGenerated == 0 ?
                        instructionCall.comment : ""), instructionCall.sourceLine));
                nextIndex++;
                if (nextIndex >= operands.size()) {
                    nextIndex = 2;
                }
                numValuesGenerated++;
            }
            //            for (int i = 2; i < operands.size(); i++) {
            //                long specificValue = getLong((Token) operands.get(i));
            //                //test that the value fits in cell size otherwise throw
            // exception
            //                //allow the bits as unsigned or signed.
            //                toBinaryString(specificValue, cellSize * numCellsPerValue,
            // false, true,
            //                        (Token) operands.get(i), false);
            //                instructionCallList.add(new AssembledInstructionCall
            // (numCellsPerValue *
            //                        cellSize, specificValue, (i == 2 ?
            // instructionCall.comment :
            //                        ""), instructionCall.sourceLine));
            //            }
        }
    }


    /**
     * adds to the aics list a new assembled instruction call corresponding
     * to the given instructionCall.
     * Along the way it checks to see that each operand value is legal,
     * based on the sign and size of the value
     * and the sign and number of bits devoted to that field. If not, it
     * throws an AssemblyException.
     *
     * @throws AssemblyException if something is illegal
     */
    private void handleNormalInstruction(InstructionCall instructionCall,
                                         List<AssembledInstructionCall> aics) throws
            AssemblyException {
        MachineInstruction machineInstruction = instructionCall.machineInstruction;
        int[] posFieldLengths = machineInstruction.getPositiveFieldLengths();
        boolean[] posLenFieldSigns = machineInstruction.getPosLenFieldSigns();
        List<Token> operands = instructionCall.operands;

        //convert opcode to a string in binary of correct field length
        //No exception should be thrown here since the opcode must be legal
        //at this point and fit in the number of bits devoted to it.
        String opcodePart = toBinaryString(machineInstruction.getOpcode(),
                posFieldLengths[0], false, false, null, true);

        //convert all the operands to binary strings of the appropriate size,
        //and add them to an array of Strings
        String[] instrParts = new String[posFieldLengths.length];
        instrParts[0] = opcodePart;
        int[] instrIndexToAssmIndex = machineInstruction.getRelativeOrderOfFields();
        for (int i = 1; i < instrParts.length; i++) {  // i = 0 refers to the opcode
            int currentFieldLength = posFieldLengths[i];
            boolean currentFieldSign = posLenFieldSigns[i];
            int assmIndex = instrIndexToAssmIndex[i];
            long op;
            if (assmIndex >= 0) { //the field's value is an operand of the assembly instr
                op = getLong(operands.get(instrIndexToAssmIndex[i]));
                instrParts[i] = toBinaryString(op, currentFieldLength,
                        currentFieldSign, true, operands.get(instrIndexToAssmIndex[i]), false);
            }
            else { // the instr operand is ignored and so not part of the assembly instr
                op = machineInstruction.getDefaultValue(i);
                instrParts[i] = toBinaryString(op, currentFieldLength,
                        currentFieldSign, true, null, false);
            }
        }

        String combinedFields = "";
        //combine all the Strings
        for (String instrPart : instrParts) {
            combinedFields += instrPart;
        }
        int numBits = combinedFields.length();

        //parse the combinedFields string, radix 2 (from binary), into a long
        //fromRootController a new Assembled instruction call with this long value and its
        //field lengths
        long longVal = Long.parseLong(combinedFields, 2);
        longVal = (longVal << (64 - numBits)) >> (64 - numBits); //extend the sign bit
        /* OLD WAY OF EXTENDING THE SIGN BIT
        if (combinedFields.charAt(0) == '1')
        //the number is supposed to be negative, correct for this
        {
            //to convert, subtract the max possible value for that number of bits
            //from it
            longVal = longVal - ((long) Math.pow(2.0,
                    (double) combinedFields.length()));
        }
        */

        aics.add(new AssembledInstructionCall(machineInstruction.length(), longVal,
                instructionCall.comment, instructionCall.sourceLine));
    }


    /**
     * converts the decimal integer into a string of 0's and 1's.
     * Opcodes don't get checked here because they get checked by
     * parser and when the machine instruction was first created.
     *
     * @param decimal    the long integer to be converted to a string of 0's and 1's
     * @param bits       the number of bits (0's and 1's) (i.e., the length of the string)
     * @param signed     true if the number must fit in the bits as a 2's complement
     *                   signed value and false if the number must fit as an
     *                   unsigned integer.
     * @param ignoreSign true if the preceding 'signed' parameter should be
     *                   ignored and any unsigned or unsigned decimal that fits
     *                   in the given number of bits is legal.
     * @param t          The Token from which the decimal was obtained-used for error
     *                   messages
     * @param isOpcode   true if this decimal is an opcode
     * @return the string of 0's and 1's representing the decimal value
     * @throws AssemblyException.ValueError if the value doesn't fit
     */
    private String toBinaryString(long decimal, int bits, boolean signed, boolean ignoreSign,
                                  Token t, boolean isOpcode) throws AssemblyException
            .ValueError {
        String result;
        long maxUnsignedValue = Long.rotateLeft(1, bits) - 1;
        long maxSignedValue = Long.rotateLeft(1, bits - 1) - 1;
        long minSignedValue = -(maxSignedValue + 1);

        if (decimal >= 0) {
            if (decimal > maxSignedValue && signed && !ignoreSign && !isOpcode) {
                throw new AssemblyException.ValueError("Value " + decimal + " is " +
                        "greater than the maximum allowed value for " + "this signed "
                        + bits + "-bit field, which is " + maxSignedValue, t);
            }
            if (decimal > maxUnsignedValue && !isOpcode) {
                throw new AssemblyException.ValueError("Value " + decimal + " is " +
                        "greater than the maximum allowed value for " + "this " + bits
                        + "-bit field, which is " + maxUnsignedValue, t);
            }
            result = Long.toBinaryString(decimal);
            //stick zeros in front of the number if necessary
            for (int i = result.length(); i < bits; i++) {
                result = "0" + result;
            }
        }
        else {
            if (!signed && !ignoreSign && !isOpcode) {
                throw new AssemblyException.ValueError("Value " + decimal + " is a " +
                        "negative value in a field that allows " + "only nonnegative "
                        + "values", t);
            }
            if (decimal < minSignedValue && !isOpcode) {
                throw new AssemblyException.ValueError("Value " + decimal + " is less "
                        + "than the least allowed " + "value for this field, which is "
                        + minSignedValue, t);
            }

            result = Long.toBinaryString(maxUnsignedValue + decimal + 1);
            //add the *negative* decimal number
        }

        return result;
    }

    //-------------------------------
    //gets the string in the token's contents and converts it to a long,
    //which it returns.
    private long getLong(Token token) throws AssemblyException.SyntaxError {
        String string = token.contents;
        long answer;
        try {
            answer = Convert.fromAnyBaseStringToLong(string);
        } catch (NumberFormatException e) {
            throw new AssemblyException.SyntaxError("The number " + token.contents + " " +
                    "" + "" + "" + "" + "could not be parsed as a long (64-bit integer)" +
                    "." + "\n  " + "  It might have illegal characters or be too " +
                    "large", token);
        }
        return answer;
    }


}  //end of class CodeGenerator











