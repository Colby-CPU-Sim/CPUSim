package cpusim.model.harness.matchers;

import com.google.common.collect.Lists;
import cpusim.model.Field;
import cpusim.model.Machine;
import cpusim.model.MachineInstruction;
import cpusim.model.util.units.ArchValue;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

import static cpusim.model.harness.matchers.NamedObjectMatchers.named;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * Matchers for {@link cpusim.model.MachineInstruction}
 */
public abstract class MachineInstructionMatchers {
    
    private MachineInstructionMatchers() {
        // no initialization
    }
    
    /**
     * Creates a {@link Matcher} for a {@link MachineInstruction}.
     * @return Matcher
     *
     * @see MachineInstruction
     */
    public static Matcher<MachineInstruction> machineInstruction(Machine machine, MachineInstruction expected) {
        return compose("Machine Instruction",
                compose(opcode(expected.getOpcode()))
                    .and(instructionFields(expected.getInstructionFields()))
                    .and(bitWidth(expected.getNumBits()))
                    .and(assemblyFields(expected.getAssemblyFields()))
                    .and(named(expected.getName())));
    }
    
    
    /** @see MachineInstruction#numBitsProperty() */
    public static Matcher<MachineInstruction> bitWidth(int width) {
        return bitWidth(ArchValue.bits(width));
    }
    
    /** @see MachineInstruction#numBitsProperty() */
    public static Matcher<MachineInstruction> bitWidth(ArchValue width) {
        return hasFeature("bit width",
                ArchValue.wrapAsBits(MachineInstruction::getNumBits),
                equalTo(width));
    }
    
    /** @see MachineInstruction#opcodeProperty() */
    public static Matcher<MachineInstruction> opcode(long opcode) {
        return hasFeature("op-code", MachineInstruction::getOpcode, is(opcode));
    }
    
    /** @see MachineInstruction#instructionFieldsProperty() */
    public static Matcher<MachineInstruction> instructionFields(Field... values) {
        return instructionFields(Arrays.asList(values));
    }
    
    /** @see MachineInstruction#instructionFieldsProperty() */
    public static Matcher<MachineInstruction> instructionFields(Iterator<? extends Field> values) {
        return instructionFields(Lists.newArrayList(values));
    }
    
    /** @see MachineInstruction#instructionFieldsProperty() */
    public static Matcher<MachineInstruction> instructionFields(Collection<? extends Field> values) {
        return new TypedMatcher<MachineInstruction>(MachineInstruction.class) {
            @Override
            public boolean typedMatches(MachineInstruction item) {
                return Matchers.containsInAnyOrder(values.stream()
                        .map(FieldMatchers::field)
                        .collect(Collectors.toList()))
                        .matches(item);
            }
            
            @Override
            public void describeTo(Description description) {
                description.appendText(" has instruction fields ")
                        .appendValue(values);
            }
        };
    }
    
    /** @see MachineInstruction#assemblyFieldsProperty() */
    public static Matcher<MachineInstruction> assemblyFields(Field... values) {
        return assemblyFields(Arrays.asList(values));
    }
    
    /** @see MachineInstruction#assemblyFieldsProperty() */
    public static Matcher<MachineInstruction> assemblyFields(Iterator<? extends Field> values) {
        return assemblyFields(Lists.newArrayList(values));
    }
    
    /** @see MachineInstruction#assemblyFieldsProperty() */
    public static Matcher<MachineInstruction> assemblyFields(Collection<? extends Field> values) {
        return new TypedMatcher<MachineInstruction>(MachineInstruction.class) {
            @Override
            public boolean typedMatches(MachineInstruction item) {
                return Matchers.containsInAnyOrder(values.stream()
                        .map(FieldMatchers::field)
                        .collect(Collectors.toList()))
                        .matches(item);
            }
            
            @Override
            public void describeTo(Description description) {
                description.appendText(" has assembly fields ")
                        .appendValue(values);
            }
        };
    }
}
