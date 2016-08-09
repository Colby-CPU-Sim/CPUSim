///////////////////////////////////////////////////////////////////////////////
// File:    	StreamObject.java
// Type:    	java application file
// Author:		Raymond H. Mazza III and Dale Skrien
// Project: 	CPU Sim 3.0
// Date:    	June, 2000
//
// Description:
//  	each file read from has its own StreamObject to hold all the necessary
//		information to stop scanning in the middle of the file when a .include
//		is reached, and continue again where it left off after it finishes the
//		.include file.


///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

package cpusim.assembler;


///////////////////////////////////////////////////////////////////////////////
// the libraries we need to import

import java.util.Stack;
import java.io.*;


///////////////////////////////////////////////////////////////////////////////
// the StreamObject class

public class StreamObject
{
    public Stack<Token> tokenStack;
    public String filename;
    public char currentChar;
    public int lineNumber;
    public int columnNumber;
    public int offset;
    public int prevLineNumber;
    public int prevColumnNumber;
    public int prevOffset;
    public BufferedReader reader;

    //-------------------------------
    // constructor
//    public StreamObject(String path, BufferedReader r, Stack<Token> stack,
//                        char ch, int l, int c, int o, int pl, int pc,
//                        int po)
//    {
//        this.currentChar = ch;
//        this.tokenStack = stack;
//        this.filename = path;
//        this.lineNumber = l;
//        this.columnNumber = c;
//        this.offset = o;
//        this.prevColumnNumber = pc;
//        this.prevLineNumber = pl;
//        this.prevOffset = po;
//        this.reader = r;
//    }

    //-------------------------------
    // constructor
    public StreamObject(String path, Token token) throws AssemblyException.ImportError
    {
        this.tokenStack = new Stack<>();
        this.filename = path;
        this.lineNumber = 0;
        this.columnNumber = 0;
        this.offset = 0;
        this.prevColumnNumber = 0;
        this.prevLineNumber = 0;
        this.prevOffset = 0;
        this.reader = getReader(path, token);
    }

    //-------------------------------
    //the path is always absolute
    private BufferedReader getReader(String path, Token token) throws AssemblyException.ImportError
    {
        //StringBuffer buffer = new StringBuffer();
        BufferedReader bufferedReader;
        File file;

        try {
            file = new File(path);
            bufferedReader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            throw new AssemblyException.ImportError("Error: File \"" + path + "\" not found",
                    token);
        }

        return bufferedReader;
    }

}  //end of class StreamObject
