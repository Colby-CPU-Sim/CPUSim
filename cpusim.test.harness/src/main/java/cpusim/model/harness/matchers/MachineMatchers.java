package cpusim.model.harness.matchers;

import com.github.npathai.hamcrestopt.OptionalMatchers;
import com.google.common.collect.Lists;
import cpusim.model.Field;
import cpusim.model.Machine;
import cpusim.model.MachineInstruction;
import cpusim.model.assembler.EQU;
import cpusim.model.harness.matchers.microinstruction.*;
import cpusim.model.harness.matchers.module.*;
import cpusim.model.microinstruction.*;
import cpusim.model.module.*;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.junit.Assert.fail;

/**
 * Matchers for {@link cpusim.model.Machine} values
 */
@SuppressWarnings({"WeakerAccess", "OptionalUsedAsFieldOrParameterType"})
public abstract class MachineMatchers {
    
    private MachineMatchers() {
        // no instantiate
    }

    /**
     * Creates a {@link Matcher} for all components of a {@link Machine}.
     *
     * @return Matcher
     * @see Machine
     */
    public static Matcher<Machine> machine(Machine expected) {
        return compose("Machine",
                compose(NamedObjectMatchers.<Machine>named(expected.getName()))
                        .and(indexedFromRight(expected.isIndexFromRight()))
                    .and(startingAddressForLoading(expected.getStartingAddressForLoading()))
                    .and(controlUnit(expected.getControlUnit()))
                    .and(programCounter(expected.getProgramCounter()))
                    .and(equs(expected.getEQUs()))
                    .and(fields(expected.getFields()))
                    .and(fetchSequence(expected.getFetchSequence()))
                    .and(modules(expected.getModuleMap()))
                    .and(micros(expected.getMicrosMap())));
    }
    
    /** @see Machine#indexFromRightProperty()  */
    public static Matcher<Machine> indexedFromRight(boolean isFromRight) {
        return hasFeature("indexed from right",
                Machine::isIndexFromRight,
                equalTo(isFromRight));
    }
    
    /** @see Machine#indexFromRightProperty()  */
    public static Matcher<Machine> indexedFromRight() {
        return hasFeature("indexed from right",
                Machine::isIndexFromRight,
                equalTo(true));
    }
    
    
    /** @see Machine#indexFromRightProperty()  */
    public static Matcher<Machine> indexedFromLeft() {
        return hasFeature("indexed from left",
                Machine::isIndexFromRight,
                equalTo(false));
    }
    
    /** @see Machine#startingAddressForLoadingProperty()  */
    public static Matcher<Machine> startingAddressForLoading(int startingAddress) {
        return hasFeature("starting address for loading",
                Machine::getStartingAddressForLoading,
                equalTo(startingAddress));
    }
    
    // Helper for optional properties
    private static <T> Matcher<Machine> forOptional(String textDesc,
                                                    Function<Machine, Optional<T>> accessor,
                                                    BiFunction<Machine, T, Matcher<T>> matcher,
                                                    Optional<T> check) {
        return new TypeSafeMatcher<Machine>(Machine.class) {
            @Override
            public boolean matchesSafely(final Machine item) {
                final Matcher<Machine> m;
                //noinspection OptionalIsPresent
                if (check.isPresent()) {
                    m = hasFeature(textDesc, accessor, OptionalMatchers.hasValue(matcher.apply(item, check.get())));
                } else {
                    m = hasFeature(textDesc, accessor, OptionalMatchers.isEmpty());
                }
                
                return m.matches(item);
            }
        
            @Override
            public void describeTo(final Description description) {
                description.appendText(textDesc)
                        .appendValue(check);
            }
        };
    }
    
    /** @see Machine#controlUnitProperty()  */
    public static Matcher<Machine> controlUnit(Optional<ControlUnit> unit) {
        return forOptional("control unit", Machine::getControlUnit, ControlUnitMatchers::controlUnit, unit);
    }
    
