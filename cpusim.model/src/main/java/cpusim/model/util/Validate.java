///////////////////////////////////////////////////////////////////////////////
// File:    	Validate.java
// Type:    	java application file
// Author:		Dale Skrien
// Project: 	CPU Sim
// Date:    	June, 2000
//
// Description:
//   This file contains the class for validating a array of MachineInstruction
//   objects.


/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard made the following modifications to 
 * this class 
 * 
 * on 10/27/13:
 * 
 * 1.) Removed the getFieldsFromFormat() method because we no longer use the format string
 * 2.) Created the nameableObjects() method that takes in an array of objects and checks
 * the validity of their names based using the preexisting allNamesAreUnique and allNamesAreNonEmpty
 * methods
 * 3.) Changed the type of the machineInstructions() method from an array of machine instructions
 * to a list thereof and made the appropriate changes to the body.  Did the same for the 
 * allOpcodesAreUnique() method
 * 4.) Moved the following methods to the the validate class from the class in parentheses
 * behind them (we also changed the return type of these methods to void and threw validation
 * exceptions where we would have returned false):
 * -  buffersAreWideEnough (IOTableController) 
 * -  registersHaveEqualWeights (LogicalTableController)
 * -  rangeInBoundSet (SetTableController)
 * -  valueFitsInNumBitsForSetMicros (SetTableController)
 * -  noNegativeDistances (ShiftTableController)
 * -  registersHaveEqualWidths (ShiftTableController)
 * -  rangeInBound (TestTableController)
 * -  rangesInBound (TransferRtoRTableController)
 * -  rangesInBound (TransferRtoATableController)
 * -  rangesInBound (TransferAtoRTableController)
 * -  someNameIsNone (ConditionBitTableController)
 * -  bitInBounds (ConditionBitTableController)
 * -  lengthsArePositive (RAMsTableController)
 * -  cellSizesAreValid (RAMsTableController)
 * -  widthsAreInBound (RegistersTableController)
 * -  initialValuesAreInbound (RegistersTableController)
 * 5.) Created the EQUNames method that checks the validity of the EQU names (we cannot
 * use the nameableObjects method because the EQU names need to be valid assembly language
 * as well)
 * 6.) Created opcodeIsValid method so that single opcodes can be checked to be valid
 * when they are entered
 * 7.) Created startingAddressIsValid and startingAddressInRamLength methods to check 
 * the validity of the starting address chosen by the user in the options dialog
 * 8.) Changed the type of the parameter of punctIsValid method from an array of PunctChars
 * to a list thereof
 * 9.) Added the containsDecodeMicro() method to be used in to validate the microinstructions in the 
 * fetch sequence
 * 
 * on 11/11/13:
 * 
 * 1.) added the method opcodeFieldIsFirst() that checks if the first assembly field of 
 * a machine instruction is the same field as the first instruction field.  Made use
 * of this methods in the machineInstructions() method
 * 2.) added the method readOnlyRegistersAreImmutable() to check that the TransferAtoR
 * and TransferRtoR aren't writing to read-only registers
 * 3.) added a method registerIsNotReadOnly() to check if a register is read only
 *
 * on 12/2/13:
 *
 * 1.) Changed the method readOnlyRegistersAreImmutable() so that it also checks the SetCondBit
 * isn't writing to read-only registers
 */

/**
 * File: Validate
 * Last update: December 2013
 * Authors: Stephen Morse, Ian Tibbits, Terrence Tan
 * Class: CS 361
 * Project 7
 * 
 * add isNewLineCharacter, isAsciiChar and isUnicodeChar method
 * refactor Field.Type and Field.Relativity
 * add fitsInBits from Convert
 */


///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

package cpusim.model.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

///////////////////////////////////////////////////////////////////////////////
// the libraries we need to import
import cpusim.model.Field;
import cpusim.model.Field.Type;
import cpusim.model.FieldValue;
import cpusim.model.Machine;
import cpusim.model.MachineInstruction;
import cpusim.model.Microinstruction;
import cpusim.model.Module;
import cpusim.model.assembler.EQU;
import cpusim.model.assembler.PunctChar;
import cpusim.model.microinstruction.CpusimSet;
import cpusim.model.microinstruction.Decode;
import cpusim.model.microinstruction.IO;
import cpusim.model.microinstruction.Logical;
import cpusim.model.microinstruction.SetCondBit;
import cpusim.model.microinstruction.Shift;
import cpusim.model.microinstruction.Test;
import cpusim.model.microinstruction.TransferAtoR;
import cpusim.model.microinstruction.TransferRtoA;
import cpusim.model.microinstruction.TransferRtoR;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.RAM;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import cpusim.model.util.conversion.ConvertLongs;
import cpusim.model.util.units.ArchValue;

import com.google.common.collect.Sets;

import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * This class has a bunch of static methods for testing the validity of
 * various values in machine instructions, micros, modules, and possibly
 * elsewhere.
 * If any of the values is not valid, the method throws a ValidationException.
 */
public class Validate
{
    
    /**
     *     ------------------------------------
     * checks whether all the MachineInstructions in the list
     * have valid properties, including
     * 1. unique, nonempty name that is a proper assembly language token
     * 2. a non-empty list of fields
     * 3. the first field is proper
     * 4. unique, positive opcode that fits in the first field
     * 5. Total length of all fields is a positive integer at most 64.
     * 6. The first instruction field and the first assembly field are the same field
    
     * It throws a ValidationException if one or more
     * of the properties are not satisfied
     * @param instrs list of the machine instructions to validate
     * @param machine the machine in its current state
     */
    public static void machineInstructions(List<MachineInstruction> instrs,
                                           Machine machine)
    {
        for (MachineInstruction instr : instrs) {
            nameIsValidAssembly(instr.getName(), machine);
            opcodeIsNonnegative(instr.getOpcode());
            fieldsListIsNotEmpty(instr);
            firstFieldIsProper(instr);
            //atMostOnePosLengthFieldIsOptional(instr); //not in this version
            opcodeFits(instr);
            fieldLengthsAreAtMost64(instr);
            opcodeFieldIsFirst(instr);
        }

        allOpcodesAreUnique(instrs);
        namedObjectsAreUniqueAndNonempty(instrs);
    }
    
    /**
     * Validates the namedObjects whose names are not used in assembly code.
     * Will throw a classCastException if the array of objects passed in are not
     * instances of NamedObject
     * @param objects the nameable objects to be checked
     * 
     * @deprecated Use {@link #namedObjectsAreUniqueAndNonempty(List)}
     */
    public static void namedObjectsAreUniqueAndNonempty(NamedObject[] objects)
    {
        namedObjectsAreUniqueAndNonempty(Arrays.asList(objects));
    }
    
