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

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import cpusim.model.Field;
import cpusim.model.Field.Type;
import cpusim.model.FieldValue;
import cpusim.model.Machine;
import cpusim.model.MachineInstruction;
import cpusim.model.assembler.EQU;
import cpusim.model.assembler.PunctChar;
import cpusim.model.iochannel.FileChannel;
import cpusim.model.iochannel.IOChannel;
import cpusim.model.iochannel.StreamChannel;
import cpusim.model.microinstruction.*;
import cpusim.model.module.*;
import cpusim.model.util.*;
import cpusim.model.util.conversion.ConvertStrings;
import javafx.collections.FXCollections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

///////////////////////////////////////////////////////////////////////////////
// the libraries we need to import

///////////////////////////////////////////////////////////////////////////////
// the MachineReader class

public class MachineReader {
	private Machine machine; // the new machine to be read from the file
	
	// FIXME #94 
	private Map<UUID, IdentifiedObject> components; // key = id, value = module
	private MachineInstruction currentInstruction;
	// the current machine instruction being constructed
	private String currentFormat; // holds the field lengths until they are
	private String opcodeString; // holds the opcode for error messages
	// messages
	private Locator locator;
	private List<RegisterRAMPair> registerRAMPairs; // holds the current pairs
	private Map<String, Integer> cellSizeInfo; // key = ram, value =
													// cellSize for the ram
													// window
	private Map<UUID, IOChannel> channels; // key = id, value = FileChannel
	private RegisterArray currentRegisterArray;
	// the current RegisterArray being constructed
	private int currentRegisterArrayIndex;
	// the index of the next register to be added to the current RegisterArray
	private Map<String, Field> fields; // key = name, value = Field

	private VersionHandler byVersion;
	private IOChannel consoleChannel;
	
	private static final Set<Character> allPunctCharacters = ImmutableSet.<Character>builder()
				.add('!').add('@')
				.add('$').add('%')
				.add('^').add('&')
				.add('*').add('(')
				.add(')').add('_')
				.add('=').add('{')
				.add('}').add('[')
				.add(']').add('|')
				.add('\\').add(':')
				.add(';').add(',')
				.add('.').add('?')
				.add('/').add('~')
				.add('-').add('+')
				.add('#').add('`')
				.build();
		
	private List<PunctChar> chars;

	private Logger logger = LogManager.getLogger(MachineReader.class);

	public MachineReader() {
		reset();
	}

	public void reset() {
		byVersion = null;
		machine = null;
		components = new HashMap<>();
		currentInstruction = null;
		currentFormat = "";
		opcodeString = null;
		locator = null;
		registerRAMPairs = new ArrayList<>();
		cellSizeInfo = new HashMap<>();
		channels = new HashMap<>();
		consoleChannel = new StreamChannel(System.in, System.out);
		channels.put(consoleChannel.getID(), consoleChannel);

		currentRegisterArray = null;
		currentRegisterArrayIndex = 0;
		fields = new HashMap<>();

		// initialize the punctuation characters to the default chars.
		// These will be replaced by new ones if the .cpu file specifies
		// punctuation characters.
		chars = Machine.getDefaultPunctChars();
	}

	private Lax newLaxAndReset() {
		Lax lax = new Lax();
		lax.reset();
		reset();
		lax.addHandler(this);
		return lax;
	}

	/**
	 * reads and stores all the info from the given file. Throws an exception if
	 * there is an error attempting to parse the file.
	 *
	 * @param fileToOpen
	 *            the file containing the machine description
	 * @throws Exception
	 *             if it has any problems reading from the file
	 */
	public void parseDataFromFile(File fileToOpen) throws Exception {
		Lax lax = newLaxAndReset();
		lax.parseDocument(true, lax, fileToOpen);
	}

	/**
	 * reads and stores all the info from the given file. Throws an exception if
	 * there is an error attempting to parse the file.
	 *
	 * @param stream Content stream to read data from.
	 * @throws Exception
	 *             if it has any problems reading from the file
	 */
	public void parseDataFromStream(InputStream stream) throws IOException, SAXException, ParserConfigurationException {
		Lax lax = newLaxAndReset();
		lax.parseDocument(true, lax, stream);
	}

	
	public Map<String, Integer> getCellSizeInfo() {
		return cellSizeInfo;
	}

	public Optional<Machine> getMachine() {
		return Optional.ofNullable(machine);
	}

	public List<RegisterRAMPair> getRegisterRAMPairs() {
		return registerRAMPairs;
	}
	
	
	@SuppressWarnings("unused")
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

	// returns an error message giving the current line number
	private String getCurrentLine() {
		if (locator == null) {
			return "";
		} else {
			return "The error is on or near line " + locator.getLineNumber() + ".\n";
		}
	}

	@SuppressWarnings("unused")
	public void startMachine(Attributes alAttrs) {
		String name = alAttrs.getValue("name");
		if (name.endsWith(".xml") || name.endsWith(".cpu")) {
			name = name.substring(0, name.length() - 4);
		}

		final String cpuSimVersion = alAttrs.getValue("cpusim-version");
		if (cpuSimVersion != null) {
			byVersion = VersionHandler.forVersion(Integer.parseInt(cpuSimVersion.trim()), consoleChannel.getID());
		} else {
			byVersion = VersionHandler.forVersion(VersionHandler.LEGACY_VERSION_NUMBER, consoleChannel.getID());
		}
		machine = new Machine(name, true);
	}

	@SuppressWarnings("unused")
	public void endMachine() {
		try {
			List<MachineInstruction> instrs = machine.getInstructions();
			NamedObject.validateUniqueAndNonempty(instrs);
			Validate.allOpcodesAreUnique(instrs);
		} catch (ValidationException e) {
			throw new MachineReaderException(getCurrentLine() + e.getMessage(), e);
		}

		// check the punctuation characters
		List<PunctChar> punctChars = Lists.newArrayList(chars);
		try {
			Validate.punctChars(chars);
		} catch (ValidationException exc) {
			throw new MachineReaderException(getCurrentLine() + exc.getMessage(), exc);
		}
		machine.setPunctChars(punctChars);

		// set the code store if not already set
		if (!machine.getModules(RAM.class).isEmpty() && machine.getCodeStore() == null) {
			machine.setCodeStore(machine.getModules(RAM.class).get(0));
		}
	}
	
	@SuppressWarnings("unused")
	public void startPunctChar(Attributes attrs) {
		String cAsString = attrs.getValue("char");
		if (cAsString.length() != 1) {
			throw new MachineReaderException(
					getCurrentLine() + "There must be only one" + " punctuation character per line.");
		}
		char c = cAsString.charAt(0);
		if (!allPunctCharacters.contains(c)) {
			throw new MachineReaderException(getCurrentLine()
					+ "Only the punctuation characters !@$%^&*()_={}[]|\\:;,.?/~+-#`" + "" + " are allowed here.");
		}
		String useString = attrs.getValue("use");
		PunctChar.Use use = Enum.valueOf(PunctChar.Use.class, useString);
		for (PunctChar aChar : chars) {
			if (aChar.getChar() == c) {
				aChar.setUse(use);
			}
		}
	}

