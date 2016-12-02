/**
 * File: FieldValue
 * Author: Dale Skrien
 * Last update: August 2013
 */
package cpusim.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;

import javax.annotation.Generated;

import cpusim.model.util.IdentifiedObject;
import cpusim.model.util.LegacyXMLSupported;
import cpusim.model.util.NamedObject;
import cpusim.xml.HtmlEncoder;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

public class FieldValue implements NamedObject, LegacyXMLSupported
{

	private String name;
	private long value;
	
	/**
	 * Creates a new Field Value with specified name
	 * and value.
	 * 
	 * @param name - Name of new FieldValue.
	 * @param value - long value of new FieldValue.
	 */
	public FieldValue(String name, long value) {
		checkArgument(!Strings.isNullOrEmpty(name));
		this.name = name;
		this.value = value;
    }
	
	/**
	 * Creates a new Field Value by copying an existing value
	 * 
	 * @param other 
	 * @throws NullPointerException if <code>null</code> argument
	 */
	public FieldValue(final FieldValue other) {
		this(checkNotNull(other).name, other.value);
    }
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the stored value
	 * @return the value
	 */
	public long getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(long value) {
		this.value = value;
	}

	/**
     * Gives the XML description of this FieldValue.
     */
	@Override
	public String getXMLDescription(String indent) {
        return "<FieldValue name=\"" + HtmlEncoder.sEncode(getName()) +
                "\" value=\"" + getValue() + "\" />";
    }

	/**
     * Gives a clone of this FieldValue.
     */
    @Override @Deprecated
    public Object clone() {
        return new FieldValue(getName(), getValue());
    }
    
    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(name, Long.valueOf(value));
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
