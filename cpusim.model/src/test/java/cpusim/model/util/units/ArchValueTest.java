package cpusim.model.util.units;

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Test;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
class ArchValueTest {
    
    @Test
    void construction() {
        assertEquals(0, ArchType.Bit.value(0).asBits());
        
        assertEquals(ArchType.Bit.value(257), ArchType.Bit.value(257));
    }
    
    @Test(expected=IllegalArgumentException.class)
    void consNegativeNumber() {
        ArchType.Bit.value(-1);
    }
    
    @Test
    void arithmetic_sameType() {
        final ArchValue v_10 = ArchType.Bit.value(10);
		final ArchValue  v_2 = ArchType.Bit.value(2);
        
        assertEquals(ArchType.Bit.value(12),    v_10.add(v_2));
        assertEquals(ArchType.Bit.value(8),     v_10.sub(v_2));
        assertEquals(ArchType.Bit.value(20),    v_10.mul(v_2));
        assertEquals(ArchType.Bit.value(5),     v_10.div(v_2));
        assertEquals(ArchType.Bit.value(0),     v_10.mod(v_2));
        
        assertEquals(
            ArchType.Bit.value(v_10.asBits() << v_2.asBits()),
            v_10.shl(v_2));
            
        assertEquals(
            ArchType.Bit.value(v_10.asBits() >> v_2.asBits()),
            v_10.shr(v_2));
            
        assertEquals(
            ArchType.Bit.value(v_10.asBits() >>> v_2.asBits()),
            v_10.ashr(v_2));
    }
    
    @Test
    void arithmetic_differentType() {
        final ArchValue v_10 = ArchType.Bit.value(10);
		final ArchValue  v_2 = ArchType.Byte.value(1);
        
        assertEquals(ArchType.Bit.value(18),    v_10.add(v_2));
        assertEquals(ArchType.Bit.value(2),     v_10.sub(v_2));
        assertEquals(ArchType.Bit.value(80),    v_10.mul(v_2));
        assertEquals(ArchType.Bit.value(1),     v_10.div(v_2));
        assertEquals(ArchType.Bit.value(2),     v_10.mod(v_2));
        
        assertEquals(
            ArchType.Bit.value(v_10.asBits() << v_2.asBits()),
            v_10.shl(v_2));
            
        assertEquals(
            ArchType.Bit.value(v_10.asBits() >> v_2.asBits()),
            v_10.shr(v_2));
            
        assertEquals(
            ArchType.Bit.value(v_10.asBits() >>> v_2.asBits()),
            v_10.ashr(v_2));
    }
    
    @Test
    void conversions() {
    	final ArchValue bits_10 = ArchType.Bit.value(10);
    	final ArchValue bytes_conv = bits_10.as(ArchType.Byte);
    	
    	// Should return the minimum number of type that allows the value to store
    	
    	assertEquals(ArchType.Byte.value(2), bytes_conv);
    	assertEquals(ArchType.Bit.value(16), bytes_conv.as(ArchType.Bit));
    }
    
}