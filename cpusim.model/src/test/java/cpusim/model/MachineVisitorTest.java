package cpusim.model;

import cpusim.model.harness.MachineInjectionRule;
import cpusim.model.util.structure.MachineVisitor;
import org.junit.Rule;
import org.junit.Test;

import java.util.Optional;

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

        MachineVisitor empty = mock(MachineVisitor.class);

        when(empty.getMicrosVisitor()).thenReturn(Optional.empty());
        when(empty.getModuleVisitor()).thenReturn(Optional.empty());

        m.acceptVisitor(empty);

        verify(empty).visitName(m.getName());
        verify(empty).visitIndexFromRight(m.isIndexFromRight());
        verify(empty).visitStartingAddressForLoading(m.getStartingAddressForLoading());

        verify(empty, atLeastOnce()).startMicros();
        verify(empty, atLeastOnce()).getMicrosVisitor();
        verify(empty, atLeastOnce()).endMicros();


        verify(empty, atLeastOnce()).startModules();
        verify(empty, atLeastOnce()).getModuleVisitor();
        verify(empty, atLeastOnce()).endModules();

        verify(empty, atLeastOnce()).startFields(m.getFields());
        verify(empty, never()).visitField(null);
        verify(empty, atLeastOnce()).endFields(m.getFields());
    }
}
