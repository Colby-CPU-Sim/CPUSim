/**
 * File: Field
 * Last Update: August 2013
 * @author Kevin Marsteller & Dale Skrien
 */
package cpusim.model;

import cpusim.model.util.Copyable;
import cpusim.model.util.LegacyXMLSupported;
import cpusim.model.util.MoreFXCollections;
import cpusim.model.util.NamedObject;
import cpusim.model.util.units.ArchType;
import cpusim.model.util.units.ArchValue;
import cpusim.xml.HTMLEncodable;
import cpusim.xml.HtmlEncoder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

//import cpusim.xml.HtmlEncoder;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Class used to hold field options for each field of 
 * a machine instruction.
 */
public class Field implements NamedObject, Cloneable, Copyable<Field>, LegacyXMLSupported, HTMLEncodable {

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
    }

    /**
     * The name of the field
     */
    private SimpleStringProperty name;    
    
    /**
     * One of the three values of enum Type
     */
    private SimpleObjectProperty<Type> type;  
    
    /**
     * The number of bits of the field
     */
    private SimpleObjectProperty<ArchValue> numBits;          	
    
    /**
     *  Absolute or pc relative
     */
    private SimpleObjectProperty<Relativity> relativity;
    
    /**
     * The acceptable values for this field
     */
    private ObservableMap<String, FieldValue> values;  		
    
    /**
     * The value to use if optional or ignored
     */
    private SimpleLongProperty defaultValue;

    /**
     * If Signed, allows only signed 2's complement values otherwise allows only unsigned binary values
     */
    private SimpleObjectProperty<SignedType> signed; 


    /** 
     * Constructor for Field with name "?".
     */
    public Field() {
        this("?");
    }
    
    /**
     * Constructor for new Field with specified Name.
     * Other values are set to defaults.
     * 
     * @param name - The name of the field.
     */
    public Field(String name) {
        this(name, Type.required, ArchType.Bit.of(0), Relativity.absolute,
             FXCollections.emptyObservableMap(), 0, SignedType.Signed);
    }
    
    /**
     * Copy constructor that copies <code>other</code> into <code>this</code>.
     * 
     * @param other
     * @throws NullPointerException if <code>other</code> is <code>null</code>.
     */
    public Field(final Field other) {
    	checkNotNull(other);
    	
    	this.name = new SimpleStringProperty(other.name.getValue()); 
    	this.type = new SimpleObjectProperty<>(other.type.getValue());
        this.numBits = new SimpleObjectProperty<>(other.numBits.getValue());
        this.relativity = new SimpleObjectProperty<>(other.relativity.getValue());
        this.defaultValue = new SimpleLongProperty(other.defaultValue.getValue().longValue());
        this.signed = new SimpleObjectProperty<>(other.signed.getValue());
    	this.values = MoreFXCollections.copyObservableMap(other.getValues());
    }
    
    /**
     * Constructor for new Field with specified values.
     * 
     * @param name - Name of Field.
     * @param type - Type of Field.
     * @param length - Numbits of field.
     * @param relativity - Enum relativity type.
     * @param values - Map of name to {@link FieldValue}.
     * @param defaultValue - The default int value.
     * @param signed - Whether or not the Field is a signed int.
     */
    @JsonCreator
    public Field(@JsonProperty("name") String name, 
    			 @JsonProperty("type") Type type, 
    			 @JsonProperty("numBits") ArchValue length, 
    			 @JsonProperty("relativity") Relativity relativity,
    			 @JsonProperty("values") ObservableMap<String, FieldValue> values, 
    			 @JsonProperty("defaultValue") long defaultValue, 
    			 @JsonProperty("signed") SignedType signed) {
        this.name = new SimpleStringProperty();
        this.type = new SimpleObjectProperty<>();
        this.numBits = new SimpleObjectProperty<>();
        this.relativity = new SimpleObjectProperty<>();
        this.defaultValue = new SimpleLongProperty();
        this.signed = new SimpleObjectProperty<>();
        
        this.name.set(checkNotNull(name));
        this.type.set(checkNotNull(type));
        this.numBits.set(checkNotNull(length));
        this.relativity.set(checkNotNull(relativity));
        this.values = values;
        this.defaultValue.set(defaultValue);
        this.signed.set(checkNotNull(signed));
    }
    
    /**
     * Constructor for new Field with specified values.
     * 
     * @param name - Name of Field.
     * @param type - Type of Field.
     * @param length - Numbits of field.
     * @param relativity - Enum relativity type.
     * @param values - List of FieldValues.
     * @param defaultValue - The default int value.
     * @param signed - Whether or not the Field is a signed int.
     * 
     * @deprecated
     * @since 2016-09-20
     */
    @Deprecated
    public Field(String name, 
    			 Type type, 
    			 ArchValue length, 
    			 Relativity relativity,
    			 ObservableList<FieldValue> values, 
    			 long defaultValue, 
    			 SignedType signed) {
        this(name, type, length, relativity, 
        		FXCollections.observableMap(values.stream()
        				.collect(Collectors.toMap(FieldValue::getName, f -> f))), 
        		defaultValue, signed);
    }

    ////////////////// Setters and getters //////////////////
    
    @Override
    public String getName() {
        return name.get();
    }

    @Override 
    public void setName(String name) {
        this.name.set(name);
    }

    @JsonProperty
    public Type getType() {
        return type.get();
    }

    public void setType(Type type) {
        this.type.set(type);
    }

    @JsonProperty
    public ArchValue getNumBits() {
        return numBits.get();
    }

    public void setNumBits(ArchValue numBits) {
        this.numBits.set(checkNotNull(numBits));
    }

    public Relativity getRelativity() {
        return relativity.get();
    }

    public void setRelativity(Relativity relativity) {
        this.relativity.set(relativity);
    }

    public ObservableMap<String, FieldValue> getValues() { return values; }

    public void setValues(ObservableMap<String, FieldValue> values) {
        this.values = values;
    }

    public long getDefaultValue() {
        return defaultValue.get();
    }

    public void setDefaultValue(long defaultValue) { 
    	this.defaultValue.set(defaultValue); 
    }

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

    public SimpleObjectProperty<SignedType> signedProperty() {
        return signed;
    }
    
    @Override
    public void copyTo(final Field newField) {
    	checkNotNull(newField);
    	
        newField.setName(getName());
        newField.setType(getType());
        newField.setRelativity(getRelativity());
        newField.setNumBits(getNumBits());
        newField.setDefaultValue(getDefaultValue());
        newField.setSigned(getSigned());
        newField.setValues(getValues());
    }

    /**
     * returns the FieldValue with the given name.
     * returns null if there is no such FieldValue
     * @param name the String with the name of the value
     * @return the FieldValue with that name, or null if none exists
     */
    public FieldValue getValue(String name) {
        return values.get(name);
    }

    /**
     * Gives a string representation of this object.
     */
    @Override
    public String toString() {
        return name.get();
    }

    /**
     * Returns a clone of this field.
     */
    @Override @Deprecated
    public Object clone() {
        Field c = null;
        try {
            c = (Field) super.clone();
            c.setName(name.get());
            c.setType(type.get());
            c.setNumBits(numBits.get());
            c.setRelativity(relativity.get());
            c.setValues(MoreFXCollections.copyObservableMap(values));
            c.setDefaultValue(defaultValue.get());
            c.setSigned(signed.get());
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e); // should never happen because Field implements Clonable
        }
        
        return c;
    }

    /**
     * Gives the XMLDescription of this Field.
     * 
     * @param indent - amount of indent. String of spaces.
     * @return - The XML description.
     */
    @Override
    public String getXMLDescription(String indent) {
        String nl = System.getProperty("line.separator");
        String result = indent + "<Field name=\"" + HtmlEncoder.sEncode(getName()) +
                "\" type=\"" + getType() + "\" numBits=\"" + getNumBits().as() +
                "\" relativity=\"" + getRelativity() + "\" signed=\"" + isSigned()
                + "\" defaultValue=\"" + getDefaultValue() +
                "\" id=\"" + getID() + "\">" + nl;
        for (FieldValue value : values.values())
            result += indent + "\t" + value.getXMLDescription(indent + "\t") + nl;
        result += indent + "</Field>";
        return result;
    }
    
    /**
     * Gives an HTML description of this Field.
     * 
     * @return Gives an HTML description of this Field.
     */
    @Override
    public String getHtmlDescription() {
        String result = "<TR><TD>" + HtmlEncoder.sEncode(name.get()) +
                "</TD><TD>" + getType() +
                "</TD><TD>" + getNumBits() +
                "</TD><TD>" + getRelativity() +
                "</TD><TD>" + isSigned() +
                // "</TD><TD>" + offset +  //offset is not yet used
                "</TD><TD>" + getDefaultValue() + "</TD><TD>";
        if (values.size() == 0) 
        	result += HtmlEncoder.sEncode("<any>");
        
        for (FieldValue fieldValue : values.values()) {
            result += HtmlEncoder.sEncode(fieldValue.getName() + "=") +
                    fieldValue.getValue() + "<BR>";
        }
        return result + "</TD></TR>";
    }
}