package cpusim.model.harness.matchers.microinstruction;

import cpusim.model.Machine;
import cpusim.model.harness.matchers.MachineBoundMatchers;
import cpusim.model.harness.matchers.module.RegisterMatchers;
import cpusim.model.iochannel.IOChannel;
import cpusim.model.microinstruction.IO;
import cpusim.model.microinstruction.IODirection;
import cpusim.model.module.Register;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.Optional;

import static cpusim.model.harness.matchers.CPUSimMatchers.optionalValue;
import static cpusim.model.harness.matchers.microinstruction.MicroinstructionMatchers.microinstruction;
import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * {@link Matcher Matchers} for {@link IO} operations.
 */
public abstract class IOMatchers {

    private IOMatchers() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Creates a {@link Matcher} for {@link IO} instructions
     * @return Matcher
     *
     * @see IO
     */
    public static Matcher<IO> io(Machine machine, IO expected) {
        return compose("IO",
                compose(microinstruction(machine, expected))
                    .and(direction(expected.getDirection()))
                    .and(buffer(machine, expected.getBuffer()))
                    .and(type(expected.getType()))
                    .and(connection(expected.getConnection())));
    }


    /** @see IO#directionProperty() */
    public static Matcher<IO> direction(IODirection direction) {
        return hasFeature("direction",
                IO::getDirection,
                equalTo(direction));
    }
    
    /** @see IO#bufferProperty() */
    public static Matcher<IO> buffer(Machine machine, Optional<Register> buffer) {
        return MachineBoundMatchers.optionalMachineValue("buffer",
                IO::getBuffer,
                RegisterMatchers::register,
                machine,
                buffer);
    }
    
    /** @see IO#bufferProperty() */
    public static Matcher<IO> buffer(Machine machine, Register buffer) {
        return buffer(machine, Optional.ofNullable(buffer));
    }

    /** @see IO#typeProperty() */
    public static Matcher<IO> type(IO.Type type) {
        return hasFeature("type",
                IO::getType,
                equalTo(type));
    }
    
    /** @see IO#typeProperty() */
    public static Matcher<IO> connection(IOChannel connection) {
        return hasFeature("channel",
                IO::getConnection,
                equalTo(connection));
    }
    
    /** @see IO#connectionProperty() */
    public static Matcher<IO> connection(Optional<IOChannel> connection) {
        return optionalValue("connection",
                IO::getConnection,
                Matchers::equalTo,
                connection);
    }
    
    
}
