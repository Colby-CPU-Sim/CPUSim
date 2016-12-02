package cpusim.model.microinstruction;

import com.google.common.base.Strings;
import cpusim.model.Machine;
import cpusim.model.Module;
import cpusim.model.module.ConditionBit;
import cpusim.model.util.IdentifiedObject;
import cpusim.model.util.ValidationException;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.UUID;

import static com.google.common.base.Preconditions.*;

/**
 * Sets the value of a {@link ConditionBit} when called.
 *
 * @since 2013-06-06
 */
public class SetCondBit extends Microinstruction<SetCondBit> {
    
    private SimpleStringProperty value;
    private SimpleObjectProperty<ConditionBit> bit;

    /**
     * Constructor
     * creates a new Branch object with input values.
     *
     * @param name name of the microinstruction.
     * @param machine the machine that the microinstruction belongs to.
     * @param bit set the conditional bit.
     * @param value size of the relative jump.
     */
    public SetCondBit(String name, UUID id, Machine machine, ConditionBit bit, String value){
        super(name, id, machine);
        this.value = new SimpleStringProperty(value);
        this.bit = new SimpleObjectProperty<>(bit);
    }
    
    /**
     * Constructor
     * creates a new Branch object with input values.
     *
     * @param name name of the microinstruction.
     * @param machine the machine that the microinstruction belongs to.
     * @param bit set the conditional bit.
     * @param value size of the relative jump.
     */
    public SetCondBit(String name, Machine machine, ConditionBit bit, String value){
        this(name, IdentifiedObject.generateRandomID(), machine, bit, value);
    }
    
    /**
     * Copy constructor
     * @param other
     */
    public SetCondBit(SetCondBit other) {
        this(other.getName(), other.machine, other.getBit(), other.getValue());
    }

    /**
     * returns the fixed value stored in the set microinstruction.
     *
     * @return the integer value of the field.
     */
    public ConditionBit getBit(){
        return bit.get();
    }

    /**
     * updates the fixed value stored in the set microinstruction.
     *
     * @param newBit the new value for the field.
     */
    public void setBit(ConditionBit newBit){
        bit.set(newBit);
    }

    /**
     * returns the fixed value stored in the set microinstruction.
     *
     * @return the integer value of the field.
     */
    public String getValue(){
        return value.get();
    }

    /**
     * updates the fixed value stored in the set microinstruction.
     *
     * @param newValue the new value for the field.
     */
    public void setValue(String newValue){
        value.set(newValue);
    }
    
    /**
     * execute the micro instruction from machine
     */
    @Override
    public void execute()
    {
        bit.get().set((value.get().equals("0") ? 0 : 1));
    }

    /**
     * returns the XML description
     * @return the XML description
     */
    @Override
    public String getXMLDescription(String indent) {
        return indent + "<SetCondBit name=\"" + getHTMLName() +
                "\" bit=\"" + getBit().getID() +
                "\" value=\"" + getValue() +
                "\" id=\"" + getID() + "\" />";
    }

    /**
     * returns the HTML description
     * @return the HTML description
     */
    @Override
    public String getHTMLDescription(String indent){
        return indent + "<TR><TD>" + getHTMLName() +
                "</TD><TD>" + getBit().getHTMLName() +
                "</TD><TD>" + getValue() +
                "</TD></TR>";
    }
    
    @Override
    protected void validateState() {
        if (Strings.isNullOrEmpty(value.get())) {
            throw new ValidationException("No value set for " + SetCondBit.class.getSimpleName() + " microinstruction.");
        }
    }
    
    @Override
    public <U extends SetCondBit> void copyTo(final U other) {
        checkNotNull(other);
        
        other.setName(getName());
        other.setValue(getValue());
        other.setBit(getBit());
    }
    
    /**
     * returns true if this microinstruction uses m
     * (so if m is modified, this micro may need to be modified.
     * @param m the module that holds the microinstruction
     * @return boolean value true if this micro used the module
     */
    @Override
    public boolean uses(Module<?> m){
        return (m == bit.get());
    }
}
