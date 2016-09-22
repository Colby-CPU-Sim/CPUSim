/**
 * 
 */
package cpusim.xml;

/**
 * Defines that a type can be encoded into HTML.
 * 
 * TODO Can this be done better?
 * 
 * @author Kevin Brightwell
 * @since 2016-09-20
 */
public interface HTMLEncodable {

	/**
	 * Gets the HTML description of a structure.
	 * 
	 * @return String with valid HTML
	 */
	public String getHtmlDescription();
}
