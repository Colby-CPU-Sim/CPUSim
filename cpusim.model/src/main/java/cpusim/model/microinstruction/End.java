package cpusim.model.microinstruction;

import cpusim.model.Machine;
import cpusim.model.Module;
import cpusim.model.util.IdentifiedObject;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Denotes the end of a sequence of {@link Microinstruction} values.
 *
 * @since 2000-06-01
 */
public class End extends Microinstruction<End>
{

    /**
     * Constructor
     * @param id Unique ID for the instruction
     * @param machine the machine that holds the micro
     */
    public End(UUID id, Machine machine)
    {
        super("End", id, machine);
    } // end constructor
    
    /**
     * Constructor
     * @param machine the machine that holds the micro
     */
    public End(Machine machine)
    {
        this(IdentifiedObject.generateRandomID(), machine);
    } // end constructor
    
    /**
     * Copy constructor
     * @param other Instance to copy from
     */
    public End(End other) {
    	this(other.machine);
    }
    
    @Override
    protected void validateState() {
        // nothing to validate
    }
    
    @Override
    public boolean uses(Module<?> m)
    {
        return false;
    }
    
    @Override
    public void copyTo(final End other) {
        checkNotNull(other);
    }
    
    /**
     * execute the micro instruction from machine
     */
    @Override
    public void execute()
    {
        machine.getControlUnit().setMicroIndex(0);
        machine.getControlUnit().setCurrentInstruction(
                machine.getFetchSequence());
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
