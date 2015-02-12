/**
 * File: FieldValue
 * Author: Dale Skrien
 * Last update: August 2013
 */
package cpusim;

import cpusim.assembler.EQU;
import cpusim.xml.HtmlEncoder;

public class FieldValue extends EQU
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new Field Value with specified name
	 * and value.
	 * 
	 * @param name - Name of new FieldValue.
	 * @param value - long value of new FieldValue.
	 */
	public FieldValue(String name, long value) {
        super(name,value);
    }
	
	/**
     * Gives the XML description of this FieldValue.
     */
	@Override
	public String getXMLDescription() {
        return "<FieldValue name=\"" + HtmlEncoder.sEncode(getName()) +
                "\" value=\"" + getValue() + "\" />";
    }

	/**
     * Gives a clone of this FieldValue.
     */
    @Override
    public Object clone() {
        return new FieldValue(getName(), getValue());
    }
    
    /**
     * Returns a string representation of the object.
     */
    @Override
    public String toString() {
        return this.getName();
    }

}
