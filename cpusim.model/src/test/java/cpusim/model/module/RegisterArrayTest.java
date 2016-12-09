package cpusim.model.module;

import cpusim.model.Machine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Test {@link RegisterArray} implementation
 * @since 2016-12-09
 */
public class RegisterArrayTest {

    private RegisterArray array;
    private Register single;

    @Before
    public void setup() {
        Machine m = mock(Machine.class);

        array = new RegisterArray("array", UUID.randomUUID(), m,
                4, 4, 0, Register.Access.readOnly());
        single = new Register("single", UUID.randomUUID(), m,
                6, 4, Register.Access.readWrite());
    }

    @Test
    public void construction() {
        assertEquals(4, array.getLength());
        assertEquals(4, array.getWidth());

        for (Register r: array.getRegisters()) {
            assertEquals(4, r.getWidth());
            assertEquals(Register.Access.readOnly(), r.getAccess());
        }

        assertEquals(4, array.getChildrenComponents().size());
    }

    @Test
    public void setWidth() throws Exception {
        array.setWidth(8);

        assertEquals(4, array.getLength());
        for (Register r: array.getRegisters()) {
            assertEquals(8, r.getWidth());
            assertEquals(Register.Access.readOnly(), r.getAccess());
        }
    }

    @Test
    public void setLength() throws Exception {
        array.setLength(8);

        assertEquals(8, array.getLength());
        for (Register r: array.getRegisters()) {
            assertEquals(4, r.getWidth());
            assertEquals(Register.Access.readOnly(), r.getAccess());
        }

        array.setLength(4);

        assertEquals(4, array.getLength());
        for (Register r: array.getRegisters()) {
            assertEquals(4, r.getWidth());
            assertEquals(Register.Access.readOnly(), r.getAccess());
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void modifyRegisters() {
        array.getRegisters().add(null);
    }

    @Test
    public void addRegister() throws Exception {
        ObservableList<Register> registers = FXCollections.observableArrayList();
        registers.addAll(array.getRegisters());

        registers.add(single);
        array.setRegisters(registers);
        assertEquals(5, array.getLength());

        Register s = array.getRegisters().get(array.getLength() - 1);

        assertEquals(s, single);
        assertEquals(4, s.getWidth());
    }

    @Test
    public void validate() throws Exception {
        array.validate();
    }

}