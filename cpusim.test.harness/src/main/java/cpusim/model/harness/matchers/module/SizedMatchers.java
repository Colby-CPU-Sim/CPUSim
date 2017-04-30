package cpusim.model.harness.matchers.module;

import cpusim.model.Field;
import cpusim.model.module.Module;
import cpusim.model.module.Sized;
import cpusim.model.util.units.ArchValue;
import org.hamcrest.Matcher;
import org.hobsoft.hamcrest.compose.ComposeMatchers;

import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * Hamcrest matchers for {@link Field}
 */
public abstract class SizedMatchers {

    private SizedMatchers() {
        // no instantiate
    }

    public static <T extends Module<T> & Sized<T>>
    Matcher<T> properties(T expected) {
        return ComposeMatchers.<T>compose("Sized", width(expected.getWidth()));
    }

    public static <T extends Module<T> & Sized<T>>
    Matcher<T> width(int width) {
        return width(ArchValue.bits(width));
    }
    
    public static <T extends Module<T> & Sized<T>>
    Matcher<T> width(ArchValue width) {
        return hasFeature("width", s -> ArchValue.bits(s.getWidth()), equalTo(width));
    }
}
