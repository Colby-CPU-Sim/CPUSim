package cpusim.model.harness.matchers.module;

import cpusim.model.Field;
import cpusim.model.Machine;
import cpusim.model.MachineInstruction;
import cpusim.model.module.ControlUnit;
import org.hamcrest.Matcher;

import javax.annotation.Nullable;

import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * Hamcrest matchers for {@link Field}
 */
public abstract class ControlUnitMatchers {

    private ControlUnitMatchers() {
        // no instantiate
    }

    public static Matcher<ControlUnit> controlUnit(Machine machine, ControlUnit expected) {
        return compose("Control Unit",
                compose(currentInstruction(expected.getCurrentInstruction()))
                    .and(currentState(expected.getCurrentState()))
                    .and(microIndex(expected.getMicroIndex()))
                    .and(ModuleMatchers.module(machine, expected)));
    }


    public static Matcher<ControlUnit> currentInstruction(@Nullable MachineInstruction ins) {
        return hasFeature("current instruction",
                ControlUnit::getCurrentInstruction,
                equalTo(ins));
    }

    public static Matcher<ControlUnit> currentState(ControlUnit.State state) {
        return hasFeature("current state",
                ControlUnit::getCurrentState,
                equalTo(state));
    }

    public static Matcher<ControlUnit> microIndex(int index) {
        return hasFeature("microinstruction index",
                ControlUnit::getMicroIndex,
                equalTo(index));
    }
}
