package cpusim.model;

import cpusim.model.harness.MachineInjectionRule;
import cpusim.model.util.structure.AbstractMachineVisitor;
import cpusim.model.util.structure.MachineVisitor;
import org.junit.Rule;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * Tests to work with the {@link cpusim.model.util.structure.MachineVisitor} traversal in a {@link Machine}
 */
public class MachineVisitorTest {

    @Rule
    public MachineInjectionRule machineInjectionRule = new MachineInjectionRule(this);

    @Test
    public void emptyValues() throws Exception {
        Machine m = machineInjectionRule.get();

        MachineVisitor empty = spy(new AbstractMachineVisitor() {});

        m.acceptVisitor(empty);

        verify(empty).visitName(m.getName());
        verify(empty).visitIndexFromRight(m.isIndexFromRight());
        verify(empty).visitStartingAddressForLoading(m.getStartingAddressForLoading());

        verify(empty, atLeastOnce()).getMicrosVisitor();
        verify(empty, never()).startMicros();
        verify(empty, never()).endMicros();

        verify(empty, atLeastOnce()).getModuleVisitor();
        verify(empty, never()).startModules();
        verify(empty, never()).endModules();

        verify(empty, atLeastOnce()).startFields(m.getFields());
        verify(empty, never()).visitField(null);
        verify(empty, atLeastOnce()).endFields(m.getFields());
    }
}
