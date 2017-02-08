package cpusim.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import cpusim.model.util.*;
import cpusim.xml.HTMLEncodable;
import cpusim.xml.HtmlEncoder;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.*;


/**
 * Class used to hold field options for each field of 
 * a machine instruction.
 *
 * @author Kevin Marsteller
 * @author Dale Skrien
 * @author Kevin Brightwell (Nava2)
 *
 * @since 2013-08-01
 */
public class Field
        implements
                NamedObject
                , IdentifiedObject
                , LegacyXMLSupported
                , HTMLEncodable
                , MachineComponent
                , Copyable<Field> {

	public enum Relativity {
		absolute, 
		pcRelativePreIncr,
		pcRelativePostIncr
	}
	
    public enum Type {
    	required, 
    	optional, 
    	ignored
    }
    
    public enum SignedType {
        Signed,
        Unsigned;
    	
    	/**
    	 * Added for convenience
    	 * 
    	 * @param isSigned {@code true} if signed
    	 * @return 
    	 * 
    	 * @deprecated Use {@link SignedType} directly instead of <code>boolean</code>s
    	 */
    	@Deprecated
    	public static SignedType fromBool(boolean isSigned) {
    		return isSigned ? Signed : Unsigned;
    	}
    }

    private final Machine machine;

    private final ReadOnlyObjectProperty<UUID> id;
    
    /**
     * The name of the field
     */
    private StringProperty name;
    
    /**
     * One of the three values of enum Type
     */
    private ObjectProperty<Type> type;
    
    /**
     * The number of bits of the field
     */
    private IntegerProperty numBits;
    
    /**
     *  Absolute or pc relative
     */
    private ObjectProperty<Relativity> relativity;
    
    /**
     * The acceptable values for this field
     */
    private ListProperty<FieldValue> values;
    
    /**
     * The value to use if optional or ignored
     */
    private LongProperty defaultValue;

    /**
     * If Signed, allows only signed 2's complement values otherwise allows only unsigned binary values
     */
    private ObjectProperty<SignedType> signed;


    /** 
     * Constructor for Field with name "?".
     */
    public Field(Machine machine) {
        this("?", UUID.randomUUID(), machine);
    }
    
    /**
     * Constructor for new Field with specified Name.
     * Other values are set to defaults.
     *
     * @param name - The name of the field.
     * @param id
     */
    public Field(String name, final UUID id, Machine machine) {
        this(name, id, machine, 0, Relativity.absolute, FXCollections.observableArrayList(),
                0, SignedType.Signed, Type.required);
    }
    
    /**
     * Copy constructor that copies <code>other</code> into <code>this</code>.
     * 
     * @param other
     * @throws NullPointerException if <code>other</code> is <code>null</code>.
     */
    public Field(final Field other) {
        this(other.getName(), UUID.randomUUID(),
                other.machine,
                other.getNumBits(), other.getRelativity(),
                other.getValues(), other.getDefaultValue(),
                other.getSigned(), other.getType());
    }
    
    /**
     * Constructor for new Field with specified values.
     * 
     * @param name - Name of Field.
     * @param id
     * @param numBits - Numbits of field.
     * @param relativity - Enum relativity type.
     * @param values - Map of name to {@link FieldValue}.
     * @param defaultValue - The default int value.
     * @param signed - Whether or not the Field is a signed int.
     * @param type - Type of Field.
     *
     * @since 2016-09-20
     */
    @JsonCreator
    public Field(@JsonProperty("name") String name,
                 @JsonProperty("id") final UUID id,
                 final Machine machine,
                 @JsonProperty("numBits") int numBits,
                 @JsonProperty("relativity") Relativity relativity,
                 @JsonProperty("values") ObservableList<FieldValue> values,
                 @JsonProperty("defaultValue") long defaultValue,
                 @JsonProperty("signed") SignedType signed,
                 @JsonProperty("type") Type type) {
        this.id = new SimpleObjectProperty<>(this, "id", checkNotNull(id));
        this.name = new SimpleStringProperty(this, "name", checkNotNull(name));
        this.machine = checkNotNull(machine);
        this.type = new SimpleObjectProperty<>(this, "type", checkNotNull(type));
        this.numBits = new SimpleIntegerProperty(this, "numBits", numBits);
        this.relativity = new SimpleObjectProperty<>(this, "relativity", checkNotNull(relativity));
        this.defaultValue = new SimpleLongProperty(this, "defaultValue", defaultValue);
        this.signed = new SimpleObjectProperty<>(this, "signed", checkNotNull(signed));
        this.values = new SimpleListProperty<>(this, "values");

        if (values == null) {
            this.values.set(FXCollections.observableArrayList());
        } else {
            this.values.set(values);
        }
    }

    ////////////////// Setters and getters //////////////////

    @Override
    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public ReadOnlyObjectProperty<UUID> idProperty() {
        return id;
    }

    @Override
    public ReadOnlyObjectProperty<Machine> machineProperty() {
        return new ReadOnlyObjectWrapper<>(this, "machine", machine);
    }

    @JsonProperty
    public Type getType() {
        return type.get();
    }

    public void setType(Type type) {
        this.type.set(type);
    }

    public ObjectProperty<Type> typeProperty() {
        return this.type;
    }

    @JsonProperty
    public int getNumBits() {
        return numBits.get();
    }

    public void setNumBits(int numBits) {
        this.numBits.set(checkNotNull(numBits));
    }

    public IntegerProperty numBitsProperty() { return this.numBits; }

    public Relativity getRelativity() {
        return relativity.get();
    }

    public ObjectProperty<Relativity> relativityProperty() { return relativity; }

    public void setRelativity(Relativity relativity) {
        this.relativity.set(relativity);
    }

    public ObservableList<FieldValue> getValues() { return values; }

    private ListProperty<FieldValue> valuesProperty() {
        return values;
    }

    public void setValues(ObservableList<FieldValue> values) {
        checkNotNull(values);

        this.values.clear();
        this.values.addAll(values);
    }

    public long getDefaultValue() {
        return defaultValue.get();
    }

    public void setDefaultValue(long defaultValue) { 
    	this.defaultValue.set(defaultValue); 
    }

    public LongProperty defaultValueProperty() { return this.defaultValue; }

    @JsonIgnore
    public boolean isSigned() {
        return signed.get() == SignedType.Signed;
    }

    public SignedType getSigned() {
    	return signed.get();
    }

    public void setSigned(SignedType signed) {
        this.signed.set(checkNotNull(signed));
    }

    public ObjectProperty<SignedType> signedProperty() {
        return signed;
    }
    
    /**
     * Checks the values within a {@link Field} are valid.
     *
     * @throws ValidationException if a field is invalid.
     */
    public void validate() {
        if (this.getNumBits() == 0 && this.getType().equals(Type.ignored)) {
            throw new ValidationException("A field of length 0 cannot be ignored." +
                    " Field " + getName() + " is such a field.");
        }
        
        if (!this.isSigned() && this.getDefaultValue() < 0) {
            throw new ValidationException("Field " + getName() + " is unsigned" +
                    " but has a negative default value.");
        }
        
        if(getValues().size() > 0 &&
                (!getRelativity().equals(Field.Relativity.absolute) || getNumBits() == 0)) {
            throw new ValidationException("Field " + getName() + " must be" +
                    " absolute and have a positive number of bits in order to use " +
                    " a set of fixed values.");
        }
        
        if (getNumBits() > 0)
            Validate.fitsInBits(getDefaultValue(), getNumBits());
        
        validateFieldValues(this, getValues());
    }
    
    /**
     * Checks that all signs are correct based on values.
     * @param field
     * @param allFieldValues
     */
    public static void validateFieldValues(Field field, List<FieldValue> allFieldValues) {
        final int numBits = field.getNumBits();
        
        for(FieldValue fieldValue : allFieldValues) {
            if (fieldValue.getValue() < 0 && !field.isSigned())
                throw new ValidationException("Field " + field.getName() + " is unsigned" +
                        " and so " + fieldValue.getName() + " cannot have a negative field value.");
            
            if (numBits > 0) {
                Validate.fitsInBits(fieldValue.getValue(), numBits);
            }
        }
    }

    @Override
    public Field cloneFor(MachineComponent.IdentifierMap oldToNew) {
        Field newInst = new Field(getName(), UUID.randomUUID(), machine,
                getNumBits(),getRelativity(), FXCollections.observableArrayList(),
                getDefaultValue(), getSigned(), getType());

        oldToNew.copyListProperty(this, newInst, Field::valuesProperty);

        return newInst;
    }

    @Override
    public <U extends Field> void copyTo(U other) {
        checkNotNull(other);

        other.setName(getName());
        other.setType(getType());
        other.setRelativity(getRelativity());
        other.setNumBits(getNumBits());
        other.setDefaultValue(getDefaultValue());
        other.setSigned(getSigned());
        other.setValues(getValues());
    }

    /**
     * returns the FieldValue with the given name.
     * returns null if there is no such FieldValue
     * @param name the String with the name of the value
     * @return the FieldValue with that name, or {@link Optional#empty()} if none exists
     */
    public Optional<FieldValue> getValue(final String name) {
        return values.stream().filter(f -> f.getName().equals(name)).findFirst();
    }

    /**
     * Gives a string representation of this object.
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Field.class)
                .add("name", getName())
                .addValue(getID())
                .toString();
    }

    @Override
    public int hashCode() {
        return getID().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Field field = (Field) o;
        if (getID().equals(field.getID())) return true;

        if (getNumBits() != field.getNumBits()) return false;
        if (getDefaultValue() != field.getDefaultValue()) return false;
        if (!getName().equals(field.getName())) return false;
        if (!getType().equals(field.getType())) return false;
        if (!getRelativity().equals(field.getRelativity())) return false;
        if (!getSigned().equals(field.getSigned())) return false;
        return getValues().equals(field.getValues());
    }

    @Override
    public String getXMLDescription(String indent) {
        String nl = System.getProperty("line.separator");
        String result = indent + "<Field name=\"" + HtmlEncoder.sEncode(getName()) +
                "\" type=\"" + getType() + "\" numBits=\"" + getNumBits() +
                "\" relativity=\"" + getRelativity() + "\" signed=\"" + isSigned()
                + "\" defaultValue=\"" + getDefaultValue() +
                "\" id=\"" + getID() + "\">" + nl;
        for (FieldValue value : values)
            result += indent + "\t" + value.getXMLDescription(indent + "\t") + nl;
        result += indent + "</Field>";
        return result;
    }
    
    @Override
    public String getHTMLDescription(String indent) {
        String result = "<TR><TD>" + HtmlEncoder.sEncode(name.get()) +
                "</TD><TD>" + getType() +
                "</TD><TD>" + getNumBits() +
                "</TD><TD>" + getRelativity() +
                "</TD><TD>" + isSigned() +
                // "</TD><TD>" + offset +  //offset is not yet used
                "</TD><TD>" + getDefaultValue() + "</TD><TD>";
        if (values.size() == 0) 
        	result += HtmlEncoder.sEncode("<any>");
        
        for (FieldValue fieldValue : values) {
            result += HtmlEncoder.sEncode(fieldValue.getName() + "=") +
                    fieldValue.getValue() + "<BR>";
        }
        return result + "</TD></TR>";
    }
}