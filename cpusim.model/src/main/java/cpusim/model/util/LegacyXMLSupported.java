/**
 * 
 */
package cpusim.model.util;

/**
 * Used to mark legacy XML support. 
 * 
 * TODO Deprecate in favour of simple-xml
 * @author Kevin Brightwell
 * @since 2016-09-20
 */
public interface LegacyXMLSupported {

	/**
	 * Get an XML description of the element.
	 * @param indent
	 * @return
	 */
	public String getXMLDescription(String indent);
	
	/**
	 * Added for legacy reasons. 
	 * 
	 * @return
	 * 
	 * @deprecated Use {@link #getXMLDescription(String)} instead.
	 * 
	 * @since 2016-10-12
	 */
	public default String getXMLDescription() {
		return this.getXMLDescription("");
	}
}
