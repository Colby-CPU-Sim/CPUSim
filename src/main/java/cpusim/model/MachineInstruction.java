///////////////////////////////////////////////////////////////////////////////
// File:    	MachineInstruction.java
// Type:    	java application file
// Author:		Dale Skrien
// Project: 	CPU Sim
// Date:    	June, 1999
//
// Description:
//   This file contains the class for Machine Instructions created using CPU Sim


///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

/*
 * Michael Goldenberg, Ben Borchard, and Jinghui Yu made the following changes 
 * 
 * on 11/6/13
 * 
 * 1.) Made it so that when the ignored fields are removed from the assemblyFields
 * ArrayList, the
 * corresponding colors are removed from the assemblyColors array so that all the
 * colors for
 * the instruction and assembly fields match up and so that there aren't more
 * assemblyColors than
 * there are assembly Fields
 * 2.) After making the first change, we needed to make sure that the instructionColor
 * ArrayList
 * and the assemblyFields ArrayList were not pointing to the same ArrayList, so we
 * modified
 * one of the overloaded constructors so that two seperate ArrayLists that are identical 
 * in what they contain are assigned to assemblyColors and instructionColors
 * 
 * on 11/11/13
 * 
 * 1.) Created the method getRelativeOrderOfFields() so that the fields will be in the
 * proper order as dictated by the order of the instructionFields and the assemblyFields
 * 2.) Created a getNumBits() method that determines the number of bits in the machine
 * instruction
 * based on the instruction fields
 */

package cpusim.model;

import cpusim.model.microinstruction.Comment;
import cpusim.util.Convert;
import cpusim.util.NamedObject;
import cpusim.xml.HtmlEncoder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

///////////////////////////////////////////////////////////////////////////////
// the MachineInstruction class

public class MachineInstruction implements Cloneable, NamedObject
{

    private String name;                //the name of the machine instruction
    private ObservableList<Microinstruction> micros;    //all the microinstructions
    private long opcode;                    //the opcode of the instruction
    private Machine machine;
    private String format; //the format String matching the list of fields
    private ArrayList<String> instructionColors;
    private ArrayList<Field> assemblyFields;
    private ArrayList<Field> instructionFields;
    private ArrayList<String> assemblyColors;

    /*
     * CLASS INVARIANT:  Except when the InstructionDialog is open,
     * the format string must correspond to a legal list of fields.
     * More precisely, Machine.getFieldsFromFormat(format) must produce the list
     * of fields.
     */

    //===================================
    // constructors

    //for backward compatibility
    public MachineInstruction(String name, long opcode, int[] fieldLengths, Machine
            machine) {
        this(name, opcode, Convert.lengths2Format(fieldLengths), machine);
    }

    public MachineInstruction(String name, long opcode, String format, Machine machine) {
        this(name, opcode, Convert.formatStringToFields(format, machine), Convert
                .formatStringToFields(format, machine), Convert
                .generateTwoListsOfRandomLightColors(format.split(" ").length), machine);
    }

    public MachineInstruction(String name, long opcode, ArrayList<Field>
            instructionFields, ArrayList<Field> assemblyFields,
                              ArrayList<ArrayList<String>> colors, Machine machine) {
        this(name, opcode, instructionFields, assemblyFields, colors.get(0), colors.get
                (1), machine);
    }

    public MachineInstruction(String name, long opcode, ArrayList<Field>
            instructionFields, ArrayList<Field> assemblyFields, ArrayList<String>
            instructionColor, ArrayList<String> assemblyColor, Machine machine) {
        this.name = name;
        this.opcode = opcode;
        this.micros = FXCollections.observableArrayList();
        this.machine = machine;
        this.assemblyFields = assemblyFields;
        this.instructionFields = instructionFields;
        this.assemblyColors = assemblyColor;
        this.instructionColors = instructionColor;

        //for backwards compatibility
        ArrayList<Field> fieldsToRemove = new ArrayList<>();
        for (Field field : this.assemblyFields) {
            if (field.getType() == Field.Type.ignored) {
                fieldsToRemove.add(field);
            }
        }
        for (Field field : fieldsToRemove) {
            this.assemblyColors.remove(this.assemblyFields.indexOf(field));
            this.assemblyFields.remove(field);
        }
    }

