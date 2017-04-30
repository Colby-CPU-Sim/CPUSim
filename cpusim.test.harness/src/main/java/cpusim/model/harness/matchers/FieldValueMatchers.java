package cpusim.model.harness.matchers;

import cpusim.model.Field;
import cpusim.model.FieldValue;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * Hamcrest matchers for {@link Field}
 */
public abstract class FieldValueMatchers extends TypedMatcher<FieldValue> {

    private FieldValueMatchers() {
        super(FieldValue.class);
    }

    public static Matcher<FieldValue> properties(FieldValue expected) {
        return compose("FieldValue",
                compose(named(expected.getName()))
                        .and(value(expected.getValue())));
    }

    public static Matcher<FieldValue> named(String name) {
        return NamedObjectMatchers.named(name);
    }

    public static Matcher<FieldValue> value(long value) {
        return hasFeature("value", FieldValue::getValue, equalTo(value));
    }
}
