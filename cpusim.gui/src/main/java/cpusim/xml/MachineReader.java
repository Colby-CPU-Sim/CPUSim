///////////////////////////////////////////////////////////////////////////////
// File:    	MachineReader.java
// Author:		Dale Skrien
// Project: 	CPU Sim
// Date:    	June, 2001
//
// Last Modified: 6/4/13
//
// Description:
//   This file contains the class for reading Machines created using
//   CPU Sim from a file in XML format, using the CPUSimMachine.dtd
//
// Adapted from sample code by Mark Johnson, JavaWorld, April 2000.
// email: mark.johnson@javaworld.com

// Things to do:
//  1.

/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard made the following modifications to 
 * this class on 10/27/13:
 * 
 * 1.) Changed the call to the Validate.allOpcodesAreUnique method so that we are passing
 * in a list of the mahcine instructions instead of an array thereof
 * 2.) Changed the call to the Validate.punctIsValid method so that we are passing
 * in a list of the Punctuation Charaters instead of an array thereof
 *
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard made the following modifications to
 * this class on 11/11/13:
 *
 * 1.) Changed startRegister method so that the parser reads in initial value and
 * read-only
 * properties of registers and registers in a register array.
 */

///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

package cpusim.xml;


import cpusim.model.*;
import cpusim.model.Field.Type;
import cpusim.model.iochannel.FileChannel;
import cpusim.model.iochannel.IOChannel;
import cpusim.assembler.EQU;
import cpusim.assembler.PunctChar;
import cpusim.model.microinstruction.*;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.RAM;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import cpusim.model.util.Convert;
import cpusim.model.util.Validate;
import cpusim.model.util.ValidationException;
import cpusim.model.util.conversion.ConvertStrings;
import cpusim.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import java.io.File;
import java.math.BigInteger;
import java.util.*;


///////////////////////////////////////////////////////////////////////////////
// the MachineReader class

public class MachineReader implements CPUSimConstants
{
    private Machine machine;  //the new machine to be read from the file
    private HashMap components; //key = id, value = module
    private MachineInstruction currentInstruction;
    //the current machine instruction being constructed
    private String currentFormat; //holds the field lengths until they are
    private String opcodeString; //holds the opcode for error messages
    private String opcodeLine; //the line number of the opcode for error messages
    private Locator locator;
    private Vector<RegisterRAMPair> registerRAMPairs; //holds the current pairs
    private HashMap cellSizeInfo; //key = ram, value = cellSize for the ram window
    private HashMap channels; //key = id, value = FileChannel
    private RegisterArray currentRegisterArray;
    //the current RegisterArray being constructed
    private int currentRegisterArrayIndex;
    //the index of the next register to be added to the current RegisterArray
    private HashMap<String, Field> fields; //key = name, value = Field
    private Set<Character> allPunctCharacters;
    private ArrayList<PunctChar> chars;

    //--------------------------
    // default constructor
    public MachineReader()
    {
        super();
        reset();
        allPunctCharacters = new HashSet<Character>();
        allPunctCharacters.add('!');
        allPunctCharacters.add('@');
        allPunctCharacters.add('$');
        allPunctCharacters.add('%');
        allPunctCharacters.add('^');
        allPunctCharacters.add('&');
        allPunctCharacters.add('*');
        allPunctCharacters.add('(');
        allPunctCharacters.add(')');
        allPunctCharacters.add('_');
        allPunctCharacters.add('=');
        allPunctCharacters.add('{');
        allPunctCharacters.add('}');
        allPunctCharacters.add('[');
        allPunctCharacters.add(']');
        allPunctCharacters.add('|');
        allPunctCharacters.add('\\');
        allPunctCharacters.add(':');
        allPunctCharacters.add(';');
        allPunctCharacters.add(',');
        allPunctCharacters.add('.');
        allPunctCharacters.add('?');
        allPunctCharacters.add('/');
        allPunctCharacters.add('~');
        allPunctCharacters.add('-');
        allPunctCharacters.add('+');
        allPunctCharacters.add('#');
        allPunctCharacters.add('`');
    }

    public void reset()
    {
        machine = null;
        components = new HashMap();
        currentInstruction = null;
        currentFormat = "";
        opcodeString = null;
        opcodeLine = null;
        locator = null;
        registerRAMPairs = new Vector<>();
        cellSizeInfo = new HashMap();
        channels = new HashMap();
        currentRegisterArray = null;
        currentRegisterArrayIndex = 0;
        fields = new HashMap<>();
        // initialize the punctuation characters to the default chars.
        // These will be replaced by new ones if the .cpu file specifies
        // punctuation characters.
        chars = new ArrayList<>(Arrays.asList(Machine.getDefaultPunctChars()));

    }

    /**
     * reads and stores all the info from the given file.
     * Throws an exception if there is an error attempting to parse the file.
     *
     * @param fileToOpen the file containing the machine description
     * @throws Exception if it has any problems reading from the file
     */
    public void parseDataFromFile(File fileToOpen) throws Exception
    {
        Lax lax = new Lax();
        lax.reset();
        reset();
        lax.addHandler(this);
        lax.parseDocument(true, lax, fileToOpen);
    }

    //--------------------------
    public HashMap getCellSizeInfo()
    {
        return cellSizeInfo;
    }

    //--------------------------
    public Machine getMachine()
    {
        return machine;
    }

    //--------------------------
    public Vector<RegisterRAMPair> getRegisterRAMPairs()
    {
        return registerRAMPairs;
    }

    //--------------------------
    public void setDocumentLocator(Locator locator)
    {
        this.locator = locator;
    }

    //--------------------------
    // returns an error message giving the current line number
    private String getCurrentLine()
    {
        if (locator == null) {
            return "";
        }
        else {
            return "The error is on or near line " + locator.getLineNumber() + ".\n";
        }
    }

    //--------------------------
    public void startMachine(Attributes alAttrs)
    {
        String name = alAttrs.getValue("name");
        if (name.endsWith(".xml") || name.endsWith(".cpu")) {
            name = name.substring(0, name.length() - 4);
        }
        machine = new Machine(name, true);
        //Don't the next lines duplicate what's in the reset() method?
        //        components.clear();
        //        registerRAMPairs.clear();
        //        windowInfo.clear();
        //        cellSizeInfo.clear();
        //        contentBaseInfo.clear();
        //        addressBaseInfo.clear();
        //        channels.clear();
    }

    //--------------------------
    public void endMachine()
    {
        try {
            MachineInstruction[] instrs = machine.getInstructions().toArray(new
                    MachineInstruction[]{});
            Validate.allOpcodesAreUnique(machine.getInstructions());
            Validate.allNamesAreUnique(instrs);
        } catch (ValidationException e) {
            throw new MachineReaderException(getCurrentLine() + e.getMessage());
        }

        //check the punctuation characters
        PunctChar[] punctChars = chars.toArray(new PunctChar[chars.size()]);
        try {
            Validate.punctChars(chars);
        } catch (ValidationException exc) {
            throw new MachineReaderException(getCurrentLine() + exc.getMessage());
        }
        machine.setPunctChars(punctChars);

        //set the code store if not already set
        if (machine.getModule("rams").size() > 0 && machine.getCodeStore() == null) {
            machine.setCodeStore((RAM) machine.getModule("rams").get(0));
        }
    }

    public void startPunctChar(Attributes attrs)
    {
        String cAsString = attrs.getValue("char");
        if (cAsString.length() != 1) {
            throw new MachineReaderException(getCurrentLine() + "There must be only one" +
                    " punctuation character per line.");
        }
        char c = cAsString.charAt(0);
        if (!allPunctCharacters.contains(c)) {
            throw new MachineReaderException(getCurrentLine() +
                    "Only the punctuation characters !@$%^&*()_={}[]|\\:;,.?/~+-#`" + "" +
                    " are allowed here.");
        }
        String useString = attrs.getValue("use");
        PunctChar.Use use = Enum.valueOf(PunctChar.Use.class, useString);
        for (PunctChar aChar : chars) {
            if (aChar.getChar() == c) {
                aChar.setUse(use);
            }
        }
    }

