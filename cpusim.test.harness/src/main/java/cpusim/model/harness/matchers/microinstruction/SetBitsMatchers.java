package cpusim.model.harness.matchers.microinstruction;

import cpusim.model.Machine;
import cpusim.model.harness.matchers.ArchValueMatchers;
import cpusim.model.harness.matchers.MachineBoundMatchers;
import cpusim.model.harness.matchers.module.RegisterMatchers;
import cpusim.model.microinstruction.SetBits;
import cpusim.model.module.Register;
import cpusim.model.util.units.ArchType;
import cpusim.model.util.units.ArchValue;
import org.hamcrest.Matcher;

import java.util.Optional;

import static cpusim.model.harness.matchers.microinstruction.MicroinstructionMatchers.microinstruction;
import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * {@link Matcher Matchers} for {@link SetBits}.
 */
public abstract class SetBitsMatchers {

    private SetBitsMatchers() {
        // no instantiation
    }
    
    /**
     * Creates a {@link Matcher} for {@link SetBits} instructions.
     * @return Matcher
     * @see SetBits
     */
    public static Matcher<SetBits> setBits(Machine machine, SetBits expected) {
        return compose("SetBits",
                compose(microinstruction(machine, expected))
                    .and(start(expected.getStart()))
                    .and(numOfBits(expected.getNumBits()))
                    .and(register(machine, expected.getRegister()))
                    .and(value(expected.getValue())));
    }


    /** @see SetBits#startProperty() */
    public static Matcher<SetBits> start(int start) {
        return hasFeature("start bit", SetBits::getStart, equalTo(start));
    }


    /** @see SetBits#numBitsProperty() */
    public static Matcher<SetBits> numOfBits(int width) {
        return numOfBits(ArchType.Bit.of(width));
    }

    /** @see SetBits#numBitsProperty() */
    public static Matcher<SetBits> numOfBits(ArchValue width) {
        return hasFeature("number of bits",
                ArchValue.wrapAsBits(SetBits::getNumBits),
                ArchValueMatchers.equalTo(width));
    }


    /** @see SetBits#registerProperty() */
    public static Matcher<SetBits> register(Machine machine, Register address) {
        return register(machine, Optional.ofNullable(address));
    }

    /** @see SetBits#registerProperty() */
    public static Matcher<SetBits> register(Machine machine, Optional<Register> register) {
        return MachineBoundMatchers.optionalMachineValue("register",
                SetBits::getRegister,
                RegisterMatchers::register,
                machine,
                register);
    }

    /** @see SetBits#valueProperty() */
    public static Matcher<SetBits> value(long value) {
        return hasFeature("value",
                SetBits::getValue,
                equalTo(value));
    }
}
