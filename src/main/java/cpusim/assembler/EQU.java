package cpusim.assembler;

import cpusim.util.*;
import cpusim.xml.*;

//import java.beans.*;
import java.io.*;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

//for Serializable and StreamTokenizer

public class EQU implements Cloneable, Serializable, NamedObject {
	private static final long serialVersionUID = 1L;
	// instance variables
    private SimpleStringProperty name;
    private SimpleLongProperty value;

    public EQU(String name, long value) {
        this.name = new SimpleStringProperty(name);
        this.value = new SimpleLongProperty(value);
    }
    
    //////////////////// Setters and Getters ////////////////////
    public void setName(String newName) {
        name.set(newName);
    }

    public String getName() {
        return name.get();
    }
    
    public SimpleStringProperty nameProperty() {
    	return name;
    }

    public void setValue(long newValue) {
        value.set(newValue);
    }

    public long getValue() {
        return value.get();
    }
    
    public SimpleLongProperty valueProperty() {
        return value;
    }

    //////////////////// Other Helpful Methods ////////////////////
    
    public Object clone() {
        return new EQU(getName(), getValue());
    } 

    public void copyDataTo(EQU newEQU) {
        newEQU.setName(getName());
        newEQU.setValue(getValue());

    }

    public String getXMLDescription() {
        return "<EQU name=\"" + HtmlEncoder.sEncode(getName()) +
                "\" value=\"" + getValue() + "\" />";
    }

    public String getHTMLDescription() {
        return "<TR><TD>" + HtmlEncoder.sEncode(getName()) + "</TD><TD>" +
                getValue() + "</TD></TR>";
    }

//    public static EQU createEQUFromDescription(StreamTokenizer tokenizer)
//            throws IOException {
//        String name = tokenizer.sval;
//        tokenizer.nextToken();  //read the value
//        long value = (long) tokenizer.nval;
//
//        return new EQU(name, value);
//    }
    
} 