    //--------------------------
    public void startField(Attributes attrs)
    {
        String name = attrs.getValue("name");
        if (fields.get(name) != null) {
            throw new MachineReaderException(getCurrentLine() +
                    "There are two fields with the same name: \"" + name +
                    "\".\nAll field names must be unique.");
        }

        String typeString = attrs.getValue("type");
        Type type;
        if (typeString.equals("required")) {
            type = Type.required;
        }
        else if (typeString.equals("optional")) {
            type = Type.optional;
        }
        else {
            type = Type.ignored;
        }

        String relativityString = attrs.getValue("relativity");
        Field.Relativity rel = Enum.valueOf(Field.Relativity.class, relativityString);
        String signedString = attrs.getValue("signed");
        boolean signed = "true".equals(signedString);

        String defaultValueString = attrs.getValue("defaultValue");
        String numBitsString = attrs.getValue("numBits");
        int numBits = 0;
        try {
            numBits = Integer.parseInt(numBitsString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() +
                    "The numBits value of the Field \"" + name +
                    "\" must be an integer, not \"" + numBitsString +
                    "\".");
        }
        if (numBits < 0) {
            throw new MachineReaderException(getCurrentLine() +
                    "The numBits value of the Field \"" + name +
                    "\" must non-negative, not \"" + numBits +
                    "\".");
        }

        int defaultValue = 0;
        try {
            defaultValue = Integer.parseInt(defaultValueString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() +
                    "The defaultValue value of the Field \"" + name +
                    "\" must be an integer, not \"" + defaultValueString + "\".");
        }
        if (numBits > 0 && !signed &&
                (defaultValue < 0 || defaultValue > Math.pow(2, numBits) - 1)) {
            throw new MachineReaderException(getCurrentLine() +
                    "The default value of the unsigned Field \"" + name +
                    "\" must be in the range 0 to 2^" + numBits + "-1.");
        }
        else if (numBits > 0 && signed &&
                (defaultValue < -Math.pow(2, numBits - 1) || defaultValue > Math.pow(2,
                        numBits - 1) - 1)) {
            throw new MachineReaderException(getCurrentLine() +
                    "The default value of the signed Field \"" + name +
                    "\" must be in the range -(2^" + (numBits - 1) + ") and" +
                    "(2^" + (numBits - 1) + "-1).");
        }

        Field f = new Field(name, type, numBits, rel, FXCollections.observableArrayList
                (new ArrayList<>()), defaultValue, signed);
        machine.getFields().add(f);
        fields.put(name, f);
    }

    //--------------------------
    public void startFieldValue(Attributes attrs)
    {
        String name = attrs.getValue("name");
        String valueString = attrs.getValue("value");
        int value = 0;
        try {
            value = Integer.parseInt(valueString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() +
                    "The value of FieldValue \"" +
                    name + "\" must be an integer, not \"" + valueString +
                    "\".");
        }
        Field lastField = machine.getFields().get(machine.getFields().size() - 1);
        lastField.getValues().add(new FieldValue(name, value));
    }

    //--------------------------
    public void startRegister(Attributes attrs)
    {
        String name = attrs.getValue("name");
        String widthString = attrs.getValue("width");
        String initialValueString = attrs.getValue("initialValue") == null ? "0" :
                attrs.getValue("initialValue");
        String readOnlyString = attrs.getValue("readOnly") == null ? "false" : attrs
                .getValue("readOnly");
        boolean readOnly = readOnlyString.equals("true");

        int width;
        int initialValue;

        try {
            width = Integer.parseInt(widthString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() +
                    "The width of register \"" + name + "\" must be an integer, not \""
                    + widthString + "\".");
        }
        if (width <= 0 || width > 64) {
            throw new MachineReaderException(getCurrentLine() + "The width of register " +
                    "\"" + name + "\" must be between 1 and 64, not " + width);
        }

        try {
            initialValue = Integer.parseInt(initialValueString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() +
                    "The initial value of register\"" + name + "\" must be an integer, " +
                    "not \"" + initialValueString + "\"" +
                    ".");
        }

        String id = attrs.getValue("id");
        Register r;

        if (currentRegisterArray != null) {
            if (currentRegisterArrayIndex >= currentRegisterArray.getLength()) {
                throw new MachineReaderException(getCurrentLine() + "The length " +
                        "of register array " + currentRegisterArray.getName() +
                        " does not match the list of registers for it");
            }
            r = currentRegisterArray.registers().get(currentRegisterArrayIndex);
            r.setName(name);
            r.setInitialValue(initialValue);
            r.setValue(initialValue);
            r.setReadOnly(readOnly);
            currentRegisterArrayIndex++;
        }
        else {
            r = new Register(name, width, initialValue, readOnly);
            ((ObservableList<Register>) machine.getModule("registers")).add(r);
        }
        components.put(id, r);
    }

    //--------------------------
    public void startRegisterArray(Attributes attrs)
    {
        String name = attrs.getValue("name");
        String widthString = attrs.getValue("width");
        int width = 0;
        try {
            width = Integer.parseInt(widthString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The width of register " +
                    "\"" + name + "\" must be an integer, not \"" + widthString +
                    "\".");
        }
        if (width <= 0 || width > 64) {
            throw new MachineReaderException(getCurrentLine() + "The width of the " +
                    "registers " +
                    "in register array \"" + name +
                    "\" must be between 1 and 64, not " + width + ".");
        }
        String lengthString = attrs.getValue("length");
        int length = 0;
        try {
            length = Integer.parseInt(lengthString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The length of register" +
                    " " + "array \"" + name + "\" must be an integer, not \"" +
                    lengthString + "\".");
        }
        if (length <= 0) {
            throw new MachineReaderException(getCurrentLine() + "The length of register" +
                    " " + "array \"" + name + "\" must be positive, not " + width +
                    ".");
        }

        RegisterArray r = new RegisterArray(name, length, width);
        String id = attrs.getValue("id");
        components.put(id, r);
        ((ObservableList<RegisterArray>) machine.getModule("registerArrays")).add(r);

        //now prepare for reading in the registers
        currentRegisterArray = r;
        currentRegisterArrayIndex = 0;
    }

    public void endRegisterArray()
    {
        currentRegisterArray = null;
    }

    //--------------------------
    public void startConditionBit(Attributes attrs)
    {
        String name = attrs.getValue("name");

        String haltString = attrs.getValue("halt");
        boolean halt = haltString.equals("true");

        String registerID = attrs.getValue("register");
        Object object = components.get(registerID);
        if (object == null || !(object instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The register attribute" +
                    " " + "of condition bit \"" + name + "\" is not valid.");
        }
        Register register = (Register) object;

        String bitString = attrs.getValue("bit");
        int bit = 0;
        try {
            bit = Integer.parseInt(bitString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The bit index of " +
                    "condition bit \"" + name + "\" must be an integer, not \"" +
                    bitString + "\".");
        }
        if (bit < 0 || bit >= register.getWidth()) {
            throw new MachineReaderException(getCurrentLine() + "The bit index of " +
                    "condition bit \"" + name + "\" is " + bit + ", which is out of " +
                    "range.");
        }

        ConditionBit c = new ConditionBit(name, machine, register, bit, halt);
        String id = attrs.getValue("id");
        components.put(id, c);
        ((ObservableList<ConditionBit>) machine.getModule("conditionBits")).add(c);
    }

