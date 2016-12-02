package cpusim.model.assembler;

//import java.beans.*;
import cpusim.model.util.Copyable;
import cpusim.model.util.LegacyXMLSupported;
import cpusim.model.util.NamedObject;
import cpusim.xml.HtmlEncoder;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.Serializable;

//for Serializable and StreamTokenizer

public class EQU implements Serializable, NamedObject, Copyable<EQU>, LegacyXMLSupported {
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

    @Override
    public void copyTo(EQU newEQU) {
        newEQU.setName(getName());
        newEQU.setValue(getValue());

    }

    @Override
    public String getXMLDescription(String indent) {
        return indent + "<EQU name=\"" + HtmlEncoder.sEncode(getName()) +
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
