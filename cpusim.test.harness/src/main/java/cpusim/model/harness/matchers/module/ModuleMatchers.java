package cpusim.model.harness.matchers.module;

import cpusim.model.Field;
import cpusim.model.Machine;
import cpusim.model.harness.matchers.MachineBoundMatchers;
import cpusim.model.harness.matchers.NamedObjectMatchers;
import cpusim.model.module.Module;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.sameInstance;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;

/**
 * Hamcrest matchers for {@link Field}
 */
public abstract class ModuleMatchers {

    private ModuleMatchers() {
        // no instantiate
    }

    public static <T extends Module<T>> Matcher<T> module(Machine machine, T expected) {
        return compose("Module",
                compose(ModuleMatchers.<T>boundTo(machine))
                    .and(named(expected.getName())));
    }

    public static <T extends Module<T>> Matcher<T> boundTo(Machine machine) {
        return MachineBoundMatchers.boundTo(sameInstance(machine));
    }

    public static <T extends Module<T>> Matcher<T> named(String name) {
        return NamedObjectMatchers.named(name);
    }
}
