package cpusim.model.harness.matchers.microinstruction;

import cpusim.model.Machine;
import cpusim.model.harness.matchers.MachineBoundMatchers;
import cpusim.model.harness.matchers.module.ControlUnitMatchers;
import cpusim.model.microinstruction.Branch;
import cpusim.model.module.ControlUnit;
import org.hamcrest.Matcher;

import java.util.Optional;

import static cpusim.model.harness.matchers.microinstruction.MicroinstructionMatchers.microinstruction;
import static org.hamcrest.Matchers.is;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * {@link Matcher Matchers} for the {@link Branch} {@link cpusim.model.microinstruction.Microinstruction}
 */
public abstract class BranchMatchers {

    private BranchMatchers() {
        // no instantiation
    }
    
    /**
     * Creates a {@link Matcher} for the {@link Branch} microinstruction.
     * @return Matcher
     *
     * @see Branch
     */
    public static Matcher<Branch> branch(Machine machine, Branch expected) {
        return compose("Branch",
                compose(microinstruction(machine, expected))
                    .and(amount(expected.getAmount()))
                    .and(controlUnit(machine, expected.getControlUnit())));
    }

    /** @see Branch#amountProperty() */
    public static Matcher<Branch> amount(int amount) {
        return hasFeature("amount",
                Branch::getAmount,
                is(amount));
    }

    /** @see Branch#controlUnitProperty() */
    public static Matcher<Branch> controlUnit(Machine machine, Optional<ControlUnit> unit) {
        return MachineBoundMatchers.optionalMachineValue("control unit",
                Branch::getControlUnit,
                ControlUnitMatchers::controlUnit,
                machine, unit);
    }

    /** @see Branch#controlUnitProperty() */
    public static Matcher<Branch> controlUnit(Machine machine, ControlUnit unit) {
        return controlUnit(machine, Optional.ofNullable(unit));
    }

}
