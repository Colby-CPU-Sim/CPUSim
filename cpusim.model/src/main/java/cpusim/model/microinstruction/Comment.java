package cpusim.model.microinstruction;

import cpusim.model.Machine;
import cpusim.model.Module;
import cpusim.model.util.IdentifiedObject;
import cpusim.xml.HtmlEncoder;

import java.util.UUID;

import static com.google.common.base.Preconditions.*;

/**
 * Comment microinstructions do nothing.  They are included just so that their
 * names can be used as comments in the fetch or execute sequences.
 * Their text can be edited directly from the fetch or execute sequence lists
 * in the dialog boxes by double-clicking on them or selecting them and then
 * pressing F2.  When you are done editing, just press Enter or Return.
 *
 * @since 2010-06-01
 */
public class Comment extends Microinstruction<Comment>
{
    /**
     * Constructor
     */
    public Comment(final String name, UUID id, Machine machine) {
        super(name, id, machine);
    } // end constructor
    
    public Comment(Comment other) {
        super(other.getName(), IdentifiedObject.generateRandomID(), other.machine);
    }

    @Override
    public boolean uses(Module<?> m)
    {
        return false;
    }

    @Override
    public void execute()
    {
        //do nothing
    }
    
    @Override
    protected void validateState() {
        // nothing to validate
    }
    
    @Override
    public <U extends Comment> void copyTo(final U other) {
        checkNotNull(other);
    }
    
    @Override
    public String getHTMLDescription(String indent)
    {
        //not used, since Comment micros only appear in the execute
        //or fetch sequences, in which case, just the name (encoded for
        //HTML is used)
        return indent + "<!-- " + getName() + "-->";
    }

    @Override
    public String getXMLDescription(String indent)
    {
        return indent + "<Comment name=\"" + HtmlEncoder.sEncode(getName()) +
                "\" id=\"" + getID() + "\" />";
    }
} // end Comment class
