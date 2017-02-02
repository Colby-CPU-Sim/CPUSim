package cpusim.model.microinstruction;

import cpusim.model.Machine;
import cpusim.model.module.ControlUnit;
import cpusim.model.module.Module;
import cpusim.model.util.MachineComponent;
import cpusim.util.MoreBindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlySetProperty;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Denotes the end of a sequence of {@link Microinstruction} values.
 *
 * @since 2000-06-01
 */
public class End extends Microinstruction<End> {

    @DependantComponent
    private final ReadOnlyObjectProperty<ControlUnit> controlUnit;

    private final ReadOnlySetProperty<MachineComponent> dependencies;

    /**
     * Constructor
     * @param id Unique ID for the instruction
     * @param machine the machine that holds the micro
     */
    public End(UUID id, Machine machine) {
        super("End", id, machine);

        this.controlUnit = MoreBindings.createReadOnlyBoundProperty(machine.controlUnitProperty());
        this.dependencies = MachineComponent.collectDependancies(this)
                .buildSet(this, "dependencies");
    } // end constructor
    
    /**
     * Constructor
     * @param machine the machine that holds the micro
     */
    public End(Machine machine)
    {
        this(UUID.randomUUID(), machine);
    } // end constructor
    
    /**
     * Copy constructor
     * @param other Instance to copy from
     */
    public End(End other) {
    	this(other.getMachine());
    }

    @Override
    public ReadOnlySetProperty<MachineComponent> getDependantComponents() {
        return dependencies;
    }

    @Override
    public boolean uses(Module<?> m)
    {
        return false;
    }

    @Override
    public End cloneFor(IdentifierMap oldToNew) {
        return new End(oldToNew.getNewMachine());
    }

    @Override
    public <U extends End> void copyTo(U other) {
        checkNotNull(other);
    }

    /**
     * execute the micro instruction from machine
     */
    @Override
    public void execute() {
        controlUnit.getValue().setMicroIndex(0);
        controlUnit.getValue().setCurrentInstruction(getMachine().getFetchSequence());
    } // end execute()

    /**
     * returns the XML description
     * @return the XML description
     */
    @Override
    public String getXMLDescription(String indent)
    {
        return "";
    }

    /**
     * returns the HTML description
     * @return the HTML description
     */
    @Override
    public String getHTMLDescription(String indent)
    {
        return "";
    }

} // end End class