    /**
     * Validates the namedObjects whose names are not used in assembly code.
     * Will throw a classCastException if the array of objects passed in are not
     * instances of NamedObject
     * @param objects the nameable objects to be checked
     */
    public static void namedObjectsAreUniqueAndNonempty(List<? extends NamedObject> objects) {
    	allNamesAreUnique(objects);
        allNamesAreNonEmpty(objects);
    }
    
    
    /**
     * Validates the names of a list of equs
     * @param equs the nameable objects to be checked
     * @param machine the machine in its current state
     */    
    public static void EQUNames(List<EQU> equs, Machine machine){
        allNamesAreUnique(equs);
        for (EQU equ : equs) {
                Validate.nameIsValidAssembly(equ.getName(), machine);
        }
    }
    
    /**
     * Validates a list of punctuation characters
     * @param chars list of punctuation characters
     */
    public static void punctChars(List<PunctChar> chars)
    {
        if (chars.size() != Machine.getDefaultPunctChars().size()) {
        	// TODO Is this really necessary? why store them if they're never different - KB
        	throw new ValidationException("The size of the chars list when done with MachineReader does not match " +
                        "the size of the default set of chars");
        }

        //check that there is exactly one each of usages of label, pseudo,
        //and comment
        char label = 'a', pseudo = 'a', comment = 'a';
        int labelCount = 0;
        int pseudoCount = 0;
        int commentCount = 0;
        for (PunctChar c : chars) {
            if (c.getUse() == PunctChar.Use.label) {
                labelCount++;
                label = c.getChar();
            }
            else if (c.getUse() == PunctChar.Use.pseudo) {
                pseudoCount++;
                pseudo = c.getChar();
            }
            else if (c.getUse() == PunctChar.Use.comment) {
                commentCount++;
                comment = c.getChar();
            }
        }

        if (labelCount != 1) {
            throw new ValidationException("There must be exactly one label character.");
        }
        else if (label == '+' || label == '-') {
            throw new ValidationException(
                    "The plus and minus signs cannot be the label character.");
        }
        if (pseudoCount != 1) {
            throw new ValidationException("There must be exactly one pseudo character.");
        }
        else if (pseudo == '+' || pseudo == '-') {
            throw new ValidationException(
                    "The plus and minus signs cannot be the pseudo character.");
        }
        if (commentCount != 1) {
            throw new ValidationException("There must be exactly one comment character.");
        }
        else if (comment == '+' || comment == '-') {
            throw new ValidationException(
                    "The plus and minus signs cannot be the comment character.");
        }
        if (label == pseudo || label == comment || pseudo == comment) {
            throw new ValidationException("The comment, label, and pseudo characters"
                    + " must all be distinct.");
        }
    }

    /**
     * validates that at most one positive-length field in a machine instruction is optional
     * @param instr the machine instruction to be checked
     */
    public static void atMostOnePosLengthFieldIsOptional(MachineInstruction instr)
    {
        List<Field> fields = instr.getAssemblyFields();
        int numOptional = 0;
        for(Field field: fields) {
            if(!(field.getType()==Type.required) && field.getNumBits() > 0)
                numOptional++;
        }
        if(numOptional > 1)
            throw new ValidationException("The instruction \"" +
                    instr.getName() + "\" has two or more" +
                    " positive-length optional fields.");
    }
    
    /**
     * validates that all names in a list of nameable objects are not empty.
     * Will throw a classCastException if the array of objects passed in are not
     * instances of NamedObject
     * @param objects list of nameable objects
     * 
     * @deprecated use {@link #allNamesAreNonEmpty(List)}
     */
    public static void allNamesAreNonEmpty(NamedObject[] objects)
    {
        allNamesAreNonEmpty(Arrays.asList(objects));
    }
    
    /**
     * validates that all names in a list of nameable objects are not empty.
     * Will throw a classCastException if the array of objects passed in are not
     * instances of NamedObject
     * @param objects list of nameable objects
     */
    public static void allNamesAreNonEmpty(List<? extends NamedObject> objects)
    {
        for (NamedObject obj : objects) {
            nameIsNonEmpty(obj.getName());
        }
    }


    /**
     * checks if all the opcodes in the list of machine instrs
     * are unique
     * @param instrs list of machine instructions whose opcodes need to be validated
     */
    public static void allOpcodesAreUnique(List<MachineInstruction> instrs)
    {
        for (int i = 0; i < instrs.size() - 1; i++) {
            for (int j = i + 1; j < instrs.size(); j++) {
                final int len1 = instrs.get(i).getInstructionFields().get(0).getNumBits();
                final int len2 = instrs.get(j).getInstructionFields().get(0).getNumBits();
                
                if (instrs.get(i).getOpcode() == instrs.get(j).getOpcode()
                        && len1 == len2) {
                    throw new ValidationException("The opcode \"" +
                             ConvertLongs.toHexString(instrs.get(i).getOpcode(), len1) +
                             "\" (hex) is used more than once.\nAll opcode " +
                             "fields must have unique sizes or values.");
                }
            }
        }
    }

    // 
    /**
     * checks if all the names in the array of machine instrs are unique.
     * Will throw a classCastException if the array of objects passed in are not
     * instances of NamedObject
     * @param list array of namedObjects
     * 
     * @deprecated Use {@link #allNamesAreUnique(List)}
     */
    public static void allNamesAreUnique(NamedObject[] list)
    {
        allNamesAreUnique(Arrays.<NamedObject>asList(list));
    }
    
    /**
     * checks if all the names in the array of machine instrs are unique.
     * Will throw a classCastException if the array of objects passed in are not
     * instances of NamedObject
     * @param list array of namedObjects
     * 
     * @since 2016-11-09
     */
    public static void allNamesAreUnique(List<? extends NamedObject> list)
    {
    	final Set<String> names = Sets.newHashSetWithExpectedSize(list.size());
    	for (NamedObject obj: list) {
    		if (names.contains(obj.getName())) {
    			throw new ValidationException("The name \"" + obj.getName() +
                        "\" is used more than once.\n" +
                        "All names must be unique.");
    		}
    		
    		names.add(obj.getName());
    	}
    }
    /**
     * Validates that a specific instruction has at least one field
     * @param instr machine instruction whose field list is to be validated
     */
    public static void fieldsListIsNotEmpty(MachineInstruction instr)
    {
        if (instr.getInstructionFields().isEmpty()) {
            throw new ValidationException("The instruction \"" +
                    instr.getName() + "\" has no fields.");
        }
    }
    
