package cpusim.model.harness.matchers.microinstruction;

import cpusim.model.Machine;
import cpusim.model.microinstruction.Microinstruction;
import cpusim.model.microinstruction.Transfer;
import cpusim.model.microinstruction.TransferRtoR;
import cpusim.model.module.Register;
import org.hamcrest.Matcher;

import java.util.Optional;

import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;

/**
 *  Matchers for {@link Microinstruction Microinstructions}
 */
public abstract class TransferRtoRMatchers extends TransferMatchers<Register, Register, TransferRtoR> {

    private TransferRtoRMatchers() {
        // no instantiation
    }

    private static final TransferRtoRMatchers INTERNAL = new TransferRtoRMatchers() { };
    
    /**
     * Matches all properties in a {@link TransferRtoR}
     * @return Matcher
     */
    public static Matcher<TransferRtoR> transferRtoR(Machine machine, TransferRtoR expected) {
        return compose("Transfer Register -> Register",
                    INTERNAL.baseProps(machine, expected)
                        .and(source(machine, expected.getSource()))
                        .and(destination(machine, expected.getDest())));
    }


    /**
     * @see Transfer#sourceProperty()
     */
    public static Matcher<TransferRtoR> source(Machine machine, Optional<Register> source) {
        return source(machine, source.orElse(null));
    }

    /**
     * @see Transfer#sourceProperty()
     */
    public static Matcher<TransferRtoR> source(Machine machine, Register source) {
        return INTERNAL.module(PROPERTY_SOURCE, TransferRtoR::getSource, machine, source);
    }

    /**
     * @see Transfer#destProperty()
     */
    public static Matcher<TransferRtoR> destination(Machine machine, Optional<Register> destination) {
        return destination(machine, destination.orElse(null));
    }

    /**
     * @see Transfer#destProperty()
     */
    public static Matcher<TransferRtoR> destination(Machine machine, Register destination) {
        return INTERNAL.module(PROPERTY_DESTINATION, TransferRtoR::getDest, machine, destination);
    }

}
