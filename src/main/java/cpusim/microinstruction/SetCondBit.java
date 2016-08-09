/**
 * Author: Jinghui Yu
 * Last editing date: 6/6/2013
 */

package cpusim.microinstruction;

import cpusim.Machine;
import cpusim.Microinstruction;
import cpusim.Module;
import cpusim.module.ConditionBit;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * The branch microinstruction is identical to the Test microinstruction except
 * that it is an unconditional jump.
 */
public class SetCondBit extends Microinstruction {
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
    public SetCondBit(String name, Machine machine, ConditionBit bit, String value){
        super(name, machine);
        this.value = new SimpleStringProperty(value);
        this.bit = new SimpleObjectProperty<>(bit);
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
     * returns the class of the microinstruction
     * @return the class of the microinstruction
     */
    @Override
    public String getMicroClass(){
        return "setCondBit";
    }

    /**
     * duplicate the set class and return a copy of the original Set class.       *
     * @return a copy of the Set class
     */
    public Object clone(){
        return new SetCondBit(getName(),machine,getBit(),getValue());
    }

    /**
     * copies the data from the current micro to a specific micro
     * @param oldMicro the micro instruction that will be updated
     */
    public void copyDataTo(Microinstruction oldMicro)
    {
        assert oldMicro instanceof SetCondBit :
                "Passed non-SetCondBit to SetCondBit.copyDataTo()";
        SetCondBit newSetCondBit = (SetCondBit) oldMicro;
        newSetCondBit.setName(getName());
        newSetCondBit.setBit(getBit());
        newSetCondBit.setValue(getValue());
    }

    /**
     * execute the micro instruction from machine
     */
    public void execute()
    {
        bit.get().set((value.get().equals("0") ? 0 : 1));
    }

    /**
     * returns the XML description
     * @return the XML description
     */
    public String getXMLDescription(){
        return "<SetCondBit name=\"" + getHTMLName() +
                "\" bit=\"" + getBit().getID() +
                "\" value=\"" + getValue() +
                "\" id=\"" + getID() + "\" />";
    }

    /**
     * returns the HTML description
     * @return the HTML description
     */
    public String getHTMLDescription(){
        return "<TR><TD>" + getHTMLName() +
                "</TD><TD>" + getBit().getHTMLName() +
                "</TD><TD>" + getValue() +
                "</TD></TR>";
    }

    /**
     * returns true if this microinstruction uses m
     * (so if m is modified, this micro may need to be modified.
     * @param m the module that holds the microinstruction
     * @return boolean value true if this micro used the module
     */
    public boolean uses(Module m){
        return (m == bit.get());
    }
}