    /**
     * checks that the first field in the assemblyFields list is the same as the first
     * field in the instructionFields list (the opcode field)
     * @param instr instruction whose first assembly field needs to be checked
     */
    public static void opcodeFieldIsFirst(MachineInstruction instr){
        if (!instr.getAssemblyColors().get(0).equals(instr.getInstructionColors().get(0))){
            throw new ValidationException("Your opcode (the first field in the instruction fields)"
                    + " must be the first field in your assembly fields.  This is not the"
                    + " case for instruction "+instr.getName()+".");
        }
    }

    /**
     * checks if the first (opcode) field of the instruction has the
     * properties that is has numBits > 0, is absolute, is
     * required and is not restricted to particular values
     *
     * @param instr the MachineInstruction to be checked
     */
    public static void firstFieldIsProper(MachineInstruction instr)
    {
        Field opField = instr.getInstructionFields().get(0);
        if (opField.getNumBits() == 0 ||
                opField.getRelativity() != Field.Relativity.absolute ||
                !(opField.getType()==Type.required) ||
                opField.getValues().size() > 0) {
            throw new ValidationException(
                    "The first field \"" + opField.getName() + "\"" +
                        " of instruction \"" + instr.getName() + "\" is not" +
                        " a legal opcode field.\nIt must have a positive length, " +
                        "be absolute, be required,\nand " +
                        "not restricted to particular values.");
        }
    }

    /**
     * checks if the opcode is non-negative
     * @param opcode instruction whose opcode needs to be validated
     */
    private static void opcodeIsNonnegative(long opcode)
    {
        if (opcode < 0) {
            throw new ValidationException("The opcode " + opcode +
                        " of instruction \"" + opcode +
                        "\" is negative.\n" +
                        "All machine instructions must have nonnegative " +
                        "opcodes.");
        }
    }

    /**
     * determines if the opcode fits given the number of bits
     * @param instr the machine instruction (needed for the error message)
     */
    public static void opcodeFits(MachineInstruction instr) {
        if (instr.getOpcode() >= (long) Math.pow(2, instr.getInstructionFields().get(0).getNumBits())) {
            throw new ValidationException("The opcode \"" +
                    ConvertLongs.toHexString(instr.getOpcode(),
                            instr.getNumBits()) +
                    "\" (hex) of instruction \"" + instr.getName() +
                    "\" is too big for the first field of the instruction.");
        }
    }

    /**
     * checks if the name is a nonempty string
     * @param name string to check
     */
    public static void nameIsNonEmpty(String name)
    {
        if (name.equals("")) {
            throw new ValidationException("A name must have at least one character.");
        }
    }

    /**
     * checks if the name is a valid assembly language name
     * @param name string to check
     * @param machine the current state of the machine
     */
    public static void nameIsValidAssembly(String name, Machine machine)
    {
        nameIsValidAssembly(name, machine.getPunctChars());
    }

    /**
     * checks if the name is a valid assembly language name
     * @param name string to be checked
     * @param punctChars array of punctuation characters
     */
    public static void nameIsValidAssembly(String name, List<PunctChar> punctChars)
    {
        nameIsNonEmpty(name);

        //set up a set of word characters
        Set<Character> validChars = new HashSet<Character>();
        for (char c = 'a'; c <= 'z'; c++)
            validChars.add(c);
        for (char c = 'A'; c <= 'Z'; c++)
            validChars.add(c);
        for (char c = '0'; c <= '9'; c++)
            validChars.add(c);
        for (PunctChar ch : punctChars)
                if(ch.getUse() == PunctChar.Use.symbol)
                        validChars.add(ch.getChar());

        //check each letter of the name for containment in the set
        for (int j = 0; j < name.length(); j++) {
            char ch = name.charAt(j);
            if(! validChars.contains(ch))
                throw new ValidationException("The name \"" + name +
                                "\" is not  a " +
                                "valid assembly language token.\nIt must " +
                                "contain only letters, digits, and "
                        + "punctuation characters that are used in symbols.");
        }

        //check that the name does not start with a digit
        char ch = name.charAt(0);
        if('0' <= ch && ch <= '9')
                throw new ValidationException("The name \"" + name +
                         "\" is not a valid assembly language symbol.\n"
                        + "It cannot start with a number.");
    }

    /**
     * validates that the field lengths of a certain instruction are at most 64 bits
     * @param instr machine instruction to be checked
     */
    public static void fieldLengthsAreAtMost64(MachineInstruction instr)
    {
        final List<Integer> fieldLengths = instr.getPositiveFieldLengths();
        final int sum = fieldLengths.stream().mapToInt(Integer::intValue).sum();
        
        if (sum <= 0 || sum > 64) {
            throw new ValidationException(
                    "The values of the field lengths" +
                    " of instruction \"" + instr.getName() +
                    "\"\nmust add up to a positive number " +
                    "less than or equal to 64.");
        }
    }

    /**
     * check if the ios of type ascii have 8-bit-wide
     * (or greater) buffers and ios of type unicode have
     * 16-bit-wide (or greater) buffers.
     * @param ios array of ios microinstruction
     */
    public static void buffersAreWideEnough(IO[] ios)
    {
        for (IO io : ios) {
            if (io.getType().equals("ascii") &&
                    io.getBuffer().getWidth() < 1) {
                throw new ValidationException("IO \"" + io + "\" is of type " +
                        "ascii and so needs a\nbuffer register at least " +
                        "8 bits wide.");
            }
            else if (io.getType().equals("unicode") &&
                    io.getBuffer().getWidth() < 2) {
                throw new ValidationException("IO \"" + io + "\" is of type " +
                        "unicode and so needs a\nbuffer register at least " +
                        "16 bits wide.");
            }
        }
    }

    /**
     * checks if the registers have equal widths
     * @param logicals an array of Logicals to check
     * Logical objects with all equal width registers
     */
    public static void registersHaveEqualWidths(Logical[] logicals)
    {

        for (Logical logical : logicals) {
            // get width of the source1, source2, and destination
            // registers, if they are different, then the validity
            // test fails
            if (logical.getSource1().getWidth() ==
                    logical.getSource2().getWidth() &&
                    logical.getSource2().getWidth() ==
                            logical.getDestination().getWidth()) {
                continue;
            }
            else {
                throw new ValidationException("At least one of the registers in the " +
                        "microinstruction \"" + logical.getName() +
                        "\" has\na bit width that is different than one " +
                        "or more of the others.\nAll registers must have " +
                        "the same number of bits.");
            }
        }
    }

