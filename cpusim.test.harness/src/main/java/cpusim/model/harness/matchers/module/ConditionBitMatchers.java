package cpusim.model.harness.matchers.module;

import cpusim.model.Field;
import cpusim.model.Machine;
import cpusim.model.module.ConditionBit;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * Hamcrest matchers for {@link Field}
 */
public abstract class ConditionBitMatchers {

    private ConditionBitMatchers() {
        // no instantiate
    }

    public static Matcher<ConditionBit> conditionBit(Machine machine, ConditionBit expected) {
        return compose("ConditionBit",
                compose(bit(expected.getBit()))
                    .and(halt(expected.getHalt()))
                    .and(ModuleMatchers.module(machine, expected)));
    }

    public static Matcher<ConditionBit> bit(int bit) {
        return hasFeature("bit", ConditionBit::getBit, equalTo(bit));
    }

    public static Matcher<ConditionBit> halt(boolean halt) {
        return hasFeature("halt", ConditionBit::getHalt, equalTo(halt));
    }
}
