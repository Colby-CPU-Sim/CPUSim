/*
 * Base.java
 */
package cpusim.gui.util;

/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 11/6/13
 * with the following changes:
 * 
 * 1.) added the unsigned decimal and ascii base
 */

/**
 * This class stores a base ("Bin", "Dec", "Hex", "Unsigned Dec", or "Ascii");
 * for displaying values of registers or RAM cells.
 * @author Ben Borchard
 */
public class Base {
	public static final String BINARY = "Bin";
	public static final String DECIMAL = "Dec";
	public static final String HEX = "Hex";
        public static final String UNSIGNEDDECIMAL = "Unsigned Dec";
        public static final String ASCII = "Ascii";
	
    private String base;
    
    public Base(String base) {
        this.base = base;
    }
    
    public String getBase() {
        return base;
    }
    
    public void setBase(String base) {
        this.base = base;
    }

    public String toString() {
    	return base;
    }
}