    /**
     * checks if the value of each Set microinstruction fits
     * in the given number of bits in the microinstruction.
     * The value is treated as either a 2's complement value or
     * and unsigned integer value and so the range of legal values
     * for n bits is -(2^(n-1)) to 2^n - 1.
     * @param sets an array of Objects to check.
     */
    public static void valueFitsInNumBitsForSetMicros(CpusimSet[] sets)
    {
        for (CpusimSet set : sets) {
            try {
                fitsInBits(set.getValue(), set.getNumBits());
            } catch (ValidationException e) {
                throw new ValidationException(e.getMessage() +
                        " in the microinstruction \"" + set.getName() + "\".");
            }
        }
    }

    /**
     * checks if Set objects with all ranges all in Bounds properly
     * @param sets an array of Sets to check
     * Set objects with all ranges all in Bounds properly
     */
    public static void rangeInBound(CpusimSet[] sets)
    {

        for (CpusimSet set1 : sets) {
            final CpusimSet set = set1;
            final int start = set.getStart();
            final int numBits = set.getNumBits();
//            final long value = set.getValue();

            Register register = set.getRegister();

            if (start < 0) {
                throw new ValidationException("You cannot specify a negative value for the " +
                        "start bit\n" +
                        "in the instruction " + set.getName() + ".");
            }
            
            if (numBits <= 0) {
                throw new ValidationException("You must specify a positive value for the " +
                        "bitwise width\nof the set range " +
                        "in the instruction " + set.getName() + ".");
            }
            
            final int regWidth = register.getWidth();
            
            if (start >= regWidth) {
                throw new ValidationException("Invalid start index for the specified register " +
                        "in the Set microinstruction " + set.getName() +
                        ".\nIt must be non-negative, and less than the " +
                        "register's length.");
            }
            
            if ((start + numBits) > regWidth) {
                throw new ValidationException("The bitwise width of the set area in the Set " +
                        "microinstruction " +
                        set.getName() + "\n is too large to fit in the register.");
            }
        }
    }

        /**
         * checks the array of Shift micros to make sure none have
         * a negative shift distances
         * @param shifts the list of shift objects
         */
    public static void noNegativeDistances(Shift[] shifts)
    {
        for (Shift shift : shifts) {
            if (shift.getDistance() <= 0) {
                throw new ValidationException("The microinstruction \"" + shift.getName() +
                        "\" has a negative or zero shift distance.\nShift distances " +
                        "must be positive.");
            }
        }
    }

    /**
     * checks if the two registers specified in the shift microinstructions have the same
     * width
     * @param shifts and array of shift microinstructions
     */
    public static void registersHaveEqualWidths(Shift[] shifts)
    {
        for (Shift shift : shifts) {
            if (shift.getSource().getWidth() !=
                    shift.getDestination().getWidth()) {
                throw new ValidationException("The microinstruction " + shift.getName() +
                        " has different-sized registers designated " +
                        "for source and destination.\nBoth registers " +
                        "must have the same number of bits.");
            }
        }
    }


    /**
     * checks if the objects with all ranges all in Bounds properly
     * @param tests an array of Sets to check
     * the objects with all ranges all in Bounds properly
     */
    public static void rangeInBound(List<Test> tests)
    {
        for (Test test : tests) {
        	final int start = test.getStart();
        	final int numBits = test.getNumBits();

            if (start < 0 || numBits < 0) {
                throw new ValidationException("You cannot specify a negative value for the " +
                        "start bits,\nor the bitwise width of the test range\n" +
                        "in the microinstruction " + test.getName() + ".");
            }
            else if (start >= test.getRegister().getWidth()) {
                throw new ValidationException("The start bit in the microinstruction "
                        + test.getName() + " is out of range.\n" +
                        "It must be non-negative, and less than the " +
                        "register's length.");
            }
            else if ((start + numBits) > test.getRegister().getWidth()) {
                throw new ValidationException("The bits specified in the Test " +
                        "microinstruction " + test.getName() +
                        " are too large to fit in the register.");
            }
        }
    }

    /**
     * check if the ranges are in bound.
     * @param transferAtoRs an array of TransferRtoRs to check.
     * TransferRtoR objects with all ranges all in Bounds properly.
     */
    public static void rangesAreInBound(List<TransferAtoR> transferAtoRs)
    {
        int srcStartBit, destStartBit, numBits, indexStart, indexNumBits;
        TransferAtoR temp;
        boolean startProblem = false;
        String boundPhrase = "";

        for (TransferAtoR transferAtoR : transferAtoRs) {
            temp = transferAtoR;
            srcStartBit = temp.getSrcStartBit();
            destStartBit = temp.getDestStartBit();
            numBits = temp.getNumBits();
            indexNumBits = temp.getIndexNumBits();
            indexStart = temp.getIndexStart();
            if (srcStartBit < 0 || destStartBit < 0 || numBits < 0 ||
                    indexStart < 0) {
                throw new ValidationException("You have a negative value for one of the " +
                        "start bits or the number of bits\nin the " +
                        "microinstruction \"" + temp.getName() + "\".");
            }
            if (srcStartBit > temp.getSource().getWidth()) {
                startProblem = true;
                boundPhrase = "srcStartBit";
            }
            else if (destStartBit > temp.getDest().getWidth()) {
                startProblem = true;
                boundPhrase = "destStartBit";
            }
            else if (indexStart > temp.getIndex().getWidth()) {
                startProblem = true;
                boundPhrase = "indexStart";
            }
            if (startProblem) {
                throw new ValidationException(boundPhrase + " has an invalid value for the " +
                        "specified register in instruction " + temp.getName() +
                        ".\nIt must be non-negative, and less than the " +
                        "register's length.");
            }
            if (indexNumBits <= 0) {
                throw new ValidationException("A positive number of bits must be specified " +
                        "for the index register.");
            }
            else if (srcStartBit + numBits >
                    temp.getSource().getWidth() ||
                    destStartBit + numBits >
                            temp.getDest().getWidth()) {
                throw new ValidationException("The number of bits being transferred is " +
                        "too large to fit in the source array or the " +
                        "destination register.\n" +
                        "Please specify a new start bit or a smaller number " +
                        "of bits to copy in the microinstruction \"" +
                        temp.getName() + ".\"");
            }
            else if (indexStart + indexNumBits > temp.getIndex().getWidth()) {
                throw new ValidationException("The number of bits specified in the index " +
                        "register is " +
                        "too large to fit in the index register.\n" +
                        "Please specify a new start bit or a smaller number " +
                        "of bits in the microinstruction \"" +
                        temp.getName() + ".\"");
            }
        }
    }

