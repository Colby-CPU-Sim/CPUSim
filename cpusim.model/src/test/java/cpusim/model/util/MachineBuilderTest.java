package cpusim.model.util;

import com.github.npathai.hamcrestopt.OptionalMatchers;
import com.google.common.collect.ImmutableMap;
import cpusim.model.Machine;
import cpusim.model.harness.matchers.MachineMatchers;
import cpusim.model.harness.matchers.module.ControlUnitMatchers;
import cpusim.model.microinstruction.Comment;
import cpusim.model.microinstruction.End;
import cpusim.model.microinstruction.Microinstruction;
import cpusim.model.module.ControlUnit;
import cpusim.model.module.Module;
import cpusim.model.module.RAM;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Test the functionality of the {@link MachineBuilder}
 */
public class MachineBuilderTest {

    private TestBuilder underTest;

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private static final String MACHINE_NAME = "test-machine";

    private RAM codeStore;
    private ControlUnit controlUnit;
    private int startingAddressForLoading = 0;
    private boolean isIndexedFromRight = true;

    @Before
    public void setup() {
        this.underTest = new TestBuilder(MACHINE_NAME);

        codeStore = new RAM("codeStore", UUID.randomUUID(), underTest.getMachine(),
                1024, 8);
        controlUnit = new ControlUnit("controlUnit", UUID.randomUUID(), underTest.getMachine());
        
        
    }

    /**
     * Helper function that verifies the behaviour of the builder is correct (returns a new instance
     * that is cloned).
     *
     * @param <P> Parameter type
     * @param method Method to call on the builder
     * @param param Parameter passed to the function
     * @return {@link #underTest}
     */
    private <P> void runMethod(Function<P, TestBuilder> method, P param) {
        TestBuilder newBuilder = method.apply(param);
        assertThat(newBuilder, not(sameInstance(underTest)));

        underTest = newBuilder;
    }

    /**
     * Test that with no changes, the "base" machine is built correctly
     */
    @Test
    public void base() {
        underTest.verify(base -> {
            errorCollector.checkThat(base, MachineMatchers.indexedFromRight());
            errorCollector.checkThat(base.getCodeStore(), OptionalMatchers.isEmpty());
            errorCollector.checkThat(base.getControlUnit(), OptionalMatchers.isEmpty());
            errorCollector.checkThat(base.getProgramCounter(), OptionalMatchers.isEmpty());

            ImmutableMap<Class<? extends Module<?>>, List<? extends Module<?>>> modules =
                    base.getModuleMap();
            for (Class<? extends Module<?>> mclazz : modules.keySet()) {
                errorCollector.checkThat(modules.get(mclazz), empty());
            }

            ImmutableMap<Class<? extends Microinstruction<?>>, List<? extends Microinstruction<?>>> micros =
                    base.getMicrosMap();
            for (Class<? extends Microinstruction<?>> mclazz : micros.keySet()) {
                if (mclazz == End.class || mclazz == Comment.class) {
                    errorCollector.checkThat(micros.get(mclazz), hasSize(1));
                } else {
                    errorCollector.checkThat(micros.get(mclazz), empty());
                }
            }
        });
    }

    @Test
    public void codeStore() {
        runMethod(underTest::withCodeStore, codeStore);

        underTest.verify(base -> {
            errorCollector.checkThat(base.getCodeStore(),
                    OptionalMatchers.hasValue(codeStore));
        });
    }

    @Test
    public void controlUnit() {
        runMethod(underTest::withControlUnit, controlUnit);

        underTest.verify(base -> {
            errorCollector.checkThat(base.getControlUnit(),
                    OptionalMatchers.hasValue(ControlUnitMatchers.controlUnit(base, controlUnit)));
        });

        runMethod(underTest::withControlUnit, null);
        underTest.verify(base -> {
            errorCollector.checkThat(base.getControlUnit(), OptionalMatchers.isEmpty());
        });
    }


    public class TestBuilder extends MachineBuilder<Machine, TestBuilder> {

        TestBuilder(String machineName) {
            super(machineName);
        }

        TestBuilder(TestBuilder other) {
            super(other);
        }

        @Override
        protected TestBuilder copyOf() {
            return new TestBuilder(this);
        }

        @Override
        public Machine build() {
            Machine fromParts = buildMachine(this);
            assertNotNull("machine is null", fromParts);

            return fromParts;
        }

        public void verify(Consumer<Machine> verification) {
            verification.accept(build());
        }
    }

}