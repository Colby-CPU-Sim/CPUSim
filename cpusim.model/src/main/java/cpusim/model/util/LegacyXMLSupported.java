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

	public String getXMLDescription(String indent);
}
