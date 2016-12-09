package cpusim.model.module;

import cpusim.model.Machine;
import cpusim.model.util.ValidationException;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.Mac;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 *
 *
 * @since 2016-12-09
 */
public class RegisterTest {

    private Register r;

    @Before
    public void setup() {
        Machine machine = mock(Machine.class);

        r = new Register("r", UUID.randomUUID(), machine,
                4, 0, Register.Access.readWrite());
    }

    @Test
    public void creation() {
        assertEquals(0, r.getValue());
        assertEquals(4, r.getWidth());
        assertEquals(Register.Access.readWrite(), r.getAccess());
    }

    @Test
    public void setWidth() {
        r.setValue(0xf);
        assertEquals(0xf, r.getValue());

        r.setWidth(5);
        assertEquals(0xf, r.getValue());

        // The width causes it to narrow
        r.setWidth(2);
        assertEquals(0, r.getValue());
    }

    @Test
    public void validate() throws Exception {
        r.validate(); // no throw
    }

}