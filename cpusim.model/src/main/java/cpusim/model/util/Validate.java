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

import com.google.common.base.Strings;
import cpusim.model.Field;
import cpusim.model.Field.Type;
import cpusim.model.Machine;
import cpusim.model.MachineInstruction;
import cpusim.model.module.Module;
import cpusim.model.assembler.EQU;
import cpusim.model.assembler.PunctChar;
import cpusim.model.microinstruction.*;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import cpusim.model.util.conversion.ConvertLongs;
import cpusim.model.util.conversion.ConvertStrings;
import cpusim.model.util.units.ArchValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

///////////////////////////////////////////////////////////////////////////////
// the libraries we need to import

/**
 * This class has a bunch of static methods for testing the validity of
 * various values in machine instructions, micros, modules, and possibly
 * elsewhere.
 * If any of the values is not valid, the method throws a ValidationException.
 */
public abstract class Validate
{
    private Validate() {
        // no-op, stop construction
    }
    
    
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
//            opcodeFieldIsFirst(instr);
        }

        allOpcodesAreUnique(instrs);
        namedObjectsAreUniqueAndNonempty(instrs);
    }
    
    /**
     * Validates the namedObjects whose names are not used in assembly code.
     * Will throw a classCastException if the array of objects passed in are not
     * instances of NamedObject
     * @param objects the nameable objects to be checked
     * @deprecated Use {@link NamedObject#validateUniqueAndNonempty(List)}
     */
    public static void namedObjectsAreUniqueAndNonempty(List<? extends NamedObject> objects) {
    	NamedObject.validateUniqueAndNonempty(objects);
    }
    
    
    /**
     * Validates the names of a list of equs
     * @param equs the nameable objects to be checked
     * @param machine the machine in its current state
     */    
    public static void EQUNames(List<EQU> equs, Machine machine){
        NamedObject.validateUniqueAndNonempty(equs);
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

    // FIXME https://github.com/Colby-CPU-Sim/CPUSimFX2015/issues/109
//    /**
//     * checks that the first field in the assemblyFields list is the same as the first
//     * field in the instructionFields list (the opcode field)
//     * @param instr instruction whose first assembly field needs to be checked
//     */
//    public static void opcodeFieldIsFirst(MachineInstruction instr){
//        if (!instr.getAssemblyColors().get(0).equals(instr.getInstructionColors().get(0))){
//            throw new ValidationException("Your opcode (the first field in the instruction fields)"
//                    + " must be the first field in your assembly fields.  This is not the"
//                    + " case for instruction "+instr.getName()+".");
//        }
//    }

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
        if (Strings.isNullOrEmpty(name)) {
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
        
        if (sum < 0 || sum > 64) {
            throw new ValidationException(
                    "The values of the field lengths" +
                    " of instruction \"" + instr.getName() +
                    "\"\nmust add up to a positive number " +
                    "less than or equal to 64, currently sum is " + sum + ".");
        }
    }

    /**
     * check if the ranges are in bound.
     * @param transferRtoAs an array of {@link TransferRtoA}s to check.
     * TransferRtoR objects with all ranges all in Bounds properly.
     *
     * @deprecated Use {@link Validatable#all(Iterable)}
     */
    public static void rangesAreInBoundTransferRToA(List<TransferRtoA> transferRtoAs)
    {
        Validatable.all(transferRtoAs);
    }

    /**
     * check if the ranges are in bound.
     * @param transferRtoRs an array of TransferRtoRs to check.
     *
     * @deprecated Use {@link Validatable#all(Iterable)}
     */
    public static void rangesAreInBound(List<TransferRtoR> transferRtoRs)
    {
        Validatable.all(transferRtoRs);
    }
    
    /**
     * checks if a given string is a legal binary, decimal, and/or hexadecimal number
     * @param string string in question
     */
    public static void stringIsLegalBinHexOrDecNumber(String string){
        try{
            ConvertStrings.toLong(string);
        } catch(NumberFormatException ex) {
            throw new ValidationException(ex);
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
     * check if all registers have appropriate width for micro instructions.
     * @param h a HashMap of key = original Registers, value = new width
     * any micros to be invalid.
     */
    public static void registerWidthsAreOkayForMicros(Machine machine, Map<? extends Module<?>, Integer> h)
    {
        //make a HashMap of old registers as keys and
        //old widths as Integer values
        Map<Module<?>, Integer> newWidths = new HashMap<>();
        final List<Register> registers = machine.getModules(Register.class);
        for (Register r: registers) {
            newWidths.put(r, r.getWidth());
        }
        
        for (RegisterArray array: machine.getModules(RegisterArray.class)) {
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
        ObservableList<Shift> shifts = machine.getMicros(Shift.class);
        for (Shift shift : shifts) {
            Register source = shift.getSource();
            int sourceWidth = newWidths.get(source);
            int destWidth = newWidths.get(shift.getDest());
            if (sourceWidth != destWidth) {
                throw new ValidationException("The new width " + sourceWidth +
                        " of register " + shift.getSource() + "\nand new width " +
                        destWidth + " of register " + shift.getDest() +
                        "\ncause microinstruction " + shift + " to be invalid.");
            }
        }
        
        ObservableList<Logical> logicals = machine.getMicros(Logical.class);
        for (Logical logical : logicals) {
            int source1Width = newWidths.get(logical.getLhs());
            int source2Width = newWidths.get(logical.getRhs());
            int destWidth = newWidths.get(logical.getDestination());
            if (source1Width != destWidth || source2Width != destWidth) {
                throw new ValidationException("The new width " + source1Width +
                        " of register " + logical.getLhs() + ",\nnew width " +
                        source2Width + " of register " + logical.getRhs() +
                        "\nand new width " + destWidth +
                        " of register " + logical.getDestination() +
                        "\ncause microinstruction " + logical + " to be invalid.");
            }
        }
        
        ObservableList<SetBits> sets = machine.getMicros(SetBits.class);
        for (SetBits set : sets) {
            int newWidth = newWidths.get(set.getRegister());
            if (newWidth < set.getStart() + set.getNumBits()) {
                throw new ValidationException("The new width " + newWidth +
                        " of register " + set.getRegister() +
                        "\ncauses microinstruction " + set + " to be invalid.");
            }
        }
        
        ObservableList<Test> tests = machine.getMicros(Test.class);
        for (Test test : tests) {
            int newWidth = newWidths.get(test.getRegister());
            if (newWidth < test.getStart() + test.getNumBits()) {
                throw new ValidationException("The new width " + newWidth +
                        " of register " + test.getRegister() +
                        "\ncauses microinstruction " + test + " to be invalid.");
            }
        }
        
        ObservableList<TransferRtoR> transferRtoRs = machine.getMicros(TransferRtoR.class);
        for (TransferRtoR t : transferRtoRs) {
            int sourceWidth = newWidths.get(t.getSource());
            int destWidth = newWidths.get(t.getDest());
            if (sourceWidth < t.getSrcStartBit() + t.getNumBits() ||
                    destWidth < t.getDestStartBit() + t.getNumBits()) {
                throw new ValidationException("The new width " + sourceWidth +
                        " of register " + t.getSource() + "\nor new width " +
                        destWidth + " of register " + t.getDest() +
                        "\ncauses microinstruction " + t + " to be invalid.");
            }
        }
        ObservableList<TransferRtoA> transferRtoAs = machine.getMicros(TransferRtoA.class);
        for (TransferRtoA t : transferRtoAs) {
            int sourceWidth = newWidths.get(t.getSource());
            if (sourceWidth < t.getSrcStartBit() + t.getNumBits()) {
                throw new ValidationException("The new width " + sourceWidth +
                        " of register " + t.getSource() +
                        "\ncauses microinstruction " + t + " to be invalid.");
            }
            int indexWidth = newWidths.get(t.getIndex());
            if (indexWidth < t.getIndexStart() + t.getIndexNumBits()) {
                throw new ValidationException("The new width " + indexWidth +
                        " of register " + t.getIndex() +
                        "\ncauses microinstruction " + t + " to be invalid.");
            }
        }
        ObservableList<TransferAtoR> transferAtoRs = machine.getMicros(TransferAtoR.class);
        for (TransferAtoR t : transferAtoRs) {
            int destWidth = newWidths.get(t.getDest());
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
        startingAddressInRAMRange(ConvertStrings.toLong(startAddr), ramLength);
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
    public static void containsDecodeMicro(List<Microinstruction<?>> micros){
        boolean containsDecode = false;
        for (Microinstruction<?> micro : micros){
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
     *
     * @deprecated Use {@link Register#validateIsNotReadOnly(Register, String)}
     */
    public static void registerIsNotReadOnly(Register register, String microName) {
        Register.validateIsNotReadOnly(register, microName);
    }

    /**
     * When saving the modified registers to machine, check if those read-only
     * regsters are used in any of the transferAtoA or transferRtoR microinstructions.
     * @param registers an array of registers
     * @param transferAtoRs an list of transferAtoR microinstructions
     * @param transferRtoRs an list of transferRtoR microinstructions
     * @param setCondBits and list of setCondBit microinstructions
     */
    public static void readOnlyRegistersAreImmutable(List<? extends Register> registers,
                                                     List<? extends TransferAtoR> transferAtoRs,
                                                     List<? extends TransferRtoR> transferRtoRs,
                                                     List<? extends SetCondBit> setCondBits){
        final Set<Register> readOnlyRegisters = registers.stream()
                .filter(r -> r.getAccess().equals(Register.Access.readOnly()))
                .collect(Collectors.toSet());

        if (readOnlyRegisters.size() > 0) {
            for (TransferAtoR t: transferAtoRs){
                if (readOnlyRegisters.contains(t.getDest())) {
                    throw new ValidationException("The register " + t.getDest().getName() + " is used as the " +
                            "destination register in the microinstruction transferAtoR " + t.getName() + ". " +
                            "You should change the microinstruction before setting the register to read-only.");
                }
            }

            for (TransferRtoR o: transferRtoRs) {
                if (readOnlyRegisters.contains(o.getDest())) {
                    throw new ValidationException("The register " + o.getDest().getName() + " is used as the " +
                            "destination register in the microinstruction transferRtoR " + o.getName() + ". " +
                            "You should change the microinstruction before setting the register to read-only.");
                }
            }

            for (SetCondBit o: setCondBits){
                if (readOnlyRegisters.contains(o.getBit().getRegister()))
                    throw new ValidationException("The register " + o.getBit().getRegister() + " is used as the " +
                            "condition flag reigster in the microinstruction setCondBit " + o.getName() + ". " +
                            "You should change the microinstruction before setting the register to read-only.");
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

    
} //end of class Validate
