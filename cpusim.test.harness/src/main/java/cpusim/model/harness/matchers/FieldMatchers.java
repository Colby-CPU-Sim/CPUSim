package cpusim.model.harness.matchers;

import cpusim.model.Field;
import cpusim.model.FieldValue;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * Hamcrest matchers for {@link Field}
 */
public abstract class FieldMatchers {

    private FieldMatchers() {
        // no instantiate
    }

    public static Matcher<Field> field(Field expected) {
        return compose("Field",
                compose(named(expected.getName()))
                    .and(type(expected.getType()))
                    .and(numBits(expected.getNumBits()))
                    .and(relativeTo(expected.getRelativity()))
                    .and(defaultValue(expected.getDefaultValue()))
                    .and(isSigned(expected.getSigned()))
                    .and(fieldValues(expected.getValues())));
    }

    public static Matcher<Field> named(String name) {
        return NamedObjectMatchers.named(name);
    }

    public static Matcher<Field> type(Field.Type type) {
        return hasFeature("type", Field::getType, equalTo(type));
    }

    public static Matcher<Field> numBits(int numbits) {
        return hasFeature("bit width", Field::getNumBits, equalTo(numbits));
    }

    public static Matcher<Field> relativeTo(Field.Relativity relativity) {
        return new TypedMatcher<Field>(Field.class) {
            @Override
            public boolean typedMatches(Field item) {
                return Objects.equals(item.getRelativity(), relativity);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(" has ")
                        .appendValue(relativity)
                        .appendText(" relativity");
            }
        };
    }

    public static Matcher<Field> defaultValue(long defaultValue) {
        return hasFeature("default value",
                Field::getDefaultValue,
                equalTo(defaultValue));
    }

    public static Matcher<Field> isSigned(Field.SignedType signedType) {
        return hasFeature("is",
                Field::getSigned,
                equalTo(signedType));
    }

    public static Matcher<Field> fieldValues(FieldValue... values) {
        return fieldValues(Arrays.asList(values));
    }

    public static Matcher<Field> fieldValues(Collection<? extends FieldValue> values) {
        return new TypedMatcher<Field>(Field.class) {
            @Override
            public boolean typedMatches(Field item) {
                return Matchers.containsInAnyOrder(values.stream()
                            .map(FieldValueMatchers::properties)
                            .collect(Collectors.toList()))
                        .matches(item.getValues());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(" has values ")
                        .appendValue(values);
            }
        };
    }


}
