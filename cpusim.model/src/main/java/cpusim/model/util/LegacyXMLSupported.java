/**
 * 
 */
package cpusim.model.util;

/**
 * Used to mark legacy XML support. 
 * 
 * TODO Deprecate in favour of simple-xml
 *
 * @since 2016-09-20
 */
public interface LegacyXMLSupported {

	/**
	 * Get an XML description of the element.
	 * @param indent Current indentation
	 * @return XML String
	 */
	String getXMLDescription(String indent);
	
	/**
	 * Added for legacy reasons. 
	 * 
	 * @return XML String
	 * 
	 * @deprecated Use {@link #getXMLDescription(String)} instead.
	 * 
	 * @since 2016-10-12
	 */
	default String getXMLDescription() {
		return this.getXMLDescription("");
	}
}
