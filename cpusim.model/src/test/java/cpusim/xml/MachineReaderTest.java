package cpusim.xml;

import cpusim.SamplesFixture;
import cpusim.model.Machine;
import cpusim.model.module.ConditionBit;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

/**
 * Test Machine Reader
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        MachineReaderTest.Wombat.class,
        MachineReaderTest.JVM.class,
        MachineReaderTest.Sim6502.class,
        MachineReaderTest.Course_Wombat.class,
        MachineReaderTest.MaxwellComputer.class
})
public class MachineReaderTest {

    MachineReader underTest = new MachineReader();

    @Test
    public void preconditions() {
        assertFalse("No machine should be present until run", underTest.getMachine().isPresent());
    }

    @Test @Ignore
    public void loadAllMachines() throws Exception {
        for (SamplesFixture samplesFixture : SamplesFixture.values()) {
            samplesFixture.loadAll().forEach(opt -> assertThat(opt, isPresent()));
        }
    }

//    @Test
//    public void parseEmptyMachine() throws Exception {
//        Machine newMachine = parseMachine("/cpus/empty.cpu");
//
//        // TODO add more verifications here
//        assertEquals("empty", newMachine.getName());
//        assertTrue(newMachine.getIndexFromRight());
//        assertThat(newMachine.getRegisters(), hasSize(0));
//    }


    // LEGACY PARSING

    public static class Wombat {

        @Test
        public void v1() throws Exception {
            Machine newMachine = SamplesFixture.WOMBAT.load(1).orElseThrow(NullPointerException::new);

            // TODO add more verifications here
            assertEquals("Wombat1", newMachine.getName());
            assertFalse(newMachine.getIndexFromRight());
            assertThat(newMachine.getRegisters(), hasSize(6));
        }

        @Test
        public void v2() throws Exception {
            Machine newMachine = SamplesFixture.WOMBAT.load(2).orElseThrow(NullPointerException::new);

            // TODO add more verifications here
            assertEquals("Wombat2", newMachine.getName());
            assertFalse(newMachine.getIndexFromRight());
            assertThat(newMachine.getRegisters(), hasSize(7));
        }

    }

    public static class JVM {
        @Test
        public void v1() throws Exception {
            Machine newMachine = SamplesFixture.COURSE_JVM.load(1).orElseThrow(NullPointerException::new);

            // TODO add more verifications here
            assertEquals("JVM1", newMachine.getName());
            assertFalse(newMachine.getIndexFromRight());
            assertThat(newMachine.getRegisters(), hasSize(11));
        }

        @Test
        public void v2() throws Exception {
            Machine newMachine = SamplesFixture.COURSE_JVM.load(2).orElseThrow(NullPointerException::new);

            // TODO add more verifications here
            assertEquals("JVM2", newMachine.getName());
            assertFalse(newMachine.getIndexFromRight());
            assertThat(newMachine.getRegisters(), hasSize(12));
        }


        @Test
        public void v3() throws Exception {
            Machine newMachine = SamplesFixture.COURSE_JVM.load(3).orElseThrow(NullPointerException::new);

            // TODO add more verifications here
            assertEquals("JVM3", newMachine.getName());
            assertFalse(newMachine.getIndexFromRight());
            assertThat(newMachine.getRegisters(), hasSize(12));
        }
    }

    public static class Course_Wombat {

        @Test
        public void v1() throws Exception {
            Machine newMachine = SamplesFixture.COURSE_WOMBAT.load(1).orElseThrow(NullPointerException::new);

            // TODO add more verifications here
            assertEquals("Wombat1", newMachine.getName());
            assertFalse(newMachine.getIndexFromRight());
            assertThat(newMachine.getRegisters(), hasSize(6));
        }

        @Test
        public void v2() throws Exception {
            Machine newMachine = SamplesFixture.COURSE_WOMBAT.load(2).orElseThrow(NullPointerException::new);

            // TODO add more verifications here
            assertEquals("Wombat2", newMachine.getName());
            assertFalse(newMachine.getIndexFromRight());
            assertThat(newMachine.getRegisters(), hasSize(7));
        }

        @Test
        public void v3() throws Exception {
            Machine newMachine = SamplesFixture.COURSE_WOMBAT.load(3).orElseThrow(NullPointerException::new);

            // TODO add more verifications here
            assertEquals("Wombat3", newMachine.getName());
            assertFalse(newMachine.getIndexFromRight());
            assertThat(newMachine.getRegisters(), hasSize(8));
        }

        @Test
        public void v4() throws Exception {
            Machine newMachine = SamplesFixture.COURSE_WOMBAT.load(4).orElseThrow(NullPointerException::new);

            // TODO add more verifications here
            assertEquals("Wombat4", newMachine.getName());
            assertFalse(newMachine.getIndexFromRight());
            assertThat(newMachine.getRegisters(), hasSize(8));
        }

        @Test
        public void v5() throws Exception {
            Machine newMachine = SamplesFixture.COURSE_WOMBAT.load(5).orElseThrow(NullPointerException::new);

            // TODO add more verifications here
            assertEquals("Wombat5", newMachine.getName());
            assertFalse(newMachine.getIndexFromRight());
            assertThat(newMachine.getRegisters(), hasSize(8));
        }

    }

    public static class Sim6502 {
        @Test
        public void v0() throws Exception {
            Machine newMachine = SamplesFixture.SIM6502.load(0).orElseThrow(NullPointerException::new);

            // TODO add more verifications here
            assertEquals("sim6502", newMachine.getName());
            assertTrue(newMachine.getIndexFromRight());
            assertThat(newMachine.getRegisters(), hasSize(11));
            assertThat(newMachine.getFields(), hasSize(4));
        }

        @Test
        public void v1() throws Exception {
            Machine newMachine = SamplesFixture.SIM6502.load(1).orElseThrow(NullPointerException::new);

            // TODO add more verifications here
            assertEquals("sim6502", newMachine.getName());
            assertTrue(newMachine.getIndexFromRight());
            assertThat(newMachine.getRegisters(), hasSize(11));
            assertThat(newMachine.getFields(), hasSize(4));

            assertThat(newMachine.getModules(ConditionBit.class), hasSize(9));
        }

        @Test
        public void v2() throws Exception {
            Machine newMachine = SamplesFixture.SIM6502.load(2).orElseThrow(NullPointerException::new);

            // TODO add more verifications here
            assertEquals("sim6502", newMachine.getName());
            assertTrue(newMachine.getIndexFromRight());
            assertThat(newMachine.getRegisters(), hasSize(11));
            assertThat(newMachine.getFields(), hasSize(4));

            assertThat(newMachine.getModules(ConditionBit.class), hasSize(9));
        }
    }

    public static class MaxwellComputer {

        @Test
        public void load() throws Exception {
            Machine newMachine = SamplesFixture.MAXWELL.load(0).orElseThrow(NullPointerException::new);

            // TODO add more verifications here
            assertEquals("Maxwell", newMachine.getName());
            assertFalse(newMachine.getIndexFromRight());
            assertThat(newMachine.getRegisters(), hasSize(8));
            assertThat(newMachine.getFields(), hasSize(17));

            assertThat(newMachine.getModules(ConditionBit.class), hasSize(5));

        }
    }


}