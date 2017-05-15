package cpusim.model.harness.matchers.module;

import cpusim.model.Machine;
import cpusim.model.module.ConditionBit;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * Hamcrest {@link Matcher Matchers} for {@link ConditionBit}
 */
public abstract class ConditionBitMatchers {

    private ConditionBitMatchers() {
        // no instantiate
    }

    /**
     * Creates a {@link Matcher} for a {@link ConditionBit} component.
     *
     * @return Matcher
     * @see ConditionBit
     */
    public static Matcher<ConditionBit> conditionBit(Machine machine, ConditionBit expected) {
        return compose("ConditionBit",
                compose(bit(expected.getBit()))
                        .and(halt(expected.getHalt()))
                        .and(ModuleMatchers.module(machine, expected)));
    }

    /** @see ConditionBit#bitProperty() */
    public static Matcher<ConditionBit> bit(int bit) {
        return hasFeature("bit", ConditionBit::getBit, equalTo(bit));
    }

    /** @see ConditionBit#haltProperty() */
    public static Matcher<ConditionBit> halt(boolean halt) {
        return hasFeature("halt", ConditionBit::getHalt, equalTo(halt));
    }
}
