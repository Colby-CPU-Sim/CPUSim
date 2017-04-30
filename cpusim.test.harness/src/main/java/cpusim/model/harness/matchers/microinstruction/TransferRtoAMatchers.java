package cpusim.model.harness.matchers.microinstruction;

import cpusim.model.Machine;
import cpusim.model.harness.matchers.ArchValueMatchers;
import cpusim.model.microinstruction.Microinstruction;
import cpusim.model.microinstruction.Transfer;
import cpusim.model.microinstruction.TransferAtoR;
import cpusim.model.microinstruction.TransferRtoA;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import cpusim.model.util.units.ArchValue;
import org.hamcrest.Matcher;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 *  Matchers for {@link Microinstruction Microinstructions}
 */
public abstract class TransferRtoAMatchers extends TransferMatchers<Register, RegisterArray, TransferRtoA> {

    private TransferRtoAMatchers() {
        // no instantiation
    }

    private static final TransferRtoAMatchers INTERNAL = new TransferRtoAMatchers() { };
    
    /**
     * Matches all properties in a {@link TransferRtoA}
     * @return Matcher
     */
    public static Matcher<TransferRtoA> transferRtoA(Machine machine, TransferRtoA expected) {
        return compose("Transfer Register -> Array",
                    INTERNAL.baseProps(machine, expected)
                        .and(source(machine, expected.getSource()))
                        .and(destination(machine, expected.getDest()))
                        .and(index(machine, expected.getIndex()))
                        .and(indexStartBit(expected.getIndexStart()))
                        .and(indexNumberOfBits(expected.getIndexNumBits())));
    }


    /**
     * @see Transfer#sourceProperty()
     */
    public static Matcher<TransferRtoA> source(Machine machine, Optional<Register> source) {
        return source(machine, source.orElse(null));
    }

    /**
     * @see Transfer#sourceProperty()
     */
    public static Matcher<TransferRtoA> source(Machine machine, Register source) {
        return INTERNAL.module(PROPERTY_SOURCE, TransferRtoA::getSource, machine, source);
    }

    /**
     * @see Transfer#destProperty()
     */
    public static Matcher<TransferRtoA> destination(Machine machine, Optional<RegisterArray> destination) {
        return destination(machine, destination.orElse(null));
    }

    /**
     * @see Transfer#destProperty()
     */
    public static Matcher<TransferRtoA> destination(Machine machine, RegisterArray destination) {
        return INTERNAL.module(PROPERTY_DESTINATION, TransferRtoA::getDest, machine, destination);
    }


    /**
     * @see TransferAtoR#indexProperty()
     */
    public static Matcher<TransferRtoA> index(Machine machine, Register index) {
        return INTERNAL.module("index", TransferRtoA::getIndex, machine, index);
    }

    /**
     * @see Transfer#destProperty()
     */
    public static Matcher<TransferRtoA> index(Machine machine, Optional<Register> index) {
        return index(machine, index.orElse(null));
    }

    /**
     * @see TransferRtoA#indexStartProperty()
     */
    public static Matcher<TransferRtoA> indexStartBit(int bit) {
        return hasFeature("index start bit", TransferRtoA::getIndexStart, equalTo(bit));
    }

    /**
     * @see TransferRtoA#indexNumBitsProperty()
     * @see #indexNumberOfBits(ArchValue)
     */
    public static Matcher<TransferRtoA> indexNumberOfBits(int bits) {
        return indexNumberOfBits(ArchValue.bits(bits));
    }

    /**
     * @see TransferRtoA#indexNumBitsProperty()
     * @see #indexNumberOfBits(int)
     */
    public static Matcher<TransferRtoA> indexNumberOfBits(ArchValue width) {
        return hasFeature("distance",
                ArchValue.wrapAsBits(TransferRtoA::getIndexNumBits),
                ArchValueMatchers.equalTo(width));
    }

}
