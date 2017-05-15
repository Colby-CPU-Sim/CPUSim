package cpusim.model.harness.matchers.microinstruction;

import cpusim.model.Machine;
import cpusim.model.harness.matchers.MachineBoundMatchers;
import cpusim.model.harness.matchers.module.RAMMatchers;
import cpusim.model.harness.matchers.module.RegisterMatchers;
import cpusim.model.microinstruction.IODirection;
import cpusim.model.microinstruction.MemoryAccess;
import cpusim.model.module.RAM;
import cpusim.model.module.Register;
import org.hamcrest.Matcher;

import java.util.Optional;

import static cpusim.model.harness.matchers.microinstruction.MicroinstructionMatchers.microinstruction;
import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * {@link Matcher Matchers} for {@link MemoryAccess}.
 */
public abstract class MemoryAccessMatchers {

    private MemoryAccessMatchers() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Creates a {@link Matcher} for {@link MemoryAccess} instructions
     * @return Matcher
     *
     * @see MemoryAccess
     */
    public static Matcher<MemoryAccess> memoryAccess(Machine machine, MemoryAccess expected) {
        return compose("MemoryAccess",
                compose(microinstruction(machine, expected))
                    .and(direction(expected.getDirection()))
                    .and(address(machine, expected.getAddress()))
                    .and(data(machine, expected.getData()))
                    .and(memory(machine, expected.getMemory())));
    }


    /** @see MemoryAccess#addressProperty() */
    public static Matcher<MemoryAccess> address(Machine machine, Register address) {
        return address(machine, Optional.ofNullable(address));
    }
    
    /** @see MemoryAccess#addressProperty() */
    public static Matcher<MemoryAccess> address(Machine machine, Optional<Register> address) {
        return MachineBoundMatchers.optionalMachineValue("address",
                MemoryAccess::getAddress,
                RegisterMatchers::register,
                machine,
                address);
    }
    
    /** @see MemoryAccess#dataProperty() */
    public static Matcher<MemoryAccess> data(Machine machine, Register data) {
        return data(machine, Optional.ofNullable(data));
    }
    
    /** @see MemoryAccess#dataProperty() */
    public static Matcher<MemoryAccess> data(Machine machine, Optional<Register> data) {
        return MachineBoundMatchers.optionalMachineValue("data buffer",
                MemoryAccess::getData,
                RegisterMatchers::register,
                machine,
                data);
    }
    
    /** @see MemoryAccess#directionProperty() */
    public static Matcher<MemoryAccess> direction(IODirection direction) {
        return hasFeature("direction",
                MemoryAccess::getDirection,
                equalTo(direction));
    }
    
    /** @see MemoryAccess#memoryProperty() */
    public static Matcher<MemoryAccess> memory(Machine machine, RAM memory) {
        return memory(machine, Optional.ofNullable(memory));
    }
    
    /** @see MemoryAccess#memoryProperty() */
    public static Matcher<MemoryAccess> memory(Machine machine, Optional<RAM> memory) {
        return MachineBoundMatchers.optionalMachineValue("memory",
                MemoryAccess::getMemory,
                RAMMatchers::ram,
                machine,
                memory);
    }
    
    



}
