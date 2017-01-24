/**
 * 
 */
package cpusim.model.iochannel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import com.google.common.primitives.Chars;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import cpusim.model.util.conversion.ConvertLongs;
import cpusim.model.util.units.ArchType;
import cpusim.model.util.units.ArchValue;

import javafx.scene.shape.Arc;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author kevin
 *
 */
public class StreamChannelTest {

	private InputStream inStream;
	private PrintStream outStream;
	
	private StreamChannel sc;
	
	@Before
	public void before() {

		outStream = mock(PrintStream.class);

	}
	
	@After
	public void after() {
		try {
			
			if (outStream != null) {
				outStream.close();
				outStream = null;
			}
			
			if (inStream != null) { 
				inStream.close();
				inStream = null;
			}
			
			if (sc != null) { 
				sc.close();
				sc = null;
			}
			
		} catch (IOException ioe) {
			throw new IllegalStateException(ioe);
		}
	}
	
	/**
	 * Test method for {@link cpusim.model.iochannel.StreamChannel#StreamChannel()}.
	 */
	@Test
	public void testStreamChannel() {
		sc = new StreamChannel();

		assertEquals(System.in, sc.getInputStream());
		assertEquals(System.out, sc.getOutputStream());
	}

	/**
	 * Test method for {@link cpusim.model.iochannel.StreamChannel#StreamChannel(java.io.InputStream, java.io.PrintStream)}.
	 */
	@Test
	public void testStreamChannelInputStreamPrintStream() {
		sc = new StreamChannel();

		assertEquals(System.in, sc.getInputStream());
		assertEquals(System.out, sc.getOutputStream());
	}

	

	/**
	 * Test method for {@link cpusim.model.iochannel.StreamChannel#readString(java.lang.String)}.
	 */
	@Test
	public void testReadString() throws IOException {
		try (PipedOutputStream pos = new PipedOutputStream();
			 PrintWriter pw = new PrintWriter(pos);
			 PipedInputStream in = new PipedInputStream(pos)) {

			outStream = mock(PrintStream.class);
			sc = new StreamChannel(in, outStream);
			
			final String INPUT = "0xDEADBEEF";
			
			pw.write(INPUT);
			pw.write(System.getProperty("line.separator"));
			pw.flush();
			
			assertTrue(in.available() > 0);
			assertEquals(INPUT, sc.readString("No: "));
		}
	}

	/**
	 * Test method for {@link cpusim.model.iochannel.StreamChannel#readLong(cpusim.model.util.units.ArchValue)}.
	 */
	@Test
	public void testReadLong() throws IOException {
		try (PipedOutputStream pos = new PipedOutputStream();
			 PipedInputStream in = new PipedInputStream(pos)) {

			outStream = mock(PrintStream.class);
			sc = new StreamChannel(in, outStream);
			
			final ArchValue bytes = ArchType.Byte.of(4);
			final long INPUT = 0xDEADBEEF & bytes.mask();
			pos.write(Longs.toByteArray(INPUT));
			pos.write(Chars.toByteArray('\n'));
			pos.flush();

			assertTrue(in.available() > 0);
			assertEquals(INPUT, sc.readLong((int)bytes.as(ArchType.Bit)));
		}
	}

	/**
	 * Test method for {@link cpusim.model.iochannel.StreamChannel#readAscii()}.
	 * @throws IOException 
	 */
	@Test
	public void testReadAscii() throws IOException {
		try (PipedOutputStream pos = new PipedOutputStream();
			 PipedInputStream in = new PipedInputStream(pos)) {

			outStream = mock(PrintStream.class);
			sc = new StreamChannel(in, outStream);
			
			final char INPUT = '$';

			pos.write(Chars.toByteArray(INPUT));
			pos.write('\n');
			pos.flush();
			
			assertTrue(in.available() > 0);
			
			assertEquals(INPUT, sc.readAscii());
		}
	}

