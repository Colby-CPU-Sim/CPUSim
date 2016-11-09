/**
 * 
 */
package cpusim.model.microinstruction;

import static org.junit.Assert.*;

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
		
		Register src = new Register("SRC", 10); 
        src.setValue(SOURCE_VALUE);
        assertEquals(SOURCE_VALUE, src.getValue());
        
        Register dst = new Register("DST", 8);
        dst.setValue(DEST_VALUE);
        
        assertEquals(DEST_VALUE, dst.getValue());
        
        Machine machine = new Machine("test");
        
        TransferRtoR micro = new TransferRtoR("milo", machine, src, 4, dst, 1, 5);
        // dest = (SOURCE_VALUE >> 4) << 1
        micro.execute();

        assertEquals(30, dst.getValue());
	}

}
