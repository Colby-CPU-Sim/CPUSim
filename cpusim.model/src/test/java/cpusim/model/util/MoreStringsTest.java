/**
 * 
 */
package cpusim.model.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * 
 * @author Kevin Brightwell
 * @since 2016-10-11
 */
public class MoreStringsTest {

	/**
	 * Test method for {@link cpusim.model.util.MoreStrings#insertSpacesInString(CharSequence, int)}.
	 */
	@Test
	public void testInsertSpacesInString() {
		assertEquals("1 2345 6789", MoreStrings.insertSpacesInString("123456789", 4));
		assertEquals("1", MoreStrings.insertSpacesInString("1", 4));
		assertEquals("1234", MoreStrings.insertSpacesInString("1234", 4));
		assertEquals(" 12  34 ", MoreStrings.insertSpacesInString(" 12 34 ", 4));
	}

	/**
	 * Test method for {@link cpusim.model.util.MoreStrings#removeAllWhiteSpace(java.lang.String)}.
	 */
	@Test
	public void testRemoveAllWhiteSpace() {
		assertEquals("12345", MoreStrings.removeAllWhiteSpace(" 1 2 3    4\n\r\n5  "));
	}

	/**
	 * Test method for {@link cpusim.model.util.MoreStrings#removeLeadingWhitespace(java.lang.String)}.
	 */
	@Test
	public void testRemoveLeadingWhitespace() {
		assertEquals("123\n\r\n5  ", MoreStrings.removeLeadingWhitespace("   \r\n\n123\n\r\n5  "));
	}

	/**
	 * Test method for {@link cpusim.model.util.MoreStrings#capitalizeFirstLetter(java.lang.String)}.
	 */
	@Test
	public void testCapitalizeFirstLetter() {
		assertEquals("", MoreStrings.capitalizeFirstLetter(""));
		assertEquals("Abc", MoreStrings.capitalizeFirstLetter("abc"));
		assertEquals(" abc", MoreStrings.capitalizeFirstLetter(" abc"));
	}

}
