package cpusim.model.harness.matchers.microinstruction;

import cpusim.model.Machine;
import cpusim.model.harness.matchers.MachineBoundMatchers;
import cpusim.model.harness.matchers.module.ConditionBitMatchers;
import cpusim.model.microinstruction.SetCondBit;
import cpusim.model.module.ConditionBit;
import org.hamcrest.Matcher;

import java.util.Optional;

import static cpusim.model.harness.matchers.microinstruction.MicroinstructionMatchers.microinstruction;
import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * {@link Matcher Matchers} for {@link SetCondBit}.
 */
public abstract class SetCondBitMatchers {

    private SetCondBitMatchers() {
        // no instantiation
    }

    public static Matcher<SetCondBit> setCondBit(Machine machine, SetCondBit expected) {
        return compose("Set Condition Bit",
                microinstruction(machine, expected))
                    .and(bit(machine, expected.getBit()))
                    .and(value(expected.getValue()));
    }
    
    /** @see SetCondBit#bitProperty() */
    public static Matcher<SetCondBit> bit(Machine machine, ConditionBit bit) {
        return bit(machine, Optional.ofNullable(bit));
    }

    /** @see SetCondBit#bitProperty() */
    public static Matcher<SetCondBit> bit(Machine machine, Optional<ConditionBit> bit) {
        return MachineBoundMatchers.optionalMachineValue(
                "condition bit",
                SetCondBit::getBit,
                ConditionBitMatchers::conditionBit,
                machine, bit);
    }


    /**
     * @see SetCondBit#valueProperty()
     */
    public static Matcher<SetCondBit> value(boolean value) {
        return hasFeature("value",
                SetCondBit::getValue,
                equalTo(value));
    }
}