	@SuppressWarnings("unused")
	public void startField(Attributes attrs) {
		String name = attrs.getValue("name");
		if (fields.get(name) != null) {
			throw new MachineReaderException(getCurrentLine() + "There are two fields with the same name: \"" + name
					+ "\".\nAll field names must be unique.");
		}

		String typeString = attrs.getValue("type");
		Type type = byVersion.getFieldType(typeString);

		String relativityString = attrs.getValue("relativity");
		Field.Relativity rel = byVersion.getFieldRelativity(relativityString);

		String signedString = attrs.getValue("signed");
		Field.SignedType signed = byVersion.getFieldSigned(signedString);

		String defaultValueString = attrs.getValue("defaultValue");
		String numBitsString = attrs.getValue("numBits");
		int numBits;
		try {
			numBits = Integer.parseInt(numBitsString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(getCurrentLine() + "The numBits value of the Field \"" + name
					+ "\" must be an integer, not \"" + numBitsString + "\".", e);
		}
		if (numBits < 0) {
			throw new MachineReaderException(getCurrentLine() + "The numBits value of the Field \"" + name
					+ "\" must non-negative, not \"" + numBits + "\".");
		}

		int defaultValue;
		try {
			defaultValue = Integer.parseInt(defaultValueString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(getCurrentLine() + "The defaultValue value of the Field \"" + name
					+ "\" must be an integer, not \"" + defaultValueString + "\".", e);
		}
		if (numBits > 0 && signed == Field.SignedType.Unsigned && (defaultValue < 0 || defaultValue > Math.pow(2, numBits) - 1)) {
			throw new MachineReaderException(getCurrentLine() + "The default value of the unsigned Field \"" + name
					+ "\" must be in the range 0 to 2^" + numBits + "-1.");
		} else if (numBits > 0 && signed == Field.SignedType.Signed
				&& (defaultValue < -Math.pow(2, numBits - 1) || defaultValue > Math.pow(2, numBits - 1) - 1)) {
			throw new MachineReaderException(getCurrentLine() + "The default value of the signed Field \"" + name
					+ "\" must be in the range -(2^" + (numBits - 1) + ") and" + "(2^" + (numBits - 1) + "-1).");
		}

		final Field f = new Field(name, UUID.randomUUID(), machine, numBits,
				rel, FXCollections.observableArrayList(), defaultValue, signed, type);
		machine.getFields().add(f);
		fields.put(name, f);
	}

	@SuppressWarnings("unused")
	public void startFieldValue(Attributes attrs) {
		String name = attrs.getValue("name");
		String valueString = attrs.getValue("value");
		int value;
		try {
			value = Integer.parseInt(valueString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(getCurrentLine() + "The value of FieldValue \"" + name
					+ "\" must be an integer, not \"" + valueString + "\".", e);
		}
		UUID id = byVersion.getUUID(attrs.getValue("id"));
		if (id == null) {
			id = UUID.randomUUID();
		}
		Field lastField = machine.getFields().get(machine.getFields().size() - 1);
		lastField.getValues().add(new FieldValue(name, id, machine, value));
	}
	


	@SuppressWarnings("unused")
	public void startRegister(Attributes attrs) {
		String name = attrs.getValue("name");
		String widthString = attrs.getValue("width");
		String initialValueString = attrs.getValue("initialValue") == null ? "0" : attrs.getValue("initialValue");

		// TODO this should be a Set of elements rather than attributes
		String readOnlyString = attrs.getValue("readOnly");
		readOnlyString = readOnlyString == null ? "false" : readOnlyString;
		EnumSet<Register.Access> access = byVersion.getMemoryAccess(readOnlyString);

		int width;
		int initialValue;

		try {
			width = Integer.parseInt(widthString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(getCurrentLine() + "The width of register \"" + name
					+ "\" must be an integer, not \"" + widthString + "\".", e);
		}
		if (width <= 0 || width > 64) {
			throw new MachineReaderException(getCurrentLine() + "The width of register " + "\"" + name
					+ "\" must be between 1 and 64, not " + width);
		}

		try {
			initialValue = Integer.parseInt(initialValueString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(getCurrentLine() + "The initial value of register\"" + name
					+ "\" must be an integer, " + "not \"" + initialValueString + "\"" + ".", e);
		}

		UUID id = byVersion.getUUID(attrs.getValue("id"));
		Register r = new Register(name, id, machine, width, initialValue, access);

		if (currentRegisterArray != null) {
			if (currentRegisterArrayIndex >= currentRegisterArray.getLength()) {
				throw new MachineReaderException(getCurrentLine() + "The length " + "of register array "
						+ currentRegisterArray.getName() + " does not match the list of registers for it");
			}
			currentRegisterArray.getRegisters().add(r);
			currentRegisterArrayIndex++;
		} else {
			machine.getModules(Register.class).add(r);
		}
		
		components.put(id, r);
	}

	@SuppressWarnings("unused")
	public void startRegisterArray(Attributes attrs) {
		String name = attrs.getValue("name");
		String widthString = attrs.getValue("width");
		int width;
		try {
			width = Integer.parseInt(widthString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(getCurrentLine() + "The width of register " + "\"" + name
					+ "\" must be an integer, not \"" + widthString + "\".", e);
		}
		if (width <= 0 || width > 64) {
			throw new MachineReaderException(getCurrentLine() + "The width of the " + "registers "
					+ "in register array \"" + name + "\" must be between 1 and 64, not " + width + ".");
		}

		String lengthString = attrs.getValue("length");
		int length;
		try {
			length = Integer.parseInt(lengthString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(getCurrentLine() + "The length of register" + " " + "array \"" + name
					+ "\" must be an integer, not \"" + lengthString + "\".", e);
		}
		if (length <= 0) {
			throw new MachineReaderException(getCurrentLine() + "The length of register" + " " + "array \"" + name
					+ "\" must be positive, not " + width + ".");
		}
		
		UUID id = byVersion.getUUID(attrs.getValue("id"));
		RegisterArray r = new RegisterArray(name, id, machine, width, length, 0, Register.Access.readWrite());
		components.put(id, r);
		machine.getModules(RegisterArray.class).add(r);

		// now prepare for reading in the registers
		currentRegisterArray = r;
		currentRegisterArrayIndex = 0;
	}
	
	@SuppressWarnings("unused")
	public void endRegisterArray() {
		currentRegisterArray = null;
	}

	@SuppressWarnings("unused")
	public void startConditionBit(Attributes attrs) {
		String name = attrs.getValue("name");

		String haltString = attrs.getValue("halt");
		boolean halt = haltString.equals("true");

		UUID registerID =byVersion.getUUID(attrs.getValue("register"));
		Object object = components.get(registerID);
		if (object == null || !(object instanceof Register)) {
			throw new MachineReaderException(getCurrentLine() + "The register attribute" + " " + "of condition bit \""
					+ name + "\" is not valid.");
		}
		Register register = (Register) object;

		String bitString = attrs.getValue("bit");
		int bit;
		try {
			bit = Integer.parseInt(bitString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(getCurrentLine() + "The bit index of " + "condition bit \"" + name
					+ "\" must be an integer, not \"" + bitString + "\".", e);
		}
		if (bit < 0 || bit >= register.getWidth()) {
			throw new MachineReaderException(getCurrentLine() + "The bit index of " + "condition bit \"" + name
					+ "\" is " + bit + ", which is out of " + "range.");
		}
		
		UUID id = byVersion.getUUID(attrs.getValue("id"));
		ConditionBit c = new ConditionBit(name, id, machine, register, bit, halt);
		
		components.put(id, c);
		machine.getModules(ConditionBit.class).add(c);
	}

	@SuppressWarnings("unused")
	public void startRAM(Attributes attrs) {
		String name = attrs.getValue("name");
		String lengthString = attrs.getValue("length");
		int length;
		try {
			length = Integer.parseInt(lengthString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(getCurrentLine() + "The length of RAM \"" + name
					+ "\" must be an integer, not \"" + lengthString + "\".", e);
		}
		if (length <= 0) {
			throw new MachineReaderException(
					getCurrentLine() + "The length of RAM \"" + name + "\" must be positive, not " + length + ".");
		}
		String cellSizeString = attrs.getValue("cellSize");
		int cellSize;
		if (cellSizeString == null) // optional field for backward compatibility
		{
			cellSize = 8; // default of 1 byte
		} else {
			try {
				cellSize = Integer.parseInt(cellSizeString);
			} catch (NumberFormatException e) {
				throw new MachineReaderException(getCurrentLine() + "The number of bits" + " of each cell of RAM \""
						+ name + "\" must be an integer, not " + "\"" + lengthString + "\".", e);
			}
		}
		if (cellSize <= 0 || cellSize > 64) {
			throw new MachineReaderException(getCurrentLine() + "The number of bits of " + "each cell of RAM \"" + name
					+ "\" must be a positive integer at " + "most" + " 64, not " + length + ".");
		}
		
		UUID id = byVersion.getUUID(attrs.getValue("id"));
		RAM ram = new RAM(name, id, machine, length, cellSize);
		components.put(id, ram);
		machine.getModules(RAM.class).add(ram);
	}

	@SuppressWarnings("unused")
	public void startIncrement(Attributes attrs)
    {
        String name = attrs.getValue("name");

        String deltaString = attrs.getValue("delta");
        int delta;
        try {
            delta = Integer.parseInt(deltaString);
        } catch (NumberFormatException e) {
            throw new MachineReaderException(getCurrentLine() + "The delta value of the" +
                    " " +
                    "Increment microinstruction \"" + name +
                    "\" must be an integer, not \"" + deltaString + "\".", e);
        }

		UUID registerID = byVersion.getUUID(attrs.getValue("register"));
        Object mod = components.get(registerID);
        if (mod == null || !(mod instanceof Register)) {
            throw new MachineReaderException(getCurrentLine() + "The register attribute" +
                    " " + "of Increment microinstruction \"" + name +
                    "\" is not valid.");
        }
        Register register = (Register) mod;

        ConditionBit overflowBit;
		UUID overflowBitID = byVersion.getUUID(attrs.getValue("overflowBit"));
        if (overflowBitID == null) {
            overflowBit = null;
        }
        else {
            mod = components.get(overflowBitID);
            if (mod == null || !(mod instanceof ConditionBit)) {
                throw new MachineReaderException(getCurrentLine() + "The overflowBit " +
                        "attribute " + "of Increment microinstruction \"" + name +
                        "\" is not valid.");
            }
            overflowBit = (ConditionBit) mod;
        }

        ConditionBit carryBit;
		UUID carryBitID = byVersion.getUUID(attrs.getValue("carryBit"));
        if (carryBitID == null) {
            carryBit = null;
        }
        else {
            mod = components.get(carryBitID);
            if (mod == null || !(mod instanceof ConditionBit)) {
                throw new MachineReaderException(getCurrentLine() + "The carryBit " +
                        "attribute " + "of Increment microinstruction \"" + name +
                        "\" is not valid.");
            }
            carryBit = (ConditionBit) mod;
        }
	
		UUID id = byVersion.getUUID(attrs.getValue("id"));
        Increment c = new Increment(name, id, machine, register, delta, carryBit, overflowBit, null);
        components.put(id, c);
        machine.getMicros(Increment.class).add(c);
    }

	@SuppressWarnings("unused")
	public void startArithmetic(Attributes attrs) {
		String name = attrs.getValue("name");
		Arithmetic.Type type = Arithmetic.Type.valueOf(attrs.getValue("type").trim());

		UUID registerID = byVersion.getUUID(attrs.getValue(byVersion.getArithmeticSource1Attribute()));
		Object object = components.get(registerID);
		if (object == null || !(object instanceof Register)) {
			throw new MachineReaderException(getCurrentLine() + "The " + byVersion.getArithmeticSource1Attribute() +
					" attribute of Arithmetic microinstruction \"" + name + "\" is not valid.");
		}
		Register source1 = (Register) object;

		registerID = byVersion.getUUID(attrs.getValue(byVersion.getArithmeticSource2Attribute()));
		object = components.get(registerID);
		if (object == null || !(object instanceof Register)) {
			throw new MachineReaderException(getCurrentLine() + "The " + byVersion.getArithmeticSource2Attribute() + " attribute " + ""
					+ "of Arithmetic microinstruction \"" + name + "\" is not valid.");
		}
		Register source2 = (Register) object;

		registerID = byVersion.getUUID(attrs.getValue("destination"));
		object = components.get(registerID);
		if (object == null || !(object instanceof Register)) {
			throw new MachineReaderException(getCurrentLine() + "The destinations " + "attribute "
					+ "of Arithmetic microinstruction \"" + name + "\" is not valid.");
		}
		Register destination = (Register) object;

		ConditionBit overflowBit;
		UUID overflowBitID = byVersion.getUUID(attrs.getValue("overflowBit"));
		if (overflowBitID == null) {
            overflowBit = null;
		} else {
			object = components.get(overflowBitID);
			if (object == null || !(object instanceof ConditionBit)) {
				throw new MachineReaderException(getCurrentLine() + "The overflowBit " + "attribute "
						+ "of Arithmetic microinstruction \"" + name + "\" is not valid.");
			}
			overflowBit = (ConditionBit) object;
		}

		ConditionBit carryBit;
		UUID carryBitID = byVersion.getUUID(attrs.getValue("carryBit"));
		if (carryBitID == null) {
            carryBit = null;
		} else {
			object = components.get(carryBitID);
			if (object == null || !(object instanceof ConditionBit)) {
				throw new MachineReaderException(getCurrentLine() + "The carryBit " + "attribute "
						+ "of Arithmetic microinstruction \"" + name + "\" is not valid.");
			}
			carryBit = (ConditionBit) object;
		}
		
		UUID id = byVersion.getUUID(attrs.getValue("id"));
		Arithmetic c = new Arithmetic(name, id, machine, type, destination, source1, source2,
				carryBit, overflowBit, null);
		components.put(id, c);
		machine.getMicros(Arithmetic.class).add(c);
	}

	@SuppressWarnings("unused")
	public void startTransferRtoR(Attributes attrs) {
		String name = attrs.getValue("name");

		UUID registerID = byVersion.getUUID(attrs.getValue("source"));
		Object object = components.get(registerID);
		if (object == null || !(object instanceof Register)) {
			throw new MachineReaderException(getCurrentLine() + "The source attribute "
					+ "of TransferRtoR microinstruction \"" + name + "\" is not valid.");
		}
		Register source = (Register) object;

		registerID = byVersion.getUUID(attrs.getValue("dest"));
		object = components.get(registerID);
		if (object == null || !(object instanceof Register)) {
			throw new MachineReaderException(getCurrentLine() + "The dest attribute "
					+ "of TransferRtoR microinstruction \"" + name + "\" is not valid.");
		}
		Register dest = (Register) object;

		String numBitsString = attrs.getValue("numBits");
		int numBits;
		try {
			numBits = Integer.parseInt(numBitsString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(
					getCurrentLine() + "The numBits value of " + "the " + "TransferRtoR microinstruction \"" + name
							+ "\" must be an integer, not \"" + numBitsString + "\".", e);
		}
		if (numBits < 0) {
			throw new MachineReaderException(getCurrentLine() + "The numBits value of " + "the "
					+ "TransferRtoR microinstruction \"" + name + "\" must be nonnegative, not " + numBits + ".");
		}

		String srcStartBitString = attrs.getValue("srcStartBit");
		int srcStartBit;
		try {
			srcStartBit = Integer.parseInt(srcStartBitString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(
					getCurrentLine() + "The srcStartBit value " + "of the " + "TransferRtoR microinstruction \"" + name
							+ "\" must be an integer, not \"" + srcStartBitString + "\".", e);
		}
		if (srcStartBit < 0) {
			throw new MachineReaderException(getCurrentLine() + "The srcStartBit value " + "of the "
					+ "TransferRtoR microinstruction \"" + name + "\" must be nonnegative, not " + srcStartBit + ".");
		}

		String destStartBitString = attrs.getValue("destStartBit");
		int destStartBit;
		try {
			destStartBit = Integer.parseInt(destStartBitString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(
					getCurrentLine() + "The destStartBit value" + " of the " + "TransferRtoR microinstruction \"" + name
							+ "\" must be an integer, not \"" + destStartBitString + "\".", e);
		}
		if (destStartBit < 0) {
			throw new MachineReaderException(getCurrentLine() + "The destStartBit value" + " of the "
					+ "TransferRtoR microinstruction \"" + name + "\" must be nonnegative, not " + destStartBit + ".");
		}
		if (destStartBit + numBits > dest.getWidth()) {
			throw new MachineReaderException(getCurrentLine() + "The destination bits " + "of the "
					+ "TransferRtoR microinstruction \"" + name + "\" are out of range for the destination register.");
		}
		if (srcStartBit + numBits > source.getWidth()) {
			throw new MachineReaderException(getCurrentLine() + "The source bits of the" + " "
					+ "TransferRtoR microinstruction \"" + name + "\" are out of range for the source register.");
		}
		
		UUID id = byVersion.getUUID(attrs.getValue("id"));
		TransferRtoR c = new TransferRtoR(name, id, machine, source, srcStartBit, dest, destStartBit, numBits);
		components.put(id, c);
		machine.getMicros(TransferRtoR.class).add(c);
	}

	@SuppressWarnings("unused")
	public void startTransferRtoA(Attributes attrs) {
		String name = attrs.getValue("name");

		UUID registerID = byVersion.getUUID(attrs.getValue("source"));
		Object object = components.get(registerID);
		if (object == null || !(object instanceof Register)) {
			throw new MachineReaderException(getCurrentLine() + "The source attribute "
					+ "of TransferRtoA microinstruction \"" + name + "\" is not valid.");
		}
		Register source = (Register) object;

		registerID = byVersion.getUUID(attrs.getValue("dest"));
		object = components.get(registerID);
		if (object == null || !(object instanceof RegisterArray)) {
			throw new MachineReaderException(getCurrentLine() + "The dest attribute "
					+ "of TransferRtoA microinstruction \"" + name + "\" is not valid.");
		}
		RegisterArray dest = (RegisterArray) object;

		registerID = byVersion.getUUID(attrs.getValue("index"));
		object = components.get(registerID);
		if (object == null || !(object instanceof Register)) {
			throw new MachineReaderException(getCurrentLine() + "The index attribute "
					+ "of TransferRtoA microinstruction \"" + name + "\" is not valid.");
		}
		Register index = (Register) object;

		String numBitsString = attrs.getValue("numBits");
		int numBits;
		try {
			numBits = Integer.parseInt(numBitsString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(
					getCurrentLine() + "The numBits value of " + "the " + "TransferRtoA microinstruction \"" + name
							+ "\" must be an integer, not \"" + numBitsString + "\".", e);
		}
		if (numBits < 0) {
			throw new MachineReaderException(getCurrentLine() + "The numBits value of " + "the "
					+ "TransferRtoA microinstruction \"" + name + "\" must be nonnegative, not " + numBits + ".");
		}

		String srcStartBitString = attrs.getValue("srcStartBit");
		int srcStartBit;
		try {
			srcStartBit = Integer.parseInt(srcStartBitString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(
					getCurrentLine() + "The srcStartBit value " + "of the " + "TransferRtoA microinstruction \"" + name
							+ "\" must be an integer, not \"" + srcStartBitString + "\".", e);
		}
		if (srcStartBit < 0) {
			throw new MachineReaderException(getCurrentLine() + "The srcStartBit value " + "of the "
					+ "TransferRtoA microinstruction \"" + name + "\" must be nonnegative, not " + srcStartBit + ".");
		}

		String indexNumBitsString = attrs.getValue("indexNumBits");
		int indexNumBits = index.getWidth();
		if (indexNumBitsString != null) {
			try {
				indexNumBits = Integer.parseInt(indexNumBitsString);
			} catch (NumberFormatException e) {
				throw new MachineReaderException(
						getCurrentLine() + "The indexNumBits value of the " + "TransferRtoA microinstruction \"" + name
								+ "\" must be an integer, not \"" + indexNumBitsString + "\".", e);
			}
			if (indexNumBits <= 0) {
				throw new MachineReaderException(getCurrentLine() + "The indexNumBits value of the "
						+ "TransferRtoA microinstruction \"" + name + "\" must be positive, not " + indexNumBits + ".");
			}
		}

		String indexStartString = attrs.getValue("indexStart");
		int indexStart = 0;
		if (indexStartString != null) {
			try {
				indexStart = Integer.parseInt(indexStartString);
			} catch (NumberFormatException e) {
				throw new MachineReaderException(
						getCurrentLine() + "The indexStart value of the " + "TransferRtoA microinstruction \"" + name
								+ "\" must be an integer, not \"" + indexStartString + "\".", e);
			}
			if (indexStart < 0) {
				throw new MachineReaderException(
						getCurrentLine() + "The indexStart value of the " + "TransferRtoA microinstruction \"" + name
								+ "\" must be nonnegative, not " + indexStart + ".");
			}
		}

		String destStartBitString = attrs.getValue("destStartBit");
		int destStartBit;
		try {
			destStartBit = Integer.parseInt(destStartBitString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(
					getCurrentLine() + "The destStartBit value" + " of the " + "TransferRtoA microinstruction \"" + name
							+ "\" must be an integer, not \"" + destStartBitString + "\".", e);
		}
		if (destStartBit < 0) {
			throw new MachineReaderException(getCurrentLine() + "The destStartBit value" + " of the "
					+ "TransferRtoA microinstruction \"" + name + "\" must be nonnegative, not " + destStartBit + ".");
		}
		if (destStartBit + numBits > dest.getWidth()) {
			throw new MachineReaderException(getCurrentLine() + "The destination bits " + "of the "
					+ "TransferRtoA microinstruction \"" + name + "\" are out of range for the destination register.");
		}
		if (srcStartBit + numBits > source.getWidth()) {
			throw new MachineReaderException(getCurrentLine() + "The source bits of the" + " "
					+ "TransferRtoA microinstruction \"" + name + "\" are out of range for the source register.");
		}
		if (indexStart + indexNumBits > index.getWidth()) {
			throw new MachineReaderException(getCurrentLine() + "The specified bits of the "
					+ "TransferRtoA microinstruction \"" + name + "\" are out of range for the index register.");
		}
		
		UUID id = byVersion.getUUID(attrs.getValue("id"));
		TransferRtoA c = new TransferRtoA(name, id, machine, source, srcStartBit, dest, destStartBit, numBits, index,
				indexStart, indexNumBits);
		components.put(id, c);
		machine.getMicros(TransferRtoA.class).add(c);
	}

	@SuppressWarnings("unused")
	public void startTransferAtoR(Attributes attrs) {
		String name = attrs.getValue("name");

		UUID registerID = byVersion.getUUID(attrs.getValue("source"));
		Object object = components.get(registerID);
		if (object == null || !(object instanceof RegisterArray)) {
			throw new MachineReaderException(getCurrentLine() + "The source attribute "
					+ "of TransferAtoR microinstruction \"" + name + "\" is not valid.");
		}
		RegisterArray source = (RegisterArray) object;

		registerID = byVersion.getUUID(attrs.getValue("dest"));
		object = components.get(registerID);
		if (object == null || !(object instanceof Register)) {
			throw new MachineReaderException(getCurrentLine() + "The dest attribute "
					+ "of TransferAtoR microinstruction \"" + name + "\" is not valid.");
		}
		Register dest = (Register) object;

		registerID = byVersion.getUUID(attrs.getValue("index"));
		object = components.get(registerID);
		if (object == null || !(object instanceof Register)) {
			throw new MachineReaderException(getCurrentLine() + "The index attribute "
					+ "of TransferAtoR microinstruction \"" + name + "\" is not valid.");
		}
		Register index = (Register) object;

		String numBitsString = attrs.getValue("numBits");
		int numBits;
		try {
			numBits = Integer.parseInt(numBitsString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(
					getCurrentLine() + "The numBits value of " + "the " + "TransferAtoR microinstruction \"" + name
							+ "\" must be an integer, not \"" + numBitsString + "\".", e);
		}
		if (numBits < 0) {
			throw new MachineReaderException(getCurrentLine() + "The numBits value of " + "the "
					+ "TransferAtoR microinstruction \"" + name + "\" must be nonnegative, not " + numBits + ".");
		}

		String srcStartBitString = attrs.getValue("srcStartBit");
		int srcStartBit;
		try {
			srcStartBit = Integer.parseInt(srcStartBitString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(
					getCurrentLine() + "The srcStartBit value " + "of the " + "TransferAtoR microinstruction \"" + name
							+ "\" must be an integer, not \"" + srcStartBitString + "\".", e);
		}
		if (srcStartBit < 0) {
			throw new MachineReaderException(getCurrentLine() + "The srcStartBit value " + "of the "
					+ "TransferAtoR microinstruction \"" + name + "\" must be nonnegative, not " + srcStartBit + ".");
		}

		String indexNumBitsString = attrs.getValue("indexNumBits");
		int indexNumBits = index.getWidth();
		if (indexNumBitsString != null) {
			try {
				indexNumBits = Integer.parseInt(indexNumBitsString);
			} catch (NumberFormatException e) {
				throw new MachineReaderException(
						getCurrentLine() + "The indexNumBits value of the " + "TransferAtoR microinstruction \"" + name
								+ "\" must be an integer, not \"" + indexNumBitsString + "\".", e);
			}
			if (indexNumBits <= 0) {
				throw new MachineReaderException(getCurrentLine() + "The indexNumBits value of the "
						+ "TransferAtoR microinstruction \"" + name + "\" must be positive, not " + indexNumBits + ".");
			}
		}

		String indexStartString = attrs.getValue("indexStart");
		int indexStart = 0;
		if (indexStartString != null) {
			try {
				indexStart = Integer.parseInt(indexStartString);
			} catch (NumberFormatException e) {
				throw new MachineReaderException(
						getCurrentLine() + "The indexStart value of the " + "TransferAtoR microinstruction \"" + name
								+ "\" must be an integer, not \"" + indexStartString + "\".", e);
			}
			if (indexStart < 0) {
				throw new MachineReaderException(
						getCurrentLine() + "The indexStart value of the " + "TransferAtoR microinstruction \"" + name
								+ "\" must be nonnegative, not " + indexStart + ".");
			}
		}

		String destStartBitString = attrs.getValue("destStartBit");
		int destStartBit;
		try {
			destStartBit = Integer.parseInt(destStartBitString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(
					getCurrentLine() + "The destStartBit value" + " of the " + "TransferAtoR microinstruction \"" + name
							+ "\" must be an integer, not \"" + destStartBitString + "\".", e);
		}
		if (destStartBit < 0) {
			throw new MachineReaderException(getCurrentLine() + "The destStartBit value" + " of the "
					+ "TransferAtoR microinstruction \"" + name + "\" must be nonnegative, not " + destStartBit + ".");
		}
		if (destStartBit + numBits > dest.getWidth()) {
			throw new MachineReaderException(getCurrentLine() + "The destination bits " + "of the "
					+ "TransferAtoR microinstruction \"" + name + "\" are out of range for the destination register.");
		}
		if (srcStartBit + numBits > source.getWidth()) {
			throw new MachineReaderException(getCurrentLine() + "The source bits of the" + " "
					+ "TransferAtoR microinstruction \"" + name + "\" are out of range for the source register.");
		}
		if (indexStart + indexNumBits > index.getWidth()) {
			throw new MachineReaderException(getCurrentLine() + "The specified bits of the "
					+ "TransferAtoR microinstruction \"" + name + "\" are out of range for the index register.");
		}
		
		UUID id = byVersion.getUUID(attrs.getValue("id"));
		TransferAtoR c = new TransferAtoR(name, id, machine, source, srcStartBit, dest, destStartBit, numBits, index,
				indexStart, indexNumBits);
		components.put(id, c);
		machine.getMicros(TransferAtoR.class).add(c);
	}

	@SuppressWarnings("unused")
	public void startShift(Attributes attrs) {
		String name = attrs.getValue("name");

		UUID registerID = byVersion.getUUID(attrs.getValue("source"));
		Object object = components.get(registerID);
		if (object == null || !(object instanceof Register)) {
			throw new MachineReaderException(getCurrentLine() + "The source attribute " + "of Shift microinstruction \""
					+ name + "\" is not valid.");
		}
		Register source = (Register) object;

		registerID = byVersion.getUUID(attrs.getValue("destination"));
		object = components.get(registerID);
		if (object == null || !(object instanceof Register)) {
			throw new MachineReaderException(getCurrentLine() + "The dest attribute " + "of Shift microinstruction \""
					+ name + "\" is not valid.");
		}
		Register dest = (Register) object;
		if (source.getWidth() != dest.getWidth()) {
			throw new MachineReaderException(getCurrentLine() + "The source and " + "destination "
					+ "registers of the Shift microinstruction \"" + name + "\" must have equal widths.");
		}

		String distanceString = attrs.getValue("distance");
		int distance;
		try {
			distance = Integer.parseInt(distanceString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(getCurrentLine() + "The distance value of " + "the "
					+ "Shift microinstruction \"" + name + "\" must be an integer, not \"" + distanceString + "\".", e);
		}
		if (distance < 0) {
			throw new MachineReaderException(getCurrentLine() + "The distance value of " + "the "
					+ "Shift microinstruction \"" + name + "\" must be nonnegative, not " + distance + ".");
		}

		Shift.ShiftType type = byVersion.getShiftType(attrs.getValue("type").trim());
		Shift.ShiftDirection direction = byVersion.getShiftDirection(attrs.getValue("direction").trim());
		
		UUID id = byVersion.getUUID(attrs.getValue("id"));
		Shift c = new Shift(name, id, machine, source, dest, type, direction, distance);
		components.put(id, c);
		machine.getMicros(Shift.class).add(c);
	}

	@SuppressWarnings("unused")
	public void startBranch(Attributes attrs) {
		String name = attrs.getValue("name");

		String amountString = attrs.getValue("amount");
		int amount;
		try {
			amount = Integer.parseInt(amountString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(getCurrentLine() + "The amount value of " + "the "
					+ "Branch microinstruction \"" + name + "\" must be an integer, not \"" + amountString + "\".", e);
		}
		
		UUID id = byVersion.getUUID(attrs.getValue("id"));
		Branch c = new Branch(name, id, machine, amount);
		components.put(id, c);
		machine.getMicros(Branch.class).add(c);
	}

	@SuppressWarnings("unused")
	public void startLogical(Attributes attrs) {
		String name = attrs.getValue("name");
		Logical.Type type = Logical.Type.valueOf(attrs.getValue("type").trim());

		UUID registerID = byVersion.getUUID(attrs.getValue("source1"));
		Object object = components.get(registerID);
		if (object == null || !(object instanceof Register)) {
			throw new MachineReaderException(getCurrentLine() + "The source1 attribute " + ""
					+ "of Logical microinstruction \"" + name + "\" is not " + "valid.");
		}
		Register source1 = (Register) object;

		registerID = byVersion.getUUID(attrs.getValue("source2"));
		object = components.get(registerID);
		if (object == null || !(object instanceof Register)) {
			throw new MachineReaderException(getCurrentLine() + "The source2 attribute " + ""
					+ "of Logical microinstruction \"" + name + "\" is not " + "valid.");
		}
		Register source2 = (Register) object;

		registerID = byVersion.getUUID(attrs.getValue("destination"));
		object = components.get(registerID);
		if (object == null || !(object instanceof Register)) {
			throw new MachineReaderException(getCurrentLine() + "The destinations " + "attribute "
					+ "of Logical microinstruction \"" + name + "\" is not" + " " + "valid.");
		}
		Register destination = (Register) object;

		if (!(source1.getWidth() == source2.getWidth() && source2.getWidth() == destination.getWidth())) {
			throw new MachineReaderException(getCurrentLine() + "The two source and " + "destination "
					+ "registers of Logical microinstruction \"" + name + "\" are not the same width.");
		}
		
		UUID id = byVersion.getUUID(attrs.getValue("id"));
		Logical c = new Logical(name, id, machine, type, destination, source1, source2, null);
		components.put(id, c);
		machine.getMicros(Logical.class).add(c);
	}

	@SuppressWarnings("unused")
	public void startSet(Attributes attrs) {
		String name = attrs.getValue("name");

		UUID registerID = byVersion.getUUID(attrs.getValue("register"));
		Object object = components.get(registerID);
		if (object == null || !(object instanceof Register)) {
			throw new MachineReaderException(getCurrentLine() + "The register attribute" + " "
					+ "of Set microinstruction \"" + name + "\" is not valid.");
		}
		Register register = (Register) object;

		String numBitsString = attrs.getValue("numBits");
		int numBits;
		try {
			numBits = Integer.parseInt(numBitsString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(getCurrentLine() + "The numBits value of " + "the "
					+ "Set microinstruction \"" + name + "\" must be an integer, not \"" + numBitsString + "\".", e);
		}
		if (numBits <= 0) {
			throw new MachineReaderException(getCurrentLine() + "The numBits value of " + "the "
					+ "Set microinstruction " + name + " must be positive, not " + numBits + ".");
		}

		String startString = attrs.getValue("start");
		int start;
		try {
			start = Integer.parseInt(startString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(getCurrentLine() + "The start value of the" + " "
					+ "Set microinstruction \"" + name + "\" must be an integer, not \"" + startString + "\".", e);
		}
		if (start < 0) {
			throw new MachineReaderException(getCurrentLine() + "The start value of the" + " "
					+ "Set microinstruction \"" + name + "\" must be nonnegative, not " + start + ".");
		}
		if (start + numBits > register.getWidth()) {
			throw new MachineReaderException(getCurrentLine() + "The register bits of " + "the "
					+ "Set microinstruction \"" + name + "\" are out of range for the register register.");
		}

		String valueString = attrs.getValue("value");
		int value;
		try {
			value = Integer.parseInt(valueString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(getCurrentLine() + "The value attribute of" + " the "
					+ "Set microinstruction \"" + name + "\" must be an integer, not \"" + valueString + "\".", e);
		}

		BigInteger bigValue = BigInteger.valueOf(value);
		BigInteger twoToBits = BigInteger.valueOf(2).pow(numBits);
		BigInteger twoToBitsMinusOne = BigInteger.valueOf(2).pow(numBits - 1);

		if (bigValue.compareTo(twoToBits) >= 0 || bigValue.compareTo(twoToBitsMinusOne.negate()) < 0) {
			throw new MachineReaderException(getCurrentLine() + "The value attribute of" + " the "
					+ "Set microinstruction \"" + name + "\" doesn't fit in the given number of bits.");
		}
		
		UUID id = byVersion.getUUID(attrs.getValue("id"));
		SetBits c = new SetBits(name, id, machine, register, start, numBits, value);
		components.put(id, c);
		machine.getMicros(SetBits.class).add(c);
	}

	@SuppressWarnings("unused")
	public void startTest(Attributes attrs) {
		String name = attrs.getValue("name");
		String comparison = attrs.getValue("comparison");

		UUID registerID = byVersion.getUUID(attrs.getValue("register"));
		Object object = components.get(registerID);
		if (object == null || !(object instanceof Register)) {
			throw new MachineReaderException(getCurrentLine() + "The register attribute" + " "
					+ "of Test microinstruction \"" + name + "\" is not valid.");
		}
		Register register = (Register) object;

		String numBitsString = attrs.getValue("numBits");
		int numBits;
		try {
			numBits = Integer.parseInt(numBitsString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(getCurrentLine() + "The numBits value of " + "the "
					+ "Test microinstruction \"" + name + "\" must be an integer, not \"" + numBitsString + "\".", e);
		}
		if (numBits < 0) {
			throw new MachineReaderException(getCurrentLine() + "The numBits value of " + "the "
					+ "Test microinstruction \"" + name + "\" must be nonnegative, not " + numBits + ".");
		}

		String startString = attrs.getValue("start");
		int start;
		try {
			start = Integer.parseInt(startString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(getCurrentLine() + "The start value of the" + " "
					+ "Test microinstruction \"" + name + "\" must be an integer, not \"" + startString + "\".", e);
		}
		if (start < 0) {
			throw new MachineReaderException(getCurrentLine() + "The start value of the" + " "
					+ "Test microinstruction \"" + name + "\" must be nonnegative, not " + start + ".");
		}
		if (start + numBits > register.getWidth()) {
			throw new MachineReaderException(getCurrentLine() + "The bits of the " + "Test microinstruction \"" + name
					+ "\" are out of range for the selected register.");
		}

		String valueString = attrs.getValue("value");
		int value;
		try {
			value = Integer.parseInt(valueString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(getCurrentLine() + "The value attribute of" + " the "
					+ "Test microinstruction \"" + name + "\" must be an integer, not \"" + valueString + "\".", e);
		}

		String omissionString = attrs.getValue("omission");
		int omission;
		try {
			omission = Integer.parseInt(omissionString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(getCurrentLine() + "The omission attribute" + " of the "
					+ "Test microinstruction \"" + name + "\" must be an integer, not \"" + omissionString + "\".", e);
		}
		
		UUID id = byVersion.getUUID(attrs.getValue("id"));
		Test c = new Test(name, id, machine, register, start, numBits, Test.Operation.valueOf(comparison), value, omission);
		components.put(id, c);
		machine.getMicros(Test.class).add(c);
	}

	@SuppressWarnings("unused")
	public void startDecode(Attributes attrs) {
		String name = attrs.getValue("name");

		UUID registerID = byVersion.getUUID(attrs.getValue("ir"));
		Object object = components.get(registerID);
		if (object == null || !(object instanceof Register)) {
			throw new MachineReaderException(getCurrentLine() + "The ir attribute "
					+ "of the Decode microinstruction \"" + name + "\" is not valid.");
		}
		Register ir = (Register) object;
		
		UUID id = byVersion.getUUID(attrs.getValue("id"));
		Decode c = new Decode(name, id, machine, ir);
		components.put(id, c);
		machine.getMicros(Decode.class).add(c);
	}

	@SuppressWarnings("unused")
	public void startFileChannel(Attributes attrs) {
		String fileName = attrs.getValue("file");
		if (channelsUse(fileName)) {
			throw new MachineReaderException(getCurrentLine() + "There can be at most one channel per file."
					+ "  File \"" + fileName + "\" has two or more channels.");
		}
		UUID id = byVersion.getUUID(attrs.getValue("id"));
		FileChannel f = new FileChannel(new File(fileName));
		channels.put(id, f);
	}

	@SuppressWarnings("unused")
	public void startIO(Attributes attrs) {
		String name = attrs.getValue("name");

		IODirection direction = byVersion.getIODirection(attrs.getValue("direction"));
		IO.Type type = byVersion.getIOType(attrs.getValue("type"));

		UUID registerID = byVersion.getUUID(attrs.getValue("buffer"));
		Object object = components.get(registerID);
		if (object == null || !(object instanceof Register)) {
			throw new MachineReaderException(getCurrentLine() + "The buffer attribute "
					+ "of the IO microinstruction \"" + name + "\" is not valid" + ".");
		}
		Register buffer = (Register) object;

		IOChannel connection = channels.get(byVersion.getChannelId(attrs.getValue("connection")));

		UUID id = byVersion.getUUID(attrs.getValue("id"));
		IO c = new IO(name, id, machine, type, buffer, direction, connection);
		components.put(id, c);
		machine.getMicros(IO.class).add(c);
	}

	@SuppressWarnings("unused")
	public void startMemoryAccess(Attributes attrs) {
		String name = attrs.getValue("name");
		IODirection direction = byVersion.getIODirection(attrs.getValue("direction"));

		UUID ramID = byVersion.getUUID(attrs.getValue("memory"));
		IdentifiedObject object = components.get(ramID);
		if (object == null || !(object instanceof RAM)) {
			throw new MachineReaderException(getCurrentLine() + "The memory attribute "
					+ "of the MemoryAccess microinstruction \"" + name + "\" is not valid.");
		}
		RAM memory = (RAM) object;

		UUID registerID = byVersion.getUUID(attrs.getValue("data"));
		object = components.get(registerID);
		if (object == null || !(object instanceof Register)) {
			throw new MachineReaderException(getCurrentLine() + "The data register " + "attribute "
					+ "of the MemoryAccess microinstruction \"" + name + "\" is not valid.");
		}
		Register data = (Register) object;
		/*
		 * //This test is no longer needed since the register can be any // size
		 * and the memory cells can be any size. if ((data.getWidth() % 8) != 0)
		 * { throw new MachineReaderException( getCurrentLine() +
		 * "The data register attribute " +
		 * "of the MemoryAccess microinstruction \"" + name +
		 * "\"\nrefers to a register that does not have a width that " +
		 * "is a multiple of 8."); }
		 */

		registerID = byVersion.getUUID(attrs.getValue("address"));
		object = components.get(registerID);
		if (object == null || !(object instanceof Register)) {
			throw new MachineReaderException(getCurrentLine() + "The address register " + "attribute "
					+ "of the MemoryAccess microinstruction \"" + name + "\" is not valid.");
		}
		Register address = (Register) object;

		UUID id = byVersion.getUUID(attrs.getValue("id"));
		MemoryAccess c = new MemoryAccess(name, id, machine, direction, memory, data, address);
		components.put(id, c);
		machine.getMicros(MemoryAccess.class).add(c);
	}

	@SuppressWarnings("unused")
	public void startSetCondBit(Attributes attrs) {
		String name = attrs.getValue("name");
		String value = attrs.getValue("value");

		UUID bitID = byVersion.getUUID(attrs.getValue("bit"));
		Object object = components.get(bitID);
		if (object == null || !(object instanceof ConditionBit)) {
			throw new MachineReaderException(getCurrentLine() + "The bit attribute "
					+ "of SetCondBit microinstruction \"" + name + "\" is not valid.");
		}
		ConditionBit bit = (ConditionBit) object;

		UUID id = byVersion.getUUID(attrs.getValue("id"));
		SetCondBit c = new SetCondBit(name, id, machine, bit, Integer.valueOf(value) == 1);
		components.put(id, c);
		machine.getMicros(SetCondBit.class).add(c);
	}
	
	
	@SuppressWarnings("unused")
	public void startEnd(Attributes attrs) {
		End c = new End(machine);
		UUID id = byVersion.getUUID(attrs.getValue("id"));
		components.put(id, c);
		machine.setEnd(c);
	}

	@SuppressWarnings("unused")
	public void startComment(Attributes attrs) {
		String name = attrs.getValue("name");
		UUID id = byVersion.getUUID(attrs.getValue("id"));
		Comment c = new Comment(name, id, machine);
		components.put(id, c);
	}

	@SuppressWarnings("unused")
	public void startFetchSequence(Attributes attrs) {
		currentInstruction = machine.getFetchSequence()
				.orElseThrow(() -> new MachineReaderException("No Fetch Sequence set"));
	}

	@SuppressWarnings("unused")
	public void startMachineInstruction(Attributes attrs) {
		// validate the name attribute
		String name = attrs.getValue("name");
		try {
			Validate.nameIsValidAssembly(name, chars);
		} catch (ValidationException e) {
			throw new MachineReaderException(getCurrentLine() + e.getMessage(), e);
		}

		// validate the opcode string
		opcodeString = attrs.getValue("opcode");
		final String opcodeLine = getCurrentLine();
		long opcode;
		try {
			opcode = Long.parseLong(opcodeString, 16);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(
					getCurrentLine() + "The opcode value " + "of the machine instruction\n\"" + name + "\" must be a"
							+ " hexadecimal integer of at most 16 hex characters,\n" + "not \"" + opcodeString + "\".", e);
		}
		if (opcodeString.charAt(0) == '-') {
			throw new MachineReaderException(getCurrentLine() + "The opcode value " + "of the machine instruction\n \""
					+ name + "\" must be a" + " nonnegative hexadecimal integer" + ", not \"" + opcodeString + "\".");
		}

		// get the format
		currentFormat = attrs.getValue("format");
		String currentInstructionFormat = attrs.getValue("instructionFormat");
		String currentAssemblyFormat = attrs.getValue("assemblyFormat");
		String instrColors = attrs.getValue("instructionColors");
		String assemblyColors = attrs.getValue("assemblyColors");

		/*
		 * For backward compatability, I need to deal with 3 possible formats of
		 * instructions. The original format was just a list of field lengths.
		 * The next version had a format string consisting of names of Fields
		 * separated by spaces. The newest version has separate assembly and
		 * machine formats and colors as well.
		 */
		if (currentFormat == null && currentInstructionFormat == null) {
			// version 1: no format and instead a list of FieldLengths
			// The format will be set in startFieldLength and then resolved in
			// endMachineInstruction
			currentFormat = "";
			currentInstruction = new MachineInstruction(name, UUID.randomUUID(), machine, opcode,
					null, null);
		} else if (currentInstructionFormat == null) {
			// version 2: A single format for machine & assembly instructions
			List<Field> fields = ConvertStrings.formatStringToFields(currentFormat, machine);
			currentInstruction = new MachineInstruction(name, UUID.randomUUID(), machine, opcode,
					fields, fields);
		} else {
			// version 3: separate formats for machine & assembly instructions
			
			currentInstruction = new MachineInstruction(name, UUID.randomUUID(), machine,
					opcode,
					Convert.formatStringToFields(currentInstructionFormat, machine),
					Convert.formatStringToFields(currentAssemblyFormat, machine));
		}

		machine.getInstructions().add(currentInstruction);
	}

	@SuppressWarnings("unused")
	public void endMachineInstruction() {
		try {
			if (currentInstruction.getInstructionFields().size() == 0) {
				// it must be the old version where there were only FieldLengths,
				// so, we split the current format by spaces, then convert them into new Fields
				// as we find them, attempting to reuse as possible
				List<Field> instFields = Splitter.on(CharMatcher.whitespace())
						.omitEmptyStrings()
						.splitToList(currentFormat)
						.stream()
						.map(fieldName ->
							fields.computeIfAbsent(fieldName, key -> {
								int fieldLength = Math.abs(Integer.parseInt(fieldName));
								Field field = new Field(fieldName, UUID.randomUUID(), machine,
										fieldLength, Field.Relativity.absolute,
										FXCollections.observableArrayList(), 0,
										Field.SignedType.Signed, Type.required);

								machine.getFields().add(field);
								return field;
							}))
						.collect(Collectors.toList());

				// put the instruction fields it into currentInstruction
				currentInstruction.setInstructionFields(instFields);
				currentInstruction.setAssemblyFields(instFields);
				// FIXME colours https://github.com/Colby-CPU-Sim/CPUSimFX2015/issues/109#109
//				currentInstruction.setInstructionColors(newInstr.getInstructionColors());
//				currentInstruction.setAssemblyColors(newInstr.getAssemblyColors());
			}

			Validate.fieldsListIsNotEmpty(currentInstruction);
			Validate.opcodeFits(currentInstruction);
			Validate.firstFieldIsProper(currentInstruction);
			Validate.fieldLengthsAreAtMost64(currentInstruction);
			// Validate.atMostOnePosLengthFieldIsOptional(currentInstruction);
		} catch (ValidationException e) {
			throw new MachineReaderException(
					(locator == null ? "" : "The error is in " + "the MachineInstruction" + " element ending at line ")
							+ locator.getLineNumber() + ".\n" + e.getMessage(), e);
		}
	}

	@SuppressWarnings("unused")
	public void startFieldLength(Attributes attrs) {
		String lengthString = attrs.getValue("length");
		int length;
		try {
			length = Integer.parseInt(lengthString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(getCurrentLine() + "Field lengths of" + " machine instruction \""
					+ currentInstruction.getName() + "\" must be an integer, not \"" + lengthString + "\".", e);
		}
		if (length == 0) {
			throw new MachineReaderException(getCurrentLine() + "Field lengths of" + " machine instruction \""
					+ currentInstruction.getName() + "\" must not be zero.  One is " + length + ".");
		}
		currentFormat += length + " ";
	}

	@SuppressWarnings("unused")
	public void startMicroinstruction(Attributes attrs) {
		UUID microID = byVersion.getUUID(attrs.getValue("microRef"));
		Object object = components.get(microID);
		if (object == null || !(object instanceof Microinstruction)) {
			throw new MachineReaderException(getCurrentLine() + "The microRef attribute \"" + microID
					+ "\" of one of \nthe microinstructions for the " + "instruction \"" + currentInstruction.getName()
					+ "\" is not valid.");
		}
		Microinstruction micro = (Microinstruction) object;
		currentInstruction.getMicros().add(micro);
	}

	@SuppressWarnings("unused")
	public void startEQU(Attributes attrs) {
		String name = attrs.getValue("name");
		String valueString = attrs.getValue("value");
		long value;
		try {
			value = Long.parseLong(valueString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(getCurrentLine() + "The value of EQU \"" + name
					+ "\" must be an integer, not \"" + valueString + "\".", e);
		}
		machine.getEQUs().add(new EQU(name, value));
	}

	@SuppressWarnings("unused")
	public void startRegisterRAMPair(Attributes attrs) {
		UUID ramID = byVersion.getUUID(attrs.getValue("ram"));
		Object object = components.get(ramID);
		if (object == null || !(object instanceof RAM)) {
			throw new MachineReaderException(
					getCurrentLine() + "The ram attribute \"" + ramID + "\" of a RegisterRAMPair is not valid.");
		}
		RAM ram = (RAM) object;

		UUID registerID = byVersion.getUUID(attrs.getValue("register"));
		object = components.get(registerID);
		if (object == null || !(object instanceof Register)) {
			throw new MachineReaderException(getCurrentLine() + "The register attribute" + " \"" + registerID
					+ "\"of a RegisterRAMPair is not valid.");
		}
		Register register = (Register) object;

		String dynamicString = attrs.getValue("dynamic");
		boolean dynamic = "true".equals(dynamicString);

		registerRAMPairs.add(new RegisterRAMPair(register, ram, dynamic));
	}

	@SuppressWarnings("unused")
	public void startLoadingInfo(Attributes attrs) {
		UUID ramID = byVersion.getUUID(attrs.getValue("ram"));
		Object object = components.get(ramID);
		if (object == null || !(object instanceof RAM)) {
			throw new MachineReaderException(getCurrentLine() + "The ram attribute \"" + ramID
					+ "\" of a LoadingInfo element is not a valid RAM " + "ID.");
		}
		RAM ram = (RAM) object;

		String addressString = attrs.getValue("startingAddress");
		int startAddress;
		try {
			startAddress = Integer.parseInt(addressString);
		} catch (NumberFormatException e) {
			throw new MachineReaderException(
					getCurrentLine() + "The starting address " + "must be an integer, not \"" + addressString + "\".", e);
		}
		if (startAddress < 0) {
			throw new MachineReaderException(
					getCurrentLine() + "The starting address " + startAddress + "\" must be a nonnegative integer.");
		}

		machine.setCodeStore(ram);
		machine.setStartingAddressForLoading(startAddress);
	}

	@SuppressWarnings("unused")
	public void startIndexingInfo(Attributes attrs) {
		String indexFromRightString = attrs.getValue("indexFromRight");
		boolean indexFromRight = Boolean.parseBoolean(indexFromRightString);
		machine.setIndexFromRight(indexFromRight);
	}

	@SuppressWarnings("unused")
	public void startProgramCounterInfo(Attributes attrs) {
		UUID pcID = byVersion.getUUID(attrs.getValue("programCounter"));
		Object object = components.get(pcID);
		if (object == null || !(object instanceof Register)) {
			throw new MachineReaderException(getCurrentLine() + "The program counter" + " attribute \"" + pcID
					+ "\" of a ProgramCounterInfo element is not a valid Register ID.");
		}
		Register pc = (Register) object;
		machine.setProgramCounter(pc);
	}
	
	// returns true if the fileName is the file associated with
	// one of the FileChannels in the channels HashMap
	private boolean channelsUse(String fileName) {
		final File findFile = new File(fileName);

		Optional<?> opt = channels.values().stream()
				.filter(c -> c instanceof FileChannel)
				.map(c -> (FileChannel)c)
				.filter(fc -> fc.getFile().equals(findFile))
				.findAny();
		return opt.isPresent();
	}

}