    /**
     * check if the ranges are in bound.
     * @param transferRtoAs an array of TransferRtoRs to check.
     * TransferRtoR objects with all ranges all in Bounds properly.
     */
    public static void rangesAreInBound(TransferRtoA[] transferRtoAs)
    {
        int srcStartBit, destStartBit, numBits, indexStart, indexNumBits;
        TransferRtoA temp;
        boolean startProblem = false;
        String boundPhrase = "";

        for (TransferRtoA transferRtoA : transferRtoAs) {
            temp = transferRtoA;
            srcStartBit = temp.getSrcStartBit();
            destStartBit = temp.getDestStartBit();
            numBits = temp.getNumBits();
            indexNumBits = temp.getIndexNumBits();
            indexStart = temp.getIndexStart();
            if (srcStartBit < 0 || destStartBit < 0 || numBits < 0 ||
                    indexStart < 0) {
                throw new ValidationException("You have a negative value for one of the " +
                        "start bits or the number of bits\nin the " +
                        "microinstruction \"" + temp.getName() + "\".");
            }
            if (srcStartBit > temp.getSource().getWidth()) {
                startProblem = true;
                boundPhrase = "srcStartBit";
            }
            else if (destStartBit > temp.getDest().getWidth()) {
                startProblem = true;
                boundPhrase = "destStartBit";
            }
            else if (indexStart > temp.getIndex().getWidth()) {
                startProblem = true;
                boundPhrase = "indexStart";
            }
            if (startProblem) {
                throw new ValidationException(boundPhrase + " has an invalid value for the " +
                        "specified register in instruction " + temp.getName() +
                        ".\nIt must be non-negative, and less than the " +
                        "register's length.");
            }
            if (indexNumBits <= 0) {
                throw new ValidationException("A positive number of bits must be specified " +
                        "for the index register.");
            }
            else if (srcStartBit + numBits > temp.getSource().getWidth() ||
                    destStartBit + numBits > temp.getDest().getWidth()) {
                throw new ValidationException("The number of bits being transferred is " +
                        "too large to fit in the source register or the " +
                        "destination array.\n" +
                        "Please specify a new start bit or a smaller number " +
                        "of bits to copy in the microinstruction \"" +
                        temp.getName() + ".\"");
            }
            else if (indexStart + indexNumBits > temp.getIndex().getWidth()) {
                throw new ValidationException("The number of bits specified in the index " +
                        "register is " +
                        "too large to fit in the index register.\n" +
                        "Please specify a new start bit or a smaller number " +
                        "of bits in the microinstruction \"" +
                        temp.getName() + ".\"");
            }
        }

    }

    /**
     * check if the ranges are in bound.
     * @param transferRtoRs an array of TransferRtoRs to check.
     * TransferRtoR objects with all ranges all in Bounds properly.
     */
    public static void rangesAreInBound(TransferRtoR[] transferRtoRs)
    {

        int srcStartBit, destStartBit, numBits;
        TransferRtoR temp;
        boolean startProblem = false;
        String boundPhrase = "";

        for (TransferRtoR transferRtoR : transferRtoRs) {
            temp = transferRtoR;
            srcStartBit = temp.getSrcStartBit();
            destStartBit = temp.getDestStartBit();
            numBits = temp.getNumBits();
            if (srcStartBit < 0 || destStartBit < 0 || numBits < 0) {
                throw new ValidationException("You cannot specify a negative value for the " +
                        "start and end bits, \nor the bitwise width of the TransferRtoR range.\n" +
                        "Please fix this in the microinstruction \"" + temp.getName() + ".\"");
            }
            if (srcStartBit > temp.getSource().getWidth()) {
                startProblem = true;
                boundPhrase = "srcStartBit";
            }
            else if (destStartBit > temp.getDest().getWidth()) {
                startProblem = true;
                boundPhrase = "destStartBit";
            }
            if (startProblem) {
                throw new ValidationException(boundPhrase + " has an invalid index for the " +
                        "specified register in instruction " + temp.getName() +
                        ".\nIt must be non-negative, and less than the register's length.");
            }
            else if (srcStartBit + numBits > temp.getSource().getWidth() ||
                    destStartBit + numBits > temp.getDest().getWidth()) {
                throw new ValidationException("In the microinstruction \"" + temp.getName() +
                        "\",\nthe bitwise width of the transfer area is too large " +
                        "to fit in \neither the source or the destination registers.");
            }
        }

    }

    /**
     * check if one or more names is "<none>"   (ConditionBit)
     * @param list a list of module objects
     * 
     * @deprecated Use {@link #someNameIsNone(List)}
     */
    public static void someNameIsNone(Module<?>[] list)
    {
        someNameIsNone(Arrays.asList(list));
    }
    
    /**
     * check if one or more names is "<none>"   (ConditionBit)
     * @param list a list of module objects
     */
    public static void someNameIsNone(List<? extends Module<?>> list)
    {
        //find if any existing micro already has the name "<none>"
        for (Module<?> aList : list) {
            if (aList.getName().equals("<none>")) {
                throw new ValidationException("A ConditionBit has been given the " +
                        "name \"<none>\".\nThat name is reserved to indicate" +
                        " that no condition bit is desired.");
            }
        }
    }

    /**
     * check if all the bis are in bounds
     * @param conditionBits an array of Registers to check
     * Register objects with all ranges all in bounds
     * properly
     */
    public static void bitInBounds(ConditionBit[] conditionBits)
    {
        int width, bit;
        ConditionBit nextCB;

        for (ConditionBit conditionBit : conditionBits) {
            nextCB = conditionBit;
            width = nextCB.getRegister().getWidth();
            bit = nextCB.getBit();
            if (bit < 0) {
                throw new ValidationException("You cannot specify a negative value for the " +
                        "bit index of the ConditionBit " + nextCB.getName() + ".");
            }
            else if (bit >= width) {
                throw new ValidationException("ConditionBit " + nextCB.getName() +
                        " must have an index less than the length of the register.");
            }
        }
    }

    /**
     * check if all the cells in the ram have the valid sizes
     * @param rams the list of rams that will be checked
     */
    public static void cellSizesAreValid(RAM[] rams) {
        for (RAM ram : rams) {
            int size = ram.getCellSize();
            if (size <= 0 || size > 64) {
                throw new ValidationException("The RAM module \"" + ram.getName() +
                        "\" has cell size " + ram.getCellSize() +
                        ".\nThe cell size must be an integer from 1 to 64.");
            }
        }
    }

    /**
     * checks whether all the RAMs have  positive length
     * @param rams an array of RAMs
     * positive length
     */
    public static void lengthsArePositive(RAM[] rams)
    {
        for (RAM ram : rams)
            if (ram.getLength() <= 0) {
                throw new ValidationException("The RAM module \"" + ram.getName() +
                        "\" has length " + ram.getLength() +
                        ".\nThe length must be a positive integer.");
            }
    }
    