    //--------------------------
    public void startRAM(Attributes attrs)
    {
        String name = attrs.getValue("name");
        String lengthString = attrs.getValue("length");
        int length = 0;
        try {
            length = Integer.parseInt(lengthString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The length of RAM \""
                    + name + "\" must be an integer, not \"" + lengthString +
                    "\".");
        }
        if (length <= 0) {
            throw new MachineReaderException(getCurrentLine() + "The length of RAM \""
                    + name + "\" must be positive, not " + length + ".");
        }
        String cellSizeString = attrs.getValue("cellSize");
        int cellSize;
        if (cellSizeString == null) //optional field for backward compatibility
        {
            cellSize = 8; //default of 1 byte
        }
        else {
            try {
                cellSize = Integer.parseInt(cellSizeString);
            } catch (NumberFormatException e) {
                throw new MachineReaderException(getCurrentLine() + "The number of bits" +
                        " of each cell of RAM \"" + name + "\" must be an integer, not " +
                        "\"" +
                        lengthString + "\".");
            }
        }
        if (cellSize <= 0 || cellSize > 64) {
            throw new MachineReaderException(getCurrentLine() + "The number of bits of " +
                    "each cell of RAM \"" + name + "\" must be a positive integer at " +
                    "most" +
                    " 64, not " + length + ".");
        }
        RAM ram = new RAM(name, length, cellSize);
        String id = attrs.getValue("id");
        components.put(id, ram);
        ((ObservableList<RAM>) machine.getModule("rams")).add(ram);
    }

    //--------------------------
    public void startIncrement(Attributes attrs)
    {
        String name = attrs.getValue("name");

        String deltaString = attrs.getValue("delta");
        int delta = 0;
        try {
            delta = Integer.parseInt(deltaString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The delta value of the" +
                    " " +
                    "Increment microinstruction \"" + name +
                    "\" must be an integer, not \"" + deltaString + "\".");
        }

        String registerID = attrs.getValue("register");
        Object object = components.get(registerID);
        if (object == null || !(object instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The register attribute" +
                    " " + "of Increment microinstruction \"" + name +
                    "\" is not valid.");
        }
        Register register = (Register) object;

        ConditionBit overflowBit;
        String overflowBitID = attrs.getValue("overflowBit");
        if (overflowBitID == null) {
            overflowBit = CPUSimConstants.NO_CONDITIONBIT;
        }
        else {
            object = components.get(overflowBitID);
            if (object == null || !(object instanceof ConditionBit)) {
                throw new MachineReaderException(getCurrentLine() + "The overflowBit " +
                        "attribute " + "of Increment microinstruction \"" + name +
                        "\" is not valid.");
            }
            overflowBit = (ConditionBit) object;
        }

        ConditionBit carryBit;
        String carryBitID = attrs.getValue("carryBit");
        if (carryBitID == null) {
            carryBit = CPUSimConstants.NO_CONDITIONBIT;
        }
        else {
            object = components.get(carryBitID);
            if (object == null || !(object instanceof ConditionBit)) {
                throw new MachineReaderException(getCurrentLine() + "The carryBit " +
                        "attribute " + "of Increment microinstruction \"" + name +
                        "\" is not valid.");
            }
            carryBit = (ConditionBit) object;
        }

        Increment c = new Increment(name, machine, register, overflowBit, carryBit,
                Long.valueOf(delta));
        String id = attrs.getValue("id");
        components.put(id, c);
        machine.getMicros("increment").add(c);
    }

    //--------------------------
    public void startArithmetic(Attributes attrs)
    {
        String name = attrs.getValue("name");
        String type = attrs.getValue("type");

        String registerID = attrs.getValue("source1");
        Object object = components.get(registerID);
        if (object == null || !(object instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The source1 attribute " +
                    "" + "of Arithmetic microinstruction \"" + name +
                    "\" is not valid.");
        }
        Register source1 = (Register) object;

        registerID = attrs.getValue("source2");
        object = components.get(registerID);
        if (object == null || !(object instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The source2 attribute " +
                    "" + "of Arithmetic microinstruction \"" + name +
                    "\" is not valid.");
        }
        Register source2 = (Register) object;

        registerID = attrs.getValue("destination");
        object = components.get(registerID);
        if (object == null || !(object instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The destinations " +
                    "attribute " + "of Arithmetic microinstruction \"" + name +
                    "\" is not valid.");
        }
        Register destination = (Register) object;

        ConditionBit overflowBit;
        String overflowBitID = attrs.getValue("overflowBit");
        if (overflowBitID == null) {
            overflowBit = CPUSimConstants.NO_CONDITIONBIT;
        }
        else {
            object = components.get(overflowBitID);
            if (object == null || !(object instanceof ConditionBit)) {
                throw new MachineReaderException(getCurrentLine() + "The overflowBit " +
                        "attribute " + "of Arithmetic microinstruction \"" + name +
                        "\" is not valid.");
            }
            overflowBit = (ConditionBit) object;
        }

        ConditionBit carryBit;
        String carryBitID = attrs.getValue("carryBit");
        if (carryBitID == null) {
            carryBit = CPUSimConstants.NO_CONDITIONBIT;
        }
        else {
            object = components.get(carryBitID);
            if (object == null || !(object instanceof ConditionBit)) {
                throw new MachineReaderException(getCurrentLine() + "The carryBit " +
                        "attribute " + "of Arithmetic microinstruction \"" + name +
                        "\" is not valid.");
            }
            carryBit = (ConditionBit) object;
        }

        Arithmetic c = new Arithmetic(name, machine, type, source1, source2,
                destination, overflowBit, carryBit);
        String id = attrs.getValue("id");
        components.put(id, c);
        machine.getMicros("arithmetic").add(c);
    }

    //--------------------------
    public void startTransferRtoR(Attributes attrs)
    {
        String name = attrs.getValue("name");

        String registerID = attrs.getValue("source");
        Object object = components.get(registerID);
        if (object == null || !(object instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The source attribute "
                    + "of TransferRtoR microinstruction \"" + name +
                    "\" is not valid.");
        }
        Register source = (Register) object;

        registerID = attrs.getValue("dest");
        object = components.get(registerID);
        if (object == null || !(object instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The dest attribute " +
                    "of TransferRtoR microinstruction \"" + name +
                    "\" is not valid.");
        }
        Register dest = (Register) object;

        String numBitsString = attrs.getValue("numBits");
        int numBits = 0;
        try {
            numBits = Integer.parseInt(numBitsString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The numBits value of " +
                    "the " +
                    "TransferRtoR microinstruction \"" + name +
                    "\" must be an integer, not \"" + numBitsString + "\".");
        }
        if (numBits < 0) {
            throw new MachineReaderException(getCurrentLine() + "The numBits value of " +
                    "the " +
                    "TransferRtoR microinstruction \"" + name +
                    "\" must be nonnegative, not " + numBits + ".");
        }

        String srcStartBitString = attrs.getValue("srcStartBit");
        int srcStartBit = 0;
        try {
            srcStartBit = Integer.parseInt(srcStartBitString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The srcStartBit value " +
                    "of the " +
                    "TransferRtoR microinstruction \"" + name +
                    "\" must be an integer, not \"" + srcStartBitString + "\".");
        }
        if (srcStartBit < 0) {
            throw new MachineReaderException(getCurrentLine() + "The srcStartBit value " +
                    "of the " +
                    "TransferRtoR microinstruction \"" + name +
                    "\" must be nonnegative, not " + srcStartBit + ".");
        }

        String destStartBitString = attrs.getValue("destStartBit");
        int destStartBit = 0;
        try {
            destStartBit = Integer.parseInt(destStartBitString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The destStartBit value" +
                    " of the " +
                    "TransferRtoR microinstruction \"" + name +
                    "\" must be an integer, not \"" + destStartBitString + "\".");
        }
        if (destStartBit < 0) {
            throw new MachineReaderException(getCurrentLine() + "The destStartBit value" +
                    " of the " +
                    "TransferRtoR microinstruction \"" + name +
                    "\" must be nonnegative, not " + destStartBit + ".");
        }
        if (destStartBit + numBits > dest.getWidth()) {
            throw new MachineReaderException(getCurrentLine() + "The destination bits " +
                    "of the " +
                    "TransferRtoR microinstruction \"" + name +
                    "\" are out of range for the destination register.");
        }
        if (srcStartBit + numBits > source.getWidth()) {
            throw new MachineReaderException(getCurrentLine() + "The source bits of the" +
                    " " +
                    "TransferRtoR microinstruction \"" + name +
                    "\" are out of range for the source register.");
        }

        TransferRtoR c = new TransferRtoR(name, machine, source, srcStartBit, dest,
                destStartBit, numBits);
        String id = attrs.getValue("id");
        components.put(id, c);
        machine.getMicros("transferRtoR").add(c);
    }