	/**
	 * Test method for {@link cpusim.model.iochannel.StreamChannel#readUnicode()}.
	 */
	@Test
	public void testReadUnicode() throws IOException {
		try (PipedOutputStream pos = new PipedOutputStream();
			 PipedInputStream in = new PipedInputStream(pos)) {

			outStream = mock(PrintStream.class);
			sc = new StreamChannel(in, outStream);
			
			final ArchValue bytes = ArchType.Byte.of(2);
			final int INPUT = (int) (0xDEADBEEF & bytes.mask());

			int[] breakdown = new int[(int)Math.ceil(Integer.bitCount(INPUT) / (8.0 * Short.BYTES))];
			byte[] asBytes = Ints.toByteArray(INPUT);
			final int breakdownOffset = (breakdown.length * 2);
			for (int i = asBytes.length - breakdownOffset, j = 0; i < asBytes.length && j < breakdown.length; i += 2, ++j) {
				breakdown[j] = (int)Shorts.fromBytes(asBytes[i], asBytes[i+1]) & bytes.imask();
			}

			for (int b: breakdown) {
				pos.write(Ints.toByteArray(b));
			}
			pos.write('\n');
			pos.flush();
			
			assertTrue(in.available() > 0);
			assertEquals(INPUT, sc.readUnicode());
		}
	}
	
	/**
	 * Test method for {@link cpusim.model.iochannel.StreamChannel#writeString(java.lang.String)}.
	 */
	@Test
	public void testWriteString() {
		outStream = mock(PrintStream.class);
		sc = new StreamChannel(mock(InputStream.class), outStream);
		
		final String TEST_OUTPUT = "test string.";
		
		sc.writeString(TEST_OUTPUT);
		verify(outStream).print(TEST_OUTPUT);
	}

	/**
	 * Test method for {@link cpusim.model.iochannel.StreamChannel#writeLong(long)}.
	 */
	@Test
	public void testWriteLong() {
		outStream = mock(PrintStream.class);
		sc = new StreamChannel(mock(InputStream.class), outStream);
		
		final long TEST_OUTPUT = Long.MAX_VALUE;
		
		sc.writeLong(TEST_OUTPUT);
		verify(outStream).print(TEST_OUTPUT);
	}

	/**
	 * Test method for {@link IOChannel#writeAscii(char)}.
	 */
	@Test
	public void testWriteAscii() {
		outStream = mock(PrintStream.class);
		sc = new StreamChannel(mock(InputStream.class), outStream);
		
		final char TEST_OUTPUT = (char) (0xDEADBEEF & ArchType.Byte.of(1).mask());
		
		sc.writeAscii(TEST_OUTPUT);
		verify(outStream).print(TEST_OUTPUT);
	}

	/**
	 * Test method for {@link cpusim.model.iochannel.StreamChannel#writeUnicode(int)}.
	 */
	@Test
	public void testWriteUnicode() {
		outStream = mock(PrintStream.class);
		sc = new StreamChannel(mock(InputStream.class), outStream);
		
		final int TEST_OUTPUT = (int) (0xDEADBEEF & ArchType.Byte.of(2).mask());
		
		sc.writeUnicode(TEST_OUTPUT);
		verify(outStream).print((long)TEST_OUTPUT);
	}

	/**
	 * Test method for {@link cpusim.model.iochannel.StreamChannel#flush(boolean)}.
	 */
	@Test
	public void testFlush() {
		outStream = mock(PrintStream.class);
		sc = new StreamChannel(mock(InputStream.class), outStream);
		
		sc.flush(true);
		verify(outStream).flush();
	}

	/**
	 * Test method for {@link cpusim.model.iochannel.StreamChannel#reset()}.
	 * @throws IOException 
	 */
	@Test
	public void testReset() throws IOException {
		inStream = mock(InputStream.class);
		sc = new StreamChannel(inStream, outStream);
		
		sc.reset();
		verify(inStream).reset();
	}

}
