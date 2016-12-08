/**
 * File: FieldValue
 * Author: Dale Skrien
 * Last update: August 2013
 */
package cpusim.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import cpusim.model.util.MachineComponent;
import cpusim.model.util.LegacyXMLSupported;
import cpusim.model.util.MoreFXCollections;
import cpusim.model.util.NamedObject;
import cpusim.xml.HtmlEncoder;
import javafx.beans.property.*;

import javax.annotation.Generated;
import java.util.Objects;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class FieldValue implements NamedObject, LegacyXMLSupported, MachineComponent {

	private ReadOnlyObjectProperty<UUID> id;
	private StringProperty name;
	private LongProperty value;

	private final Machine machine;
	
	/**
	 * Creates a new Field Value with specified name
	 * and value.
	 * 
	 * @param name - Name of new FieldValue.
	 * @param value - long value of new FieldValue.
	 */
	public FieldValue(String name, UUID id, Machine machine, long value) {
		checkArgument(!Strings.isNullOrEmpty(name));

		this.name = new SimpleStringProperty(this, "name", name);
		this.id = new SimpleObjectProperty<>(this, "id", checkNotNull(id));
		this.machine = machine;
		this.value = new SimpleLongProperty(this, "value", value);
    }
	
	/**
	 * Creates a new Field Value by copying an existing value
	 * 
	 * @param other instance to copy.
	 * @throws NullPointerException if <code>null</code> argument
	 */
	public FieldValue(final FieldValue other) {
		this(checkNotNull(other).getName(), UUID.randomUUID(), other.machine, other.value.get());
    }

    @Override
    public ReadOnlySetProperty<MachineComponent> getChildrenComponents() {
        return new ReadOnlySetWrapper<>(this, "childrenComponents", MoreFXCollections.emptyObservableSet());
    }

    @Override
    public ReadOnlySetProperty<MachineComponent> getDependantComponents() {
        return new ReadOnlySetWrapper<>(this, "dependantComponents", MoreFXCollections.emptyObservableSet());
    }

    @Override
	public StringProperty nameProperty() {
		return name;
	}

	@Override
	public ReadOnlyProperty<UUID> idProperty() {
		return id;
	}

	@Override
	public ReadOnlyObjectProperty<Machine> machineProperty() {
		return new ReadOnlyObjectWrapper<>(this, "machine", machine);
	}

	/**
	 * Get the stored value
	 * @return the value
	 */
	public long getValue() {
		return value.get();
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(long value) {
		this.value.setValue(value);
	}

	public LongProperty valueProperty() {
		return value;
	}

	@Override
	public FieldValue cloneFor(MachineComponent.IdentifierMap oldToNew) {
		return new FieldValue(getName(), UUID.randomUUID(), oldToNew.getNewMachine(), getValue());
	}



	/**
     * Gives the XML description of this FieldValue.
     */
	@Override
	public String getXMLDescription(String indent) {
        return "<FieldValue name=\"" + HtmlEncoder.sEncode(getName()) +
                "\" value=\"" + getValue() + "\" />";
    }
    
    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(name, value);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override @Generated("Eclipse")
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		FieldValue other = (FieldValue) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (value != other.value) {
			return false;
		}
		return true;
	}

	/**
     * Returns a string representation of the object.
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(FieldValue.class)
        		.addValue(name)
        		.add("value", value).toString();
    }

}
