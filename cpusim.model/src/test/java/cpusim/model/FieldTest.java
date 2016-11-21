/**
 * 
 */
package cpusim.model;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * 
 * @author Kevin Brightwell
 * @since 2016-09-20
 */
public class FieldTest {

	/**
	 * Test method for:
	 * 
	 *   * {@link cpusim.model.Field#Field()}.
	 *   * {@link cpusim.model.Field#Field(java.lang.String)}.
	 */
	@Test
	public void testField() {
		final Field f = new Field();
		
		assertEquals("?", f.getName());
		
		final Field f2 = new Field("f2");
		assertEquals("f2", f2.getName());
	}

}
