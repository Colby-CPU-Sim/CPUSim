package cpusim.model.util.units;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class ArchValueTest {
    
    @Test
    public void construction() {
        assertEquals(0, ArchType.Bit.of(0).as());
        
        assertEquals(ArchType.Bit.of(257), ArchType.Bit.of(257));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void consNegativeNumber() {
        ArchType.Bit.of(-1);
    }
    
    @Test
    public void arithmetic_sameType() {
        final ArchValue v_10 = ArchType.Bit.of(10);
		final ArchValue  v_2 = ArchType.Bit.of(2);
        
        assertEquals(ArchType.Bit.of(12),    v_10.add(v_2));
        assertEquals(ArchType.Bit.of(8),     v_10.sub(v_2));
        assertEquals(ArchType.Bit.of(20),    v_10.mul(v_2));
        assertEquals(ArchType.Bit.of(5),     v_10.div(v_2));
        assertEquals(ArchType.Bit.of(0),     v_10.mod(v_2));
        
        assertEquals(
            ArchType.Bit.of(v_10.as() << v_2.as()),
            v_10.shl(v_2));
            
        assertEquals(
            ArchType.Bit.of(v_10.as() >> v_2.as()),
            v_10.shr(v_2));
            
        assertEquals(
            ArchType.Bit.of(v_10.as() >>> v_2.as()),
            v_10.ashr(v_2));
    }
    
    @Test
    public void arithmetic_differentType() {
        final ArchValue v_10b = ArchType.Bit.of(10);
		final ArchValue  v_1B = ArchType.Byte.of(1);
        
        assertEquals(ArchType.Bit.of(18),    v_10b.add(v_1B));
        assertEquals(ArchType.Bit.of(2),     v_10b.sub(v_1B));
        assertEquals(ArchType.Bit.of(80),    v_10b.mul(v_1B));
        assertEquals(ArchType.Bit.of(1),     v_10b.div(v_1B));
        assertEquals(ArchType.Bit.of(2),     v_10b.mod(v_1B));
        
        assertEquals(
            ArchType.Bit.of(v_10b.as() << v_1B.as()),
            v_10b.shl(v_1B));
            
        assertEquals(
            ArchType.Bit.of(v_10b.as() >> v_1B.as()),
            v_10b.shr(v_1B));
            
        assertEquals(
            ArchType.Bit.of(v_10b.as() >>> v_1B.as()),
            v_10b.ashr(v_1B));
    }
    
    @Test
    public void conversions() {
    	final ArchValue bits_10 = ArchType.Bit.of(10);
    	final ArchValue bytes_conv = bits_10.convert(ArchType.Byte);
    	
    	// Should return the minimum number of type that allows the value to store
    	
    	assertEquals(ArchType.Byte.of(2), bytes_conv);
    	assertEquals(ArchType.Bit.of(16), bytes_conv.convert(ArchType.Bit));
    }
    
    @Test
    public void serialization() throws JsonParseException, JsonMappingException, IOException {
    	
    	ObjectMapper mapper = new ObjectMapper(); // create once, reuse
    	ArchValue read = mapper.readValue("{ \"type\": \"Bit\", \"value\": 0 }", ArchValue.class);

    	assertEquals(ArchType.Bit.of(0), read);
    	
    	try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
    		mapper.writeValue(out, ArchType.Bit.of(2));
    		
    		final String strOut = out.toString(Charset.defaultCharset().name());
        	assertEquals(ArchType.Bit.of(2), mapper.readValue(strOut, ArchValue.class));
    	}
    }

    @Test(expected = IllegalStateException.class)
    public void mask_tooLarge() {
        final ArchValue bytes_8 = ArchType.Byte.of(9);
        bytes_8.mask();
    }
}