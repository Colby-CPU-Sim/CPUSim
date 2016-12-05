package cpusim.model.microinstruction;


import com.google.common.base.Strings;
import cpusim.model.Machine;
import cpusim.model.Module;
import cpusim.model.util.Copyable;
import cpusim.model.util.IdentifiedObject;
import cpusim.model.util.LegacyXMLSupported;
import cpusim.model.util.NamedObject;
import cpusim.model.util.Validatable;
import cpusim.xml.HTMLEncodable;
import cpusim.xml.HtmlEncoder;
import javafx.beans.property.*;

import java.util.UUID;

import static com.google.common.base.Preconditions.*;


/**
 * The base of all Microinstructions in CPUSim.
 *
 * @param <T> Subtype that inherits from {@code this} type.
 *
 * @since 1999-06-01
 */
public abstract class Microinstruction<T extends Microinstruction<T>>
        implements IdentifiedObject, NamedObject, LegacyXMLSupported, HTMLEncodable, Validatable, Copyable<T>
{
	
    // name of the microinstruction
    private SimpleStringProperty name;
    
    private final ReadOnlyObjectProperty<UUID> uuid;
    
    protected Machine machine;
    

    //------------------------------
    // constructor
    
    Microinstruction(String name, UUID id, Machine machine) {
        checkArgument(!Strings.isNullOrEmpty(name));
        
        this.name = new SimpleStringProperty(this, "name", name);
        this.machine = machine;
        this.uuid = new SimpleObjectProperty<>(this, "id", checkNotNull(id));
    }
    
    /**
	 * Copy constructor: copies data in <code>other</code>. This generates a new value for {@link #getID()}.
	 * @param other Instance to copy from
	 * 
	 * @throws NullPointerException if <code>other</code> is <code>null</code>.
	 */
	public Microinstruction(final Microinstruction<T> other) {
		this(checkNotNull(other).getName(), IdentifiedObject.generateRandomID(), other.machine);
	}

    @Override
    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public ReadOnlyProperty<UUID> idProperty() {
        return uuid;
    }
    
    public String getHTMLName()
    {
        return HtmlEncoder.sEncode(getName());
    }

    public String toString()
    {
        return name.get();
    }

    //------------------------------
    // abstract methods
    // These methods should be overridden by all subclasses

    public abstract void execute();

    /**
     * returns true if this microinstruction uses m (so if m is modified, this micro may need to be modified.
     * 
     * @param m
     * @return
     * 
     * @throws NullPointerException if <code>m</code> is <code>null</code>.
     */
    public abstract boolean uses(Module<?> m);

    @Override
    public void validate() {
        NamedObject.super.validate();
    }
    
}  // end of class Microinstruction
