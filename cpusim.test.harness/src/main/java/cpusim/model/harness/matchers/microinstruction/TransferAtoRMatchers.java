package cpusim.model.harness.matchers.microinstruction;

import cpusim.model.Machine;
import cpusim.model.harness.matchers.ArchValueMatchers;
import cpusim.model.microinstruction.Microinstruction;
import cpusim.model.microinstruction.Transfer;
import cpusim.model.microinstruction.TransferAtoR;
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
public abstract class TransferAtoRMatchers extends TransferMatchers<RegisterArray, Register, TransferAtoR> {

    private TransferAtoRMatchers() {
        // no instantiation
    }

    private static final TransferAtoRMatchers INTERNAL = new TransferAtoRMatchers() { };
    
    /**
     * Matches all properties in a {@link TransferAtoR}
     * @return Matcher
     */
    public static Matcher<TransferAtoR> transferAtoR(Machine machine, TransferAtoR expected) {
        return compose("Transfer Array -> Register",
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
    public static Matcher<TransferAtoR> source(Machine machine, Optional<RegisterArray> source) {
        return source(machine, source.orElse(null));
    }

    /**
     * @see Transfer#sourceProperty()
     */
    public static Matcher<TransferAtoR> source(Machine machine, RegisterArray source) {
        return INTERNAL.module(PROPERTY_SOURCE, TransferAtoR::getSource, machine, source);
    }

    /**
     * @see Transfer#destProperty()
     */
    public static Matcher<TransferAtoR> destination(Machine machine, Optional<Register> destination) {
        return destination(machine, destination.orElse(null));
    }

    /**
     * @see Transfer#destProperty()
     */
    public static Matcher<TransferAtoR> destination(Machine machine, Register destination) {
        return INTERNAL.module(PROPERTY_DESTINATION, TransferAtoR::getDest, machine, destination);
    }


    /**
     * @see TransferAtoR#indexProperty()
     */
    public static Matcher<TransferAtoR> index(Machine machine, Register index) {
        return INTERNAL.module("index", TransferAtoR::getIndex, machine, index);
    }

    /**
     * @see Transfer#destProperty()
     */
    public static Matcher<TransferAtoR> index(Machine machine, Optional<Register> index) {
        return index(machine, index.orElse(null));
    }

    /**
     * @see TransferAtoR#indexStartProperty()
     */
    public static Matcher<TransferAtoR> indexStartBit(int bit) {
        return hasFeature("index start bit", TransferAtoR::getIndexStart, equalTo(bit));
    }

    /**
     * @see TransferAtoR#indexNumBitsProperty()
     * @see #indexNumberOfBits(ArchValue)
     */
    public static Matcher<TransferAtoR> indexNumberOfBits(int bits) {
        return indexNumberOfBits(ArchValue.bits(bits));
    }

    /**
     * @see TransferAtoR#indexNumBitsProperty()
     * @see #indexNumberOfBits(int)
     */
    public static Matcher<TransferAtoR> indexNumberOfBits(ArchValue width) {
        return hasFeature("distance",
                ArchValue.wrapAsBits(TransferAtoR::getIndexNumBits),
                ArchValueMatchers.equalTo(width));
    }

}
