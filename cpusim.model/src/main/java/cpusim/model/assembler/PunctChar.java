/*
 * File: PunctChar.java
 * Author: Dale Skrien
 * Date: Aug 23, 2007
 */
package cpusim.model.assembler;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import cpusim.model.util.LegacyXMLSupported;
import cpusim.xml.HTMLEncodable;
import cpusim.xml.HtmlEncoder;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Text;

/**
 * This class stores the role or use of each punctuation character in the
 * assembly language. The characters involved include 30 characters:
 *  !@#$%^&*()_-+={}[]|\:;"',.?/~`
 */
public class PunctChar implements HTMLEncodable, LegacyXMLSupported {
	
	public enum Use {
		symbol, label, comment, pseudo, token, illegal
	}

	@Text
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
    
	@Attribute
    public Use getUse() {
        return use.get();
    }

	@Attribute
    public void setUse(Use u) {
    	use.setValue(u);
    }

    @Override
    public String getXMLDescription(String indent) {
        return indent + "<PunctChar char=\"" + HtmlEncoder.sEncode(getChar() + "") +
                "\" use=\"" + use.get() + "\" />";
    }

    @Override
    public String getHTMLDescription(String indent) {
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
