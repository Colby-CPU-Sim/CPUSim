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
 * {@link org.hamcrest.Matcher Matchers} for {@link cpusim.model.microinstruction.Transfer}s.
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

    ConjunctionMatcher<T> baseProps(Machine machine, T expected) {
        return compose(microinstruction(machine, expected))
                .and(sourceStartBit(expected.getSrcStartBit()))
                .and(destStartBit(expected.getDestStartBit()))
                .and(numberOfBits(expected.getNumBits()));
    }

    Matcher<T> sourceStartBit(int bit) {
        return hasFeature("source starting bit",
                Transfer::getSrcStartBit,
                equalTo(bit));
    }

    Matcher<T> destStartBit(int bit) {
        return hasFeature("destination starting bit",
                Transfer::getDestStartBit,
                equalTo(bit));
    }

    Matcher<T> numberOfBits(int size) {
        return numberOfBits(ArchValue.bits(size));
    }

    Matcher<T> numberOfBits(ArchValue size) {
        return hasFeature("number of bits",
                ArchValue.wrapAsBits(Transfer::getNumBits),
                equalTo(size));
    }

}
