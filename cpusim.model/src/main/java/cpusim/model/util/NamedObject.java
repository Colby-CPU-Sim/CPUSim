package cpusim.model.util;

/**
 * Defines that an object has a `name` and `ID` property.
 * @author Josh Ladieu
 * @since 2000-11-01
 */
public interface NamedObject extends Cloneable
{
	/**
     * Get the name of a component
     */
    public String getName();
    
    /**
     * Set the name of a component
     */
    public void setName(String name);
    
    /**
     * Unique Identifier for a {@link NamedObject}
     * @return
     * 
     * @author Kevin Brightwell
     * @since 2016-09-20
     */
    public default String getID() {
	    final String s = this.toString();
	    final int index = s.indexOf('@');
	            
        if(index == -1) { 
            return s; 
        } else  {
            return s.substring(7, index) + s.substring(index + 1);
        }
    }
}