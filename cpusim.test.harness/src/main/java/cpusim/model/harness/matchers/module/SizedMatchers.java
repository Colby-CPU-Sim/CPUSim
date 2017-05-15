package cpusim.model.harness.matchers.module;

import cpusim.model.module.Module;
import cpusim.model.module.Sized;
import cpusim.model.util.units.ArchValue;
import org.hamcrest.Matcher;
import org.hobsoft.hamcrest.compose.ConjunctionMatcher;

import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * Hamcrest {@link Matcher Matchers} for {@link Sized}
 */
public abstract class SizedMatchers {

    private SizedMatchers() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a {@link Matcher} for a {@link Sized} component.
     *
     * @return Matcher
     * @see Sized
     */
    public static <T extends Module<T> & Sized<T>>
    ConjunctionMatcher<T> sized(T expected) {
        return compose("sized", width(expected.getWidth()));
    }

    /** @see Sized#widthProperty() */
    public static <T extends Module<T> & Sized<T>>
    Matcher<T> width(int width) {
        return width(ArchValue.bits(width));
    }

    /** @see Sized#widthProperty() */
    public static <T extends Module<T> & Sized<T>>
    Matcher<T> width(ArchValue width) {
        return hasFeature("width", s -> ArchValue.bits(s.getWidth()), equalTo(width));
    }
}
