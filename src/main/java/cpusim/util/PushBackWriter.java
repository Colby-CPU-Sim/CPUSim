/*
 * Jinghui Yu, Michael Goldenberg, and Ben Borchard created this file on 12/5/13
 */

package cpusim.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A character-stream writer that allows characters to be pushed back into the
 * stream.
 */
public class PushBackWriter {

    /** Pushback buffer */
    private char[] buf;

    /** Current position in buffer */
    private int pos;

    private File file;

    private FileWriter writer = null;

    /**
     * Creates a new pushback writer with a pushback buffer of the file size.
     *
     * @param   file  The writer from which characters will write to
     * @exception IOException when reading in a file
     */
    public PushBackWriter(File file){
        this.file = file;
        buf = new char[500];
        pos = 0;
    }

    /**
     * writes a string to the file
     * @param c the string that is written to the file
     */
    public void write(String c){
        char[] cbuf = c.toCharArray();
        // check if there is enough available spac
        while ((pos + cbuf.length) >= buf.length){
            char[] newBuf = new char[buf.length*2];
            System.arraycopy(buf,0,newBuf,0,buf.length);
            buf = newBuf;
        }
        for (int i = 0; i < cbuf.length; i++){
            buf[pos++] = (char) cbuf[i];
        }

    }

    /**
     * write one character to the file
     * @param c the character in integer representation
     */
    public void write(int c) {
        write("" + (char) c);
    }

    /**
     * unwrite a character and return it
     * @return the character that is unwritten
     */
    public int unwrite(){
        if (pos > 0){
            return buf[--pos];
        }
        else
            return -1;

    }

    /**
     * write the whole buffer the a file and flush the buffer
     * @throws IOException
     */
    public void writeBufToFile() throws IOException {
        writer = new FileWriter(file);
        writer.write(buf,0,pos);
    }

    /**
     * flush the writer
     * @throws IOException
     */
    public void flush() throws IOException {
        if (writer != null)
            writer.flush();
    }

    /**
     * close the writer
     * @throws IOException
     */
    public void close() throws IOException {
        if (writer != null){
            writer.close();
            pos = 0;
        }
    }


}