    //--------------------------
    public void startTransferRtoA(Attributes attrs)
    {
        String name = attrs.getValue("name");

        String registerID = attrs.getValue("source");
        Object object = components.get(registerID);
        if (object == null || !(object instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The source attribute "
                    + "of TransferRtoA microinstruction \"" + name +
                    "\" is not valid.");
        }
        Register source = (Register) object;

        registerID = attrs.getValue("dest");
        object = components.get(registerID);
        if (object == null || !(object instanceof RegisterArray)) {
            throw new MachineReaderException(getCurrentLine() + "The dest attribute " +
                    "of TransferRtoA microinstruction \"" + name +
                    "\" is not valid.");
        }
        RegisterArray dest = (RegisterArray) object;

        registerID = attrs.getValue("index");
        object = components.get(registerID);
        if (object == null || !(object instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The index attribute "
                    + "of TransferRtoA microinstruction \"" + name +
                    "\" is not valid.");
        }
        Register index = (Register) object;

        String numBitsString = attrs.getValue("numBits");
        int numBits = 0;
        try {
            numBits = Integer.parseInt(numBitsString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The numBits value of " +
                    "the " +
                    "TransferRtoA microinstruction \"" + name +
                    "\" must be an integer, not \"" + numBitsString + "\".");
        }
        if (numBits < 0) {
            throw new MachineReaderException(getCurrentLine() + "The numBits value of " +
                    "the " +
                    "TransferRtoA microinstruction \"" + name +
                    "\" must be nonnegative, not " + numBits + ".");
        }

        String srcStartBitString = attrs.getValue("srcStartBit");
        int srcStartBit = 0;
        try {
            srcStartBit = Integer.parseInt(srcStartBitString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The srcStartBit value " +
                    "of the " +
                    "TransferRtoA microinstruction \"" + name +
                    "\" must be an integer, not \"" + srcStartBitString + "\".");
        }
        if (srcStartBit < 0) {
            throw new MachineReaderException(getCurrentLine() + "The srcStartBit value " +
                    "of the " +
                    "TransferRtoA microinstruction \"" + name +
                    "\" must be nonnegative, not " + srcStartBit + ".");
        }

        String indexNumBitsString = attrs.getValue("indexNumBits");
        int indexNumBits = index.getWidth();
        if (indexNumBitsString != null) {
            try {
                indexNumBits = Integer.parseInt(indexNumBitsString);
            } catch (NumberFormatException e) {
                throw new MachineReaderException(getCurrentLine() +
                        "The indexNumBits value of the " +
                        "TransferRtoA microinstruction \"" + name +
                        "\" must be an integer, not \"" + indexNumBitsString + "\".");
            }
            if (indexNumBits <= 0) {
                throw new MachineReaderException(getCurrentLine() +
                        "The indexNumBits value of the " +
                        "TransferRtoA microinstruction \"" + name +
                        "\" must be positive, not " + indexNumBits + ".");
            }
        }

        String indexStartString = attrs.getValue("indexStart");
        int indexStart = 0;
        if (indexStartString != null) {
            try {
                indexStart = Integer.parseInt(indexStartString);
            } catch (NumberFormatException e) {
                throw new MachineReaderException(getCurrentLine() +
                        "The indexStart value of the " +
                        "TransferRtoA microinstruction \"" + name +
                        "\" must be an integer, not \"" + indexStartString + "\".");
            }
            if (indexStart < 0) {
                throw new MachineReaderException(getCurrentLine() +
                        "The indexStart value of the " +
                        "TransferRtoA microinstruction \"" + name +
                        "\" must be nonnegative, not " + indexStart + ".");
            }
        }

        String destStartBitString = attrs.getValue("destStartBit");
        int destStartBit = 0;
        try {
            destStartBit = Integer.parseInt(destStartBitString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The destStartBit value" +
                    " of the " +
                    "TransferRtoA microinstruction \"" + name +
                    "\" must be an integer, not \"" + destStartBitString + "\".");
        }
        if (destStartBit < 0) {
            throw new MachineReaderException(getCurrentLine() + "The destStartBit value" +
                    " of the " +
                    "TransferRtoA microinstruction \"" + name +
                    "\" must be nonnegative, not " + destStartBit + ".");
        }
        if (destStartBit + numBits > dest.getWidth()) {
            throw new MachineReaderException(getCurrentLine() + "The destination bits " +
                    "of the " +
                    "TransferRtoA microinstruction \"" + name +
                    "\" are out of range for the destination register.");
        }
        if (srcStartBit + numBits > source.getWidth()) {
            throw new MachineReaderException(getCurrentLine() + "The source bits of the" +
                    " " +
                    "TransferRtoA microinstruction \"" + name +
                    "\" are out of range for the source register.");
        }
        if (indexStart + indexNumBits > index.getWidth()) {
            throw new MachineReaderException(getCurrentLine() +
                    "The specified bits of the " +
                    "TransferRtoA microinstruction \"" + name +
                    "\" are out of range for the index register.");
        }

        TransferRtoA c = new TransferRtoA(name, machine, source, srcStartBit, dest,
                destStartBit, numBits, index, indexStart, indexNumBits);
        String id = attrs.getValue("id");
        components.put(id, c);
        machine.getMicros("transferRtoA").add(c);
    }

