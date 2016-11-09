/**
 * 
 */
package cpusim.model.util.conversion;

import static org.junit.Assert.*;

import cpusim.model.Machine;

import org.junit.Test;

/**
 * 
 * @author Kevin Brightwell
 * @since 2016-11-09
 */
public class ConvertStringsTest {
	

	private Machine m = new Machine("test");

	/**
	 * Test method for {@link cpusim.model.util.conversion.ConvertStrings#formatStringToFields(java.lang.String, cpusim.model.Machine)}.
	 */
	@Test
	public void testFormatStringToFieldsEmptyString() {
		
		// This used to throw an NPE, it should return an empty List.
		assertTrue(ConvertStrings.formatStringToFields("", m).isEmpty());
	}

}