    //===================================
    // getters and setters

    public Machine getMachine() {
        return machine;
    }

    public String getFormat() {
        return format;
    }

    public ArrayList<Field> getAssemblyFields() {
        return assemblyFields;
    }

    /**
     * This method checks the validity of the format for the instruction.
     * If it is valid, it updates the fields to match this format.
     * If it is not valid, it prints an error message.
     * This method is the only way to change the fields of this instruction.
     *
     * @param instructionFields A List of the Fields of the instruction
     */
    public void setInstructionFields(ArrayList<Field> instructionFields) {
        this.instructionFields = instructionFields;

    }

    public ArrayList<Field> getInstructionFields() {
        return instructionFields;
    }

    public void setInstructionColors(ArrayList<String> instructionColors) {
        this.instructionColors = instructionColors;
    }

    public ArrayList<String> getInstructionColors() {
        return this.instructionColors;
    }

    public void setAssemblyColors(ArrayList<String> assemblyColors) {
        this.assemblyColors = assemblyColors;
    }

    public ArrayList<String> getAssemblyColors() {
        return this.assemblyColors;
    }

    /**
     * returns the number of bits in the machine instruction
     *
     * @return the number of bits in the machine instruction
     */
    public int getNumBits() {
        int numBits = 0;
        for (Field field : this.instructionFields) {
            numBits += field.getNumBits();
        }
        return numBits;
    }

    /**
     * returns the default value of the instruction field with the given index
     * @param fieldIndex the index of the field of interest
     * @return the default value of that field
     */
    public long getDefaultValue(int fieldIndex) {
        return instructionFields.get(fieldIndex).getDefaultValue();
    }

    /**
     * @return an array of all the field lengths.
     */
    public int[] getPositiveFieldLengths() {
        List<Field> fields = getInstructionFields();
        // TODO:  Delete this next loop after testing to ensure I understand correctly
        for (Field field : fields)
            assert field.getNumBits() > 0;
        int[] lengths = new int[fields.size()];
        int j = 0;
        for (Field field : fields) {
            lengths[j] = field.getNumBits();
            j++;
        }

        return lengths;
    }

    /**
     * @return an array of booleans indicating whether each positive-length
     * field is supposed to contain signed values or not.
     */
    public boolean[] getPosLenFieldSigns() {
        List<Field> fields = getInstructionFields();
        boolean[] isSigned = new boolean[fields.size()];
        int j = 0;
        for (Field field : fields) {
            isSigned[j] = field.isSigned();
            j++;
        }

        return isSigned;
    }

    /**
     * returns an array of integers that holds the indices of the assembly operands
     * that correspond to the instruction fields.  That is, the array that is returned
     * could be called "instrIndicesToAssmIndices".  For each slot i in the array,
     * the value of the slot is the index of the assembly operand that matches
     * the i-th field of the machine instruction.
     *
     * @return relative positions array
     */
    public int[] getRelativeOrderOfFields() {
        int[] relativePositions = new int[instructionColors.size()];
        for (int i = 0; i < relativePositions.length; i++) {
            relativePositions[i] = assemblyColors.indexOf(instructionColors.get(i));
        }

        return relativePositions;
    }

    public void setAssemblyFields(ArrayList<Field> assemblyFields) {
        this.assemblyFields = assemblyFields;
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        name = newName;
    }

    public ObservableList<Microinstruction> getMicros() {
        return micros;
    }

    public void setMicros(ObservableList<Microinstruction> v) {
        micros = v;
    }

    public long getOpcode() {
        return opcode;
    }

