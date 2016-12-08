package cpusim.model.microinstruction;

import cpusim.model.Machine;
import cpusim.model.module.Module;
import cpusim.model.module.ConditionBit;
import cpusim.model.util.IdentifiedObject;
import cpusim.model.util.MachineComponent;
import javafx.beans.property.*;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Sets the value of a {@link ConditionBit} when called.
 *
 * @since 2013-06-06
 */
public class SetCondBit extends Microinstruction<SetCondBit> {
    
    private BooleanProperty value;

    @DependantComponent
    private ObjectProperty<ConditionBit> bit;

    private ReadOnlySetProperty<MachineComponent> dependencies;

    /**
     * Constructor
     * creates a new Branch object with input values.
     *
     * @param name name of the microinstruction.
     * @param machine the machine that the microinstruction belongs to.
     * @param bit set the conditional bit.
     * @param value size of the relative jump.
     */
    public SetCondBit(String name, UUID id, Machine machine, ConditionBit bit, boolean value){
        super(name, id, machine);
        this.value = new SimpleBooleanProperty(this, "value", value);
        this.bit = new SimpleObjectProperty<>(this, "bit", bit);

        this.dependencies = MachineComponent.collectDependancies(this);
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
    public SetCondBit(String name, Machine machine, ConditionBit bit, boolean value){
        this(name, IdentifiedObject.generateRandomID(), machine, bit, value);
    }
    
    /**
     * Copy constructor
     * @param other
     */
    public SetCondBit(SetCondBit other) {
        this(other.getName(), other.getMachine(), other.getBit(), other.getValue());
    }

    @Override
    public ReadOnlySetProperty<MachineComponent> getDependantComponents() {
        return this.dependencies;
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
    public boolean getValue(){
        return value.get();
    }

    /**
     * updates the fixed value stored in the set microinstruction.
     *
     * @param newValue the new value for the field.
     */
    public void setValue(boolean newValue){
        value.set(newValue);
    }

    /**
     * updates the fixed value stored in the set microinstruction.
     *
     * @param newValue {@code 1 == true}, else {@code false}
     */
    public void setValue(int newValue){
        value.set(newValue == 1);
    }
    
    /**
     * execute the micro instruction from machine
     */
    @Override
    public void execute()
    {
        bit.get().set((value.get() ? 1 : 0));
    }

    /**
     * returns the XML description
     * @return the XML description
     */
    @Override
    public String getXMLDescription(String indent) {
        return indent + "<SetCondBit name=\"" + getHTMLName() +
                "\" bit=\"" + getBit().getID() +
                "\" value=\"" + (getValue() ? 1 : 0) +
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
                "</TD><TD>" + (getValue() ? 1 : 0) +
                "</TD></TR>";
    }

    @Override
    public SetCondBit cloneFor(IdentifierMap oldToNew) {
        return new SetCondBit(getName(), UUID.randomUUID(), oldToNew.getNewMachine(),
                oldToNew.get(getBit()), getValue());
    }

    @Override
    public <U extends SetCondBit> void copyTo(U other) {
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
