///////////////////////////////////////////////////////////////////////////////
// File:    	AssemblyException.java
// Type:    	java application file
// Author:		Raymond H. Mazza III and Dale Skrien
// Project: 	CPU Sim 3.0
// Date:    	June, 2000
//
// Description:
// An extension of RuntimeException to handle any errors while assembling


///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

package cpusim.assembler;


///////////////////////////////////////////////////////////////////////////////
// the AssemblyException class

/** The parent of all exceptions thrown while assembling. Extends Exception, indicating
  * that all subclasses of this Exception must be caught or declared. Is not abstract,
  * to reduce code duplication, but it is recommended that a subclass be used when 
  * possible to provide the most specific information should the Exception be thrown.
**/
public class AssemblyException extends Exception{
    public Token token;
    
    /** Constructs a new AssemblyException.
      * @param message The notification to be reported when thrown
      * @param t The Token of code relevant when being reported
    **/
    public AssemblyException(String message, Token t)
    {
        super(message);
        this.token = t;
    }
    
    /** An Exception thrown when a Syntactical mistake is found when assembling code.
      * These include undefined operand errors, non-aSCII character errors, and similar
      * errors. The indicator "SyntaxError" is reported with any message given by this
      * Exception.
    **/
    public static class SyntaxError extends AssemblyException{
		/** Constructs a new SyntaxError.
         * @param message The notification to be reported when thrown
      	 * @param t The Token of code relevant when being reported
    	**/
		public SyntaxError(String message, Token t) {
			super("Syntax Error:\n"+message, t);
		} 	
    }
    
    /** An Exception thrown when two values of incompatible types are assigned to a
      * variable of one identity, such as using the same word for a EQU and a label.
      * The indicator "NameSpaceError" is reported with any message given by this
      * Exception.
    **/
    public static class NameSpaceError extends AssemblyException{
		/** Constructs a new NameSpaceError.
         * @param message The notification to be reported when thrown
      	 * @param t The Token of code relevant when being reported
    	**/
		public NameSpaceError(String message, Token t) {
			super("NameSpace Error:\n"+message, t);
		}
    }
    
    /** An Exception thrown when there is a misuse or unmanageably high call to memory
	  * during assembly, such as overflow when constructing a pseudo instruction. The
	  * indicator "MemoryError" is reported with any message given by the Exception.
	**/
    public static class MemoryError extends AssemblyException{
		/** Constructs a new MemoryError.
         * @param message The notification to be reported when thrown
      	 * @param t The Token of code relevant when being reported
    	**/
		public MemoryError(String message, Token t) {
			super("MemoryError:\n"+message, t);
		}
    	
    }
    
    /** An Exception thrown when the program cannot import a file. The indicator
      * "ImportError" is reported with any message given by the Exception.
    **/
    public static class ImportError extends AssemblyException{
		/** Constructs a new ImportError.
         * @param message The notification to be reported when thrown
      	 * @param t The Token of code relevant when being reported
    	**/
		public ImportError(String message, Token t) {
			super("ImportError:\n"+message, t);
		}
    	
    }
    
    /** An Exception thrown when an invalid object or quantity is passed into a variable
      * that, by declaration, accepts the object or quantity. For example, an array might
      * be indexed by integers, but the index -1 is undefined over the array. The 
      * indicator "ValueError" is reported with any message given by the Exception.
    **/
    public static class ValueError extends AssemblyException{
		/** Constructs a new ValueError.
         * @param message The notification to be reported when thrown
      	 * @param t The Token of code relevant when being reported
    	**/
		public ValueError(String message, Token t) {
			super("ValueError:\n"+message, t);
		}
    }
    
    /** An Exception thrown when a value of is assigned to a type that is incompatible
      * by declaration. The indicator "TypeError" is reported with any message given by
      * the Exception.
    **/
    public static class TypeError extends AssemblyException{
		/** Constructs a new TypeError.
         * @param message The notification to be reported when thrown
      	 * @param t The Token of code relevant when being reported
    	**/
		public TypeError(String message, Token t) {
			super("TypeError:\n"+message, t);
		}
    }
    
    /** An Exception thrown when an unkown operand is identified. The indicator 
      * "UndefinedOperatorError" is reported with any message given by the Exception.
    **/
    public static class UndefinedOperandError extends AssemblyException{
		/** Constructs a new UndefinedOperandError.
         * @param message The notification to be reported when thrown
      	 * @param t The Token of code relevant when being reported
    	**/
		public UndefinedOperandError(String message, Token t) {
			super("UndefinedOperandError:\n"+message, t);
		}
    }
    /** An Exception thrown when a defined operand is misused. The indicator 
      * "InvalidOperandError" is reported with any message given by the Exception.
    **/
    public static class InvalidOperandError extends AssemblyException{

		public InvalidOperandError(String message, Token t) {
			super("InvalidOperandError:\n"+message, t);
		}
    }
    
    
    public static class InvalidTokenException extends AssemblyException{
    	
    	public InvalidTokenException(String message, Token t) {
			super("InvalidTokenException:\n"+message, t);

    	}
    }
    
}
