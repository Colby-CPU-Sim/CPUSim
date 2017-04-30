package cpusim.model.harness.matchers;

import cpusim.model.Field;
import cpusim.model.util.NamedObject;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * Hamcrest matchers for {@link Field}
 */
public abstract class NamedObjectMatchers extends TypedMatcher<NamedObject> {

    private NamedObjectMatchers() {
        super(NamedObject.class);
    }

    public static <T extends NamedObject> Matcher<T> named(String name) {
        return hasFeature("name", NamedObject::getName, equalTo(name));
    }
}