    //--------------------------
    public void startTransferAtoR(Attributes attrs)
    {
        String name = attrs.getValue("name");

        String registerID = attrs.getValue("source");
        Object object = components.get(registerID);
        if (object == null || !(object instanceof RegisterArray)) {
            throw new MachineReaderException(getCurrentLine() + "The source attribute "
                    + "of TransferAtoR microinstruction \"" + name +
                    "\" is not valid.");
        }
        RegisterArray source = (RegisterArray) object;

        registerID = attrs.getValue("dest");
        object = components.get(registerID);
        if (object == null || !(object instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The dest attribute " +
                    "of TransferAtoR microinstruction \"" + name +
                    "\" is not valid.");
        }
        Register dest = (Register) object;

        registerID = attrs.getValue("index");
        object = components.get(registerID);
        if (object == null || !(object instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The index attribute "
                    + "of TransferAtoR microinstruction \"" + name +
                    "\" is not valid.");
        }
        Register index = (Register) object;

        String numBitsString = attrs.getValue("numBits");
        int numBits = 0;
        try {
            numBits = Integer.parseInt(numBitsString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The numBits value of " +
                    "the " +
                    "TransferAtoR microinstruction \"" + name +
                    "\" must be an integer, not \"" + numBitsString + "\".");
        }
        if (numBits < 0) {
            throw new MachineReaderException(getCurrentLine() + "The numBits value of " +
                    "the " +
                    "TransferAtoR microinstruction \"" + name +
                    "\" must be nonnegative, not " + numBits + ".");
        }

        String srcStartBitString = attrs.getValue("srcStartBit");
        int srcStartBit = 0;
        try {
            srcStartBit = Integer.parseInt(srcStartBitString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The srcStartBit value " +
                    "of the " +
                    "TransferAtoR microinstruction \"" + name +
                    "\" must be an integer, not \"" + srcStartBitString + "\".");
        }
        if (srcStartBit < 0) {
            throw new MachineReaderException(getCurrentLine() + "The srcStartBit value " +
                    "of the " +
                    "TransferAtoR microinstruction \"" + name +
                    "\" must be nonnegative, not " + srcStartBit + ".");
        }

        String indexNumBitsString = attrs.getValue("indexNumBits");
        int indexNumBits = index.getWidth();
        if (indexNumBitsString != null) {
            try {
                indexNumBits = Integer.parseInt(indexNumBitsString);
            } catch (NumberFormatException e) {
                throw new MachineReaderException(getCurrentLine() +
                        "The indexNumBits value of the " +
                        "TransferAtoR microinstruction \"" + name +
                        "\" must be an integer, not \"" + indexNumBitsString + "\".");
            }
            if (indexNumBits <= 0) {
                throw new MachineReaderException(getCurrentLine() +
                        "The indexNumBits value of the " +
                        "TransferAtoR microinstruction \"" + name +
                        "\" must be positive, not " + indexNumBits + ".");
            }
        }

        String indexStartString = attrs.getValue("indexStart");
        int indexStart = 0;
        if (indexStartString != null) {
            try {
                indexStart = Integer.parseInt(indexStartString);
            } catch (NumberFormatException e) {
                throw new MachineReaderException(getCurrentLine() +
                        "The indexStart value of the " +
                        "TransferAtoR microinstruction \"" + name +
                        "\" must be an integer, not \"" + indexStartString + "\".");
            }
            if (indexStart < 0) {
                throw new MachineReaderException(getCurrentLine() +
                        "The indexStart value of the " +
                        "TransferAtoR microinstruction \"" + name +
                        "\" must be nonnegative, not " + indexStart + ".");
            }
        }

        String destStartBitString = attrs.getValue("destStartBit");
        int destStartBit = 0;
        try {
            destStartBit = Integer.parseInt(destStartBitString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The destStartBit value" +
                    " of the " +
                    "TransferAtoR microinstruction \"" + name +
                    "\" must be an integer, not \"" + destStartBitString + "\".");
        }
        if (destStartBit < 0) {
            throw new MachineReaderException(getCurrentLine() + "The destStartBit value" +
                    " of the " +
                    "TransferAtoR microinstruction \"" + name +
                    "\" must be nonnegative, not " + destStartBit + ".");
        }
        if (destStartBit + numBits > dest.getWidth()) {
            throw new MachineReaderException(getCurrentLine() + "The destination bits " +
                    "of the " +
                    "TransferAtoR microinstruction \"" + name +
                    "\" are out of range for the destination register.");
        }
        if (srcStartBit + numBits > source.getWidth()) {
            throw new MachineReaderException(getCurrentLine() + "The source bits of the" +
                    " " +
                    "TransferAtoR microinstruction \"" + name +
                    "\" are out of range for the source register.");
        }
        if (indexStart + indexNumBits > index.getWidth()) {
            throw new MachineReaderException(getCurrentLine() +
                    "The specified bits of the " +
                    "TransferAtoR microinstruction \"" + name +
                    "\" are out of range for the index register.");
        }

        TransferAtoR c = new TransferAtoR(name, machine, source, srcStartBit, dest,
                destStartBit, numBits, index, indexStart, indexNumBits);
        String id = attrs.getValue("id");
        components.put(id, c);
        machine.getMicros("transferAtoR").add(c);
    }

    //--------------------------
    public void startShift(Attributes attrs)
    {
        String name = attrs.getValue("name");

        String registerID = attrs.getValue("source");
        Object object = components.get(registerID);
        if (object == null || !(object instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The source attribute "
                    + "of Shift microinstruction \"" + name + "\" is not valid.");
        }
        Register source = (Register) object;

        registerID = attrs.getValue("destination");
        object = components.get(registerID);
        if (object == null || !(object instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The dest attribute " +
                    "of Shift microinstruction \"" + name + "\" is not valid.");
        }
        Register dest = (Register) object;
        if (source.getWidth() != dest.getWidth()) {
            throw new MachineReaderException(getCurrentLine() + "The source and " +
                    "destination " + "registers of the Shift microinstruction \"" + name +
                    "\" must have equal widths.");
        }

        String distanceString = attrs.getValue("distance");
        int distance = 0;
        try {
            distance = Integer.parseInt(distanceString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The distance value of " +
                    "the " +
                    "Shift microinstruction \"" + name +
                    "\" must be an integer, not \"" + distanceString + "\".");
        }
        if (distance < 0) {
            throw new MachineReaderException(getCurrentLine() + "The distance value of " +
                    "the " +
                    "Shift microinstruction \"" + name +
                    "\" must be nonnegative, not " + distance + ".");
        }

        String type = attrs.getValue("type");
        String direction = attrs.getValue("direction");

        Shift c = new Shift(name, machine, source, dest, type, direction, distance);
        String id = attrs.getValue("id");
        components.put(id, c);
        machine.getMicros("shift").add(c);
    }

    //--------------------------
    public void startBranch(Attributes attrs)
    {
        String name = attrs.getValue("name");

        String amountString = attrs.getValue("amount");
        int amount = 0;
        try {
            amount = Integer.parseInt(amountString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The amount value of " +
                    "the " +
                    "Branch microinstruction \"" + name +
                    "\" must be an integer, not \"" + amountString + "\".");
        }

        Branch c = new Branch(name, machine, amount, machine.getControlUnit());
        String id = attrs.getValue("id");
        components.put(id, c);
        machine.getMicros("branch").add(c);
    }

    //--------------------------
    public void startLogical(Attributes attrs)
    {
        String name = attrs.getValue("name");
        String type = attrs.getValue("type");

        String registerID = attrs.getValue("source1");
        Object object = components.get(registerID);
        if (object == null || !(object instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The source1 attribute " +
                    "" + "of Logical microinstruction \"" + name + "\" is not " +
                    "valid.");
        }
        Register source1 = (Register) object;

        registerID = attrs.getValue("source2");
        object = components.get(registerID);
        if (object == null || !(object instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The source2 attribute " +
                    "" + "of Logical microinstruction \"" + name + "\" is not " +
                    "valid.");
        }
        Register source2 = (Register) object;

        registerID = attrs.getValue("destination");
        object = components.get(registerID);
        if (object == null || !(object instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The destinations " +
                    "attribute " + "of Logical microinstruction \"" + name + "\" is not" +
                    " " +
                    "valid.");
        }
        Register destination = (Register) object;

        if (!(source1.getWidth() == source2.getWidth() && source2.getWidth() ==
                destination.getWidth())) {
            throw new MachineReaderException(getCurrentLine() + "The two source and " +
                    "destination " + "registers of Logical microinstruction \"" + name +
                    "\" are not the same width.");
        }

        Logical c = new Logical(name, machine, type, source1, source2, destination);
        String id = attrs.getValue("id");
        components.put(id, c);
        machine.getMicros("logical").add(c);
    }

    //--------------------------
    public void startSet(Attributes attrs)
    {
        String name = attrs.getValue("name");

        String registerID = attrs.getValue("register");
        Object object = components.get(registerID);
        if (object == null || !(object instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The register attribute" +
                    " " + "of Set microinstruction \"" + name + "\" is not valid.");
        }
        Register register = (Register) object;

        String numBitsString = attrs.getValue("numBits");
        int numBits = 0;
        try {
            numBits = Integer.parseInt(numBitsString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The numBits value of " +
                    "the " +
                    "Set microinstruction \"" + name +
                    "\" must be an integer, not \"" + numBitsString + "\".");
        }
        if (numBits <= 0) {
            throw new MachineReaderException(getCurrentLine() + "The numBits value of " +
                    "the " +
                    "Set microinstruction " + name +
                    " must be positive, not " + numBits + ".");
        }

        String startString = attrs.getValue("start");
        int start = 0;
        try {
            start = Integer.parseInt(startString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The start value of the" +
                    " " +
                    "Set microinstruction \"" + name +
                    "\" must be an integer, not \"" + startString + "\".");
        }
        if (start < 0) {
            throw new MachineReaderException(getCurrentLine() + "The start value of the" +
                    " " +
                    "Set microinstruction \"" + name +
                    "\" must be nonnegative, not " + start + ".");
        }
        if (start + numBits > register.getWidth()) {
            throw new MachineReaderException(getCurrentLine() + "The register bits of " +
                    "the " +
                    "Set microinstruction \"" + name +
                    "\" are out of range for the register register.");
        }

        String valueString = attrs.getValue("value");
        int value = 0;
        try {
            value = Integer.parseInt(valueString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The value attribute of" +
                    " the " +
                    "Set microinstruction \"" + name +
                    "\" must be an integer, not \"" + valueString + "\".");
        }

        BigInteger bigValue = BigInteger.valueOf(value);
        BigInteger twoToBits = BigInteger.valueOf(2).pow(numBits);
        BigInteger twoToBitsMinusOne = BigInteger.valueOf(2).pow(numBits - 1);

        if (bigValue.compareTo(twoToBits) >= 0 || bigValue.compareTo(twoToBitsMinusOne
                .negate()) < 0) {
            throw new MachineReaderException(getCurrentLine() + "The value attribute of" +
                    " the " +
                    "Set microinstruction \"" + name +
                    "\" doesn't fit in the given number of bits.");
        }

        CpusimSet c = new CpusimSet(name, machine, register, start, numBits, Long
                .valueOf(value));
        String id = attrs.getValue("id");
        components.put(id, c);
        machine.getMicros("set").add(c);
    }

