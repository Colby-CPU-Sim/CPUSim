///////////////////////////////////////////////////////////////////////////////
// File:    	MacroDef.java
// Type:    	java application file
// Author:		Raymond H. Mazza III and Dale Skrien
// Project: 	CPU Sim 3.0
// Date:    	June, 2000
//
// Description:
//   referenced by macro calls to push a specific version of the macro
//	 onto the scanner's token stack


///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

package cpusim.assembler;


///////////////////////////////////////////////////////////////////////////////
// the libraries we need to import

import java.util.List;

///////////////////////////////////////////////////////////////////////////////
// the MacroDef class

public class MacroDef
{
    public Token name;			//holds the VAR token that is the name of the macro
    public List<Token> parameters;	//holds the VAR parameter tokens
    public List<Token> body;			//holds all tokens of the macro's body -- everything
    //after the parameters and before the ENDM token

    //-------------------------------
    // constructor
    public MacroDef(Token t, List<Token> p, List<Token> b)
    {
        name = t;
        parameters = p;
        body = b;
    }

}  //end of class MacroDef
