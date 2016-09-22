/*
 * Jinghui Yu, Michael Goldenberg, and Ben Borchard created this file on 12/5/13
 */

package cpusim.util;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * A character-stream reader that allows characters to be pushed back into the
 * stream.
 */
public class PushBackReader extends FilterReader {

    /** Pushback buffer */
    private char[] buf;

    /** Current position in buffer */
    private int pos;

    /**
     * Creates a new pushback reader with a pushback buffer of the file size.
     *
     * @param   file   The reader from which characters will be read
     * @exception IllegalArgumentException if size is <= 0
     */
    public PushBackReader(Reader file) throws IOException {
        super(file);
        StringBuilder s = new StringBuilder();
        int c = super.read();
        while (c != -1){
            s.append((char) c);
            c = super.read();
        }
        buf = s.toString().toCharArray();

        pos = 0;
    }

    /**
     * Reads a single character.
     *
     * @return     The character read, or -1 if the end of the stream has been
     *             reached
     */
    public int read(){
        if ( pos < buf.length )
            return buf[pos++];
        else
            return -1;
    }

    /**
     * Unreads a single character.
     *
     * @return     The character unread, or -1 if the beginning of the stream has been
     *             reached
     */
    public int unread(){
        if ( pos > 0 )
            return buf[--pos];
        else
            return -1;
    }

}