    //--------------------------
    public void startTest(Attributes attrs)
    {
        String name = attrs.getValue("name");
        String comparison = attrs.getValue("comparison");

        String registerID = attrs.getValue("register");
        Object object = components.get(registerID);
        if (object == null || !(object instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The register attribute" +
                    " " + "of Test microinstruction \"" + name + "\" is not valid.");
        }
        Register register = (Register) object;

        String numBitsString = attrs.getValue("numBits");
        int numBits = 0;
        try {
            numBits = Integer.parseInt(numBitsString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The numBits value of " +
                    "the " +
                    "Test microinstruction \"" + name +
                    "\" must be an integer, not \"" + numBitsString + "\".");
        }
        if (numBits < 0) {
            throw new MachineReaderException(getCurrentLine() + "The numBits value of " +
                    "the " +
                    "Test microinstruction \"" + name +
                    "\" must be nonnegative, not " + numBits + ".");
        }

        String startString = attrs.getValue("start");
        int start = 0;
        try {
            start = Integer.parseInt(startString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The start value of the" +
                    " " +
                    "Test microinstruction \"" + name +
                    "\" must be an integer, not \"" + startString + "\".");
        }
        if (start < 0) {
            throw new MachineReaderException(getCurrentLine() + "The start value of the" +
                    " " +
                    "Test microinstruction \"" + name +
                    "\" must be nonnegative, not " + start + ".");
        }
        if (start + numBits > register.getWidth()) {
            throw new MachineReaderException(getCurrentLine() + "The bits of the " +
                    "Test microinstruction \"" + name +
                    "\" are out of range for the selected register.");
        }

        String valueString = attrs.getValue("value");
        int value = 0;
        try {
            value = Integer.parseInt(valueString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The value attribute of" +
                    " the " +
                    "Test microinstruction \"" + name +
                    "\" must be an integer, not \"" + valueString + "\".");
        }

        String omissionString = attrs.getValue("omission");
        int omission = 0;
        try {
            omission = Integer.parseInt(omissionString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The omission attribute" +
                    " of the " +
                    "Test microinstruction \"" + name +
                    "\" must be an integer, not \"" + omissionString + "\".");
        }

        Test c = new Test(name, machine, register, start, numBits, comparison, Long
                .valueOf(value), omission);
        String id = attrs.getValue("id");
        components.put(id, c);
        machine.getMicros("test").add(c);
    }

    //--------------------------
    public void startDecode(Attributes attrs)
    {
        String name = attrs.getValue("name");

        String registerID = attrs.getValue("ir");
        Object object = components.get(registerID);
        if (object == null || !(object instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The ir attribute " +
                    "of the Decode microinstruction \"" + name + "\" is not valid.");
        }
        Register ir = (Register) object;

        Decode c = new Decode(name, machine, ir);
        String id = attrs.getValue("id");
        components.put(id, c);
        machine.getMicros("decode").add(c);
    }

    //--------------------------
    public void startFileChannel(Attributes attrs)
    {
        String fileName = attrs.getValue("file");
        if (channelsUse(fileName)) {
            throw new MachineReaderException(getCurrentLine() +
                    "There can be at most one channel per file." +
                    "  File \"" + fileName + "\" has two or more channels.");
        }
        FileChannel f = new FileChannel(new File(fileName));
        String id = attrs.getValue("id");
        channels.put(id, f);
    }

    //--------------------------
    public void startIO(Attributes attrs)
    {
        String name = attrs.getValue("name");
        String direction = attrs.getValue("direction");
        String type = attrs.getValue("type");
        if ("character".equals(type)) {
            throw new MachineReaderException(getCurrentLine() +
                    "IO microinstructions of type " + "character are not yet " +
                    "implemented.  The microinstruction \"" + name + "\" is not valid.");
        }

        String registerID = attrs.getValue("buffer");
        Object object = components.get(registerID);
        if (object == null || !(object instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The buffer attribute "
                    + "of the IO microinstruction \"" + name + "\" is not valid" +
                    ".");
        }
        Register buffer = (Register) object;

        IOChannel connection;
        String channelID = attrs.getValue("connection");
        if (channelID == null || channelID.equals("") || channelID.equals("[console]")
                || channelID.equals("[Console]")) {
            connection = CONSOLE_CHANNEL;
        }
        //the case of null and "" and "[console]" above are for backward compatibility
        else if (channelID.equals("[user]") || channelID.equals("[Dialog]")) {
            connection = DIALOG_CHANNEL;
        }
        else {
            object = channels.get(channelID);
            if (object == null) {
                throw new MachineReaderException(getCurrentLine() +
                        "The connection attribute " + "of the IO microinstruction \"" +
                        name +
                        "\" is not valid.");
            }
            connection = (IOChannel) object;
        }

        IO c = new IO(name, machine, type, buffer, direction, connection);
        String id = attrs.getValue("id");
        components.put(id, c);
        machine.getMicros("io").add(c);
    }

    //--------------------------
    public void startMemoryAccess(Attributes attrs)
    {
        String name = attrs.getValue("name");
        String direction = attrs.getValue("direction");

        String ramID = attrs.getValue("memory");
        Object object = components.get(ramID);
        if (object == null || !(object instanceof RAM)) {
            throw new MachineReaderException(getCurrentLine() + "The memory attribute "
                    + "of the MemoryAccess microinstruction \"" + name +
                    "\" is not valid.");
        }
        RAM memory = (RAM) object;

        String registerID = attrs.getValue("data");
        object = components.get(registerID);
        if (object == null || !(object instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The data register " +
                    "attribute " + "of the MemoryAccess microinstruction \"" + name +
                    "\" is not valid.");
        }
        Register data = (Register) object;
        /*
        //This test is no longer needed since the register can be any
        //   size and the memory cells can be any size.
        if ((data.getWidth() % 8) != 0) {
            throw new MachineReaderException(
                    getCurrentLine() + "The data register attribute "
                    + "of the MemoryAccess microinstruction \"" + name +
                    "\"\nrefers to a register that does not have a width that "
                    + "is a multiple of 8.");
        }
        */

        registerID = attrs.getValue("address");
        object = components.get(registerID);
        if (object == null || !(object instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The address register " +
                    "attribute " + "of the MemoryAccess microinstruction \"" + name +
                    "\" is not valid.");
        }
        Register address = (Register) object;

        MemoryAccess c = new MemoryAccess(name, machine, direction, memory, data,
                address);
        String id = attrs.getValue("id");
        components.put(id, c);
        machine.getMicros("memoryAccess").add(c);
    }

    //--------------------------
    public void startSetCondBit(Attributes attrs)
    {
        String name = attrs.getValue("name");
        String value = attrs.getValue("value");

        String bitID = attrs.getValue("bit");
        Object object = components.get(bitID);
        if (object == null || !(object instanceof ConditionBit)) {
            throw new MachineReaderException(getCurrentLine() + "The bit attribute " +
                    "of SetCondBit microinstruction \"" + name +
                    "\" is not valid.");
        }
        ConditionBit bit = (ConditionBit) object;

        SetCondBit c = new SetCondBit(name, machine, bit, value);
        String id = attrs.getValue("id");
        components.put(id, c);
        machine.getMicros("setCondBit").add(c);
    }

