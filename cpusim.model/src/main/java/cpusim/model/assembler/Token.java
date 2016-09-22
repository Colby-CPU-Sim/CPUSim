///////////////////////////////////////////////////////////////////////////////
// File:    	Token.java
// Type:    	java application file
// Author:		Raymond H. Mazza III and Dale Skrien
// Project: 	CPU Sim 3.0
// Date:    	June, 2000
//
// Description:
//   A Token object is just a bag of data, and so has public instance variables
//   and no methods other than constructors.  It contains the data about one
//   token read by the scanner.

//Edited by Terrence Tan on 10/28/13:
// added the Type enum to replace the public static final variables and refactored all
//calls to said variables to refer to the enums

/*
 * Michael Goldenberg, Ben Borchard, and Jinghui Yu made the following changes in 12/5/13
 * 
 * 1.) Added GLOBAL to the enum Type for the global pseudoinstruction
 * 
 */

///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

package cpusim.model.assembler;

///////////////////////////////////////////////////////////////////////////////
// the libraries we need to import


///////////////////////////////////////////////////////////////////////////////
// the Token class

public class Token
{
    //--------------------------------------
    // These Enums are all possible token types. They are associated with integer values 
    //to allow comparison with Scanner.java inputs
    public enum Type{
        ERROR,
        OPCODE,
        DATA,
        VAR, //a variable
        EOL, //EOL
        LABEL, //a label token
        CONSTANT, //a number/constant
        EQU, //the string "EQU" for equates
        COMMA, //a comma ','
        EOF, //an end of file (EOF) token
        COMMENT, //comment
        MACRO, //for reserved word "macro"
        ENDM, //for reserved word "endm"
        LBRACKET, //for '['
        RBRACKET, //for ']'
        INCLUDE, //for pseudo-instruction ".include"
        ASCII,  //reserved word ".ascii"
        GLOBAL, //reserved word ".global"
        QUOTEDSTRING, //quoted strings for .include & .ascii
        LPARENTHESIS, // "(" used for separators
        RPARENTHESIS, //")" used for separators
        PUNCTUATION, //"*" used for separators
        ALIGN,  //reserved word ".align"
        POS,  //reserved word ".pos"
        LONG, //reserved word ".long"
        SHORT //reserved word ".short"
    }

    public final String filename;	//the file the token was found in
    public final String contents;	//the contents of the token
    public final Type type;			//the kind of token
    public final int lineNumber;	//the source line in which the token occurred
    public final int columnNumber;	//the source column in which the token starts
    public final int offset;		//the offset from the start of the file
    public final boolean isLegal; 	//true if the token is a legal token


    //--------------------------------------
    //			CONSTRUCTORS
//--------------------------------------
// method: constructor
// parameters: a token type, a line number and column number the token ended
//			   on, and a boolean for whether the token is legal or not
// return value: none
// description: constructs a new token and sets its attributes
    public Token(String filename, Type type, int line, int column,
                 int offset, String contents, boolean isLegal)
    {
        this.filename = filename;
        this.type = type;
        this.contents = contents;
        this.lineNumber = line;
        this.columnNumber = column;
        this.offset = offset;
        this.isLegal = isLegal;
    }


    //--------------------------------------
    //			PUBLIC METHODS
    //--------------------------------------

    //-------------------------------
    public String toString()
    {
        return this.contents;
    }

    //-------------------------------
    //used only for debugging
//    public String getTokenTypeString()
//    {
//        // if (type == tokenType.ERROR)
//        if (type == Type.ERROR)
//            return "ERROR";
//        else if (type == Type.OPCODE)
//            return "OPCODE";
//        else if (type == Type.DATA)
//            return "DATA";
//        else if (type == Type.VAR)
//            return "VAR";
//        else if (type == Type.EOL)
//            return "EOL";
//        else if (type == Type.LABEL)
//            return "LABEL";
//        else if (type == Type.CONSTANT)
//            return "CONSTANT";
//        else if (type == Type.EQU)
//            return "EQU";
//        else if (type == Type.COMMA)
//            return "COMMA";
//        else if (type == Type.EOF)
//            return "EOF";
//        else if (type == Type.COMMENT)
//            return "COMMENT";
//        else if (type == Type.MACRO)
//            return "MACRO";
//        else if (type == Type.ENDM)
//            return "ENDM";
//        else if (type == Type.LBRACKET)
//            return "LBRACKET";
//        else if (type == Type.RBRACKET)
//            return "RBRACKET";
//        else if (type == Type.QUOTEDSTRING)
//            return "QUOTEDSTRING";
//        else if (type == Type.ASCII)
//            return "ASCII";
//        else if (type == Type.INCLUDE)
//            return "INCLUDE";
//        else
//            return "TOKEN TYPE UNKNOWN: " + type;
//    }

    public String contentsToStringWithEscapes() throws AssemblyException.InvalidOperandError {
	    final StringBuilder result = new StringBuilder((int)(contents.length() * 1.25));
	    
	    boolean escaping = false;
	    for (int c : contents.codePoints().toArray()){
	        if (c == '\\' && !escaping){
	            escaping = true;
	        } else {
	        	// Not the escape character
	        	if (escaping) {
	        		
	        		// check for valid characters
	        		switch (c) {
	        		
	        		case '\\':
	        			result.append('\\');
	        			break;
	        		case 'n':
	        			result.append('\n');
	        			break;
	        		case 't':
	        			result.append('\t');
	        			break;
	        		case 'r':
	        			result.append('\r');
	        			break;
	        		case '"':
	        			result.append('\"');
	        			break;
	        			
	        		default:
	        			throw new AssemblyException.InvalidOperandError("The ascii string "+contents+" has the "
			                    + "escape character (\"\\\") before an the invalid character \'"+c+
			                    "\'.  Valid characters to escape from are \'\\\', \'n\', \'r\', \'t\', and \'\"\'"
			                    + "for backslash, new line, carraige return, tab, and quotation respectively.", this);
	        		}
	        		
	        		// set it here, it's either thrown or was valid.
	        		escaping = false;
	        	} else {
		            result.append(Character.toChars(c));
		        }
	        }
	    }
	    
	    return result.toString();
	}

    //-------------------------------
    public boolean isLegal()
    {
        return this.isLegal;
    }


    //-------------------------------
    //necessary to have this in order to override the Hashtable's built in
    //key comparison.  use in conjunction with the new equals() method, below.
    public int hashCode()
    {
        return this.contents.hashCode();
    }


    //-------------------------------
    //this is the comparison the Hashtables will now use (with the above
    //hashCode() method) when comparing keys into a Hashtable that are Tokens.
    //We want the only criterion to be that the contents are the same.
    public boolean equals(Object o)
    {
        if (!(o instanceof Token)) {
            return false;
        }
        else {
            Token t = (Token) o;
            return t.contents.equals(this.contents);
        }
    }

}//end class Token
