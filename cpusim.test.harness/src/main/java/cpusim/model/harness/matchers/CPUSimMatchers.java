package cpusim.model.harness.matchers;

import com.github.npathai.hamcrestopt.OptionalMatchers;
import cpusim.model.util.IdentifiedObject;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * Assorted {@link Matcher} instances for testing.
 *
 * @since 2016-12-12
 */
public abstract class CPUSimMatchers {
    
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

    public static <T, V>
    Matcher<T> optionalValue(String desc,
                                    Function<T, Optional<V>> accessor,
                                    Function<V, Matcher<V>> matcher,
                                    Optional<V> check) {
        if (check.isPresent()) {
            return hasFeature(desc,
                    accessor,
                    OptionalMatchers.hasValue(matcher.apply(check.get())));
        } else {
            return hasFeature(desc, accessor, OptionalMatchers.isEmpty());
        }
    }
}
