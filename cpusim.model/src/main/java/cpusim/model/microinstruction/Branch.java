/**
 * Author: Jinghui Yu
 * Last editing date: 6/6/2013
 */

package cpusim.model.microinstruction;

import cpusim.model.Machine;
import cpusim.model.Module;
import cpusim.model.module.ControlUnit;
import cpusim.model.util.Copyable;
import cpusim.model.util.ValidationException;
import javafx.beans.property.SimpleIntegerProperty;

import static com.google.common.base.Preconditions.*;

/**
 * The branch microinstruction is identical to the Test microinstruction except
 * that it is an unconditional jump.
 */
public class Branch extends Microinstruction implements Copyable<Branch>
{
    private SimpleIntegerProperty amount;
    private ControlUnit controlUnit;

    /**
     * Constructor
     * creates a new Branch object with input values.
     *
     * @param name name of the microinstruction.
     * @param machine the microinstruction belongs to.
     * @param amount size of the relative jump.
     */
    public Branch(String name, Machine machine,
                  int amount,
                  ControlUnit controlUnit)
    {
        super(name, machine);
        this.amount = new SimpleIntegerProperty(amount);
        this.controlUnit = controlUnit;
    }

    /**
     * returns the fixed value stored in the set microinstruction.
     *
     * @return the integer value of the field.
     */
    public int getAmount()
    {
        return amount.get();
    }

    /**
     * updates the fixed value stored in the set microinstruction.
     *
     * @param newAmount the new value for the field.
     */
    public void setAmount(int newAmount)
    {
        amount.set(newAmount);
    }
    
    /**
     * returns the class of the microinstruction
     * @return the class of the microinstruction
     */
    @Override
    public String getMicroClass(){
        return "branch";
    }
    
    /**
     * increment the micro index by the amount specified by the instruction
     */
    @Override
    public void execute()
    {
        controlUnit.incrementMicroIndex(amount.get());
    }
    
    @Override
    protected void validateState() {
        if (controlUnit == null) {
            throw new ValidationException("No control unit is set for Branch " + getName());
        }
    }
    
    /**
     * copies the data from the current micro to a specific micro
     * @param oldMicro the micro instruction that will be updated
     */
    @Override
    public void copyTo(Branch oldMicro)
    {
        checkNotNull(oldMicro);
        oldMicro.setName(getName());
        oldMicro.setAmount(getAmount());
        oldMicro.controlUnit = controlUnit;
    }

    /**
     * returns the XML description
     * @return the XML description
     */
    @Override
    public String getXMLDescription(String indent)
    {
        return indent + "<Branch name=\"" + getHTMLName() +
                "\" amount=\"" + getAmount() +
                "\" id=\"" + getID() + "\" />";
    }

    /**
     * returns the HTML description
     * @return the HTML description
     */
    @Override
    public String getHTMLDescription(String indent)
    {
        return indent + "<TR><TD>" + getHTMLName() + "</TD><TD>" +
                getAmount() + "</TD></TR>";
    }

    /**
     * returns true if this microinstruction uses m
     * (so if m is modified, this micro may need to be modified.
     * @param m the module that holds the microinstruction
     * @return boolean value true if this micro used the module
     */
    @Override
    public boolean uses(Module<?> m)
    {
        return false;
    }
}