    //--------------------------
    public void startEnd(Attributes attrs)
    {
        End c = new End(machine);
        String id = attrs.getValue("id");
        components.put(id, c);
        machine.setEnd(c);
    }

    //--------------------------
    public void startComment(Attributes attrs)
    {
        String name = attrs.getValue("name");
        Comment c = new Comment();
        c.setName(name);
        String id = attrs.getValue("id");
        components.put(id, c);
    }

    //--------------------------
    public void startFetchSequence(Attributes attrs)
    {
        currentInstruction = machine.getFetchSequence();
    }

    //--------------------------
    public void startMachineInstruction(Attributes attrs)
    {
        //validate the name attribute
        String name = attrs.getValue("name");
        try {
            Validate.nameIsValidAssembly(name, chars.toArray(new PunctChar[]{}));
        } catch (ValidationException e) {
            throw new MachineReaderException(getCurrentLine() + e.getMessage());
        }

        //validate the opcode string
        opcodeString = attrs.getValue("opcode");
        opcodeLine = getCurrentLine();
        long opcode = 0;
        try {
            opcode = Long.parseLong(opcodeString, 16);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The opcode value " +
                    "of the machine instruction\n\"" + name + "\" must be a" +
                    " hexadecimal integer of at most 16 hex characters,\n" +
                    "not \"" + opcodeString + "\".");
        }
        if (opcodeString.charAt(0) == '-') {
            throw new MachineReaderException(getCurrentLine() + "The opcode value " +
                    "of the machine instruction\n \"" + name + "\" must be a" +
                    " nonnegative hexadecimal integer" +
                    ", not \"" + opcodeString + "\".");
        }

        //get the format
        currentFormat = attrs.getValue("format");
        String currentInstructionFormat = attrs.getValue("instructionFormat");
        String currentAssemblyFormat = attrs.getValue("assemblyFormat");
        String instrColors = attrs.getValue("instructionColors");
        String assemblyColors = attrs.getValue("assemblyColors");

