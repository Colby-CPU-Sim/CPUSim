package cpusim.model.harness;

import cpusim.model.util.IdentifiedObject;
import cpusim.model.util.NamedObject;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.UUID;

/**
 * Assorted {@link Matcher} instances for testing.
 *
 * @since 2016-12-12
 */
public abstract class CPUSimMatchers {

    /**
     * Returns a {@link Matcher} implementation for a {@link NamedObject}, checking the name.
     * @param name Name to check against, uses {@link String#equals(Object)}.
     * @param <T>
     * @return Matcher instance for use in JUnit.
     */
    public static <T extends NamedObject> Matcher<T> isNamed(final String name) {
        return new BaseMatcher<T>() {
            @Override
            public boolean matches(Object item) {
                if (item != null && NamedObject.class.isAssignableFrom(item.getClass())) {
                    return ((NamedObject)item).getName().equals(name);
                }

                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("name should be").appendText(name);
            }
        };
    }

    /**
     * Returns a {@link Matcher} implementation for a {@link IdentifiedObject}, checking the
     * {@link IdentifiedObject#getID()} value.
     *
     * @param uuid {@link UUID} to check against.
     * @param <T>
     * @return Matcher instance for use in JUnit.
     */
    public static <T extends IdentifiedObject> Matcher<T> isId(final UUID uuid) {
        return new BaseMatcher<T>() {
            @Override
            public boolean matches(Object item) {
                if (item != null && IdentifiedObject.class.isAssignableFrom(item.getClass())) {
                    return ((IdentifiedObject)item).getID().equals(uuid);
                }

                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("ID should be").appendValue(uuid);
            }
        };
    }

    /**
     * Returns a {@link Matcher} implementation for a {@link IdentifiedObject}, checking the
     * {@link IdentifiedObject#getID()} value.
     *
     * @param object Object with an ID to check for
     * @param <T>
     * @return Matcher instance for use in JUnit
     *
     * @see #isId(UUID)
     */
    public static <T extends IdentifiedObject> Matcher<T> isId(final IdentifiedObject object) {
        return isId(object.getID());
    }
}
