package cpusim.model;

import com.google.common.base.MoreObjects;
import cpusim.model.microinstruction.Comment;
import cpusim.model.microinstruction.Microinstruction;
import cpusim.model.util.*;
import cpusim.model.util.conversion.ConvertLongs;
import cpusim.model.util.conversion.ConvertStrings;
import cpusim.model.util.units.ArchType;
import cpusim.model.util.units.ArchValue;
import cpusim.xml.HTMLEncodable;
import cpusim.xml.HtmlEncoder;
import javafx.beans.property.*;
import javafx.beans.property.adapter.JavaBeanLongPropertyBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This file contains the class for Machine Instructions created using CPU Sim
 *
 * @since 1999-06-01
 */
public class MachineInstruction
        implements IdentifiedObject
                , NamedObject
                , LegacyXMLSupported
                , HTMLEncodable
                , ReadOnlyMachineBound
                , MachineComponent
                , Copyable<MachineInstruction> {
    
    private final StringProperty name;				//the name of the machine instruction
    private final ReadOnlyObjectProperty<UUID> id;

    @DependantComponent
    private final ListProperty<Microinstruction<?>> micros;	//all the microinstructions

    /**
     * the opcode of the instruction
     */
    private final LongProperty opcode;
    private final Machine machine;

    @DependantComponent
    private final ListProperty<Field> instructionFields;
    @DependantComponent
    private final ListProperty<Field> assemblyFields;

    private final ReadOnlySetProperty<MachineComponent> dependants;

    /**
     * Create a new {@link MachineInstruction}.
     *  @param name Name (see {@link NamedObject#nameProperty()}
     * @param id Identifier (see {@link IdentifiedObject#idProperty()}
     * @param opcode Start value for the opcode
     * @param instructionFields Instruction layout
     * @param assemblyFields Assembly layout
     */
    public MachineInstruction(String name,
                              UUID id,
                              Machine machine,
                              long opcode,
                              @Nullable List<Field> instructionFields,
                              @Nullable List<Field> assemblyFields) {
        this.name = new SimpleStringProperty(this, "name", checkNotNull(name));
        this.id = new SimpleObjectProperty<>(this, "id", checkNotNull(id));
        this.machine = machine;
        
        this.opcode = new SimpleLongProperty(this, "opcode", opcode);
        
        this.micros = new SimpleListProperty<>(this, "micros", FXCollections.observableArrayList());

        this.instructionFields = new SimpleListProperty<>(this, "instructionFields",
                FXCollections.observableArrayList());
        this.assemblyFields = new SimpleListProperty<>(this, "assemblyFields",
                FXCollections.observableArrayList());

        if (instructionFields != null) {
            this.instructionFields.addAll(instructionFields);
        }

        if (assemblyFields != null) {
            this.assemblyFields.addAll(assemblyFields);
        }

        this.dependants = MachineComponent.collectDependancies(this)
                .buildSet(this, "dependencies");
    }

    /**
     * Copy constructor
     * @param other instance to copy
     */
    public MachineInstruction(final MachineInstruction other) {
        this(other.name.getValue(), UUID.randomUUID(), other.getMachine(),
                other.opcode.get(), other.instructionFields, other.assemblyFields);
    }

    //===================================
    // getters and setters


    @Override
    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public ReadOnlyProperty<UUID> idProperty() {
        return id;
    }

    @Override
    public ReadOnlyObjectProperty<Machine> machineProperty() {
        return new ReadOnlyObjectWrapper<>(machine);
    }

    @Override
    public ReadOnlySetProperty<MachineComponent> getDependantComponents() {
        return dependants;
    }

    public ObservableList<Field> getAssemblyFields(){
        return assemblyFields;
    }

    public void setAssemblyFields(List<Field> assemblyFields){
        this.assemblyFields.clear();
        this.assemblyFields.addAll(assemblyFields);
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
        checkNotNull(instructionFields);

        this.instructionFields.clear();
        this.instructionFields.addAll(instructionFields);

    }
    
    public ObservableList<Field> getInstructionFields(){
        return instructionFields;
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

        // FIXME Why do the colours matter? KB
//        //get all instruction colors correponding to positive length fields that arent
//        //the opcode
//        List<String> iColors = new ArrayList<>(instructionColors);
//        iColors.remove(0);
//
//        //get all assembly colors correponding to positive length fields that arent
//        //the opcode
//        List<String> aColors = new ArrayList<>();
//
//        int i = 0;
//        for(Field field : assemblyFields){
//            if(field.getNumBits() > 0){
//                aColors.add(assemblyColors.get(i));
//            }
//            i++;
//        }
//        aColors.remove(0);
//
//        //add any colors that correspond to ignored fields at the proper index
//        i = 0;
//        for (String color : iColors){
//            if (!aColors.contains(color)){
//                aColors.add(i, color);
//            }
//            i++;
//        }
//
//        //fromRootController the relative positions of the instruction fields compared to the
//        //assembly fields
//        int[] relativePositions = new int[aColors.size()];
//        i = 0;
//        for (String color : iColors){
//            relativePositions[i] = aColors.indexOf(color);
//            i++;
//        }
//
        return new int[]{ 0, 1 };
    }

    public ObservableList<Microinstruction<?>> getMicros()
    {
        return micros;
    }

    public void setMicros(List<? extends Microinstruction<?>> v) {
        micros.clear();
        micros.addAll(v);
    }

    public long getOpcode()
    {
        return opcode.get();
    }

    public void setOpcode(long newOpcode) {
        opcode.setValue(newOpcode);
    }

    public LongProperty opcodeProperty() {
        return opcode;
    }

    //===================================
    // other utility methods

    @Override
    public String toString() {
        MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(getClass())
                .addValue(getID())
                .add("name", getName())
                .add("machine", getMachine().getName())
                .add("opcode", getOpcode())
                .add("micros", String.format("%s[%d]", Microinstruction.class.getSimpleName(), micros.size()))
                .add("assembly", String.format("%s[%d]", Field.class.getSimpleName(), assemblyFields.size()))
                .add("instructions", String.format("%s[%d]", Field.class.getSimpleName(), instructionFields.size()));

        return helper.toString();
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
    public void removeMicro(Microinstruction m) {
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
                ConvertStrings.fieldsToFormatString(assemblyFields) + "\" >" + nl;
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
                    instructionFields.get(i).getNumBits())/length)*100)+"%\">"
                    + instructionFields.get(i).getName()+"</td>";
        }
        
        instrFormat += "</tr></table>";
        
        for (int i=0; i<assemblyFields.size(); i++){
            assembFormat += "<td align=\"center\" width=\""+Math.rint((1.0
                    /(double)assemblyFields.size())*100)+"%\">"+ assemblyFields.get(i).getName()+"</td>";
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
    public MachineInstruction cloneFor(MachineComponent.IdentifierMap oldToNew) {
        return new MachineInstruction(getName(), UUID.randomUUID(), oldToNew.getNewMachine(),
                getOpcode(), null, null);
    }

    @Override
    public <U extends MachineInstruction> void copyTo(U other) {
        checkNotNull(other);

        other.setName(getName());
        other.setOpcode(getOpcode());
        other.setInstructionFields(getInstructionFields());
        other.setAssemblyFields(getAssemblyFields());
    }
} // end class MachineInstruction
