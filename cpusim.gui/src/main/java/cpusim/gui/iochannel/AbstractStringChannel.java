package cpusim.gui.iochannel;

import cpusim.model.ExecutionException;
import cpusim.model.iochannel.IOChannel;
import cpusim.model.util.units.ArchType;

/**
 * Base class for both {@link ConsoleChannel} and {@link DialogChannel} that fills in the "blanks" from the
 * {@link IOChannel} requirements, simplifying their implementation. All implemented methods from {@link IOChannel} are
 * pushed through the {@link #readString(String)} and {@link #writeString(String)} respectively.
 *
 * @author Kevin Brightwell (Nava2)
 *
 * @since 2016-11-10
 */
abstract class AbstractStringChannel implements IOChannel {
    
    @Override
    public long readLong(final int numBits) {
        final String sLong = readString("Enter integer (" + numBits + " bits): ");
        
        try {
            final long v = Long.parseLong(sLong.trim());
            
            return v & ArchType.Bit.getMask(numBits);
        } catch (NumberFormatException nfe) {
            throw new ExecutionException("Invalid input for Long value: " + sLong, nfe);
        }
    }
    
    @Override
    public char readAscii() {
        final String schar = readString("Enter Character: ");
    
        if (schar.length() != 1) {
            throw new ExecutionException("Invalid character entered, wrong size: '" + schar + "'");
        }
        
        return schar.charAt(0);
    }
    
    @Override
    public int readUnicode() {
        final String schar = readString("Enter 16-bit Character: ");
        
        if (schar.length() > 2) {
            throw new ExecutionException("Invalid character entered, wrong size: '" + schar + "'");
        }
    
        return (int) (schar.codePointAt(0) & ArchType.Byte.getMask(2));
    }
    
    @Override
    public void writeLong(final long value) {
        writeString(Long.toString(value));
    }
    
    @Override
    public void writeAscii(final char value) {
        writeString(Character.toString(value));
    }
    
    @Override
    public void writeUnicode(final int unicodeChar) {
        writeString(String.valueOf(Character.toChars(unicodeChar)));
    }
    
    @Override
    public void flush(final boolean saveInputBuffers) {
        // no op
    }
    
    @Override
    public void reset() {
        // no op
    }
}
