package cpusim.model.module;

import cpusim.model.Machine;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static cpusim.model.harness.matchers.MachineBoundMatchers.boundTo;
import static cpusim.model.harness.matchers.NamedObjectMatchers.named;
import static cpusim.model.harness.matchers.module.RegisterMatchers.access;
import static cpusim.model.harness.matchers.module.RegisterMatchers.value;
import static cpusim.model.harness.matchers.module.SizedMatchers.width;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 *
 *
 * @since 2016-12-09
 */
public class RegisterTest {

    private Machine machine;
    private Register r;

    @Before
    public void setup() {
        machine = mock(Machine.class);

        r = new Register("r", UUID.randomUUID(), machine,
                4, 0, Register.Access.readWrite());
    }

    @Test
    public void creation() {
        assertThat(r, boundTo(sameInstance(machine)));
        assertThat(r, named("r"));
        assertThat(r, value(0));
        assertThat(r, width(r.getWidth()));
        assertThat(r, access(Register.Access.readWrite()));
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