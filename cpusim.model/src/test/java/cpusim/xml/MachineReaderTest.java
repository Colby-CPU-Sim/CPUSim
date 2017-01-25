package cpusim.xml;

import cpusim.model.Machine;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

/**
 * Test Machine Reader
 */
public class MachineReaderTest {

    MachineReader underTest = new MachineReader();

    @Test
    public void preconditions() {
        assertFalse("No machine should be present until run", underTest.getMachine().isPresent());
    }

    Machine parseMachine(String resourceURI) throws Exception {
        checkNotNull(resourceURI);

        try (InputStream in = getClass().getResourceAsStream(resourceURI)) {
            underTest.parseDataFromStream(in);
            Optional<Machine> opt = underTest.getMachine();

            return opt.orElseThrow(AssertionError::new);
        }
    }

    @Test
    public void parseEmptyMachine() throws Exception {
        Machine newMachine = parseMachine("/cpus/empty.cpu");

        // TODO add more verifications here
        assertEquals("empty", newMachine.getName());
        assertTrue(newMachine.getIndexFromRight());
        assertThat(newMachine.getRegisters(), hasSize(0));
    }


    // LEGACY PARSING


    @Test
    public void legacy_parseWombat1() throws Exception {
        Machine newMachine = parseMachine("/cpus/legacy/Wombat1.cpu");

        // TODO add more verifications here
        assertEquals("Wombat1", newMachine.getName());
        assertFalse(newMachine.getIndexFromRight());
        assertThat(newMachine.getRegisters(), hasSize(6));
    }

    @Test @Ignore
    public void legacy_parseWombat2() throws Exception {
        Machine newMachine = parseMachine("/cpus/legacy/Wombat2.cpu");

        // TODO add more verifications here
        assertEquals("Wombat2", newMachine.getName());
        assertFalse(newMachine.getIndexFromRight());
        assertThat(newMachine.getRegisters(), hasSize(4));
    }

    @Test @Ignore
    public void legacy_parseJVM() throws Exception {
        Machine newMachine = parseMachine("/cpus/legacy/JVM2.cpu");
        // TODO add more verifications here
        assertEquals("JVM", newMachine.getName());
        assertFalse(newMachine.getIndexFromRight());
        assertThat(newMachine.getRegisters(), hasSize(4));
    }


}