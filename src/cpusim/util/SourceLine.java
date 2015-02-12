/*
 * File: SourceLine.java
 * Author: djskrien
 * Class: CS 361
 * Project: 
 * Date: May 27, 2010
 */

package cpusim.util;

/**
 * stores a file name and a line number.  Used to display the currently
 * executing line in an assembly file when stepping through execution in
 * debug mode.
 */
public class SourceLine
{
    private final int line;
    private final String fileName;

    public SourceLine(int line, String fileName)
    {
        this.line = line;
        this.fileName = fileName;
    }

    public int getLine()
    {
        return line;
    }

    public String getFileName()
    {
        return fileName;
    }
}
