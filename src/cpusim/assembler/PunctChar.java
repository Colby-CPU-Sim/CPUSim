/*
 * File: PunctChar.java
 * Author: Dale Skrien
 * Date: Aug 23, 2007
 */
package cpusim.assembler;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import cpusim.xml.HtmlEncoder;

/**
 * This class stores the role or use of each punctuation character in the
 * assembly language. The characters involved include 30 characters:
 *  !@#$%^&*()_-+={}[]|\:;"',.?/~`
 */
public class PunctChar {
    public static enum Use {symbol, label, comment, pseudo, token, illegal}

    private final SimpleStringProperty ch;
    private SimpleObjectProperty<Use> use;

    public PunctChar(char c, Use u) {
        ch = new SimpleStringProperty();
        ch.setValue(String.valueOf(c)); 
        use = new SimpleObjectProperty<>();
        use.setValue(u);
    }

    public char getChar() {
        return ch.get().charAt(0);
    }

    public Use getUse() {
        return use.get();
    }

    public void setUse(Use u) {
    	use.setValue(u);
    }

    public String getXMLDescription(String indent) {
        return indent + "<PunctChar char=\"" + HtmlEncoder.sEncode(getChar() + "") +
                "\" use=\"" + use.get() + "\" />";
    }

    public String getHTMLDescription() {
        return "<TR><TD>" + HtmlEncoder.sEncode(getChar() + "") + "</TD><TD>" +
                use.get() + "</TD></TR>";
    }
    
    public SimpleStringProperty CharProperty() {
    	return ch;
    }
    
    public SimpleObjectProperty<Use> UseProperty() {
    	return use;
    }
    
    public PunctChar copy() {
    	return new PunctChar(getChar(), getUse());
    }
}
