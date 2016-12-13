package cpusim.gui.harness;

import javafx.scene.control.IndexedCell;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 *
 *
 * @since 2016-12-12
 */
public abstract class FXMatchers {

    /**
     * Creates a {@link Matcher} for delegating to a {@code Matcher} against {@link IndexedCell#getItem()} value.
     * @param onItem
     * @param <T>
     * @return
     */
    public static <T, C extends IndexedCell<T>> Matcher<C> forItem(Matcher<? extends T> onItem) {
        return new BaseMatcher<C>() {
            @Override
            public boolean matches(Object item) {
                if (item != null && IndexedCell.class.isAssignableFrom(item.getClass())) {
                    return onItem.matches(((C)item).getItem());
                }

                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("IndexedCell<T> item matches").appendDescriptionOf(onItem);
            }
        };
    }
}
