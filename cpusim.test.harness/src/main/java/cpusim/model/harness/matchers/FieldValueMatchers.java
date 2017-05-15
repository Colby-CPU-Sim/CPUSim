package cpusim.model.harness.matchers;

import cpusim.model.FieldValue;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * Hamcrest matchers for {@link FieldValue FieldValues}
 */
public abstract class FieldValueMatchers {

    private FieldValueMatchers() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a {@link Matcher} for {@link FieldValue} parts.
     *
     * @return Matcher
     * @see FieldValue
     */
    public static Matcher<FieldValue> properties(FieldValue expected) {
        return compose("FieldValue",
                compose(NamedObjectMatchers.<FieldValue>named(expected.getName()))
                        .and(value(expected.getValue())));
    }

    /** @see FieldValue#valueProperty() */
    public static Matcher<FieldValue> value(long value) {
        return hasFeature("value", FieldValue::getValue, equalTo(value));
    }
}
