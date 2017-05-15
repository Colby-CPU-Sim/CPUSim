package cpusim.model.harness.matchers.microinstruction;

import cpusim.model.Machine;
import cpusim.model.harness.matchers.NamedObjectMatchers;
import cpusim.model.microinstruction.Microinstruction;
import org.hamcrest.Matcher;
import org.hobsoft.hamcrest.compose.ConjunctionMatcher;

import static cpusim.model.harness.matchers.MachineBoundMatchers.boundTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 *  {@link Matcher Matchers} for {@link cpusim.model.microinstruction.Microinstruction Microinstructions}
 */
public abstract class MicroinstructionMatchers {
    
    MicroinstructionMatchers() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a {@link Matcher} for the base {@link Microinstruction}.
     *
     * @return Matcher
     * @see Microinstruction
     */
    public static <T extends Microinstruction<T>>
    ConjunctionMatcher<T> microinstruction(Machine machine, T expected) {
        return compose(NamedObjectMatchers.<T>named(expected.getName()))
                    .and(cycles(expected.getCycleCount()))
                    .and(boundTo(sameInstance(machine)));
    }

    /** @see Microinstruction#cycleCountProperty() */
    public static <T extends Microinstruction<T>> Matcher<T> cycles(int count) {
        return hasFeature("cycle count", Microinstruction::getCycleCount, is(count));
    }

}
