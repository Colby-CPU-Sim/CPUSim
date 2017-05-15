package cpusim.model.harness.matchers;

import com.github.npathai.hamcrestopt.OptionalMatchers;
import cpusim.model.Field;
import cpusim.model.Machine;
import cpusim.model.util.ReadOnlyMachineBound;
import org.hamcrest.Matcher;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.hamcrest.Matchers.sameInstance;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * Hamcrest matchers for {@link Field}
 */
public abstract class MachineBoundMatchers {

    private MachineBoundMatchers() {
        // no instantiate
    }


    public static <T extends ReadOnlyMachineBound> Matcher<T> properties(T expected) {
        return compose("MachineBound",
                compose(boundTo(sameInstance(expected.getMachine()))));
    }

    public static <T extends ReadOnlyMachineBound>
    Matcher<T> boundTo(Matcher<Machine> machine) {
        return hasFeature("machine",
                ReadOnlyMachineBound::getMachine,
                machine);
    }

    public static <T, V extends ReadOnlyMachineBound>
    Matcher<T> optionalMachineValue(String desc,
                                    Function<T, Optional<V>> accessor,
                                    BiFunction<Machine, V, Matcher<V>> matcher,
                                    Machine machine, Optional<V> check) {
        if (check.isPresent()) {
            return hasFeature(desc,
                    accessor,
                    OptionalMatchers.hasValue(matcher.apply(machine, check.get())));
        } else {
            return hasFeature(desc, accessor, OptionalMatchers.isEmpty());
        }
    }
}
