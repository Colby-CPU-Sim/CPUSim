package cpusim.model.microinstruction;


import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import cpusim.model.util.*;
import cpusim.model.Machine;
import cpusim.model.module.Module;
import cpusim.util.MoreFXCollections;
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
        implements IdentifiedObject,
                NamedObject,
                LegacyXMLSupported,
                HTMLEncodable,
                Validatable,
                MachineBound,
                MachineComponent,
                Copyable<T>
{
	
    // name of the microinstruction
    private final StringProperty name;

    /**
     * Cycle count for an operation.
     */
    private final IntegerProperty cycles;

    private final ReadOnlyObjectProperty<UUID> uuid;
    
    private final ObjectProperty<Machine> machine;
    

    //------------------------------
    // constructor
    
    Microinstruction(String name, UUID id, Machine machine) {
        checkArgument(!Strings.isNullOrEmpty(name));
        
        this.name = new SimpleStringProperty(this, "name", name);
        this.cycles = new SimpleIntegerProperty(this, "cycleCount", 1);
        this.machine = new SimpleObjectProperty<>(this, "machine",
                checkNotNull(machine, "machine == null"));
        this.uuid = new ReadOnlyObjectWrapper<>(this, "id",
                checkNotNull(id, "id == null"));
    }
    
    /**
	 * Copy constructor: copies data in <code>other</code>. This generates a new value for {@link #getID()}.
	 * @param other Instance to copy from
	 * 
	 * @throws NullPointerException if <code>other</code> is <code>null</code>.
	 */
	public Microinstruction(final Microinstruction<T> other) {
		this(checkNotNull(other).getName(), IdentifiedObject.generateRandomID(), other.machine.get());
	}

    @Override
    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public ReadOnlyProperty<UUID> idProperty() {
        return uuid;
    }

    @Override
    public ObjectProperty<Machine> machineProperty() {
        return machine;
    }

    @Override
    public ReadOnlySetProperty<MachineComponent> getChildrenComponents() {
        return new ReadOnlySetWrapper<>(this, "childrenComponents", MoreFXCollections.emptyObservableSet());
    }

    public int getCycleCount() {
        return cycles.get();
    }

    public IntegerProperty cycleCountProperty() {
        return cycles;
    }

    public String getHTMLName()
    {
        return HtmlEncoder.sEncode(getName());
    }

    protected final MoreObjects.ToStringHelper toStringHelper() {
	    return MoreObjects.toStringHelper(getClass())
                .addValue(getID())
                .add("name", getName());
    }

    @Override
    public String toString() {
        return toStringHelper().toString();
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

    @Override
    public abstract T cloneFor(MachineComponent.IdentifierMap oldToNew);
}  // end of class Microinstruction
