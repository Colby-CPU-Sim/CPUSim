package cpusim.model.harness.matchers.microinstruction;

import cpusim.model.Machine;
import cpusim.model.harness.matchers.MachineBoundMatchers;
import cpusim.model.harness.matchers.module.ConditionBitMatchers;
import cpusim.model.harness.matchers.module.RegisterMatchers;
import cpusim.model.microinstruction.Increment;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.Register;
import org.hamcrest.Matcher;

import java.util.Optional;
import java.util.function.Function;

import static cpusim.model.harness.matchers.microinstruction.MicroinstructionMatchers.microinstruction;
import static org.hamcrest.CoreMatchers.is;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * {@link Matcher Matchers} for {@link Increment} operations.
 */
public abstract class IncrementMatchers {

    private IncrementMatchers() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Creates a {@link Matcher} for {@link Increment} instructions.
     * @return Matcher
     *
     * @see Increment
     */
    public static Matcher<Increment> increment(Machine machine, Increment expected) {
        return compose("Increment",
                microinstruction(machine, expected)
                    .and(delta(expected.getDelta()))
                    .and(register(machine, expected.getRegister()))
                    .and(carryBit(machine, expected.getCarryBit()))
                    .and(overflowBit(machine, expected.getOverflowBit()))
                    .and(zeroBit(machine, expected.getZeroBit())));
    }

    /** @see Increment#registerProperty() */
    public static Matcher<Increment> register(Machine machine, Register reg) {
        return hasFeature("register",
                Increment::getRegister,
                RegisterMatchers.register(machine, reg));
    }

    /** @see Increment#deltaProperty() */
    public static Matcher<Increment> delta(long delta) {
        return hasFeature("delta", Increment::getDelta, is(delta));
    }


    // Used to save writing when getting a condition bit
    private static Matcher<Increment> conditionBitProp(String desc, Function<Increment, Optional<ConditionBit>> getter, Machine machine,
                                                       Optional<ConditionBit> bit) {
        return MachineBoundMatchers.optionalMachineValue(desc, getter,
                ConditionBitMatchers::conditionBit,
                machine, bit);
    }

    /** @see Increment#carryBitProperty() */
    public static Matcher<Increment> carryBit(Machine machine, Optional<ConditionBit> carryBit) {
        return conditionBitProp("carryBit", Increment::getCarryBit, machine,
                carryBit
        );
    }

    /** @see Increment#overflowBitProperty() */
    public static Matcher<Increment> overflowBit(Machine machine, Optional<ConditionBit>  overflowBit) {
        return conditionBitProp("overflowBit", Increment::getOverflowBit, machine,
                overflowBit
        );
    }

    /** @see Increment#zeroBitProperty() */
    public static Matcher<Increment> zeroBit(Machine machine, Optional<ConditionBit> zeroBit) {
        return conditionBitProp("zeroBit", Increment::getZeroBit, machine,
                zeroBit
        );
    }

}