        /*
         For backward compatability, I need to deal with 3 possible
         formats of instructions.  The original format was just a list of
         field lengths.  The next version had a format string consisting of
         names of Fields separated by spaces.  The newest version has separate
         assembly and machine formats and colors as well.
         */
        if (currentFormat == null && currentInstructionFormat == null) {
            // version 1:  no format and instead a list of FieldLengths
            // The format will be set in startFieldLength and then resolved in
            // endMachineInstruction
            currentFormat = "";
            currentInstruction = new MachineInstruction(name, opcode, new ArrayList<>()
                    , new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), machine);
        }
        else if (currentInstructionFormat == null) {
            // version 2: A single format for machine & assembly instructions
            currentInstruction = new MachineInstruction(name, opcode, currentFormat,
                    machine);
        }
        else {
            // version 3: separate formats for machine & assembly instructions
            currentInstruction = new MachineInstruction(name, opcode, ConvertStrings
                    .formatStringToFields(currentInstructionFormat, machine), ConvertStrings
                    .formatStringToFields(currentAssemblyFormat, machine), Convert
                    .xmlToColorsList(instrColors), Convert.xmlToColorsList
                    (assemblyColors), machine);
        }

        machine.getInstructions().add(currentInstruction);
    }

    //--------------------------
    public void endMachineInstruction()
    {
        try {
            if (currentInstruction.getInstructionFields().size() == 0) {
                // it must be the old version where there were only FieldLengths,
                // so now fromRootController the Fields and then set the instruction and
                // assembly fields
                String[] fieldNames = currentFormat.split("\\s");
                for (String fieldName : fieldNames) {
                    if (!fields.containsKey(fieldName)) {
                        int fieldLength = Math.abs(Integer.parseInt(fieldName));
                        Field field = new Field(fieldName, Field.Type.required,
                                fieldLength, Field.Relativity.absolute, FXCollections
                                .observableArrayList(), 0, true);

                        machine.getFields().add(field);
                        fields.put(fieldName, field);
                    }
                }
                // generate the correct instruction
                MachineInstruction newInstr = new MachineInstruction(currentInstruction
                        .getName(), currentInstruction.getOpcode(), currentFormat,
                        machine);
                // copy it into currentInstruction
                currentInstruction.setInstructionFields(newInstr.getInstructionFields());
                currentInstruction.setAssemblyFields(newInstr.getAssemblyFields());
                currentInstruction.setInstructionColors(newInstr.getInstructionColors());
                currentInstruction.setAssemblyColors(newInstr.getAssemblyColors());
            }

            Validate.fieldsListIsNotEmpty(currentInstruction);
            Validate.opcodeFits(currentInstruction);
            Validate.firstFieldIsProper(currentInstruction);
            Validate.fieldLengthsAreAtMost64(currentInstruction);
            //Validate.atMostOnePosLengthFieldIsOptional(currentInstruction);
        } catch (ValidationException e) {
            throw new MachineReaderException((locator == null ? "" : "The error is in " +
                    "the MachineInstruction" + " element ending at line ") +
                    locator.getLineNumber() + ".\n" + e.getMessage());
        }
    }

    //--------------------------
    public void startFieldLength(Attributes attrs)
    {
        String lengthString = attrs.getValue("length");
        int length = 0;
        try {
            length = Integer.parseInt(lengthString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "Field lengths of" +
                    " machine instruction \"" +
                    currentInstruction.getName() +
                    "\" must be an integer, not \"" + lengthString +
                    "\".");
        }
        if (length == 0) {
            throw new MachineReaderException(getCurrentLine() + "Field lengths of" +
                    " machine instruction \"" +
                    currentInstruction.getName() +
                    "\" must not be zero.  One is " + length + ".");
        }
        currentFormat += length + " ";
    }

    //--------------------------
    public void startMicroinstruction(Attributes attrs)
    {
        String microID = attrs.getValue("microRef");
        Object object = components.get(microID);
        if (object == null || !(object instanceof Microinstruction)) {
            throw new MachineReaderException(getCurrentLine() +
                    "The microRef attribute \"" +
                    microID + "\" of one of \nthe microinstructions for the " +
                    "instruction \"" + currentInstruction.getName() +
                    "\" is not valid.");
        }
        Microinstruction micro = (Microinstruction) object;
        currentInstruction.getMicros().add(micro);
    }

    //--------------------------
    public void startEQU(Attributes attrs)
    {
        String name = attrs.getValue("name");
        String valueString = attrs.getValue("value");
        long value = 0;
        try {
            value = Long.parseLong(valueString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() +
                    "The value of EQU \"" +
                    name + "\" must be an integer, not \"" + valueString + "\".");
        }
        machine.getEQUs().add(new EQU(name, value));
    }

    //--------------------------
    public void startRegisterRAMPair(Attributes attrs)
    {
        String ramID = attrs.getValue("ram");
        Object object = components.get(ramID);
        if (object == null || !(object instanceof RAM)) {
            throw new MachineReaderException(getCurrentLine() + "The ram attribute \""
                    + ramID + "\" of a RegisterRAMPair is not valid.");
        }
        RAM ram = (RAM) object;

        String registerID = attrs.getValue("register");
        object = components.get(registerID);
        if (object == null || !(object instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The register attribute" +
                    " \"" + registerID + "\"of a RegisterRAMPair is not valid.");
        }
        Register register = (Register) object;

        String dynamicString = attrs.getValue("dynamic");
        boolean dynamic = "true".equals(dynamicString);

        registerRAMPairs.add(new RegisterRAMPair(register, ram, dynamic));
    }

    //--------------------------
    public void startLoadingInfo(Attributes attrs)
    {
        String ramID = attrs.getValue("ram");
        Object object = components.get(ramID);
        if (object == null || !(object instanceof RAM)) {
            throw new MachineReaderException(getCurrentLine() + "The ram attribute \""
                    + ramID + "\" of a LoadingInfo element is not a valid RAM " +
                    "ID.");
        }
        RAM ram = (RAM) object;

        String addressString = attrs.getValue("startingAddress");
        int startAddress = 0;
        try {
            startAddress = Integer.parseInt(addressString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The starting address "
                    + "must be an integer, not \"" + addressString + "\".");
        }
        if (startAddress < 0) {
            throw new MachineReaderException(getCurrentLine() + "The starting address "
                    + startAddress + "\" must be a nonnegative integer.");
        }

        machine.setCodeStore(ram);
        machine.setStartingAddressForLoading(startAddress);
    }

    //--------------------------
    public void startIndexingInfo(Attributes attrs)
    {
        String indexFromRightString = attrs.getValue("indexFromRight");
        boolean indexFromRight;
        if (indexFromRightString.equals("true")) {
            indexFromRight = true;
        }
        else if (indexFromRightString.equals("false")) {
            indexFromRight = false;
        }
        else {
            throw new MachineReaderException(getCurrentLine() + "The indexFromRight " +
                    "property must be either " + "\"true\" or \"false\".  The string + " +
                    indexFromRightString +
                    "is not a valid value for this property.");
        }
        machine.setIndexFromRight(indexFromRight);
    }

    //--------------------------
    public void startProgramCounterInfo(Attributes attrs)
    {
        String pcID = attrs.getValue("programCounter");
        Object object = components.get(pcID);
        if (object == null || !(object instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The program counter" +
                    " attribute \"" + pcID +
                    "\" of a ProgramCounterInfo element is not a valid Register ID.");
        }
        Register pc = (Register) object;
        machine.setProgramCounter(pc);
    }

    //--------------------------
    //    public void startRegisterWindowInfo(Attributes attrs) {
    //        windowInfo.put("Registers", getRectangle(attrs));
    //
    //        String baseString = attrs.getValue("base");
    //        //for backward compatibility, the next line has been added
    //        if (baseString == null) {
    //            baseString = "Decimal";
    //        }
    //        else if (baseString.equals("UnsignedDec")) {
    //            baseString = "Unsigned Dec";
    //        }
    //        contentBaseInfo.put("Registers", baseString);
    //    }

    //--------------------------
    //    public void startRegisterArrayWindowInfo(Attributes attrs) {
    //        String arrayID = attrs.getValue("array");
    //        Object object = components.get(arrayID);
    //        if (object == null || !(object instanceof RegisterArray)) {
    //            throw new MachineReaderException(getCurrentLine() +
    //                    "The array attribute \"" + arrayID +
    //                    "\" of a \nRegisterArrayWindowInfo element is " +
    //                    "not a valid register array ID.");
    //        }
    //
    //        windowInfo.put(object, getRectangle(attrs));
    //
    //        String baseString = attrs.getValue("base");
    //        //for backward compatibility, the next line has been added
    //        if (baseString == null) {
    //            baseString = "Decimal";
    //        }
    //        else if (baseString.equals("UnsignedDec")) {
    //            baseString = "Unsigned Dec";
    //        }
    //        contentBaseInfo.put(object, baseString);
    //    }

    //--------------------------
    //    public void startRAMWindowInfo(Attributes attrs) {
    //        String ramID = attrs.getValue("ram");
    //        Object object = components.get(ramID);
    //        if (object == null || !(object instanceof RAM)) {
    //            throw new MachineReaderException(getCurrentLine() +
    //                    "The ram attribute \"" + ramID +
    //                    "\" of a RAMWindowInfo element is not a valid RAM ID.");
    //        }
    //
    //        windowInfo.put(object, getRectangle(attrs));
    //
    //        String contentBaseString = attrs.getValue("contentsbase");
    //        String addressBaseString = attrs.getValue("addressbase");
    //        String oldBaseString = attrs.getValue("base");//for backwards compatability
    //        if (contentBaseString != null && contentBaseString.equals("UnsignedDec")) {
    //            contentBaseInfo.put(object, "Unsigned Dec");
    //        }
    //        else if (contentBaseString != null) {
    //            contentBaseInfo.put(object, contentBaseString);
    //        }
    //        else if (oldBaseString != null) {
    //            contentBaseInfo.put(object, oldBaseString);
    //        }
    //        else {
    //            contentBaseInfo.put(object, "Decimal");
    //        }
    //        if (addressBaseString != null) {
    //            addressBaseInfo.put(object, addressBaseString);
    //        }
    //        else if (oldBaseString != null && !oldBaseString.equals("Ascii")) {
    //            addressBaseInfo.put(object, oldBaseString);
    //        }
    //        else {
    //            addressBaseInfo.put(object, "Decimal");
    //        }
    //
    //        String cellSizeString = attrs.getValue("cellSize");
    //        int rowSize = 0;
    //        try {
    //            rowSize = Integer.parseInt(cellSizeString);
    //        } catch (NumberFormatException e) {
    //            throw new MachineReaderException(getCurrentLine() + "The row size "
    //                    + "(called 'cellSize') must be an integer, not \"" +
    //                    cellSizeString + "\".");
    //        }
    //        if (rowSize < 1 || rowSize > 8) {
    //            throw new MachineReaderException(getCurrentLine() + "The row size "
    //                    + "(called 'cellSize') must be an integer from 1 to 8," +
    //                    " not \"" + cellSizeString + "\".");
    //        }
    //
    //        cellSizeInfo.put(object, new Integer(rowSize));
    //    }

    //--------------------------
    // private utility method
    //    private Rectangle getRectangle(Attributes attrs) {
    //        String topString = attrs.getValue("top");
    //        int top = 0;
    //        try {
    //            top = Integer.parseInt(topString);
    //        } catch (NumberFormatException e) {
    //            throw new MachineReaderException(
    //                    getCurrentLine() + "The top attribute "
    //                            + "must be an integer, not \"" + topString + "\".");
    //        }
    //
    //        String leftString = attrs.getValue("left");
    //        int left = 0;
    //        try {
    //            left = Integer.parseInt(leftString);
    //        } catch (NumberFormatException e) {
    //            throw new MachineReaderException(
    //                    getCurrentLine() + "The left attribute "
    //                            + "must be an integer, not \"" + leftString + "\".");
    //        }
    //
    //        String widthString = attrs.getValue("width");
    //        int width = 0;
    //        try {
    //            width = Integer.parseInt(widthString);
    //        } catch (NumberFormatException e) {
    //            throw new MachineReaderException(
    //                    getCurrentLine() + "The width attribute "
    //                            + "must be an integer, not \"" + widthString + "\".");
    //        }
    //
    //        String heightString = attrs.getValue("height");
    //        int height = 0;
    //        try {
    //            height = Integer.parseInt(heightString);
    //        } catch (NumberFormatException e) {
    //            throw new MachineReaderException(
    //                    getCurrentLine() + "The height attribute "
    //                            + "must be an integer, not \"" + heightString + "\".");
    //        }
    //
    //        return new Rectangle(left, top, width, height);
    //    }

    //---------------------------------
    // returns true if the fileName is the file associated with
    // one of the FileChannels in the channels HashMap
    private boolean channelsUse(String fileName)
    {
        Collection e = channels.values();
        Iterator it = e.iterator();
        while (it.hasNext()) {
            FileChannel channel = (FileChannel) it.next();
            if (channel.getFile().toString().equals(fileName)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Simple Unit test.
     */
    //    public static void main(String[] args) {
    //        MachineReader reader = new MachineReader();
    //        String filename = "/Users/djskrien/Documents/CPU Sim/CPUSim3.8.2/" +
    //                           "SampleAssignments/Wombat1.cpu";
    //        try {
    //            reader.parseDataFromFile(new File(filename));
    //            Machine m = reader.getMachine();
    //            System.out.println(m.toString());
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //        }
    //
    //    }

}