package cpusim.model;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import cpusim.model.microinstruction.Comment;
import cpusim.model.microinstruction.Microinstruction;
import cpusim.model.util.Colors;
import cpusim.model.util.Copyable;
import cpusim.model.util.IdentifiedObject;
import cpusim.model.util.LegacyXMLSupported;
import cpusim.model.util.NamedObject;
import cpusim.model.util.conversion.ConvertLongs;
import cpusim.model.util.conversion.ConvertStrings;
import cpusim.model.util.units.ArchType;
import cpusim.model.util.units.ArchValue;
import cpusim.xml.HTMLEncodable;
import cpusim.xml.HtmlEncoder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This file contains the class for Machine Instructions created using CPU Sim
 *
 * @since 1999-06-01
 */
public class MachineInstruction
        implements IdentifiedObject, NamedObject,
                    LegacyXMLSupported, HTMLEncodable, Copyable<MachineInstruction> {
    
    private String name;				//the name of the machine instruction
    private final UUID id;
    private ObservableList<Microinstruction<?>> micros;	//all the microinstructions
    private long opcode;					//the opcode of the instruction
    private Machine machine;
    private String format; //the format String matching the list of fields
    private List<String> instructionColors;
    private List<Field> assemblyFields;
    private List<Field> instructionFields;
    private List<String> assemblyColors;

    /*
     * CLASS INVARIANT:  Except when the InstructionDialog is open,
     * the format string must correspond to a legal list of fields.
     * More precisely, Machine.getFieldsFromFormat(format) must produce the list
     * of fields.
     */

    public MachineInstruction(String name, final UUID id, Machine machine, long opcode, String format)
    {
        this(name, id, machine,
                Lists.newArrayList(ConvertStrings.formatStringToFields(format, machine)),
                Lists.newArrayList(ConvertStrings.formatStringToFields(format, machine)),
                opcode,
                Colors.generateTwoListsOfRandomLightColors(
                    Splitter.on(' ')
                            .omitEmptyStrings()
                            .splitToList(format).size()));
    }
    
    public MachineInstruction(String name, final UUID id, Machine machine, List<Field> assemblyFields, List<Field> instructionFields, long opcode,
                              List<List<String>> colors){
        this(name, IdentifiedObject.generateRandomID(), machine, assemblyFields,
                colors.get(0), colors.get(1), opcode, instructionFields);
    }
    
    public MachineInstruction(String name,
                              final UUID id,
                              Machine machine,
                              List<Field> newAssemblyFields,
                              List<String> newInstrColors,
                              List<String> newAssemblyColors,
                              long opcode,
                              List<Field> newInstrFields)
    {
        this.name = name;
        this.id = id;
        this.opcode = opcode;
        this.micros = FXCollections.observableArrayList();
        this.machine = machine;
        this.assemblyFields = newAssemblyFields;
        this.instructionFields = newInstrFields;
        this.assemblyColors = newAssemblyColors;
        this.instructionColors = newInstrColors;
        
        //for backwards compatibility
        List<Field> fieldsToRemove = new ArrayList<>();
        for (Field field : this.assemblyFields){
            if (field.getType() == Field.Type.ignored){
                fieldsToRemove.add(field);
            }
        }
        
        for (Field field : fieldsToRemove){
            this.assemblyColors.remove(this.assemblyFields.indexOf(field));
            this.assemblyFields.remove(field);
        }
    }
    
    public MachineInstruction(final MachineInstruction other){
        this(other.name, IdentifiedObject.generateRandomID(), other.machine, other.assemblyFields, other.instructionColors, other.assemblyColors, other.opcode, other.instructionFields
        );
    }

    //===================================
    // getters and setters
    
    
    @Override
    public UUID getID() {
        return this.id;
    }
    
    public Machine getMachine() {
        return machine;
    }

    public String getFormat()
    {
        return format;
    }

    public List<Field> getAssemblyFields(){
        return assemblyFields;
    }

    /**
     * This method checks the validity of the format for the instruction.
     * If it is valid, it updates the fields to match this format.
     * If it is not valid, it prints an error message.
     * This method is the only way to change the fields of this instruction.
     * @param instructionFields A List of the Fields of the instruction
     */
    public void setInstructionFields(List<Field> instructionFields)
    {
        this.instructionFields = instructionFields;

    }
    
    public List<Field> getInstructionFields(){
        return instructionFields;
    }
    
    public void setInstructionColors(List<String> instructionColors){
        this.instructionColors = instructionColors;
    }
    
    public List<String> getInstructionColors(){
        return this.instructionColors;
    }
    
    public void setAssemblyColors(List<String> assemblyColors){
        this.assemblyColors = assemblyColors;
    }
    
    public List<String> getAssemblyColors(){
        return this.assemblyColors;
    }
    
    /**
     * returns the number of bits in the machine instruction
     * @return the number of bits in the machine instruction
     */
    public ArchValue getNumBits(){
        int numBits = 0;
        for (Field field : this.instructionFields){
            numBits += field.getNumBits();
        }
        
        return ArchType.Bit.of(numBits);
    }

    /**
     * @return an array of all the field lengths that are positive.
     */
    public List<Integer> getPositiveFieldLengths()
    {
        return getInstructionFields().stream()
        		.mapToInt(Field::getNumBits)
        		.filter(len -> len < 0)
        		.boxed()
        		.collect(Collectors.toList());
    }

    /**
     * @return an array of booleans indicating whether each positive-length
     * field is supposed to contain signed values or not.
     */
    public boolean[] getPosLenFieldSigns()
    {
        List<Field> fields = getInstructionFields();
        int size = 0;
        for(Field field : fields)
            if(field.getNumBits() > 0)
                size++;
        boolean[] isSigned = new boolean[size];
        int j = 0;
        for (Field field : fields)
            if (field.getNumBits() > 0) {
                isSigned[j] = field.isSigned();
                j++;
            }

        return isSigned;
    }
    
    /**
     * returns an array of integers that holds the indices of the non-opcode fields in the order
     * the code generator should copy them into the code as dictated.
     * For example, if the fifth instruction field corresponds to the first assembly 
     * instruction, the first value in the relative positions array will be 4 so that
     * the field read in first by the assembler will be put into the fifth position in
     * the binary machine instruction
     * @return relative positions array
     */
    public int[] getRelativeOrderOfFields(){
        
        //get all instruction colors correponding to positive length fields that arent
        //the opcode
        List<String> iColors = new ArrayList<>(instructionColors);
        iColors.remove(0);

        //get all assembly colors correponding to positive length fields that arent
        //the opcode
        List<String> aColors = new ArrayList<>();
        		
        int i = 0;
        for(Field field : assemblyFields){
            if(field.getNumBits() > 0){
                aColors.add(assemblyColors.get(i));
            }
            i++;
        }
        aColors.remove(0);
        
        //add any colors that correspond to ignored fields at the proper index
        i = 0;
        for (String color : iColors){
            if (!aColors.contains(color)){
                aColors.add(i, color);
            }
            i++;
        }
        
        //fromRootController the relative positions of the instruction fields compared to the
        //assembly fields
        int[] relativePositions = new int[aColors.size()];
        i = 0;
        for (String color : iColors){
            relativePositions[i] = aColors.indexOf(color);
            i++;
        }
        
        return relativePositions;
    }
    
    public void setAssemblyFields(List<Field> assemblyFields){
        this.assemblyFields = assemblyFields;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName(String newName)
    {
        name = newName;
    }

    public ObservableList<Microinstruction<?>> getMicros()
    {
        return micros;
    }

    public void setMicros(ObservableList<Microinstruction<?>> v)
    {
        micros = v;
    }

    public long getOpcode()
    {
        return opcode;
    }

    public void setOpcode(long newOpcode)
    {
        opcode = newOpcode;
    }

    public void setMachine(Machine machine)
    {
        this.machine = machine;
    }

    //===================================
    // other utility methods

    public String toString()
    {
        return name;
    }
    

    //-----------------------------------
    //returns length in bits
    public int length()
    {
        int length = 0;
        for (Field field : getInstructionFields()) {
            length += field.getNumBits();
        }
        return length;
    }

    //-----------------------------------
    // returns true if m is in the micros list
    public boolean usesMicro(Microinstruction m)
    {
        return micros.contains(m);
    }

    //-----------------------------------
    // deletes every occurence of m in the micros list
    public void removeMicro(Microinstruction m)
    {
        boolean didRemoval = micros.remove(m);
        if (didRemoval)
            removeMicro(m); //recursively call to remove more occurences of m
    }

    //-----------------------------------
    // returns true if f is in the fields list
    public boolean usesField(Field f)
    {
        return getInstructionFields().contains(f);
    }
    
    

    //-----------------------------------
    @Override
    public String getXMLDescription(String indent)
    {
        String nl = System.getProperty("line.separator");
        String result = indent + "<MachineInstruction name=\"" +
                HtmlEncoder.sEncode(getName()) + "\" opcode=\"" +
                Long.toHexString(getOpcode()) + "\" instructionFormat=\"" + 
                ConvertStrings.fieldsToFormatString(instructionFields) + "\" assemblyFormat=\"" +
                ConvertStrings.fieldsToFormatString(assemblyFields) + "\" instructionColors=\"" + 
                Colors.toXML(instructionColors)+ "\" assemblyColors=\"" + 
                Colors.toXML(assemblyColors)+ "\" >" + nl;
        for (Microinstruction micro : micros) {
            result += indent + "\t<Microinstruction microRef=\"" +
                    micro.getID() +
                    "\" />" + nl;
        }
        result += indent + "</MachineInstruction>";
        return result;
    }

	@Override
	public String getHTMLDescription(String indent) {
		String instrFormat = "<table width=\"100%\"><tr>";
        String assembFormat = "<table width=\"100%\"><tr>";
        
        double length = 0.0;
        for(Field field : instructionFields){
            length += (double)field.getNumBits();
        }
        
        for (int i=0; i<instructionFields.size(); i++){
            instrFormat += "<td align=\"center\" width=\""+Math.rint((((double)
                    instructionFields.get(i).getNumBits())/length)*100)+"%\" bgcolor=\""
                    +instructionColors.get(i)+
                    "\">"+ instructionFields.get(i).getName()+"</td>";
        }
        
        instrFormat += "</tr></table>";
        
        for (int i=0; i<assemblyFields.size(); i++){
            assembFormat += "<td align=\"center\" width=\""+Math.rint((1.0
                    /(double)assemblyFields.size())*100)+"%\" bgcolor=\""
                    +assemblyColors.get(i)+
                    "\">"+ assemblyFields.get(i).getName()+"</td>";
        }
        
        assembFormat += "</tr></table>";
        
        String result = "<TR><TD>" + HtmlEncoder.sEncode(getName()) +
                "</TD><TD>" + ConvertLongs.toHexString(getOpcode(),
                        getInstructionFields().get(0).getNumBits()) +
                "</TD><TD>" + instrFormat + "</TD><TD>" + assembFormat;
        result += "</TD><TD>";
        for (int i = 0; i < getMicros().size(); i++) {
            Microinstruction micro = getMicros().get(i);
            String htmlName = micro.getHTMLName();
            if( micro instanceof Comment)
                htmlName = "<em><font color=gray>" + htmlName + "</em></font>";
            result += htmlName + "<BR>";
        }
        return indent + result + "</TD></TR>";
	}

	@Override
	public <U extends MachineInstruction> void copyTo(U other) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method, copyTo");
	}
	
} // end class MachineInstruction
