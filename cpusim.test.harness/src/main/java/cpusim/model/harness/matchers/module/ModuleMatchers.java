package cpusim.model.harness.matchers.module;

import cpusim.model.Machine;
import cpusim.model.harness.matchers.MachineBoundMatchers;
import cpusim.model.harness.matchers.NamedObjectMatchers;
import cpusim.model.module.Module;
import org.hamcrest.Matcher;
import org.hobsoft.hamcrest.compose.ConjunctionMatcher;

import static org.hamcrest.Matchers.sameInstance;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;

/**
 * Hamcrest {@link Matcher Matchers} for {@link Module}
 */
public abstract class ModuleMatchers {

    private ModuleMatchers() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a {@link Matcher} for a {@link Module} component.
     *
     * @return Matcher
     * @see Module
     */
    public static <T extends Module<T>>
    ConjunctionMatcher<T> module(Machine machine, T expected) {
        return compose(ModuleMatchers.<T>boundTo(machine))
                .and(NamedObjectMatchers.named(expected.getName()));
    }

    /** @see MachineBoundMatchers#boundTo(org.hamcrest.Matcher) */
    public static <T extends Module<T>> Matcher<T> boundTo(Machine machine) {
        return MachineBoundMatchers.boundTo(sameInstance(machine));
    }
}
