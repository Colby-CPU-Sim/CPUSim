package cpusim.util;

import cpusim.util.ReflectiveProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by kevin on 27/04/2017.
 */
public class ReflectivePropertyTest {

    ReflectiveProperty<Foo, String> readOnly = new ReflectiveProperty<>(Foo.class, "readOnly");
    ReflectiveProperty<Foo, String> nonReadOnly = new ReflectiveProperty<>(Foo.class, "NonReadOnly");

    @Test
    public void getPropertyName() throws Exception {
        assertEquals("Failed normally",
                "readOnlyProperty", readOnly.getPropertyMethodName());
        assertEquals("Failed with capital first letter",
                "nonReadOnlyProperty", nonReadOnly.getPropertyMethodName());
    }

    @Test
    public void getPropMethod() throws Exception {
        assertEquals(Foo.class.getMethod("readOnlyProperty"), readOnly.getPropMethod());
        assertEquals(Foo.class.getMethod("nonReadOnlyProperty"), nonReadOnly.getPropMethod());
    }

    @Test
    public void getProperty() throws Exception {
        Foo underTest = new Foo();

        assertEquals("Normal letters",
                underTest.readOnly, readOnly.getProperty(underTest));
        assertEquals("Capital letter start",
                underTest.nonReadOnly, nonReadOnly.getProperty(underTest));
    }

    public static final String NON_READ_ONLY = "foo";
    public static final String READ_ONLY = "bar";

    static class Foo {
        final StringProperty nonReadOnly = new SimpleStringProperty(this, "NonReadOnly", NON_READ_ONLY);
        final ReadOnlyStringProperty readOnly = new SimpleStringProperty(this, "readOnly", READ_ONLY);

        public String getNonReadOnly() {
            return nonReadOnly.get();
        }

        public StringProperty nonReadOnlyProperty() {
            return nonReadOnly;
        }

        public void setNonReadOnly(String nonReadOnly) {
            this.nonReadOnly.set(nonReadOnly);
        }

        public String getReadOnly() {
            return readOnly.get();
        }

        public ReadOnlyStringProperty readOnlyProperty() {
            return readOnly;
        }
    }
}