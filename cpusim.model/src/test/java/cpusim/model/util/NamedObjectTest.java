/**
 * 
 */
package cpusim.model.util;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.junit.Test;

import com.google.common.collect.Lists;

/**
 *
 * @since 2016-10-11
 */
public class NamedObjectTest {

	private static class Foo implements NamedObject {

		private StringProperty name;
		
		Foo(final String name) {
			this.name = new SimpleStringProperty(name);
		}

		@Override
		public StringProperty nameProperty() {
			return name;
		}
	}
	
	final static List<Foo> foos = Lists.newArrayList(new Foo("1"), new Foo("2"));
	
	/**
	 * Test method for {@link cpusim.model.util.NamedObject#toNamedMap(java.lang.Iterable)}.
	 */
	@Test
	public void testToNamedMap() {
		final Map<String, Foo> actual = NamedObject.toNamedMap(foos);
		
		final Map<String, Foo> expected = foos.stream()
				.collect(Collectors.toMap(NamedObject::getName, Function.identity()));
		assertEquals(expected, actual);
	}

}
