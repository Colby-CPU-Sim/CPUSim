/**
 * 
 */
package cpusim.model.microinstruction;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import cpusim.model.Machine;
import cpusim.model.module.Register;

import org.junit.Test;

/**
 * 
 * @author Kevin Brightwell
 * @since 2016-11-09
 */
public class TransferRtoRTest {

	/**
	 * Test method for {@link cpusim.model.microinstruction.TransferRtoR#execute()}.
	 */
	@Test
	public void testExecute() {

		final long SOURCE_VALUE = 0xFA; // 0b11111010
		final long DEST_VALUE = 0; // 0x00

		final Machine testMachine = new Machine("test");

		Register src = new Register("src", testMachine, 8, SOURCE_VALUE, Register.Access.readOnly());
        assertEquals(SOURCE_VALUE, src.getValue());
        
        Register dst = new Register("dest", testMachine, 8, DEST_VALUE, Register.Access.readWrite());
        assertEquals(DEST_VALUE, dst.getValue());

        TransferRtoR micro = new TransferRtoR("xfer", testMachine, src, 4, dst, 1, 4);

        // dest = (SOURCE_VALUE >> 4) << 1
        micro.execute();

        assertEquals((SOURCE_VALUE >> 4) << 1, dst.getValue());
	}

}
