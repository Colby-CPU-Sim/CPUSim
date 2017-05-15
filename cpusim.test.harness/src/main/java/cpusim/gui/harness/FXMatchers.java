package cpusim.gui.harness;

import javafx.scene.control.Cell;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.function.Function;

/**
 *
 *
 * @since 2016-12-12
 */
public abstract class FXMatchers {

    /**
     * Creates a {@link Matcher} for delegating to a {@code Matcher} against {@link Cell#getItem()} value.
     * @param onItem
     * @param <T>
     * @return
     */
    public static <T, C extends Cell<T>> Matcher<C> forItem(Matcher<? extends T> onItem) {
        return new BaseMatcher<C>() {
            @Override @SuppressWarnings("unchecked")
            public boolean matches(Object item) {
                if (item != null && Cell.class.isAssignableFrom(item.getClass())) {
                    return onItem.matches(((C)item).getItem());
                }

                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(" Cell<T> item matches ").appendDescriptionOf(onItem);
            }
        };
    }
    
    public static <T, C> Matcher<C> allItems(Class<C> klass,
                                             Function<C, ? extends Iterable<? extends T>> mapper,
                                             Matcher<? extends Iterable<? extends T>> onItem) {
        return new BaseMatcher<C>() {
            @Override
            public boolean matches(final Object item) {
                if (item != null) {
                    if (klass.isAssignableFrom(item.getClass())) {
                        return onItem.matches(mapper.apply((C)item));
                    }
                }
                
                return false;
            }
    
            @Override
            public void describeTo(final Description description) {
                description.appendValue(klass.getCanonicalName())
                        .appendText(" items all ")
                        .appendDescriptionOf(onItem);
            }
        };
    }
    
    public static <T, C> Matcher<C> hasValue(Class<C> klass,
                                             Function<C, ? extends T> mapper,
                                             Matcher<? extends T> forValue) {
        return new BaseMatcher<C>() {
            @Override
            public boolean matches(final Object item) {
                if (item != null) {
                    if (klass.isAssignableFrom(item.getClass())) {
                        return forValue.matches(mapper.apply((C)item));
                    }
                }
                
                return false;
            }
            
            @Override
            public void describeTo(final Description description) {
                description.appendValue(klass.getCanonicalName())
                        .appendText(" item value ")
                        .appendDescriptionOf(forValue);
            }
        };
    }
    
    
}
