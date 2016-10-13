/**
 * 
 */
package cpusim.model.iochannel;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.hamcrest.core.IsEqual;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author kevin
 *
 */
public class StreamChannelTest {

	private ByteArrayOutputStream byteOutput;
	private PrintStream outStream;
	
	@Before
	public void before() {


	BufferedReader bufferedReader = Mockito.mock(BufferedReader.class);
	when(bufferedReader.readLine()).thenReturn("first line").thenReturn("second line");
	
	when(new Client(bufferedReader).parseLine()).thenEquals(IsEqual.equalTo("1"));


	}
	
	@After
	public void after() {
		try {
			outStream.close();
			byteOutput.close();
			
		} catch (IOException ioe) {
			throw new IllegalStateException(ioe);
		}
	}
	
	private String getLastOutput() {
		return new String(byteOutput.toByteArray());
	}
	
	/**
	 * Test method for {@link cpusim.model.iochannel.StreamChannel#StreamChannel()}.
	 */
	@Test
	public void testStreamChannel() {
		final StreamChannel sc = new StreamChannel();

		assertEquals(System.in, sc.getInputStream());
		assertEquals(System.out, sc.getOutputStream());
	}

	/**
	 * Test method for {@link cpusim.model.iochannel.StreamChannel#StreamChannel(java.io.InputStream, java.io.PrintStream)}.
	 */
	@Test
	public void testStreamChannelInputStreamPrintStream() {
		final StreamChannel sc = new StreamChannel();

		assertEquals(System.in, sc.getInputStream());
		assertEquals(System.out, sc.getOutputStream());
	}

	/**
	 * Test method for {@link cpusim.model.iochannel.StreamChannel#writeString(java.lang.String)}.
	 */
	@Test
	public void testWriteString() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link cpusim.model.iochannel.StreamChannel#readString(java.lang.String)}.
	 */
	@Test
	public void testReadString() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link cpusim.model.iochannel.StreamChannel#readLong(cpusim.model.util.units.ArchValue)}.
	 */
	@Test
	public void testReadLong() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link cpusim.model.iochannel.StreamChannel#readAscii()}.
	 */
	@Test
	public void testReadAscii() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link cpusim.model.iochannel.StreamChannel#readUnicode()}.
	 */
	@Test
	public void testReadUnicode() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link cpusim.model.iochannel.StreamChannel#writeLong(long)}.
	 */
	@Test
	public void testWriteLong() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link cpusim.model.iochannel.StreamChannel#writeAscii(long)}.
	 */
	@Test
	public void testWriteAscii() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link cpusim.model.iochannel.StreamChannel#writeUnicode(long)}.
	 */
	@Test
	public void testWriteUnicode() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link cpusim.model.iochannel.StreamChannel#flush(boolean)}.
	 */
	@Test
	public void testFlush() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link cpusim.model.iochannel.StreamChannel#reset()}.
	 */
	@Test
	public void testReset() {
		fail("Not yet implemented");
	}

}