    /** @see Machine#controlUnitProperty()  */
    public static Matcher<Machine> controlUnit(ControlUnit unit) {
        return controlUnit(Optional.ofNullable(unit));
    }
    
    /** @see Machine#codeStoreProperty()  */
    public static Matcher<Machine> codeStore(Optional<RAM> codeStore) {
        return forOptional("has code store",
                Machine::getCodeStore,
                RAMMatchers::ram,
                codeStore);
    }
    
    /** @see Machine#codeStoreProperty()  */
    public static Matcher<Machine> codeStore(RAM codeStore) {
        return codeStore(Optional.ofNullable(codeStore));
    }
    
    /** @see Machine#programCounterProperty()  */
    public static Matcher<Machine> programCounter(Optional<Register> pc) {
        return forOptional("has program counter",
                Machine::getProgramCounter,
                RegisterMatchers::register,
                pc);
    }
    
    /** @see Machine#programCounterProperty()  */
    public static Matcher<Machine> programCounter(Register pc) {
        return programCounter(Optional.ofNullable(pc));
    }
    
    /** @see Machine#equsProperty() */
    public static Matcher<Machine> equs(EQU... values) {
        return equs(Arrays.asList(values));
    }
    
    /** @see Machine#equsProperty() */
    public static Matcher<Machine> equs(Iterator<? extends EQU> values) {
        return equs(Lists.newArrayList(values));
    }
    
    /** @see Machine#equsProperty() */
    public static Matcher<Machine> equs(Collection<? extends EQU> values) {
        return new TypeSafeMatcher<Machine>(Machine.class) {
            @Override
            public boolean matchesSafely(Machine item) {
                return Matchers.containsInAnyOrder(values.stream()
                                .map(EQUMatchers::equ)
                                .collect(Collectors.toList()))
                                .matches(item);
            }
            
            @Override
            public void describeTo(Description description) {
                description.appendText(" has EQUs ")
                        .appendValue(values);
            }
        };
    }
    
    /** @see Machine#fieldsProperty() */
    public static Matcher<Machine> fields(Field... values) {
        return fields(Arrays.asList(values));
    }
    
    /** @see Machine#fieldsProperty() */
    public static Matcher<Machine> fields(Iterator<? extends Field> values) {
        return fields(Lists.newArrayList(values));
    }
    
    /** @see Machine#fieldsProperty() */
    public static Matcher<Machine> fields(Collection<? extends Field> values) {
        return new TypeSafeMatcher<Machine>(Machine.class) {
            @Override
            public boolean matchesSafely(Machine item) {
                return Matchers.containsInAnyOrder(values.stream()
                        .map(FieldMatchers::field)
                        .collect(Collectors.toList()))
                        .matches(item);
            }
            
            @Override
            public void describeTo(Description description) {
                description.appendText(" has EQUs ")
                        .appendValue(values);
            }
        };
    }
    
    /** @see Machine#fetchSequenceProperty() */
    public static Matcher<Machine> fetchSequence(MachineInstruction machineInstruction) {
        return fetchSequence(Optional.ofNullable(machineInstruction));
    }
    
    /** @see Machine#fetchSequenceProperty() */
    public static Matcher<Machine> fetchSequence(Optional<MachineInstruction> machineInstruction) {
        return forOptional("fetch sequence",
                Machine::getFetchSequence,
                MachineInstructionMatchers::machineInstruction,
                machineInstruction);
    }
    
    /** @see Machine#modulesProperty() */
    public static Matcher<Machine> modules(Class<? extends Module<?>> clazz, Module<?>... values) {
        return modules(clazz, Arrays.asList(values));
    }
    
    /** @see Machine#modulesProperty() */
    public static Matcher<Machine> modules(Class<? extends Module<?>> clazz, Iterator<? extends Module<?>> values) {
        return modules(clazz, Lists.newArrayList(values));
    }
    
