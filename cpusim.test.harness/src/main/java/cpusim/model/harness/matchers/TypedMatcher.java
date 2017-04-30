package cpusim.model.harness.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * Creates a type-safe matcher.
 */
public abstract class TypedMatcher<T> extends BaseMatcher<T> {

    private final Class<T> typeClazz;

    protected TypedMatcher(Class<T> typeClazz) {
        this.typeClazz = typeClazz;
    }

    @Override @SuppressWarnings("unchecked")
    public final boolean matches(Object item) {
        if (item == null || typeClazz.isAssignableFrom(item.getClass())) return false;

        return typedMatches((T) item);
    }

    public abstract boolean typedMatches(T item);

    @Override
    public abstract void describeTo(Description description);
}
