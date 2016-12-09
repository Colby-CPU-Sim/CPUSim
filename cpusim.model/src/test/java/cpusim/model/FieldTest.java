/**
 * 
 */
package cpusim.model;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import cpusim.model.util.IdentifiedObject;
import org.junit.Test;

import javax.crypto.Mac;
import java.util.UUID;

/**
 *
 * @since 2016-09-20
 */
public class FieldTest {

	/**
	 * Test method for:
	 */
	@Test
	public void testField() {
		final Field f = new Field("?", UUID.randomUUID(), mock(Machine.class));
		
		assertEquals("?", f.getName());
	}

}
