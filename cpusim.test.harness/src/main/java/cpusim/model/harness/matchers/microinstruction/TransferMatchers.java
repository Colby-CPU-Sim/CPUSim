package cpusim.model.harness.matchers.microinstruction;

import cpusim.model.Machine;
import cpusim.model.harness.matchers.MachineBoundMatchers;
import cpusim.model.harness.matchers.module.RAMMatchers;
import cpusim.model.harness.matchers.module.RegisterArrayMatchers;
import cpusim.model.harness.matchers.module.RegisterMatchers;
import cpusim.model.microinstruction.Transfer;
import cpusim.model.module.Module;
import cpusim.model.module.RAM;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import cpusim.model.module.Sized;
import cpusim.model.util.units.ArchValue;
import org.hamcrest.Matcher;
import org.hobsoft.hamcrest.compose.ConjunctionMatcher;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static cpusim.model.harness.matchers.microinstruction.MicroinstructionMatchers.microinstruction;
import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * {@link Matcher Matchers} for {@link Transfer}s. This class provides utilities
 * for the other sub-classes to use easily.
 */
abstract class TransferMatchers<From extends Module<From> & Sized<From>,
                                To extends Module<To> & Sized<To>,
                                T extends Transfer<From, To, T>> {

    <M extends Module<M> & Sized<M>>
    Matcher<T> module(String name, Function<T, Optional<M>> method,
                      Machine machine, M module,
                      BiFunction<Machine, M, Matcher<M>> matcher) {
        return MachineBoundMatchers.optionalMachineValue(name, method,
                matcher, machine,
                Optional.ofNullable(module));
    }

    Matcher<T> module(String name, Function<T, Optional<Register>> method,
                      Machine machine, Register register) {
        return module(name, method, machine, register, RegisterMatchers::register);
    }

    Matcher<T> module(String name, Function<T, Optional<RAM>> method,
                      Machine machine, RAM memory) {
        return module(name, method, machine, memory, RAMMatchers::ram);
    }

    Matcher<T> module(String name, Function<T, Optional<RegisterArray>> method,
                      Machine machine, RegisterArray array) {
        return module(name, method, machine, array, RegisterArrayMatchers::registerArray);
    }

    static final String PROPERTY_SOURCE = "source";
    static final String PROPERTY_DESTINATION = "destination";

    /**
     * Provides a {@link Matcher} for the common properties of a {@link Transfer}
     * operation.
     *
     * @return Matcher
     * @see Transfer
     */
    ConjunctionMatcher<T> transfer(Machine machine, T expected) {
        return compose(microinstruction(machine, expected))
                .and(h_sourceStartBit(expected.getSrcStartBit()))
                .and(h_destStartBit(expected.getDestStartBit()))
                .and(h_numberOfBits(expected.getNumBits()));
    }

    /** @see Transfer#srcStartBitProperty() */
    Matcher<T> h_sourceStartBit(int bit) {
        return hasFeature("source starting bit",
                Transfer::getSrcStartBit,
                equalTo(bit));
    }

    /** @see Transfer#destStartBitProperty() */
    Matcher<T> h_destStartBit(int bit) {
        return hasFeature("destination starting bit",
                Transfer::getDestStartBit,
                equalTo(bit));
    }

    /** @see Transfer#numBitsProperty() */
    Matcher<T> h_numberOfBits(int size) {
        return h_numberOfBits(ArchValue.bits(size));
    }

    /** @see Transfer#numBitsProperty() */
    Matcher<T> h_numberOfBits(ArchValue size) {
        return hasFeature("number of bits",
                ArchValue.wrapAsBits(Transfer::getNumBits),
                equalTo(size));
    }

}