    public void setOpcode(long newOpcode) {
        opcode = newOpcode;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    //===================================
    // other utility methods

    public String toString() {
        return name;
    }

    //-----------------------------------
    // clone:  returns a copy of this instruction that shares microinstructions
    // but creates a new microList vector and a new fields list.
    public Object clone() {
        MachineInstruction theClone = new MachineInstruction(name, opcode, format,
                machine);
        //noinspection unchecked
        theClone.setMicros((ObservableList<Microinstruction>) ((ArrayList) micros)
                .clone());
        return theClone;
    }

    //-----------------------------------
    //returns length in bits
    public int length() {
        int length = 0;
        for (Field field : getInstructionFields()) {
            length += field.getNumBits();
        }
        return length;
    }

    //-----------------------------------
    // returns true if m is in the micros list
    public boolean usesMicro(Microinstruction m) {
        return micros.contains(m);
    }

    //-----------------------------------
    // deletes every occurrence of m in the micros list
    public void removeMicro(Microinstruction m) {
        boolean didRemoval = micros.remove(m);
        if (didRemoval) {
            removeMicro(m); //recursively call to remove more occurences of m
        }
    }

    //-----------------------------------
    // returns true if f is in the fields list
    public boolean usesField(Field f) {
        return getInstructionFields().contains(f);
    }


    //-----------------------------------
    public String getXMLDescription(String indent) {
        String nl = System.getProperty("line.separator");
        String result = indent + "<MachineInstruction name=\"" + HtmlEncoder.sEncode
                (getName()) + "\" opcode=\"" + Long.toHexString(getOpcode()) + "\" " +
                "instructionFormat=\"" + Convert.fieldsToFormatString
                (instructionFields) + "\" assemblyFormat=\"" + Convert
                .fieldsToFormatString(assemblyFields) + "\" instructionColors=\"" +
                Convert.colorsListToXML(instructionColors) + "\" assemblyColors=\"" +
                Convert.colorsListToXML(assemblyColors) + "\" >" + nl;
        for (Microinstruction micro : micros) {
            result += indent + "\t<Microinstruction microRef=\"" + micro.getID() + "\" " +
                    "/>" + nl;
        }
        result += indent + "</MachineInstruction>";
        return result;
    }

    //--------------------------------------
    public String getHTMLDescription() {
        String instrFormat = "<table width=\"100%\"><tr>";
        String assembFormat = "<table width=\"100%\"><tr>";

        double length = 0.0;
        for (Field field : instructionFields) {
            length += (double) field.getNumBits();
        }

        for (int i = 0; i < instructionFields.size(); i++) {
            instrFormat += "<td align=\"center\" width=\"" + Math.rint((((double)
                    instructionFields.get(i).getNumBits()) / length) * 100) + "%\" " +
                    "bgcolor=\"" + instructionColors.get(i) + "\">" + instructionFields
                    .get(i).getName() + "</td>";
        }

        instrFormat += "</tr></table>";

        for (int i = 0; i < assemblyFields.size(); i++) {
            assembFormat += "<td align=\"center\" width=\"" + Math.rint((1.0 / (double)
                    assemblyFields.size()) * 100) + "%\" bgcolor=\"" + assemblyColors
                    .get(i) + "\">" + assemblyFields.get(i).getName() + "</td>";
        }

        assembFormat += "</tr></table>";

        String result = "<TR><TD>" + HtmlEncoder.sEncode(getName()) + "</TD><TD>" +
                Convert.fromLongToHexadecimalString(getOpcode(), getInstructionFields()
                        .get(0).getNumBits()) + "</TD><TD>" + instrFormat + "</TD><TD>"
                + assembFormat;
        result += "</TD><TD>";
        for (int i = 0; i < getMicros().size(); i++) {
            Microinstruction micro = getMicros().get(i);
            String htmlName = micro.getHTMLName();
            if (micro instanceof Comment) {
                htmlName = "<em><font color=gray>" + htmlName + "</em></font>";
            }
            result += htmlName + "<BR>";
        }
        return result + "</TD></TR>";
    }
} // end class MachineInstruction
