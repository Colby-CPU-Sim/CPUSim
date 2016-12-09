package cpusim.model.microinstruction;

import cpusim.model.Machine;
import cpusim.model.module.Module;
import cpusim.model.module.ControlUnit;
import cpusim.model.util.MachineComponent;
import cpusim.model.util.MoreBindings;
import cpusim.model.util.ValidationException;
import javafx.beans.property.*;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The branch microinstruction is identical to the {@link Test} microinstruction except
 * that it is an unconditional jump.
 *
 * @since 2013-06-07
 */
public class Branch extends Microinstruction<Branch>
{
    private final IntegerProperty amount;

    @DependantComponent
    private final ReadOnlyObjectProperty<ControlUnit> controlUnit;

    private final ReadOnlySetProperty<MachineComponent> dependants;
    
    /**
     * Constructor
     * creates a new Branch object with input values.
     *
     * @param name name of the microinstruction.
     * @param machine the microinstruction belongs to.
     * @param amount size of the relative jump.
     */
    public Branch(String name,
                  UUID id,
                  Machine machine,
                  int amount) {
        super(name, id, machine);
        this.amount = new SimpleIntegerProperty(this, "amount", amount);

        this.controlUnit = MoreBindings.createReadOnlyBoundProperty(machine.controlUnitProperty());

        dependants = MachineComponent.collectDependancies(this)
                .buildSet(this, "dependantComponents");
    }

    /**
     * Creates a new instance
     * @param name Name of instruction
     * @param id Unique ID
     * @param machine Binding machine
     */
    private Branch(String name, UUID id, Machine machine) {
        this(name, id, machine, -1);
    }
    
    /**
     * Copy constructor
     * @param other instance copied from.
     */
    public Branch(Branch other) {
        this(other.getName(), UUID.randomUUID(), other.getMachine(), other.getAmount());
    }

    @Override
    public ReadOnlySetProperty<MachineComponent> getDependantComponents() {
        return dependants;
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
     * Gets a property for the amount.
     * @return property bound to the amount to branch
     *
     * @see #getAmount()
     * @see #setAmount(int)
     */
    public IntegerProperty amountProperty() {
        return amount;
    }

    /**
     * increment the micro index by the amount specified by the instruction
     */
    @Override
    public void execute() {
        controlUnit.get().incrementMicroIndex(amount.get());
    }

    @Override
    public void validate() {
        super.validate();

        if (controlUnit == null) {
            throw new ValidationException("No control unit is set for Branch " + getName());
        }
    }

    @Override
    public Branch cloneFor(MachineComponent.IdentifierMap oldToNew) {
        // don't copy the controlUnit, its bound from the machine on construction
        return new Branch(getName(), UUID.randomUUID(), oldToNew.getNewMachine(), getAmount());
    }

    @Override
    public <U extends Branch> void copyTo(U other) {
        checkNotNull(other);
        other.setName(getName());
        other.setAmount(getAmount());
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
