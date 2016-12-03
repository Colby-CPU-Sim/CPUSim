package cpusim.model.microinstruction;

import cpusim.model.Machine;
import cpusim.model.Module;
import cpusim.model.module.Register;
import cpusim.model.util.IdentifiedObject;
import cpusim.model.util.ValidationException;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.UUID;

import static com.google.common.base.Preconditions.*;

/**
 * The logical microinstructions perform the bit operations of AND, OR, NOT, NAND,
 * NOR, or XOR on the specified registers.
 */
public class Logical extends Microinstruction<Logical> {
	
	// FIXME replace type with Enum
	
    private SimpleStringProperty type;
    private SimpleObjectProperty<Register> source1;
    private SimpleObjectProperty<Register> source2;
    private SimpleObjectProperty<Register> destination;

    /**
     * Constructor
     * creates a new {@link Logical} object with input values.
     *
     * @param name name of the microinstruction.
     * @param machine the machine that the microinstruction belongs to.
     * @param type type of logical microinstruction.
     * @param source1 the source1 register.
     * @param source2 the source2 register.
     * @param destination the destination register.
     */
    public Logical(String name, Machine machine,
                   String type,
                   Register source1,
                   Register source2,
                   Register destination){
        this(name, IdentifiedObject.generateRandomID(), machine, type, source1, source2, destination);
    }
    
    /**
     * Constructor
     * creates a new {@link Logical} object with input values.
     *
     * @param name name of the microinstruction.
     * @param id Unique ID of instruction
     * @param machine the machine that the microinstruction belongs to.
     * @param type type of logical microinstruction.
     * @param source1 the source1 register.
     * @param source2 the source2 register.
     * @param destination the destination register.
     */
    public Logical(String name,
                   UUID id,
                   Machine machine,
                   String type,
                   Register source1,
                   Register source2,
                   Register destination){
        super(name, id, machine);
        this.type = new SimpleStringProperty(this, "type", type);
        this.source1 = new SimpleObjectProperty<>(this, "source1", source1);
        this.source2 = new SimpleObjectProperty<>(this, "source2", source2);
        this.destination = new SimpleObjectProperty<>(this, "destination", destination);
    }

    /**
     * returns the register to be calculated.
     * @return the name of the register.
     */
    public Register getSource1(){
        return source1.get();
    }

    /**
     * updates the register used by the microinstruction.
     * @param newSource1 the new source register for the logical microinstruction.
     */
    public void setSource1(Register newSource1){
        source1.set(newSource1);
    }

    /**
     * returns the register to be calculated.
     * @return the name of the register.
     */
    public Register getSource2(){
        return source2.get();
    }

    /**
     * updates the register used by the microinstruction.
     * @param newSource2 the new source register for the logical microinstruction.
     */
    public void setSource2(Register newSource2){
        source2.set(newSource2);
    }

    /**
     * returns the register to put result.
     * @return the name of the register.
     */
    public Register getDestination(){
        return destination.get();
    }

    /**
     * updates the register used by the microinstruction.
     * @param newDestination the new destination for the logical microinstruction.
     */
    public void setDestination(Register newDestination){
        destination.set(newDestination);
    }

    /**
     * returns the type of shift.
     * @return type of shift as a string.
     */
    public String getType(){
        return type.get();
    }

    /**
     * updates the type used by the microinstruction.
     * @param newType the new string of type.
     */
    public void setType(String newType){
        type.set(newType);
    }
    
    @Override
    public <U extends Logical> void copyTo(final U newLogical) {
        checkNotNull(newLogical);
        
        newLogical.setName(getName());
        newLogical.setType(getType());
        newLogical.setSource1(getSource1());
        newLogical.setSource2(getSource2());
        newLogical.setDestination(getDestination());
    }
    
    /**
     * execute the micro instruction from machine
     */
    @Override
    public void execute()
    {
        // FIXME Replace with Enum
        long op1 = source1.get().getValue();
        long op2 = source2.get().getValue();
        long result = 0;

        if (type.get().equals("AND"))
            result = op1 & op2;
        else if (type.get().equals("OR"))
            result = op1 | op2;
        else if (type.get().equals("NAND"))
            result = ~(op1 & op2);
        else if (type.get().equals("NOR"))
            result = ~(op1 | op2);
        else if (type.get().equals("XOR"))
            result = op1 ^ op2;
        else if (type.get().equals("NOT"))
            result = ~op1;

        destination.get().setValue(result);
    }

    /**
     * returns the XML description
     * @return the XML description
     */
    @Override
    public String getXMLDescription(String indent) {
        return indent + "<Logical name=\"" + getHTMLName() +
                "\" type=\"" + getType() +
                "\" source1=\"" + getSource1().getID() +
                "\" source2=\"" + getSource2().getID() +
                "\" destination=\"" + getDestination().getID() +
                "\" id=\"" + getID() + "\" />";
    }

    /**
     * returns the HTML description
     * @return the HTML description
     */
    @Override
    public String getHTMLDescription(String indent) {
        return indent + "<TR><TD>" + getHTMLName() + "</TD><TD>" + getType() +
                "</TD><TD>" + getSource1().getHTMLName() + "</TD><TD>" + getSource2().getHTMLName() +
                "</TD><TD>" + getDestination().getHTMLName() + "</TD></TR>";
    }

    /**
     * returns true if this microinstruction uses m
     * (so if m is modified, this micro may need to be modified.
     * @param m the module that holds the microinstruction
     * @return boolean value true if this micro used the module
     */
    @Override
    public boolean uses(Module<?> m){
        return (m == source1.get() || m == source2.get() || m == destination.get());
    }

    @Override
    public void validate() {
        super.validate();

        // get width of the source1, source2, and destination
        // registers, if they are different, then the validity
        // test fails
        if (!(getSource1().getWidth() == getSource2().getWidth() &&
                getSource2().getWidth() == getDestination().getWidth())) {
            throw new ValidationException("At least one of the registers in the " +
                    "microinstruction \"" + getName() +
                    "\" has\na bit width that is different than one " +
                    "or more of the others.\nAll registers must have " +
                    "the same number of bits.");
        }
    }

}