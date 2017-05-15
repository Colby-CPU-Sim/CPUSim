package cpusim.model.harness.matchers.microinstruction;

import cpusim.model.Machine;
import cpusim.model.harness.matchers.ArchValueMatchers;
import cpusim.model.harness.matchers.MachineBoundMatchers;
import cpusim.model.harness.matchers.module.ControlUnitMatchers;
import cpusim.model.harness.matchers.module.RegisterMatchers;
import cpusim.model.microinstruction.ArithmeticLogicOperation;
import cpusim.model.microinstruction.Test;
import cpusim.model.module.ControlUnit;
import cpusim.model.module.Register;
import cpusim.model.util.units.ArchValue;
import org.hamcrest.Matcher;

import java.util.Optional;

import static cpusim.model.harness.matchers.microinstruction.MicroinstructionMatchers.microinstruction;
import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 *  Base {@link Matcher Matchers} for {@link ArithmeticLogicOperation}s
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "WeakerAccess"})
public abstract class TestMatchers {

    TestMatchers() {
        // no instantiation
    }
    
    /**
     * Creates a {@link Matcher} to test all properties of a {@link Test} instance
     * @return Matcher
     * @see Test
     */
    public static Matcher<Test> test(Machine machine, Test expected) {
        return compose("Test",
                microinstruction(machine, expected)
                    .and(register(machine, expected.getRegister()))
                    .and(controlUnit(machine, expected.getControlUnit()))
                    .and(start(expected.getStart()))
                    .and(numberOfBits(expected.getNumBits()))
                    .and(comparison(expected.getComparison()))
                    .and(value(expected.getValue()))
                    .and(omission(expected.getOmission())));
    }

    /** @see Test#controlUnitProperty() */
    public static Matcher<Test> controlUnit(Machine machine, Optional<ControlUnit> controlUnit) {
        return MachineBoundMatchers.optionalMachineValue(
                "control unit",
                Test::getControlUnit,
                ControlUnitMatchers::controlUnit,
                machine, controlUnit);
    }

    /** @see Test#registerProperty() */
    public static Matcher<Test> register(Machine machine, Optional<Register> register) {
        return MachineBoundMatchers.optionalMachineValue(
                "register",
                Test::getRegister,
                RegisterMatchers::register,
                machine, register);
    }

    /** @see Test#startProperty() */
    public static Matcher<Test> start(int start) {
        return hasFeature("starting bit",
                Test::getStart,
                equalTo(start));
    }

    /**
     * @see Test#numBitsProperty()
     * @see #numberOfBits(ArchValue)
     */
    public static Matcher<Test> numberOfBits(int bits) {
        return numberOfBits(ArchValue.bits(bits));
    }

    /**
     * @see Test#numBitsProperty()
     * @see #numberOfBits(int)
     */
    public static Matcher<Test> numberOfBits(ArchValue width) {
        return hasFeature("width tested",
                ArchValue.wrapAsBits(Test::getNumBits),
                ArchValueMatchers.equalTo(width));
    }

    /** @see Test#comparisonProperty() */
    public static Matcher<Test> comparison(Test.Operation comparison) {
        return hasFeature("starting bit",
                Test::getComparison,
                equalTo(comparison));
    }

    /** @see Test#omissionProperty() */
    public static Matcher<Test> omission(int omission) {
        return hasFeature("relative omission",
                Test::getOmission,
                equalTo(omission));
    }

    /** @see Test#valueProperty() */
    public static Matcher<Test> value(long value) {
        return hasFeature("testing value",
                Test::getValue,
                equalTo(value));
    }

}
