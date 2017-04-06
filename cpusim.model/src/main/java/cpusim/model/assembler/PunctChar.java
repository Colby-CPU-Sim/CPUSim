/*
 * File: PunctChar.java
 * Author: Dale Skrien
 * Date: Aug 23, 2007
 */
package cpusim.model.assembler;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import cpusim.model.util.LegacyXMLSupported;
import cpusim.xml.HTMLEncodable;
import cpusim.xml.HtmlEncoder;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * This class stores the role or use of each punctuation character in the
 * assembly language. The characters involved include 30 characters:
 *  !@#$%^&*()_-+={}[]|\:;"',.?/~`
 */
public class PunctChar implements HTMLEncodable, LegacyXMLSupported {
	
	public enum Use {
		symbol, label, comment, pseudo, token, illegal
	}

    private final ObjectProperty<Character> ch;
	
    private final ObjectProperty<Use> use;

    public PunctChar(@JsonProperty("char") char c, 
    		@JsonProperty("use") @JacksonXmlProperty(isAttribute=true) Use u) {
        ch = new SimpleObjectProperty<>(this, "ch", c);
        use = new SimpleObjectProperty<>(this, "use", u);
    }

    @JsonProperty("char")
    public char getChar() {
        return ch.get();
    }
    
    @JsonProperty("use")
    @JacksonXmlProperty(isAttribute=true)
    public Use getUse() {
        return use.get();
    }

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
    
    public ObjectProperty<Character> charProperty() {
    	return ch;
    }
    
    public ObjectProperty<Use> useProperty() {
    	return use;
    }
    
    public PunctChar copy() {
    	return new PunctChar(getChar(), getUse());
    }
}