    /**
     * checks if a given string is a legal binary, decimal, and/or hexadecimal number
     * @param string string in question
     */
    public static void stringIsLegalBinHexOrDecNumber(String string){
        try{
            Convert.fromAnyBaseStringToLong(string);
        }
        catch(NumberFormatException ex){
            throw new ValidationException(ex.getMessage());
        }
    }
    
    /**
     * checks if the opcode of a certain machine instruction is unique to that instruction
     * and if it is positive
     * @param machineInstructions
     * @param machineInstruction 
     */
    public static void instructionsOpcodeIsValid(List<MachineInstruction> machineInstructions, 
    		MachineInstruction machineInstruction){
        
        opcodeIsNonnegative(machineInstruction.getOpcode());
        
        for (MachineInstruction instr : machineInstructions){
            if (!instr.equals(machineInstruction)){
                if (instr.getOpcode() == machineInstruction.getOpcode()){
                    throw new ValidationException("This opcode is currently the opcode "
                            + "of the instruction "+instr.getName()+".  All opcodes "
                            + "must be unique.");
                }
            }
        }
    }
    
    /**
     * check whether array has Register objects with all widths
     * all in bounds properly
     *
     * @param registers an array of Registers to check
     * with all widths all in bounds properly
     */
    public static void widthsAreInBound(Register[] registers)
    {
        int width;
        Register nextRegister;

        for (Register register : registers) {
            nextRegister = register;
            width = nextRegister.getWidth();
            if (width <= 0) {
                throw new ValidationException("You must specify a positive value for the " +
                        "bitwise width\nof the Register " + nextRegister.getName() + ".");
            }
            if (width > 64) {
                throw new ValidationException("Register " + nextRegister.getName() +
                        " can have a width of at most 64 bits.");
            }
        }

    } // end rangesAreInBound()

    /**
     * Check of the initialValue of some registers are in the proper bound 
     * @param registers registers to be checked
     */
    public static void initialValuesAreInbound(Register[] registers){
        int width;
        Register nextRegister;

        for (Register register : registers) {
            nextRegister = register;
            width = nextRegister.getWidth();
            BigInteger max = BigInteger.valueOf(2).pow(width - 1);
            BigInteger initial = BigInteger.valueOf(nextRegister.getInitialValue());
            BigInteger unsignedMax = max.shiftLeft(1).subtract(BigInteger.ONE);
            if (!(max.negate().compareTo(initial) <= 0 &&
                    initial.compareTo(unsignedMax) <= 0)) {
                throw new ValidationException("The initial value of register " +
                        nextRegister.getName() +
                        " is out of range. It must be set to a value greater than " +
                        "or equal to " + max.negate() + " and smaller than or equal to " +
                        unsignedMax + ".");
            }
        }
    }


    

    /**
     * check if all registers have appropriate width for micro instructions.
     * @param h a HashMap of key = original Registers, value = new width
     * any micros to be invalid.
     */
    public static void registerWidthsAreOkayForMicros(Machine machine, Map<? extends Module<?>, Integer> h)
    {
        //make a HashMap of old registers as keys and
        //old widths as Integer values
        Map<Module<?>, Integer> newWidths = new HashMap<>();
        final List<Register> registers = machine.getModule("registers", Register.class);
        for (Register r: registers) {
            newWidths.put(r, r.getWidth());
        }
        
        for (RegisterArray array: machine.getModule("registerArrays", RegisterArray.class)) {
            for (Register r: array) {
                newWidths.put(r, r.getWidth());
            }
        }

        //now adjust the HashMap to use the new proposed widths in h
        newWidths.putAll(h);

        //now go through all micros to see if width changes will make them
        //illegal.  Affected micros are:
        //		shift, logical, set, test, and the 3 transfers
        //Shifts and logicals are special since registers used
        //by them must be the same widths.
        ObservableList<Microinstruction> shifts = machine.getMicros("shift");
        for (Microinstruction shift1 : shifts) {
            Shift shift = (Shift) shift1;
            Register source = shift.getSource();
            int sourceWidth = newWidths.get(source);
            int destWidth = newWidths.get(shift.getDestination());
            if (sourceWidth != destWidth) {
                throw new ValidationException("The new width " + sourceWidth +
                        " of register " + shift.getSource() + "\nand new width " +
                        destWidth + " of register " + shift.getDestination() +
                        "\ncause microinstruction " + shift + " to be invalid.");
            }
        }
        
        ObservableList<Microinstruction> logicals = machine.getMicros("logical");
        for (Microinstruction logical1 : logicals) {
            Logical logical = (Logical) logical1;
            int source1Width = newWidths.get(logical.getSource1());
            int source2Width = newWidths.get(logical.getSource2());
            int destWidth = newWidths.get(logical.getDestination());
            if (source1Width != destWidth || source2Width != destWidth) {
                throw new ValidationException("The new width " + source1Width +
                        " of register " + logical.getSource1() + ",\nnew width " +
                        source2Width + " of register " + logical.getSource2() +
                        "\nand new width " + destWidth +
                        " of register " + logical.getDestination() +
                        "\ncause microinstruction " + logical + " to be invalid.");
            }
        }
        
        ObservableList<Microinstruction> sets = machine.getMicros("set");
        for (Microinstruction set1 : sets) {
            CpusimSet set = (CpusimSet) set1;
            int newWidth = newWidths.get(set.getRegister()).intValue();
            if (newWidth < set.getStart() + set.getNumBits()) {
                throw new ValidationException("The new width " + newWidth +
                        " of register " + set.getRegister() +
                        "\ncauses microinstruction " + set + " to be invalid.");
            }
        }
        
        ObservableList<Microinstruction> tests = machine.getMicros("test");
        for (Microinstruction test1 : tests) {
            Test test = (Test) test1;
            int newWidth =
                    (Integer) newWidths.get(test.getRegister());
            if (newWidth < test.getStart() + test.getNumBits()) {
                throw new ValidationException("The new width " + newWidth +
                        " of register " + test.getRegister() +
                        "\ncauses microinstruction " + test + " to be invalid.");
            }
        }
        
        ObservableList<Microinstruction> transferRtoRs = machine.getMicros("transferRtoR");
        for (Microinstruction transferRtoR : transferRtoRs) {
            TransferRtoR t = (TransferRtoR) transferRtoR;
            int sourceWidth =
                    (Integer) newWidths.get(t.getSource());
            int destWidth =
                    (Integer) newWidths.get(t.getDest());
            if (sourceWidth < t.getSrcStartBit() + t.getNumBits() ||
                    destWidth < t.getDestStartBit() + t.getNumBits()) {
                throw new ValidationException("The new width " + sourceWidth +
                        " of register " + t.getSource() + "\nor new width " +
                        destWidth + " of register " + t.getDest() +
                        "\ncauses microinstruction " + t + " to be invalid.");
            }
        }
        ObservableList<Microinstruction> transferRtoAs = machine.getMicros("transferRtoA");
        for (Microinstruction transferRtoA : transferRtoAs) {
            TransferRtoA t = (TransferRtoA) transferRtoA;
            int sourceWidth =
                    (Integer) newWidths.get(t.getSource());
            if (sourceWidth < t.getSrcStartBit() + t.getNumBits()) {
                throw new ValidationException("The new width " + sourceWidth +
                        " of register " + t.getSource() +
                        "\ncauses microinstruction " + t + " to be invalid.");
            }
            int indexWidth =
                    (Integer) newWidths.get(t.getIndex());
            if (indexWidth < t.getIndexStart() + t.getIndexNumBits()) {
                throw new ValidationException("The new width " + indexWidth +
                        " of register " + t.getIndex() +
                        "\ncauses microinstruction " + t + " to be invalid.");
            }
        }
        ObservableList<Microinstruction> transferAtoRs = machine.getMicros("transferAtoR");
        for (Microinstruction transferAtoR : transferAtoRs) {
            TransferAtoR t = (TransferAtoR) transferAtoR;
            int destWidth =
                    (Integer) newWidths.get(t.getDest());
            if (destWidth < t.getDestStartBit() + t.getNumBits()) {
                throw new ValidationException("The new width " + destWidth +
                        " of register " + t.getDest() +
                        "\ncauses microinstruction " + t + " to be invalid.");
            }
            int indexWidth = newWidths.get(t.getIndex());
            if (indexWidth < t.getIndexStart() + t.getIndexNumBits()) {
                throw new ValidationException("The new width " + indexWidth +
                        " of register " + t.getIndex() +
                        "\ncauses microinstruction " + t + " to be invalid.");
            }
        }
        // The next code segment is commented out since we don't care about
        // the relationship between memory buffer register size and
        // RAM's cell size.  A read or write will just start writing
        // to the high order bits of the current cell and will keep
        // going until all the bits of the register have been read or written.
        /*
        Vector memoryAccesses = machine.getMicros("memoryAccess");
        for (int i = 0; i < memoryAccesses.size(); i++) {
            MemoryAccess t = (MemoryAccess) memoryAccesses.elementAt(i);
            int newWidth = (Integer) newWidths.get(t.getData());
            if ((newWidth % 8) != 0) {
                displayError("The new width " + newWidth +
                        " of register " + t.getData() +
                        "\ncauses microinstruction " + t + " to be invalid." +
                        "\nData register widths must be a multiple of 8.");
                return false;
            }
        }
        */

    }

