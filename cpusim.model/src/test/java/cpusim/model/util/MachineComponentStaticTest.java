package cpusim.model.util;

import cpusim.model.Machine;
import cpusim.model.module.Register;
import javafx.beans.property.*;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by kevin on 08/12/2016.
 */
public class MachineComponentStaticTest {

    abstract class TestComponent implements MachineComponent {

        @DependantComponent
        final ObjectProperty<Register> register = new SimpleObjectProperty<>(this, "register", null);


    }

    @Test
    public void collectDependancies() throws Exception {
        TestComponent testComponent = mock(TestComponent.class);

        ReadOnlySetProperty<MachineComponent> dependents = MachineComponent.collectDependancies(testComponent);

        assertEquals(0, dependents.size());

        UUID testId = UUID.randomUUID();
        Register mockReg = mock(Register.class);
        when(mockReg.getID()).thenReturn(testId);

        testComponent.register.set(mockReg);

        assertEquals(1, dependents.size());
        testComponent.register.setValue(null);

        assertEquals(0, dependents.size());
    }

    @Test
    public void copyOrNull() throws Exception {

    }

    @Test
    public void copyProperty() throws Exception {

    }

    @Test
    public void copyProperty1() throws Exception {

    }

    @Test
    public void copyListProperty() throws Exception {

    }

    @Test
    public void copyListProperty1() throws Exception {

    }

}