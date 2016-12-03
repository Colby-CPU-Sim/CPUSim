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

		Register src = mock(Register.class);
        src.setValue(SOURCE_VALUE);
        assertEquals(SOURCE_VALUE, src.getValue());
        
        Register dst = mock(Register.class);
        dst.setValue(DEST_VALUE);
        
        assertEquals(DEST_VALUE, dst.getValue());

        TransferRtoR micro = mock(TransferRtoR.class);
        micro.setSource(src);
        micro.setSrcStartBit(4);

        micro.setDest(dst);
        micro.setDestStartBit(1);

        micro.setNumBits(5);

        // dest = (SOURCE_VALUE >> 4) << 1
        micro.execute();

        assertEquals((SOURCE_VALUE >> 4) << 1, dst.getValue());
	}

}