    /**
     * Checks whether array has Register objects with all
     * ranges all in bounds properly
     * @param registerArrays an array of Registers to check
     * with all ranges all in bounds properly
     */
    public static void rangesAreInBound(RegisterArray[] registerArrays)
    {
        int width;
        RegisterArray nextArray;

        for (RegisterArray registerArray : registerArrays) {
            nextArray = registerArray;
            width = nextArray.getWidth();
            if (width <= 0) {
                throw new ValidationException("You must specify a positive value for the " +
                        "bitwise width\nof the registers in the RegisterArray " +
                        nextArray.getName() + ".");
            }
            else if (width > 64) {
                throw new ValidationException("The registers in RegisterArray " + nextArray.getName() +
                        " can be at most 64 bits wide.");
            }
        }

    } // end rangesAreInBound()

    

    

   
    /**
     * Check if the address in the Loading tab is valid and in bound.
     * @param address string of the address
     * @param ramLength length of the RAM
     */
    public static void addressIsValidAndInBound(String address,
                                                int ramLength,
                                                TabPane tabPane,
                                                Tab loadingTab){
        int startInt = 0;
        // Catch non number entries
        try {
            startInt = Integer.parseInt(address);
        } catch (NumberFormatException nfe) {
            if (tabPane.getSelectionModel().getSelectedItem() != loadingTab) {
                tabPane.getSelectionModel().select(loadingTab);
            throw new ValidationException(
                    "The starting address is invalid. Please change to a" +
                            " valid integer.");
            }
        }

        // Catch out of bound entries
        if (0 > startInt || startInt >= ramLength) {
            if (tabPane.getSelectionModel().getSelectedItem() != loadingTab) {
                tabPane.getSelectionModel().select(loadingTab);
                throw new ValidationException(
                    "The starting address is out of range. Please change to a" +
                            " valid integer between 0 and " + (ramLength - 1) + ".");
            }
        }
    }
    
    /**
     * Checks if the starting address of a ram is valid
     * @param startAddr the starting address
     * @param ramLength the length of the ram in which the code is being stored
     */
    public static void startingAddressIsValid(String startAddr, int ramLength){
        stringIsLegalBinHexOrDecNumber(startAddr);
        startingAddressInRAMRange(Convert.fromAnyBaseStringToLong(startAddr), ramLength);
    }
    
    /**
     * Checks if the starting address is in range of the RAM
     * @param startAddr starting address
     * @param ramLength the length of the ram in which the code is stored
     */
    public static void startingAddressInRAMRange(long startAddr, int ramLength){
        if (0 > startAddr || startAddr >= ramLength){
            throw new ValidationException("The starting address is out of range. Please change to a" +
	                            " valid integer between 0 and " + (ramLength - 1) + ".");
        }
    }
    
    /**
     * checks a list of microinstruction to see if at least one of them is a decode
     * microinstruction
     * @param micros list of microinstructions
     */
    public static void containsDecodeMicro(List<Microinstruction> micros){
        boolean containsDecode = false;
        for (Microinstruction micro : micros){
            if (micro instanceof Decode){
                containsDecode = true;
                break;
            }
        }
        if (!containsDecode){
            throw new ValidationException("The fetch sequence must contain a decode "
                    + "microinstruction.");
        }
    }

    /**
     * checks if the dest register of the given microinstruction is read-only
     * @param register destination register of transferRtoR or transferAtoR
     * @param microName name of the given microinstruction
     */
    public static void registerIsNotReadOnly(Register register, String microName){
        if (register.getReadOnly() == true)
            throw new ValidationException("The destination register " +
                    register.getName() + " of the microinstruction " +
                    microName + " is read-only.");
    }

