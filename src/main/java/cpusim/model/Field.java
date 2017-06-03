/**
 * File: Field
 * Last Update: August 2013
 *
 * @author Kevin Marsteller & Dale Skrien
 */
package cpusim.model;

import cpusim.util.NamedObject;
import cpusim.xml.HtmlEncoder;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;

/**
 * Class used to hold field options for each field of 
 * a machine instruction.
 */
public class Field implements NamedObject, Cloneable
{

    public enum Relativity
    {
        absolute, pcRelativePreIncr, pcRelativePostIncr
    }

    public enum Type
    {
        required, optional, ignored
    }

    // The name of the field
    private SimpleStringProperty name;
    // One of the three values of enum Type
    private SimpleObjectProperty<Type> type;
    // The number of bits of the field
    private SimpleIntegerProperty numBits;
    // Absolute or pc relative
    private SimpleObjectProperty<Relativity> relativity;
    // The acceptable values for this field
    private ObservableList<FieldValue> values;
    // The value to use if optional or ignored
    private SimpleLongProperty defaultValue;

    // If true, allows only signed 2's complement values
    // If false, allows only unsigned binary values
    private SimpleBooleanProperty signed;

    private String ID; //used for saving to XML


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
        this(name, Type.required, 0, Relativity.absolute, FXCollections
                .observableArrayList(new ArrayList<>()), 0, true);
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
     */
    public Field(String name, Type type, int length, Relativity relativity,
                 ObservableList<FieldValue> values, long defaultValue, boolean signed) {
        this.name = new SimpleStringProperty(name);
        this.type = new SimpleObjectProperty<>(type);
        this.numBits = new SimpleIntegerProperty(length);
        this.relativity = new SimpleObjectProperty<>(relativity);
        this.defaultValue = new SimpleLongProperty(defaultValue);
        this.signed = new SimpleBooleanProperty(signed);
        this.values = values;

        String s = super.toString();
        int index = s.indexOf('@');
        if (index == -1) {
            this.ID = s;
        }
        else {
            this.ID = s.substring(7, index) + s.substring(index + 1);
        }
    }

    ////////////////// Setters and getters //////////////////

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public Type getType() {
        return type.get();
    }

    public void setType(Type type) {
        this.type.set(type);
    }

    public int getNumBits() {
        return numBits.get();
    }

    public void setNumBits(int numBits) {
        this.numBits.set(numBits);
    }

    public Relativity getRelativity() {
        return relativity.get();
    }

    public void setRelativity(Relativity relativity) {
        this.relativity.set(relativity);
    }

    public ObservableList<FieldValue> getValues() { return values; }

    public void setValues(ObservableList<FieldValue> values) {
        this.values = values;
    }

    public long getDefaultValue() {
        return defaultValue.get();
    }

    public void setDefaultValue(long defaultValue) { this.defaultValue.set
            (defaultValue); }

    public boolean isSigned() {
        return signed.get();
    }

    public void setSigned(boolean signed) {
        this.signed.set(signed);
    }

    public SimpleBooleanProperty signedProperty() {
        return signed;
    }

    public String getID() {
        return ID;
    }

    /**
     * Puts all the data in this field into
     * the specified field.
     *
     * @param newField - The specified field that
     * the data should be copied into.
     */
    public void copyDataTo(Field newField) {
        newField.setName(getName());
        newField.setType(getType());
        newField.setRelativity(getRelativity());
        newField.setNumBits(getNumBits());
        newField.setDefaultValue(getDefaultValue());
        newField.setSigned(isSigned());
        newField.setValues(getValues());
    }

    /**
     * returns the FieldValue with the given name.
     * returns null if there is no such FieldValue
     * @param name the String with the name of the value
     * @return the FieldValue with that name, or null if none exists
     */
    public FieldValue getValue(String name) {
        for (FieldValue value : getValues()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        return null;
    }

    /**
     * Gives a string representation of this object.
     */
    public String toString() {
        return name.get();
    }

    /**
     * Returns a clone of this field.
     */
    public Object clone() {
        Field c = null;
        try {
            c = (Field) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace(); //should never happen since Fields implement Cloneable
        }
        c.name = new SimpleStringProperty(name.get());
        c.type = new SimpleObjectProperty<>(type.get());
        c.numBits = new SimpleIntegerProperty(numBits.get());
        c.relativity = new SimpleObjectProperty<>(relativity.get());
        c.defaultValue = new SimpleLongProperty(defaultValue.get());
        c.signed = new SimpleBooleanProperty(signed.get());
        c.values = FXCollections.observableArrayList(new ArrayList<>());
        c.values.addAll(this.values); // don't clone the FieldValues

        String s = super.toString();
        int index = s.indexOf('@');
        if (index == -1) {
            c.ID = s;
        }
        else {
            c.ID = s.substring(7, index) + s.substring(index + 1);
        }
        return c;
    }

    /**
     * clones the list of FieldValues
     * @param values the list of FieldValues to be cloned
     * @return the new list of clones of hte FieldValues
     */
    private ObservableList<FieldValue> copyOfFieldValues(ObservableList<FieldValue>
                                                                 values) {
        ObservableList<FieldValue> result = FXCollections.observableArrayList();
        for (FieldValue value : values) {
            result.add(new FieldValue(value.getName(), value.getValue()));
        }
        return result;
    }

    /**
     * Gives the XMLDescription of this Field.
     *
     * @param indent - amount of indent. String of spaces.
     * @return - The XML description.
     */
    public String getXMLDescription(String indent) {
        String nl = System.getProperty("line.separator");
        String result = indent + "<Field name=\"" + HtmlEncoder.sEncode(getName()) +
                "\" type=\"" + getType() + "\" numBits=\"" + getNumBits() + "\" " +
                "relativity=\"" + getRelativity() + "\" signed=\"" + isSigned() + "\" " +
                "defaultValue=\"" + getDefaultValue() + "\" id=\"" + getID() + "\">" + nl;
        for (FieldValue value : values)
            result += indent + "\t" + value.getXMLDescription() + nl;
        result += indent + "</Field>";
        return result;
    }

    /**
     * Gives an HTML description of this Field.
     *
     * @return Gives an HTML description of this Field.
     */
    public String getHTMLDescription() {
        String result = "<TR><TD>" + HtmlEncoder.sEncode(name.get()) + "</TD><TD>" +
                getType() + "</TD><TD>" + getNumBits() + "</TD><TD>" + getRelativity()
                + "</TD><TD>" + isSigned() +
                // "</TD><TD>" + offset +  //offset is not yet used
                "</TD><TD>" + getDefaultValue() + "</TD><TD>";
        if (values.size() == 0) {
            result += HtmlEncoder.sEncode("<any>");
        }
        for (FieldValue fieldValue : values) {
            result += HtmlEncoder.sEncode(fieldValue.getName() + "=") + fieldValue
                    .getValue() + "<BR>";
        }
        return result + "</TD></TR>";
    }
}