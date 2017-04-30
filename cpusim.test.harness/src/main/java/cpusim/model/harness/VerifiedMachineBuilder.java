package cpusim.model.harness;

import cpusim.model.Field;
import cpusim.model.Machine;
import cpusim.model.harness.matchers.FieldMatchers;
import cpusim.model.harness.matchers.module.*;
import cpusim.model.microinstruction.Microinstruction;
import cpusim.model.module.*;
import cpusim.model.util.MachineBuilder;
import cpusim.model.util.NamedObject;
import cpusim.model.util.structure.*;
import org.hamcrest.Matcher;
import org.junit.rules.ErrorCollector;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasKey;

/**
 * {@link MachineBuilder} that builds a {@link Machine} and verifies that all of the
 * contents are as "they're supposed to be." This utilizes all of the matchers found in
 * {@link cpusim.model.harness.matchers}.
 *
 * <br />
 *
 * Provides utilities for building and verifying machines. This utility relies on similarly named
 * modules/micros/instructions be named <strong>uniquely</strong> so they be identified. This
 * is required because IDs are randomly assigned and can not be recalled on the fly to verify if two
 * are "conceptually" the same.
 */
public class VerifiedMachineBuilder extends MachineBuilder<Machine, VerifiedMachineBuilder> {

    private final ErrorCollector collector;

    public VerifiedMachineBuilder(ErrorCollector collector, final String machineName) {
        super(machineName);

        this.collector = collector;
    }

    private VerifiedMachineBuilder(final VerifiedMachineBuilder other) {
        super(other);

        this.collector = other.collector;
    }

    @Override
    protected VerifiedMachineBuilder copyOf() {
        return new VerifiedMachineBuilder(this);
    }

    @Override
    public Machine build() {
        Machine built = buildMachine(this);

        // We now use the Visitor to visit all the controlUnit and make sure it was
        // built correctly.
        built.acceptVisitor(new VerificationVisitor(built));

        return built;
    }

    private <T extends NamedObject> void visitMapping(Map<String, T> mapping,
                                                      T value,
                                                      Function<T, Matcher<T>> matcherFactory) {
        collector.checkThat(mapping, hasKey(value.getName()));

        T expected = mapping.get(value.getName());
        collector.checkThat(value, matcherFactory.apply(expected));
        mapping.remove(value.getName());
    }

    /**
     * Used to start visiting sub-components
     */
    private class VerificationVisitor extends AbstractMachineVisitor {

        private final Machine machine;
        private final Map<String, Field> fieldMapping;

        private VerificationVisitor(Machine machine) {
            this.machine = machine;

            this.fieldMapping = fields.stream()
                    .collect(Collectors.toMap(NamedObject::getName, Function.identity()));
        }

        @Override
        public Optional<ModuleVisitor> getModuleVisitor() {
            return Optional.of(new ModuleVerificationVisitor(machine));
        }

//        @Override
//        public Optional<MicroinstructionVisitor> getMicrosVisitor() {
//            return Optional.of(new MicroinstructionVerificationVisitor(machine));
//        }

        @Override
        public VisitResult visitIndexFromRight(boolean indexFromRight) {
            collector.checkThat(indexFromRight,
                    is(VerifiedMachineBuilder.this.isIndexedFromRight));

            return VisitResult.Continue;
        }

        @Override
        public VisitResult visitStartingAddressForLoading(int loadingAddress) {
            collector.checkThat(loadingAddress,
                    is(VerifiedMachineBuilder.this.startingAddressForLoading));

            return VisitResult.Continue;
        }

        @Override
        public VisitResult visitField(Field field) {
            visitMapping(fieldMapping, field, FieldMatchers::field);

            return VisitResult.Continue;
        }
    }

    @ParametersAreNonnullByDefault
    private class ModuleVerificationVisitor extends AbstractModuleVisitor {

        private final Machine machine;

        private final Map<Class<? extends Module<?>>, Map<String, ? extends Module<?>>> mapping;

        ModuleVerificationVisitor(Machine m) {
            this.machine = m;
            mapping = new HashMap<>();

            for (Class<? extends Module<?>> mclazz : modules.keySet()) {
                mapping.put(mclazz, modules.get(mclazz).stream()
                        .collect(Collectors.toMap(NamedObject::getName, Function.identity())));
            }
        }

        @Override
        public VisitResult visitCodeStore(@Nullable RAM codeStore) {
            collector.checkThat(codeStore,
                    is(RAMMatchers.ram(machine, VerifiedMachineBuilder.this.codeStore)));

            return VisitResult.Continue;
        }

        @Override
        public VisitResult visitProgramCounter(@Nullable Register pc) {
            collector.checkThat(pc,
                    is(RegisterMatchers.register(machine, VerifiedMachineBuilder.this.programCounter)));

            return VisitResult.Continue;
        }

        @Override
        public VisitResult visitControlUnit(@Nullable ControlUnit controlUnit) {
            collector.checkThat(controlUnit,
                    is(ControlUnitMatchers.controlUnit(machine, VerifiedMachineBuilder.this.controlUnit)));

            return VisitResult.Continue;
        }

        private <T extends Module<T>> void visitModule(Class<? extends T> clazz,
                                                       T value,
                                                       BiFunction<Machine, T, Matcher<T>> matcherFactory) {
            @SuppressWarnings("unchecked")
            Map<String, T> values = (Map<String, T>) mapping.get(clazz);
            visitMapping(values, value, t -> matcherFactory.apply(machine, t));
        }

        @Override
        public VisitResult visitRam(RAM ram) {
            visitModule(RAM.class, ram, RAMMatchers::ram);

            return VisitResult.Continue;
        }

        @Override
        public VisitResult visitConditionBit(ConditionBit conditionBit) {
            visitModule(ConditionBit.class, conditionBit, ConditionBitMatchers::conditionBit);

            return VisitResult.Continue;
        }

        @Override
        public VisitResult visitRegister(Register register) {
            visitModule(Register.class, register, RegisterMatchers::register);

            return VisitResult.Continue;
        }

        @Override
        public VisitResult visitRegisterArray(RegisterArray registerArray) {
            visitModule(RegisterArray.class, registerArray, RegisterArrayMatchers::registerArray);

            return VisitResult.Continue;
        }


    }

    @ParametersAreNonnullByDefault
    private class MicroinstructionVerificationVisitor implements MicroinstructionVisitor {

        private final Machine machine;

        MicroinstructionVerificationVisitor(Machine machine) {
            this.machine = machine;
        }

        @Override
        public VisitResult visitCategory(String category) {
            return null;
        }

        @Override
        public VisitResult visitType(Class<? extends Microinstruction<?>> clazz) {
            return null;
        }

        @Override
        public VisitResult visitMicro(Microinstruction<?> micro) {
            return null;
        }
    }
}