    /**
     * When saving the modified registers to machine, check if those read-only
     * regsters are used in any of the transferAtoA or transferRtoR microinstructions.
     * @param registers an array of registers
     * @param transferAtoRs an list of transferAtoR microinstructions
     * @param transferRtoRs an list of transferRtoR microinstructions
     * @param setCondBits and list of setCondBit microinstructions
     */
    public static void readOnlyRegistersAreImmutable(Register[] registers,
                                                     ObservableList transferAtoRs,
                                                     ObservableList transferRtoRs,
                                                     ObservableList setCondBits){
        List readOnlyRegisters = new ArrayList<String>();
        for (int i = 0; i < registers.length; i++){
            if (registers[i].getReadOnly()==true)
                readOnlyRegisters.add(registers[i].getName());
        }

        if (readOnlyRegisters.size() == 0)
            return;
        else {
            for (Object o: transferAtoRs){
                if (readOnlyRegisters.contains(((TransferAtoR)o).getDest().getName()))
                    throw new ValidationException("The register " +
                            ((TransferAtoR)o).getDest().getName() +
                            " is used as the destination reigster in the " +
                            "microinstruction transferAtoR " + ((TransferAtoR)o).getName() +
                            ". You should change the microinstruction before " +
                            "setting the register to read-only.");
            }

            for (Object o: transferRtoRs){
                if (readOnlyRegisters.contains(((TransferRtoR)o).getDest().getName()))
                throw new ValidationException("The register " +
                        ((TransferRtoR)o).getDest().getName() +
                        " is used as the destination reigster in the " +
                        "microinstruction transferRtoR " + ((TransferRtoR)o).getName() +
                        ". You should change the microinstruction before " +
                        "setting the register to read-only.");
            }

            for (Object o: setCondBits){
                if (readOnlyRegisters.contains(((SetCondBit)o).getBit().getRegister()))
                    throw new ValidationException("The register " +
                            ((SetCondBit)o).getBit().getRegister() +
                            " is used as the condition flag reigster in the " +
                            "microinstruction setCondBit " + ((SetCondBit)o).getName() +
                            ". You should change the microinstruction before " +
                            "setting the register to read-only.");
            }
        }
    }

    /**
     * Validates that the registers containing condition bits are not read only.
     * Otherwise it throws a validation exception
     * @param conditionBits the condition bits to check
     */
    public static void registersNotReadOnly(ConditionBit[] conditionBits) {
        for(ConditionBit cb : conditionBits){
            if(cb.getRegister().getReadOnly()){
                throw new ValidationException("The register \""+cb.getRegister().getName()+
                        "\" contains the halt bit \""+cb.getName()+"\".  This is not allowed. "
                        + "Any register containing a condition bit must be able to be written"
                        + " to.");
            }
        }
    }
    
    /**
     * check for newLine character
     * 
     * @throws ValidationException if the input is not the newLine character
     */
    public static void isNewLineCharacter(char c) throws ValidationException {
        if (c != '\n') {
            throw new ValidationException("The inputted character is not equal to the " +
                    "\n newLine character");
        }
    }

    /**
     * Checks that the character is a valid ASCII
     * character, in the range of 0 to 255.
     * 
     * @param longValOfChar - long value of char to check
     * @throws ValidationException - When not a valid ASCII char. 
     */
    public static void isAsciiChar(long longValOfChar) throws ValidationException {
            isTypeChar(longValOfChar, true);
    }

    /**
     * Checks that the character is a valid UNICODE
     * character, in the range of 0 to 65535.
     * 
     * @param longValOfChar - long value of char to check
     * @throws ValidationException - When not a valid UNICODE char. 
     */
    public static void isUnicodeChar(long longValOfChar) throws ValidationException {
            isTypeChar(longValOfChar, false);
    }
    
    /**
	 * Checks that the character is a valid ASCII/UNICODE
	 * character, in the range of 0 to 255 or 65535, respectively.
	 * 
	 * @param longValOfChar - long value of char to check
	 * @param asciiNotUnicode - To tell whether we are testing
	 * for ASCII or UNICODE. 
	 * @throws ValidationException - When not a valid ASCII/UNICODE char. 
	 */
	private static void isTypeChar(long longValOfChar, boolean asciiNotUnicode) {
		long maxVal = asciiNotUnicode ? 255 : 65535;
		String typeString = asciiNotUnicode ? "ASCII" : "UNICODE";

		if(!(0 <= longValOfChar && longValOfChar <= maxVal)) {
			throw new ValidationException("Character entered:" + ((char) longValOfChar) +
					". This is not a valid character for inputs " +
					"of type "+typeString+".");
		}
	}
        
    /**
     * Checks whether the given long value can be represented in twos
     * complement using the given number of bits.
     * @param value the long value to be checked
     * @param numBits the number of bits into which the value must fit
     * @throws ValidationException if the value does not fit in the given
     *         number of bits
     */
    public static void fitsInBits(long value, ArchValue numBits) {
        if (!numBits.fitsWithin(value)) {
            throw new ValidationException("The value " + value +
                    " doesn't fit in " + numBits + " bits\n");
        }
    }
    
    /**
     * Checks whether the given long value can be represented in twos
     * complement using the given number of bits.
     * @param value the long value to be checked
     * @param numBits the number of bits into which the value must fit
     * @throws ValidationException if the value does not fit in the given
     *         number of bits
     * 
     * @deprecated Use {@link #fitsInBits(long, ArchValue)}
     */
    public static void fitsInBits(long value, int numBits) {
        fitsInBits(value, ArchValue.bits(numBits));
    }

    public static void fieldIsValid(Field field) {
        if (field.getNumBits() == 0 && field.getType().equals(Type.ignored)) {
            throw new ValidationException("A field of length 0 cannot be ignored." +
                    " Field " + field.getName() + " is such a field.");
    	}
        
        if (!field.isSigned() && field.getDefaultValue() < 0) {
            throw new ValidationException("Field " + field.getName() + " is unsigned" +
                    " but has a negative default value.");
        }
        
        if(field.getValues().size() > 0 &&
                (! field.getRelativity().equals(Field.Relativity.absolute) ||
                 field.getNumBits() == 0)) {
            throw new ValidationException("Field " + field.getName() + " must be" +
                    " absolute and have a positive number of bits in order to use " +
                    " a set of fixed values.");
        }
        
        if(field.getNumBits() > 0)
            fitsInBits(field.getDefaultValue(), field.getNumBits());
        
        fieldValuesAreValid(field, field.getValues());
    }

    public static void fieldValuesAreValid(Field field, ObservableMap<String, FieldValue> allFieldValues) {
    	final int numBits = field.getNumBits();
    	
        for(FieldValue fieldValue : allFieldValues.values()) {
            if (fieldValue.getValue() < 0 && !field.isSigned())
                throw new ValidationException("Field " + field.getName() + " is unsigned" +
                    " and so " + fieldValue.getName() + " cannot have a negative field value.");
            
            if (numBits > 0)
                fitsInBits(fieldValue.getValue(), numBits);
        }
    }
} //end of class Validate
