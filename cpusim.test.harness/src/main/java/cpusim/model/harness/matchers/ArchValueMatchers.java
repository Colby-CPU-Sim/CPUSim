package cpusim.model.harness.matchers;

import cpusim.model.util.units.ArchType;
import cpusim.model.util.units.ArchValue;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * {@link Matcher Matchers} for {@link ArchValue}
 */
public class ArchValueMatchers {

    public static Matcher<ArchValue> equalTo(ArchValue value) {
        return new TypeSafeMatcher<ArchValue>(ArchValue.class) {
            @Override
            public boolean matchesSafely(ArchValue item) {
                return value.as(ArchType.Bit) == item.as(ArchType.Bit);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(" has value ")
                        .appendValue(value.getValue())
                        .appendText(" ")
                        .appendValue(value.getType());
            }
        };
    }
}