    // Helper method for module collections
    private static <T extends Module<T>> Matcher<Machine> moduleMatcher(Class<T> clazz,
                                                                        BiFunction<Machine, T, Matcher<T>> matcher,
                                                                        Collection<? extends T> values) {
        return new TypeSafeMatcher<Machine>(Machine.class) {
            @Override
            public boolean matchesSafely(Machine item) {
                return Matchers.containsInAnyOrder(values.stream()
                        .map(r -> matcher.apply(item, r))
                        .collect(Collectors.toList()))
                        .matches(item);
            }
        
            @Override
            public void describeTo(Description description) {
                description.appendText(" has ")
                        .appendText(clazz.getSimpleName())
                        .appendText(" modules ")
                        .appendValue(values);
            }
        };
    }
    
    /** @see Machine#modulesProperty() */
    @SuppressWarnings("unchecked")
    public static Matcher<Machine> modules(
            Class<? extends Module<?>> clazz,
            Collection<? extends Module<?>> values) {
        
        if (RAM.class.isAssignableFrom(clazz)) {
            return moduleMatcher(RAM.class,
                    RAMMatchers::ram,
                    (Collection<RAM>)values);
        } else if (Register.class.isAssignableFrom(clazz)) {
            return moduleMatcher(Register.class,
                    RegisterMatchers::register,
                    (Collection<Register>)values);
        } else if (RegisterArray.class.isAssignableFrom(clazz)) {
            return moduleMatcher(RegisterArray.class,
                    RegisterArrayMatchers::registerArray,
                    (Collection<RegisterArray>)values);
        } else if (ConditionBit.class.isAssignableFrom(clazz)) {
            return moduleMatcher(ConditionBit.class,
                    ConditionBitMatchers::conditionBit,
                    (Collection<ConditionBit>)values);
        } else {
            fail("Unknown Module: " + clazz);
            return null;
        }
    }
    
    
    /** @see Machine#modulesProperty()  */
    public static Matcher<Machine> modules(Map<Class<? extends Module<?>>, List<? extends Module<?>>> modulesMap) {
        Iterable<Matcher<? super Machine>> matchers = modulesMap.entrySet().stream()
                .map(e -> modules(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return new TypeSafeMatcher<Machine>(Machine.class) {
            @Override
            public boolean matchesSafely(final Machine item) {
                return allOf(matchers).matches(item);
            }
            
            @Override
            public void describeTo(final Description description) {
                description.appendList("micros: ", ", ", " ", matchers);
            }
        };
    }
    
    // Helper for matching microinstruction Collections
    private static <T extends Microinstruction<T>>
    Matcher<Machine> microMatcher(Class<T> clazz,
                                  BiFunction<Machine, T, Matcher<T>> matcher,
                                  Collection<? extends T> values) {
        return new TypeSafeMatcher<Machine>(Machine.class) {
            @Override
            public boolean matchesSafely(Machine item) {
                return Matchers.containsInAnyOrder(values.stream()
                        .map(r -> matcher.apply(item, r))
                        .collect(Collectors.toList()))
                        .matches(item);
            }
            
            @Override
            public void describeTo(Description description) {
                description.appendText(" has ")
                        .appendText(clazz.getSimpleName())
                        .appendText(" micros ")
                        .appendValue(values);
            }
        };
    }
    
    /** @see Machine#microsProperty() ()  */
    public static Matcher<Machine> micros(Class<? extends Microinstruction<?>> clazz, Microinstruction<?>... values) {
        return micros(clazz, Arrays.asList(values));
    }
    
    /** @see Machine#microsProperty() ()  */
    public static Matcher<Machine> micros(Class<? extends Microinstruction<?>> clazz, Iterator<? extends Microinstruction<?>> values) {
        return micros(clazz, Lists.newArrayList(values));
    }
    
    /** @see Machine#microsProperty() ()  */
    @SuppressWarnings("unchecked")
    public static Matcher<Machine> micros(
            Class<? extends Microinstruction<?>> clazz,
            Collection<? extends Microinstruction<?>> values) {
        // This method makes me super sad. I could not think of any way to do this while maintaining any kind of
        // *real* type-safety. This does keep it, but is clunky and brittle.
        
        if (Arithmetic.class.isAssignableFrom(clazz)) {
            return microMatcher(Arithmetic.class,
                    ArithmeticMatchers::arithmetic,
                    (Collection<Arithmetic>)values);
        }
        
        else if (Branch.class.isAssignableFrom(clazz)) {
            return microMatcher(Branch.class,
                    BranchMatchers::branch,
                    (Collection<Branch>)values);
        }
        
        else if (Comment.class.isAssignableFrom(clazz)) {
            return microMatcher(Comment.class,
                    CommentMatchers::comment,
                    (Collection<Comment>)values);
        }
        
        else if (Decode.class.isAssignableFrom(clazz)) {
            return microMatcher(Decode.class,
                    DecodeMatchers::decode,
                    (Collection<Decode>)values);
        }

        else if (Increment.class.isAssignableFrom(clazz)) {
            return microMatcher(Increment.class,
                    IncrementMatchers::increment,
                    (Collection<Increment>)values);
        }
        
        else if (IO.class.isAssignableFrom(clazz)) {
            return microMatcher(IO.class,
                    IOMatchers::io,
                    (Collection<IO>)values);
        }

        else if (Logical.class.isAssignableFrom(clazz)) {
            return microMatcher(Logical.class,
                    LogicalMatchers::logical,
                    (Collection<Logical>)values);
        }

        else if (MemoryAccess.class.isAssignableFrom(clazz)) {
            return microMatcher(MemoryAccess.class,
                    MemoryAccessMatchers::memoryAccess,
                    (Collection<MemoryAccess>)values);
        }

        else if (SetBits.class.isAssignableFrom(clazz)) {
            return microMatcher(SetBits.class,
                    SetBitsMatchers::setBits,
                    (Collection<SetBits>)values);
        }

        else if (SetCondBit.class.isAssignableFrom(clazz)) {
            return microMatcher(SetCondBit.class,
                    SetCondBitMatchers::setCondBit,
                    (Collection<SetCondBit>)values);
        }

        else if (Shift.class.isAssignableFrom(clazz)) {
            return microMatcher(Shift.class,
                    ShiftMatchers::shift,
                    (Collection<Shift>)values);
        }
        
        else if (Test.class.isAssignableFrom(clazz)) {
            return microMatcher(Test.class,
                    TestMatchers::test,
                    (Collection<Test>)values);
        }

        else if (TransferAtoR.class.isAssignableFrom(clazz)) {
            return microMatcher(TransferAtoR.class,
                    TransferAtoRMatchers::transferAtoR,
                    (Collection<TransferAtoR>)values);
        }

        else if (TransferRtoA.class.isAssignableFrom(clazz)) {
            return microMatcher(TransferRtoA.class,
                    TransferRtoAMatchers::transferRtoA,
                    (Collection<TransferRtoA>)values);
        }

        else if (TransferRtoR.class.isAssignableFrom(clazz)) {
            return microMatcher(TransferRtoR.class,
                    TransferRtoRMatchers::transferRtoR,
                    (Collection<TransferRtoR>)values);
        } else {
            
            fail("No matcher for microinstruction class: " + clazz);
            return null;
        }
    }
    
    /** @see Machine#microsProperty() ()  */
    public static Matcher<Machine> micros(Map<Class<? extends Microinstruction<?>>, List<? extends Microinstruction<?>>> microsMap) {
        Iterable<Matcher<? super Machine>> matchers = microsMap.entrySet().stream()
                .map(e -> micros(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return new TypeSafeMatcher<Machine>(Machine.class) {
            @Override
            public boolean matchesSafely(final Machine item) {
                return allOf(matchers).matches(item);
            }
    
            @Override
            public void describeTo(final Description description) {
                description.appendList("micros: ", ", ", " ", matchers);
            }
        };
    }
}
