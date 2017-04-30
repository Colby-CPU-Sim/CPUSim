package cpusim.model.harness.matchers.microinstruction;

import cpusim.model.Machine;
import cpusim.model.harness.matchers.ArchValueMatchers;
import cpusim.model.microinstruction.Shift;
import cpusim.model.module.Register;
import cpusim.model.util.units.ArchValue;
import org.hamcrest.Matcher;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * {@link Matcher Matchers} for {@link Shift}
 */
public abstract class ShiftMatchers extends TransferMatchers<Register, Register, Shift> {

    private ShiftMatchers() {
        // no instantiation
    }

    private static final ShiftMatchers INTERNAL = new ShiftMatchers() { };
    
    
    /**
     * Creates a {@link Matcher} to test all properties of a {@link Shift} instance
     * @return Matcher
     * @see Shift
     */
    public static Matcher<Shift> shift(Machine machine, Shift expected) {
        return compose("Shift",
                    INTERNAL.baseProps(machine, expected)
                        .and(source(machine, expected.getSource()))
                        .and(destination(machine, expected.getDest()))
                        .and(type(expected.getType()))
                        .and(direction(expected.getDirection()))
                        .and(distance(expected.getDistance())));
    }


    /**
     * @see Shift#typeProperty()
     */
    public static Matcher<Shift> type(Shift.ShiftType type) {
        return hasFeature("type",
                Shift::getType,
                equalTo(type));
    }

    /**
     * @see Shift#directionProperty()
     */
    public static Matcher<Shift> direction(Shift.ShiftDirection direction) {
        return hasFeature("direction",
                Shift::getDirection,
                equalTo(direction));
    }

    /**
     * @see Shift#distanceProperty()
     * @see #distance(ArchValue)
     */
    public static Matcher<Shift> distance(int distance) {
        return distance(ArchValue.bits(distance));
    }

    /**
     * @see Shift#distanceProperty()
     * @see #distance(int)
     */
    public static Matcher<Shift> distance(ArchValue distance) {
        return hasFeature("distance",
                ArchValue.wrapAsBits(Shift::getDistance),
                ArchValueMatchers.equalTo(distance));
    }

    /**
     * @see Shift#sourceProperty()
     */
    public static Matcher<Shift> source(Machine machine, Optional<Register> source) {
        return source(machine, source.orElse(null));
    }

    /**
     * @see Shift#sourceProperty()
     */
    public static Matcher<Shift> source(Machine machine, Register source) {
        return INTERNAL.module(PROPERTY_SOURCE, Shift::getSource, machine, source);
    }

    /**
     * @see Shift#destProperty()
     */
    public static Matcher<Shift> destination(Machine machine, Optional<Register> destination) {
        return destination(machine, destination.orElse(null));
    }

    /**
     * @see Shift#destProperty()
     */
    public static Matcher<Shift> destination(Machine machine, Register destination) {
        return INTERNAL.module(PROPERTY_DESTINATION, Shift::getSource, machine, destination);
    }

}
