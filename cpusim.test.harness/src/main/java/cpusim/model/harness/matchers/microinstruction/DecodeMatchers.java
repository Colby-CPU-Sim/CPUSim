package cpusim.model.harness.matchers.microinstruction;

import cpusim.model.Machine;
import cpusim.model.harness.matchers.MachineBoundMatchers;
import cpusim.model.harness.matchers.module.ControlUnitMatchers;
import cpusim.model.harness.matchers.module.RegisterMatchers;
import cpusim.model.microinstruction.Decode;
import cpusim.model.module.ControlUnit;
import cpusim.model.module.Register;
import org.hamcrest.Matcher;

import java.util.Optional;

import static cpusim.model.harness.matchers.microinstruction.MicroinstructionMatchers.microinstruction;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;

/**
 * {@link Matcher Matchers} for {@link Decode} operations.
 */
public abstract class DecodeMatchers {

    private DecodeMatchers() {
        // no instantiation
    }
    
    /**
     * Creates a {@link Matcher} for {@link Decode} instructions
     * @return Matcher
     *
     * @see Decode
     */
    public static Matcher<Decode> decode(Machine machine, Decode expected) {
        return compose("Decode",
                compose(microinstruction(machine, expected))
                    .and(ir(machine, expected.getIr()))
                    .and(controlUnit(machine, expected.getControlUnit())));
    }

    /** @see Decode#controlUnitProperty() */
    public static Matcher<Decode> controlUnit(Machine machine, Optional<ControlUnit> unit) {
        return MachineBoundMatchers.optionalMachineValue("control unit",
                Decode::getControlUnit,
                ControlUnitMatchers::controlUnit,
                machine, unit);
    }

    /** @see Decode#controlUnitProperty() */
    public static Matcher<Decode> controlUnit(Machine machine, ControlUnit unit) {
        return controlUnit(machine, Optional.ofNullable(unit));
    }

    /** @see Decode#irProperty()  */
    public static Matcher<Decode> ir(Machine machine, Optional<Register> ir) {
        return MachineBoundMatchers.optionalMachineValue("instruction register",
                Decode::getIr,
                RegisterMatchers::register,
                machine, ir);
    }

    /** @see Decode#irProperty() */
    public static Matcher<Decode> ir(Machine machine, Register unit) {
        return ir(machine, Optional.ofNullable(unit));
    }
}
