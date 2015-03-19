///////////////////////////////////////////////////////////////////////////////
// File:    	Scanner.java
// Type:    	java application file
// Author:		Raymond H. Mazza III and Dale Skrien
// Project: 	CPU Sim 3.0
// Date:    	June, 2000
//
// Description:
//   This file contains the code for the Main class that gets things started.

///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

package cpusim.assembler;

///////////////////////////////////////////////////////////////////////////////
// the libraries we need to import

/*
 * Michael Goldenberg, Ben Borchard, and Jinghui Yu made the following changes in 12/4/13
 * 
 * 1.) Added a method thisFileIsCurrentlyBeingScanned() so that the parser can test
 * if it is scanning a certain file
 * 2.) Added the ability to give a token a GLOBAL type if it is a global pseudoinstruction
 * 3.) Added a bunch of states to the nextState matrix so that the global token can be
 * identified
 * 4.) Added a char type C_b because that character is needed to spell 'global'
 * 
 */

import cpusim.Machine;
import cpusim.MachineInstruction;
import cpusim.util.Convert;

import java.io.File;
import java.io.IOException;
import java.util.*;

///////////////////////////////////////////////////////////////////////////////
// the Scanner class

public class Scanner
{
    //States
    private static final int ILLEGAL = 0;        //illegal chars & after final state
    private static final int START = 1;            //starting a new token
    private static final int PSEUDO = 2;            //starts a pseudo-instruction
//    private static final int LBRACKET = 3;        //for left bracket '['
//    private static final int RBRACKET = 4;        //for right bracket ']'
//    private static final int SIGN = 5;            //for + or - signs
    private static final int NUMBER = 6;        //for numbers
//    private static final int COMMA = 7;            //for a comma
    private static final int NEWLINE = 8;    //for a newline token
    private static final int COMMENT = 9;        //part of a comment
    private static final int INSTR_VAR = 10;    //instruct'n, variable, "data" "EQU"
    private static final int LABEL = 11;        //for a label token
    private static final int OPENDQUOTE = 12;    //for double-quoted strings
    private static final int CLOSEDQUOTE = 13;
    private static final int DOTD = 14;            //states 14-17 are for .data
    private static final int DOTDA = 15;
    private static final int DOTDAT = 16;
    private static final int DOTDATA = 17;
    private static final int DOTI = 18;            //states 18-24 are for .include
    private static final int DOTIN = 19;
    private static final int DOTINC = 20;
    private static final int DOTINCL = 21;
    private static final int DOTINCLU = 22;
    private static final int DOTINCLUD = 23;
    private static final int DOTINCLUDE = 24;
    private static final int DOTA = 25;            //states 18-24 are for .ascii
    private static final int DOTAS = 26;
    private static final int DOTASC = 27;
    private static final int DOTASCI = 28;
    private static final int DOTASCII = 29;
    private static final int DOTG = 58;            //states 58-63 are for .global
    private static final int DOTGL = 59;
    private static final int DOTGLO = 60;
    private static final int DOTGLOB = 61;
    private static final int DOTGLOBA = 62;
    private static final int DOTGLOBAL = 63;
    private static final int OPENSQUOTE = 30;   //for single-quoted characters
    private static final int CHARSQUOTE = 31;
    private static final int CLOSESQUOTE = 32;
    private static final int OPENCQUOTE = 33;  //for chevron-quoted string
    private static final int CLOSECQUOTE = 34;
    private static final int PUNCTUATION = 35; //for all other punctuation
//    private static final int LPARENTHISIS = 37; // for (
//    private static final int RPARENTHISIS = 38; // for )
    //private static final int DOTA = 39;            //states 40-43 are for .align
    private static final int DOTAL = 40;
    private static final int DOTALI = 41;
    private static final int DOTALIG = 42;
    private static final int DOTALIGN = 43;
    private static final int DOTP = 44;             //states 44-46 are for .pos
    private static final int DOTPO = 45;
    private static final int DOTPOS = 46;
    private static final int DOTL = 47;   //states 47-50 are for .long
    private static final int DOTLO = 48;
    private static final int DOTLON = 49;
    private static final int DOTLONG = 50;
    private static final int DOTS = 51;     //states 51-55 are for .short
    private static final int DOTSH = 52;
    private static final int DOTSHO = 53;
    private static final int DOTSHOR = 54;
    private static final int DOTSHORT = 55;
    private static final int PLUS = 56;
    private static final int MINUS = 57;

    //end of file value
    private static final int EOF = -1;

    //character types
    private static final int C_DOUBLEQUOTE = 0;    //  '"'
    private static final int C_NUMBER = 1;        //1-9, 0 has its own type
    private static final int C_WHITESPACE = 2;    //whitespace, tab
    private static final int C_NEWLINE = 3;        // \n
    private static final int C_SEMICOLON = 4;    // ';' used before comments
//    private static final int C_COLON = 5;        // ':' used for labels
//    private static final int C_DOT = 6;            // '.'
//    private static final int C_COMMA = 7;        // ','
    private static final int C_PLUSSIGN = 8;        //'+' for numbers only
//    private static final int C_LBRACKET = 9;    // '[' for use with 'data'
//    private static final int C_RBRACKET = 10;    // ']'
    private static final int C_LETTER = 11;     //a-z,A-Z excluding the following
    private static final int C_a = 12;
    private static final int C_b = 56;
    private static final int C_c = 13;
    private static final int C_d = 14;
    private static final int C_e = 15;
    private static final int C_i = 16;
    private static final int C_l = 17;
    private static final int C_n = 18;
    private static final int C_s = 19;
    private static final int C_t = 20;
    private static final int C_u = 21;
//    private static final int C_UNDERSCORE = 22;
    private static final int C_SINGLEQUOTE = 23; // '
    private static final int C_LCHEVRON = 24; // <
    private static final int C_RCHEVRON = 25; // >
    private static final int C_OTHER = 26;    //anything else, will give an error
//    private static final int C_PERCENT = 27; // % //*
//    private static final int C_DOLLAR = 28; // $
//    private static final int C_LPARENTHISIS = 29; // (
//    private static final int C_RPARENTHISIS = 30; // )
//    private static final int C_QUESTIONMARK = 31; // ?
//    private static final int C_EXCLAMATION = 32; // !
//    private static final int C_CAROT = 33; // ^
//    private static final int C_FOWARDSLASH = 34; // "/"
//    private static final int C_BACKSLASH = 35; // "\"
//    private static final int C_EQUALS = 36; // "="
//    private static final int C_TILDE = 37; //"~"
//    private static final int C_AMPERSAND = 38; // &
//    private static final int C_ASTERISK = 39; // *
//    private static final int C_ATSYMBOL = 40; // @
//    private static final int C_VERTTICALBAR = 41; // "|"
//    private static final int C_LSBRACKET = 42; // {
//    private static final int C_RSBRACKET = 43; // }
    private static final int C_g = 44; //for "g" in .align
    private static final int C_p = 45; //for "p" in .pos
    private static final int C_o = 46; //for "o" in .pos
    private static final int C_h = 47; //for "h" in .short
    private static final int C_r = 48; //for "r" in .short
    private static final int C_COMMENTCHAR = 49;
    private static final int C_PSEUDOCHAR = 50;
    private static final int C_LABELCHAR = 51;
//    private static final int C_POUND = 52;
    private static final int C_MINUSSIGN = 53;
//    private static final int C_BACKQUOTE = 54;
    private static final int C_TOKEN = 55; //one-character tokens


    //Fields
    private Stack<StreamObject> streamStack; //a stack of StreamObjects to scan from
    private HashSet<String> filenames;    //all .include files (to avoid recursion)
    private StreamObject top;        //points to the top Stream Object on the stack
    private Machine machine;        //the machine (to get the instruction names)
    private int[][] nextState;        //state transition matrix
    private int[] charType;            //char type array

    private char pseudoChar;
    private boolean plusIsSymbol, minusIsSymbol;
    private boolean plusIsToken, minusIsToken;


    //-------------------------------
    // constructor
    //initializes the character and the next state array for this scanner
    public Scanner(Machine machine)
    {
        this.filenames = new HashSet<>();
        this.streamStack = new Stack<>();
        this.machine = machine;
        initializeCharTypeArray();
        initializeNextStateArray();
    }
    
    /**
     * checks if input file is being scanned so the parser can check what file is being scanned
     * @return true if input file is file being scanned, else false
     */
    public boolean thisFileIsCurrentlyBeingScanned(String filename){
        return top.filename.equals(filename);
        
    }

    //-------------------------------
    // startScanning
    //initializes everything else and grabs the first character.
    //this is used to start scanning any new stream (a file).
    //The path must be the full (absolute) pathname of the file.
    public void startScanning(String fullPathName) throws AssemblyException.ImportError, AssemblyException.SyntaxError
    {
        Token pathToken = new Token("", null, -1, -1, -1,
                "\"" + fullPathName + "\"", true);
        filenames.clear(); //empty the filenames hashtable
        streamStack.clear(); //empty the stack of StreamObjects
        //this should only be necessary if an error
        //occurred during assembly
        startScanning(pathToken);
    }

    //-------------------------------
    // startScanning
    //initializes everything else and grabs the first character.
    //this is used to start scanning any new stream (a file).
    //The token is the token from the other file containing the file name
    //for the new file.  If this is the first time, then token.filename is ""
    //and token.contents is the quoted full (absolute) pathname.  Otherwise,
    //token.contents is the quoted pathname of the new file relative to the
    //token.filename path.
    public void startScanning(Token token) throws AssemblyException.ImportError, AssemblyException.SyntaxError
    {
        String fullPath;

        //get the full (absolute) path
        if (token.filename.equals("")) {
            //the first time this method is called, the token's filename is ""
            fullPath = token.contents.substring(1, token.contents.length() - 1);
        }
        else {
            File f = (token.contents.charAt(0) == '<' ?
                    null : new File(token.filename));
            String basePath = (f != null ? f.getParent() : null);

            //remove the quotes from around the relative pathname
            String relativePath =
                    token.contents.substring(1, token.contents.length() - 1);
            if (basePath == null)
                fullPath = relativePath;
            else
                fullPath = basePath + File.separator + relativePath;
        }

        //if the file led to the call of itself, it will probably cause an
        //infinite loop
//        System.out.println("Filename is "+fullPath);
//        System.out.println("Filenames is "+filenames);

        if (filenames.contains(fullPath)) {
            throw new AssemblyException.ImportError("The .include file \"" + fullPath +
                    "\" is being included recursively\n       and will cause " +
                    "an infinite loop",
                    token);
        }
        else {
            filenames.add(fullPath);
        }

        streamStack.push(new StreamObject(fullPath, token));
        this.top = streamStack.peek();

        //get the next char after all initialization
        top.currentChar = getNextChar();
    }


    //-------------------------------
    //getMachine:  returns the machine currently being used.
    public Machine getMachine()
    {
        return this.machine;
    }

    //-------------------------------
    //setMachine:  changes the machine currently being used
    //(if the user wants to start over with a new machine).
//    public void setMachine(Machine newMachine)
//    {
//        this.machine = newMachine;
//    }

    //------------------------------------
    //	PRIVATE INITIALIZATION METHODS
    //------------------------------------


    //-------------------------------
    //initialize char type array
    //create an array of size 128 to accommodate the 128 possible
    //ASCII characters, then set the ones we need to deal with to their
    //corresponding constants.
    private void initializeCharTypeArray()
    {
        charType = new int[128];    //the 128 ASCII characters

        //initialize to C_OTHER
        for (int i = 0; i < charType.length; i++) {
            charType[i] = C_OTHER;    //these will all be illegal chars unless
            //in a comment
        }

        for (int i = 'a'; i <= 'z'; i++) {
            charType[i] = C_LETTER;    //letter
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            charType[i] = C_LETTER;    //letter
        }
        for (int i = '0'; i <= '9'; i++) {
            charType[i] = C_NUMBER;    //digit
        }

        //the white space characters
        charType['\t'] = C_WHITESPACE;
        charType[' '] = C_WHITESPACE;
        charType['\n'] = C_NEWLINE;
//        charType['\r'] = C_NEWLINE;
//        we would group \r carriage return with \n newline but
//        we ignore all \r chars so no need to worry about them here.

        //quoting characters
        charType['\''] = C_SINGLEQUOTE;
        charType['"'] = C_DOUBLEQUOTE;
        charType['<'] = C_LCHEVRON;
        charType['>'] = C_RCHEVRON;

        //the punctuation characters
//        charType[';'] = C_SEMICOLON;
//        charType[':'] = C_COLON;
//        charType[','] = C_COMMA;
//        charType['['] = C_LBRACKET;
//        charType[']'] = C_RBRACKET;
//        charType['.'] = C_DOT;
//        charType['_'] = C_UNDERSCORE;
//        charType['\''] = C_SINGLEQUOTE;
//        charType['%'] = C_PERCENT;
//        charType['$'] = C_DOLLAR;
//        charType['('] = C_LPARENTHISIS;
//        charType[')'] = C_RPARENTHISIS;
//        charType['?'] = C_QUESTIONMARK;
//        charType['!'] = C_EXCLAMATION;
//        charType['^'] = C_CAROT;
//        charType['/'] = C_FOWARDSLASH;
//        charType['\\'] = C_BACKSLASH;
//        charType['='] = C_EQUALS;
//        charType['~'] = C_TILDE;
//        charType['&'] = C_AMPERSAND;
//        charType['*'] = C_ASTERISK;
//        charType['@'] = C_ATSYMBOL;
//        charType['|'] = C_VERTTICALBAR;
//        charType['{'] = C_LSBRACKET;
//        charType['}'] = C_RSBRACKET;
//        charType['#'] = C_POUND;
//        charType['`'] = C_BACKQUOTE;
        plusIsSymbol = false;
        minusIsSymbol = false;
        plusIsToken = false;
        minusIsToken = false;
        for(PunctChar c : machine.getPunctChars()) {
            if (c.getUse() == PunctChar.Use.symbol && c.getChar() == '+')
                plusIsSymbol = true;
            else if (c.getUse() == PunctChar.Use.symbol && c.getChar() == '-')
                minusIsSymbol = true;
            else if (c.getUse() == PunctChar.Use.token && c.getChar() == '+')
                plusIsToken = true;
            else if (c.getUse() == PunctChar.Use.token && c.getChar() == '-')
                minusIsToken = true;
            else if( c.getUse() == PunctChar.Use.symbol)
                charType[c.getChar()] = C_LETTER; //behaves like a letter
            else if (c.getUse() == PunctChar.Use.label)
                charType[c.getChar()] = C_LABELCHAR;
            else if (c.getUse() == PunctChar.Use.pseudo) {
                charType[c.getChar()] = C_PSEUDOCHAR;
                pseudoChar = c.getChar();
            }
            else if (c.getUse() == PunctChar.Use.comment)
                charType[c.getChar()] = C_COMMENTCHAR;
            else if (c.getUse() == PunctChar.Use.illegal)
                charType[c.getChar()] = C_OTHER;
            else //if (c.getUse() == PunctChar.Use.token)
                charType[c.getChar()] = C_TOKEN;
        }
        //plus and minus signs are special since they can start a number
        charType['-'] = C_MINUSSIGN;
        charType['+'] = C_PLUSSIGN;

        //define specific lowercase letters
        charType['a'] = C_a;
        charType['b'] = C_b;
        charType['c'] = C_c;
        charType['d'] = C_d;
        charType['e'] = C_e;
        charType['i'] = C_i;
        charType['l'] = C_l;
        charType['n'] = C_n;
        charType['s'] = C_s;
        charType['t'] = C_t;
        charType['u'] = C_u;
        charType['g'] = C_g;
        charType['p'] = C_p;
        charType['o'] = C_o;
        charType['h'] = C_h;
        charType['r'] = C_r;
    }


    //-------------------------------
    //the finite autonoma is implemented in a matrix below called the
    //'nextStateArray' for each state, the current state and the current
    //character type are indices into the next state array to the next state.
    private void initializeNextStateArray()
    {
        //the next state is nextState[state][char_type]
        nextState = new int[64][57];    //58 rows (for states) and
                                        //56 columns (for char types)

        for (int x = 0; x < nextState.length; x++) {
            for (int y = 0; y < nextState[0].length; y++) {
                //init to illegal since most states will be illegal
                nextState[x][y] = ILLEGAL;
            }
        }

        nextState[START][C_NUMBER] = NUMBER;
        nextState[START][C_WHITESPACE] = START;
        nextState[START][C_NEWLINE] = NEWLINE;
        nextState[START][C_COMMENTCHAR] = COMMENT;
        nextState[START][C_PSEUDOCHAR] = PSEUDO;
        nextState[START][C_LETTER] = INSTR_VAR;
        nextState[START][C_a] = INSTR_VAR;
        nextState[START][C_b] = INSTR_VAR;
        nextState[START][C_c] = INSTR_VAR;
        nextState[START][C_d] = INSTR_VAR;
        nextState[START][C_e] = INSTR_VAR;
        nextState[START][C_i] = INSTR_VAR;
        nextState[START][C_l] = INSTR_VAR;
        nextState[START][C_n] = INSTR_VAR;
        nextState[START][C_s] = INSTR_VAR;
        nextState[START][C_t] = INSTR_VAR;
        nextState[START][C_u] = INSTR_VAR;
        nextState[START][C_g] = INSTR_VAR;
        nextState[START][C_p] = INSTR_VAR;
        nextState[START][C_o] = INSTR_VAR;
        nextState[START][C_h] = INSTR_VAR;
        nextState[START][C_r] = INSTR_VAR;
        nextState[START][C_DOUBLEQUOTE] = OPENDQUOTE;
        nextState[START][C_SINGLEQUOTE] = OPENSQUOTE;
        nextState[START][C_LCHEVRON] = OPENCQUOTE;
        nextState[START][C_TOKEN] = PUNCTUATION;
        nextState[START][C_MINUSSIGN] = MINUS;
        nextState[START][C_PLUSSIGN] = PLUS;
//        nextState[START][C_COMMA] = COMMA;
//        nextState[START][C_LBRACKET] = LBRACKET;
//        nextState[START][C_RBRACKET] = RBRACKET;
//        nextState[START][C_LPARENTHISIS] = LPARENTHISIS;
//        nextState[START][C_RPARENTHISIS] = RPARENTHISIS;
//        nextState[START][C_LBRACKET] = LBRACKET; //*
//        nextState[START][C_RBRACKET] = RBRACKET; //*
//        nextState[START][C_ASTERISK] = PUNCTUATION; //*
//        nextState[START][C_PERCENT] = PUNCTUATION;//*
//        nextState[START][C_DOLLAR] = PUNCTUATION;//*
//        nextState[START][C_QUESTIONMARK] = PUNCTUATION;//*
//        nextState[START][C_EXCLAMATION] = PUNCTUATION;//*
//        nextState[START][C_CAROT] = PUNCTUATION;//*
//        nextState[START][C_FOWARDSLASH] = PUNCTUATION;//*
//        nextState[START][C_BACKSLASH] = PUNCTUATION;//*
//        nextState[START][C_EQUALS] = PUNCTUATION;//*
//        nextState[START][C_TILDE] = PUNCTUATION;//*
//        nextState[START][C_AMPERSAND] = PUNCTUATION;//*
//        nextState[START][C_ATSYMBOL] = PUNCTUATION;//*
//        nextState[START][C_VERTTICALBAR] = PUNCTUATION;//*
//        nextState[START][C_LSBRACKET] = PUNCTUATION;
//        nextState[START][C_RSBRACKET] = PUNCTUATION;
//        nextState[START][C_POUND] = PUNCTUATION;

        nextState[PLUS][C_NUMBER] = NUMBER;
        nextState[MINUS][C_NUMBER] = NUMBER;
        nextState[NUMBER][C_NUMBER] = NUMBER;
        nextState[NUMBER][C_LETTER] = NUMBER; //to allow hex characters b,f,A-F
        nextState[NUMBER][C_a] = NUMBER; //to allow hex character a
        nextState[NUMBER][C_b] = NUMBER; //to allow hex character b
        nextState[NUMBER][C_c] = NUMBER; //to allow hex character c
        nextState[NUMBER][C_d] = NUMBER; //to allow hex characters d
        nextState[NUMBER][C_e] = NUMBER; //to allow hex characters e

        if (plusIsSymbol) {
            nextState[PLUS][C_LABELCHAR] = LABEL;
            nextState[PLUS][C_LETTER] = INSTR_VAR;
            nextState[PLUS][C_a] = INSTR_VAR;
            nextState[PLUS][C_b] = INSTR_VAR;
            nextState[PLUS][C_c] = INSTR_VAR;
            nextState[PLUS][C_d] = INSTR_VAR;
            nextState[PLUS][C_e] = INSTR_VAR;
            nextState[PLUS][C_i] = INSTR_VAR;
            nextState[PLUS][C_l] = INSTR_VAR;
            nextState[PLUS][C_n] = INSTR_VAR;
            nextState[PLUS][C_s] = INSTR_VAR;
            nextState[PLUS][C_t] = INSTR_VAR;
            nextState[PLUS][C_u] = INSTR_VAR;
            nextState[PLUS][C_g] = INSTR_VAR;
            nextState[PLUS][C_p] = INSTR_VAR;
            nextState[PLUS][C_o] = INSTR_VAR;
            nextState[PLUS][C_h] = INSTR_VAR;
            nextState[PLUS][C_r] = INSTR_VAR;
        }

        if (minusIsSymbol) {
            nextState[MINUS][C_LABELCHAR] = LABEL;
            nextState[MINUS][C_LETTER] = INSTR_VAR;
            nextState[MINUS][C_a] = INSTR_VAR;
            nextState[MINUS][C_b] = INSTR_VAR;
            nextState[MINUS][C_c] = INSTR_VAR;
            nextState[MINUS][C_d] = INSTR_VAR;
            nextState[MINUS][C_e] = INSTR_VAR;
            nextState[MINUS][C_i] = INSTR_VAR;
            nextState[MINUS][C_l] = INSTR_VAR;
            nextState[MINUS][C_n] = INSTR_VAR;
            nextState[MINUS][C_s] = INSTR_VAR;
            nextState[MINUS][C_t] = INSTR_VAR;
            nextState[MINUS][C_u] = INSTR_VAR;
            nextState[MINUS][C_g] = INSTR_VAR;
            nextState[MINUS][C_p] = INSTR_VAR;
            nextState[MINUS][C_o] = INSTR_VAR;
            nextState[MINUS][C_h] = INSTR_VAR;
            nextState[MINUS][C_r] = INSTR_VAR;
        }

        nextState[INSTR_VAR][C_NUMBER] = INSTR_VAR;
        nextState[INSTR_VAR][C_LABELCHAR] = LABEL;
        nextState[INSTR_VAR][C_LETTER] = INSTR_VAR;
        nextState[INSTR_VAR][C_a] = INSTR_VAR;
        nextState[INSTR_VAR][C_b] = INSTR_VAR;
        nextState[INSTR_VAR][C_c] = INSTR_VAR;
        nextState[INSTR_VAR][C_d] = INSTR_VAR;
        nextState[INSTR_VAR][C_e] = INSTR_VAR;
        nextState[INSTR_VAR][C_i] = INSTR_VAR;
        nextState[INSTR_VAR][C_l] = INSTR_VAR;
        nextState[INSTR_VAR][C_n] = INSTR_VAR;
        nextState[INSTR_VAR][C_s] = INSTR_VAR;
        nextState[INSTR_VAR][C_t] = INSTR_VAR;
        nextState[INSTR_VAR][C_u] = INSTR_VAR;
        nextState[INSTR_VAR][C_g] = INSTR_VAR;
        nextState[INSTR_VAR][C_p] = INSTR_VAR;
        nextState[INSTR_VAR][C_o] = INSTR_VAR;
        nextState[INSTR_VAR][C_h] = INSTR_VAR;
        nextState[INSTR_VAR][C_r] = INSTR_VAR;
        if (plusIsSymbol)
            nextState[INSTR_VAR][C_PLUSSIGN] = INSTR_VAR;
        if (minusIsSymbol)
            nextState[INSTR_VAR][C_MINUSSIGN] = INSTR_VAR;

//        nextState[INSTR_VAR][C_UNDERSCORE] = INSTR_VAR;
//        nextState[INSTR_VAR][C_PLUSSIGN] = INSTR_VAR;
//        nextState[INSTR_VAR][C_PERCENT] = INSTR_VAR;//*
//        nextState[INSTR_VAR][C_DOLLAR] = INSTR_VAR;//*
//        nextState[INSTR_VAR][C_QUESTIONMARK] = INSTR_VAR;//*
//        nextState[INSTR_VAR][C_EXCLAMATION] = INSTR_VAR;//*
//        nextState[INSTR_VAR][C_CAROT] = INSTR_VAR;//*
//        nextState[INSTR_VAR][C_FOWARDSLASH] = INSTR_VAR;//*
//        nextState[INSTR_VAR][C_BACKSLASH] = INSTR_VAR;//*
//        nextState[INSTR_VAR][C_EQUALS] = INSTR_VAR;//*
//        nextState[INSTR_VAR][C_TILDE] = INSTR_VAR;//*
//        nextState[INSTR_VAR][C_AMPERSAND] = INSTR_VAR;//*
//        nextState[INSTR_VAR][C_ATSYMBOL] = INSTR_VAR;//*
//        nextState[INSTR_VAR][C_VERTTICALBAR] = INSTR_VAR;//*
//        nextState[INSTR_VAR][C_LSBRACKET] = INSTR_VAR;
//        nextState[INSTR_VAR][C_RSBRACKET] = INSTR_VAR;
//        nextState[INSTR_VAR][C_POUND] = INSTR_VAR;

        nextState[COMMENT][C_DOUBLEQUOTE] = COMMENT;
        nextState[COMMENT][C_NUMBER] = COMMENT;
        nextState[COMMENT][C_WHITESPACE] = COMMENT;
        nextState[COMMENT][C_NEWLINE] = ILLEGAL;
        nextState[COMMENT][C_PSEUDOCHAR] = COMMENT;
        nextState[COMMENT][C_LABELCHAR] = COMMENT;
        nextState[COMMENT][C_COMMENTCHAR] = COMMENT;
        nextState[COMMENT][C_LETTER] = COMMENT;
        nextState[COMMENT][C_a] = COMMENT;
        nextState[COMMENT][C_b] = COMMENT;
        nextState[COMMENT][C_c] = COMMENT;
        nextState[COMMENT][C_d] = COMMENT;
        nextState[COMMENT][C_e] = COMMENT;
        nextState[COMMENT][C_i] = COMMENT;
        nextState[COMMENT][C_l] = COMMENT;
        nextState[COMMENT][C_n] = COMMENT;
        nextState[COMMENT][C_s] = COMMENT;
        nextState[COMMENT][C_t] = COMMENT;
        nextState[COMMENT][C_u] = COMMENT;
        nextState[COMMENT][C_g] = COMMENT;
        nextState[COMMENT][C_p] = COMMENT;
        nextState[COMMENT][C_o] = COMMENT;
        nextState[COMMENT][C_h] = COMMENT;
        nextState[COMMENT][C_r] = COMMENT;
        nextState[COMMENT][C_SINGLEQUOTE] = COMMENT;
        nextState[COMMENT][C_LCHEVRON] = COMMENT;
        nextState[COMMENT][C_RCHEVRON] = COMMENT;
        nextState[COMMENT][C_OTHER] = COMMENT;
        nextState[COMMENT][C_PLUSSIGN] = COMMENT;
        nextState[COMMENT][C_MINUSSIGN] = COMMENT;
        nextState[COMMENT][C_TOKEN] = COMMENT;
//        nextState[COMMENT][C_COMMA] = COMMENT;
//        nextState[COMMENT][C_LBRACKET] = COMMENT;
//        nextState[COMMENT][C_RBRACKET] = COMMENT;
//        nextState[COMMENT][C_SEMICOLON] = COMMENT;
//        nextState[COMMENT][C_COLON] = COMMENT;
//        nextState[COMMENT][C_DOT] = COMMENT;
//        nextState[COMMENT][C_UNDERSCORE] = COMMENT;
//        nextState[COMMENT][C_PERCENT] = COMMENT;//*
//        nextState[COMMENT][C_DOLLAR] = COMMENT;
//        nextState[COMMENT][C_LPARENTHISIS] = COMMENT;
//        nextState[COMMENT][C_RPARENTHISIS] = COMMENT;
//        nextState[COMMENT][C_QUESTIONMARK] = COMMENT;//*
//        nextState[COMMENT][C_EXCLAMATION] = COMMENT;//*
//        nextState[COMMENT][C_CAROT] = COMMENT;//*
//        nextState[COMMENT][C_FOWARDSLASH] = COMMENT;//*
//        nextState[COMMENT][C_BACKSLASH] = COMMENT;//*
//        nextState[COMMENT][C_EQUALS] = COMMENT;//*
//        nextState[COMMENT][C_TILDE] = COMMENT;//*
//        nextState[COMMENT][C_AMPERSAND] = COMMENT;//*
//        nextState[COMMENT][C_ATSYMBOL] = COMMENT;//*
//        nextState[COMMENT][C_VERTTICALBAR] = COMMENT;//*
//        nextState[COMMENT][C_LSBRACKET] = COMMENT;
//        nextState[COMMENT][C_RSBRACKET] = COMMENT;
//        nextState[COMMENT][C_ASTERISK] = COMMENT;
//        nextState[COMMENT][C_POUND] = COMMENT;

        nextState[OPENSQUOTE][C_DOUBLEQUOTE]    = CHARSQUOTE;
        nextState[OPENSQUOTE][C_NUMBER]         = CHARSQUOTE;
        nextState[OPENSQUOTE][C_WHITESPACE]     = CHARSQUOTE;
        nextState[OPENSQUOTE][C_NEWLINE]      = CHARSQUOTE;
        nextState[OPENSQUOTE][C_LETTER]         = CHARSQUOTE;
        nextState[OPENSQUOTE][C_a]              = CHARSQUOTE;
        nextState[OPENSQUOTE][C_b]              = CHARSQUOTE;
        nextState[OPENSQUOTE][C_c]              = CHARSQUOTE;
        nextState[OPENSQUOTE][C_d]              = CHARSQUOTE;
        nextState[OPENSQUOTE][C_e]              = CHARSQUOTE;
        nextState[OPENSQUOTE][C_i] = CHARSQUOTE;
        nextState[OPENSQUOTE][C_l] = CHARSQUOTE;
        nextState[OPENSQUOTE][C_n] = CHARSQUOTE;
        nextState[OPENSQUOTE][C_s] = CHARSQUOTE;
        nextState[OPENSQUOTE][C_t] = CHARSQUOTE;
        nextState[OPENSQUOTE][C_u] = CHARSQUOTE;
        nextState[OPENSQUOTE][C_g] = CHARSQUOTE;
        nextState[OPENSQUOTE][C_p] = CHARSQUOTE;
        nextState[OPENSQUOTE][C_o] = CHARSQUOTE;
        nextState[OPENSQUOTE][C_h] = CHARSQUOTE;
        nextState[OPENSQUOTE][C_r] = CHARSQUOTE;
        nextState[OPENSQUOTE][C_PSEUDOCHAR]     = CHARSQUOTE;
        nextState[OPENSQUOTE][C_LABELCHAR]      = CHARSQUOTE;
        nextState[OPENSQUOTE][C_COMMENTCHAR]    = CHARSQUOTE;
        nextState[OPENSQUOTE][C_TOKEN]          = CHARSQUOTE;
        nextState[OPENSQUOTE][C_OTHER]          = CHARSQUOTE;
        nextState[OPENSQUOTE][C_LCHEVRON]       = CHARSQUOTE;
        nextState[OPENSQUOTE][C_LCHEVRON]       = CHARSQUOTE;
        nextState[OPENSQUOTE][C_PLUSSIGN]       = CHARSQUOTE;
        nextState[OPENSQUOTE][C_MINUSSIGN]      = CHARSQUOTE;

        nextState[CHARSQUOTE][C_SINGLEQUOTE] = CLOSESQUOTE;
        
//        nextState[OPENSQUOTE][C_COMMA] = CHARSQUOTE;
//        nextState[OPENSQUOTE][C_SEMICOLON] = CHARSQUOTE;
//        nextState[OPENSQUOTE][C_COLON] = CHARSQUOTE;
//        nextState[OPENSQUOTE][C_DOT] = CHARSQUOTE;
//        nextState[OPENSQUOTE][C_LBRACKET] = CHARSQUOTE;
//        nextState[OPENSQUOTE][C_RBRACKET] = CHARSQUOTE;
//        nextState[OPENSQUOTE][C_UNDERSCORE] = CHARSQUOTE;
//        nextState[OPENSQUOTE][C_SINGLEQUOTE] = CHARSQUOTE;
//        nextState[OPENSQUOTE][C_QUESTIONMARK] = CHARSQUOTE;//*
//        nextState[OPENSQUOTE][C_EXCLAMATION] = CHARSQUOTE;//*
//        nextState[OPENSQUOTE][C_CAROT] = CHARSQUOTE;//*
//        nextState[OPENSQUOTE][C_FOWARDSLASH] = CHARSQUOTE;//*
//        nextState[OPENSQUOTE][C_BACKSLASH] = CHARSQUOTE;//*
//        nextState[OPENSQUOTE][C_EQUALS] = CHARSQUOTE;//*
//        nextState[OPENSQUOTE][C_TILDE] = CHARSQUOTE;//*
//        nextState[OPENSQUOTE][C_AMPERSAND] = CHARSQUOTE;//*
//        nextState[OPENSQUOTE][C_ATSYMBOL] = CHARSQUOTE;//*
//        nextState[OPENSQUOTE][C_VERTTICALBAR] = CHARSQUOTE;//*
//        nextState[OPENSQUOTE][C_LSBRACKET] = CHARSQUOTE;
//        nextState[OPENSQUOTE][C_RSBRACKET] = CHARSQUOTE;
//        nextState[OPENSQUOTE][C_ASTERISK] = CHARSQUOTE;
//        nextState[OPENSQUOTE][C_LPARENTHISIS] = CHARSQUOTE;
//        nextState[OPENSQUOTE][C_RPARENTHISIS] = CHARSQUOTE;
//        nextState[OPENSQUOTE][C_PERCENT] = CHARSQUOTE;
//        nextState[OPENSQUOTE][C_POUND] = CHARSQUOTE;

        nextState[OPENDQUOTE][C_DOUBLEQUOTE] = CLOSEDQUOTE;
        nextState[OPENDQUOTE][C_NUMBER] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_WHITESPACE] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_NEWLINE] = ILLEGAL;
        nextState[OPENDQUOTE][C_PSEUDOCHAR] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_LABELCHAR] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_COMMENTCHAR] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_LETTER] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_a] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_b] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_c] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_d] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_e] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_i] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_l] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_n] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_s] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_t] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_u] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_g] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_p] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_o] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_h] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_r] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_SEMICOLON] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_SINGLEQUOTE] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_LCHEVRON] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_RCHEVRON] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_OTHER] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_PLUSSIGN] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_MINUSSIGN] = OPENDQUOTE;
        nextState[OPENDQUOTE][C_TOKEN] = OPENDQUOTE;
//        nextState[OPENDQUOTE][C_COLON] = OPENDQUOTE;
//        nextState[OPENDQUOTE][C_DOT] = OPENDQUOTE;
//        nextState[OPENDQUOTE][C_COMMA] = OPENDQUOTE;
//        nextState[OPENDQUOTE][C_LBRACKET] = OPENDQUOTE;
//        nextState[OPENDQUOTE][C_RBRACKET] = OPENDQUOTE;
//        nextState[OPENDQUOTE][C_UNDERSCORE] = OPENDQUOTE;
//        nextState[OPENDQUOTE][C_QUESTIONMARK] = OPENDQUOTE;//*
//        nextState[OPENDQUOTE][C_EXCLAMATION] = OPENDQUOTE;//*
//        nextState[OPENDQUOTE][C_CAROT] = OPENDQUOTE;//*
//        nextState[OPENDQUOTE][C_FOWARDSLASH] = OPENDQUOTE;//*
//        nextState[OPENDQUOTE][C_BACKSLASH] = OPENDQUOTE;//*
//        nextState[OPENDQUOTE][C_EQUALS] = OPENDQUOTE;//*
//        nextState[OPENDQUOTE][C_TILDE] = OPENDQUOTE;//*
//        nextState[OPENDQUOTE][C_AMPERSAND] = OPENDQUOTE;//*
//        nextState[OPENDQUOTE][C_ATSYMBOL] = OPENDQUOTE;//*
//        nextState[OPENDQUOTE][C_VERTTICALBAR] = OPENDQUOTE;//*
//        nextState[OPENDQUOTE][C_LSBRACKET] = OPENDQUOTE;
//        nextState[OPENDQUOTE][C_RSBRACKET] = OPENDQUOTE;
//        nextState[OPENDQUOTE][C_ASTERISK] = OPENDQUOTE;
//        nextState[OPENDQUOTE][C_LPARENTHISIS] = OPENDQUOTE;
//        nextState[OPENDQUOTE][C_RPARENTHISIS] = OPENDQUOTE;
//        nextState[OPENDQUOTE][C_PERCENT] = OPENDQUOTE;
//        nextState[OPENDQUOTE][C_POUND] = OPENDQUOTE;

        nextState[OPENCQUOTE][C_NUMBER] = OPENCQUOTE;    //SBH
        nextState[OPENCQUOTE][C_WHITESPACE] = OPENCQUOTE;    //SBH
        nextState[OPENCQUOTE][C_NEWLINE] = ILLEGAL;    //SBH
        nextState[OPENCQUOTE][C_PSEUDOCHAR] = OPENCQUOTE;
        nextState[OPENCQUOTE][C_LABELCHAR] = OPENCQUOTE;
        nextState[OPENCQUOTE][C_COMMENTCHAR] = OPENCQUOTE;
        nextState[OPENCQUOTE][C_LETTER] = OPENCQUOTE;    //SBH
        nextState[OPENCQUOTE][C_a] = OPENCQUOTE;    //SBH
        nextState[OPENCQUOTE][C_b] = OPENCQUOTE;    //SBH
        nextState[OPENCQUOTE][C_c] = OPENCQUOTE;    //SBH
        nextState[OPENCQUOTE][C_d] = OPENCQUOTE;    //SBH
        nextState[OPENCQUOTE][C_e] = OPENCQUOTE;    //SBH
        nextState[OPENCQUOTE][C_i] = OPENCQUOTE;    //SBH
        nextState[OPENCQUOTE][C_l] = OPENCQUOTE;    //SBH
        nextState[OPENCQUOTE][C_n] = OPENCQUOTE;    //SBH
        nextState[OPENCQUOTE][C_s] = OPENCQUOTE;    //SBH
        nextState[OPENCQUOTE][C_t] = OPENCQUOTE;    //SBH
        nextState[OPENCQUOTE][C_u] = OPENCQUOTE;    //SBH
        nextState[OPENCQUOTE][C_g] = OPENCQUOTE;
        nextState[OPENCQUOTE][C_p] = OPENCQUOTE;
        nextState[OPENCQUOTE][C_o] = OPENCQUOTE;
        nextState[OPENCQUOTE][C_h] = OPENCQUOTE;
        nextState[OPENCQUOTE][C_r] = OPENCQUOTE;
        nextState[OPENCQUOTE][C_SINGLEQUOTE] = OPENCQUOTE;    //SBH
        nextState[OPENCQUOTE][C_OTHER] = OPENCQUOTE;    //SBH
        nextState[OPENCQUOTE][C_MINUSSIGN] = OPENCQUOTE;    //SBH
        nextState[OPENCQUOTE][C_PLUSSIGN] = OPENCQUOTE;    //SBH
        nextState[OPENCQUOTE][C_TOKEN] = OPENCQUOTE;    //SBH
//        nextState[OPENCQUOTE][C_SEMICOLON] = OPENCQUOTE;    //SBH
//        nextState[OPENCQUOTE][C_COLON] = OPENCQUOTE;    //SBH
//        nextState[OPENCQUOTE][C_DOT] = OPENCQUOTE;    //SBH
//        nextState[OPENCQUOTE][C_COMMA] = OPENCQUOTE;    //SBH
//        nextState[OPENCQUOTE][C_LBRACKET] = OPENCQUOTE;    //SBH
//        nextState[OPENCQUOTE][C_RBRACKET] = OPENCQUOTE;    //SBH
//        nextState[OPENCQUOTE][C_UNDERSCORE] = OPENCQUOTE;    //SBH
//        nextState[OPENCQUOTE][C_LPARENTHISIS] = OPENCQUOTE;
//        nextState[OPENCQUOTE][C_RPARENTHISIS] = OPENCQUOTE;
//        nextState[OPENCQUOTE][C_PERCENT] = OPENCQUOTE;
//        nextState[OPENCQUOTE][C_LCHEVRON] = ILLEGAL;    //SBH
//        nextState[OPENCQUOTE][C_ASTERISK] = OPENCQUOTE;
//        nextState[OPENCQUOTE][C_RCHEVRON] = CLOSECQUOTE;    //SBH
//        nextState[OPENCQUOTE][C_POUND] = CLOSECQUOTE;


        nextState[PSEUDO][C_d] = DOTD;
        nextState[DOTD][C_a] = DOTDA;
        nextState[DOTDA][C_t] = DOTDAT;
        nextState[DOTDAT][C_a] = DOTDATA;

        nextState[PSEUDO][C_i] = DOTI;
        nextState[DOTI][C_n] = DOTIN;
        nextState[DOTIN][C_c] = DOTINC;
        nextState[DOTINC][C_l] = DOTINCL;
        nextState[DOTINCL][C_u] = DOTINCLU;
        nextState[DOTINCLU][C_d] = DOTINCLUD;
        nextState[DOTINCLUD][C_e] = DOTINCLUDE;

        nextState[PSEUDO][C_a] = DOTA;
        nextState[DOTA][C_s] = DOTAS;
        nextState[DOTAS][C_c] = DOTASC;
        nextState[DOTASC][C_i] = DOTASCI;
        nextState[DOTASCI][C_i] = DOTASCII;
        
        nextState[PSEUDO][C_g] = DOTG;
        nextState[DOTG][C_l] = DOTGL;
        nextState[DOTGL][C_o] = DOTGLO;
        nextState[DOTGLO][C_b] = DOTGLOB;
        nextState[DOTGLOB][C_a] = DOTGLOBA;
        nextState[DOTGLOBA][C_l] = DOTGLOBAL;

        nextState[PSEUDO][C_a] = DOTA;
        nextState[DOTA][C_l] = DOTAL;
        nextState[DOTAL][C_i] = DOTALI;
        nextState[DOTALI][C_g] = DOTALIG;
        nextState[DOTALIG][C_n] = DOTALIGN;

        nextState[PSEUDO][C_p] = DOTP;
        nextState[DOTP][C_o] = DOTPO;
        nextState[DOTPO][C_s] = DOTPOS;

        nextState[PSEUDO][C_l] = DOTL;
        nextState[DOTL][C_o] = DOTLO;
        nextState[DOTLO][C_n] = DOTLON;
        nextState[DOTLON][C_g] = DOTLONG;

        nextState[PSEUDO][C_s] = DOTS;
        nextState[DOTS][C_h] = DOTSH;
        nextState[DOTSH][C_o] = DOTSHO;
        nextState[DOTSHO][C_r] = DOTSHOR;
        nextState[DOTSHOR][C_t] = DOTSHORT;

        //all other states are final and lead to ILLEGAL
    }

    //------------------------------------
    //	     OTHER PRIVATE METHODS
    //------------------------------------


    //-------------------------------
    //returns true if the given state is final
    //not calling ILLEGAL a final state
    private boolean isFinalState(int state)
    {
        return (state == LABEL)        || (state == INSTR_VAR) ||
                (state == CLOSEDQUOTE) || (state == NEWLINE) ||
                (state == COMMENT)     || (state == NUMBER) ||
                (state == DOTINCLUDE)  || (state == DOTASCII) ||
                (state == DOTDATA)     ||
                ((plusIsSymbol || plusIsToken) && state == PLUS) ||
                ((minusIsSymbol || minusIsToken) && state == MINUS) ||
                (state == ILLEGAL)     || (state == CLOSESQUOTE) ||
                (state == CLOSECQUOTE) || (state == PUNCTUATION) ||
                (state == DOTALIGN)    || (state == DOTPOS) ||
                (state == DOTSHORT)    || (state == DOTLONG) || 
                (state == DOTGLOBAL);
    }

    //-------------------------------
    //getNextChar:  returns the next character from the current file
    //(main file or include file).
    private char getNextChar() throws AssemblyException.SyntaxError
    {
        char c;
        top.prevColumnNumber = top.columnNumber;    //hold old values
        top.prevLineNumber = top.lineNumber;
        top.prevOffset = top.offset;

        try {
            c = (char) top.reader.read();
            if (c == '\r') {
                //the only place \r occurs in modern computers (?) is
                //immediately before a \n and so we will just toss it to
                //avoid counting lines incorrectly.
                c = (char) top.reader.read();
            }
        } catch (IOException ioe) {
            throw new AssemblyException.SyntaxError("CPU Sim couldn't read a character",
                    new Token(top.filename, Token.Type.ERROR, top.lineNumber,
                            top.columnNumber, top.offset, "", false));
        }
        if (c != (char) EOF && c >= 128) {
            throw new AssemblyException.SyntaxError("non-ASCII character found",
                    new Token(top.filename, Token.Type.ERROR, top.lineNumber,
                            top.columnNumber, top.offset, "" + c, false));
        }
        if (c == (char) EOF) {
            // add an EOL before the EOF if there isn't one there:
            // if top.currentChar != EOL then put an EOF token on the tokenstack
            // and return EOL instead.  Otherwise do the stuff below
            // Note: char 'c' is not the same char as 'top.currentChar' because
            // top.currentChar is set to c only after this method returns

            if (charType[top.currentChar] != C_NEWLINE) {
                c = '\n';   //the scanner will finish returning this token before
                //using the stack's EOF token
            }
            else {

                //use the same method to get the filename as used in
                //startScanning() when the filename was added, and remove that
                //filename since it is done being used.  Now it can be legally
                //called again in another .include reference.
                filenames.remove(top.filename);
                streamStack.pop();

                //either this is the end of the program or the end of a .include file
                if (!streamStack.empty()) {
                    top = streamStack.peek();
                    return top.currentChar;
                }
            }
        }

        //keep track of line and column numbers
        if (c != (char) EOF) {
            if (charType[c] == C_NEWLINE) {
                top.lineNumber++;
                top.columnNumber = 0;
            } else {
                top.columnNumber++;
            }
            top.offset++;
        }
        return c;
    }


    //-------------------------------
    //uses the string in the token and the current state to get
    //the type of the token and returns it (as an int)
    private Token.Type getTokenType(String string, int currentState)
    {
        Token.Type tokenType;
        boolean isInstr = false;    //is the token string a machine instruction

        //loop through all of the machine's instructions
        List<MachineInstruction> instructions = machine.getInstructions();
        for (MachineInstruction instruction : instructions) {
            if (string.equals(instruction.getName()))
                isInstr = true;
        }

        if (isInstr) {
            tokenType = Token.Type.OPCODE;
        }
        else if (string.equals(pseudoChar + "data")) {
            tokenType = Token.Type.DATA;
        }
        else if (string.equals(pseudoChar + "include")) {
            tokenType = Token.Type.INCLUDE;
        }
        else if (string.equals(pseudoChar + "ascii")) {
            tokenType = Token.Type.ASCII;
        }
        else if (string.equals(pseudoChar + "pos")) {
            tokenType = Token.Type.POS;
        }
        else if (string.equals(pseudoChar + "align")) {
            tokenType = Token.Type.ALIGN;
        }
        else if (string.equals(pseudoChar + "short")) {
            tokenType = Token.Type.SHORT;
        }
        else if (string.equals(pseudoChar + "long")) {
            tokenType = Token.Type.LONG;
        }
        else if (string.equals(pseudoChar + "global")) {
            tokenType = Token.Type.GLOBAL;
        }
        else if (string.equals("EQU")) {
            tokenType = Token.Type.EQU;
        }
        else if (string.equals("MACRO")) {
            tokenType = Token.Type.MACRO;
        }
        else if (string.equals("ENDM")) {
            tokenType = Token.Type.ENDM;
        }
        else if (currentState == LABEL) {
            tokenType = Token.Type.LABEL;
        }
        else if (currentState == CLOSEDQUOTE) {
            tokenType = Token.Type.QUOTEDSTRING;
        }
        else if (currentState == NEWLINE) {
            tokenType = Token.Type.EOL;
        }
        else if (currentState == COMMENT) {
            tokenType = Token.Type.COMMENT;
        }
        else if (currentState == NUMBER) {
            tokenType = Token.Type.CONSTANT;
        }
        else if (currentState == PLUS) {
            tokenType = Token.Type.VAR;
        }
        else if (currentState == MINUS) {
            tokenType = Token.Type.VAR;
        }
//        else if (currentState == COMMA) {
//            tokenType = Token.Type.COMMA;
//        }
//        else if (currentState == LBRACKET) {
//            tokenType = Token.Type.LBRACKET;
//        }
//        else if (currentState == RBRACKET) {
//            tokenType = Token.Type.RBRACKET;
//        }
//        else if (currentState == LPARENTHESIS) {
//            tokenType = Token.Type.LPARENTHESIS;
//        }
//        else if (currentState == RPARENTHISIS) {
//            tokenType = Token.Type.RPARENTHESIS;
//        }
//        else if (currentState == LBRACKET) {
//            tokenType = Token.Type.LBRACKET;
//        }
//        else if (currentState == RBRACKET) {
//            tokenType = Token.Type.RBRACKET;
//        }
        else if (currentState == ILLEGAL) {
            tokenType = Token.Type.ERROR;
        }
        else if (currentState == CLOSESQUOTE) {
            tokenType = Token.Type.CONSTANT;
        }
        else if (currentState == CLOSECQUOTE) {
            tokenType = Token.Type.QUOTEDSTRING;
        }
        else if (currentState == PUNCTUATION) {
            tokenType = Token.Type.PUNCTUATION;
        }
        else {
            tokenType = Token.Type.VAR;
        }
        //don't look for EOF

        return tokenType;
    }

    //------------------------------------
    //		    PUBLIC METHODS
    //------------------------------------

    //-------------------------------
    //pushes tokens onto tokenStack from the parser when it comes
    //across a macro call
    public void pushTokens(List<Token> v)
    {
        //push the tokens on the stack in reverse order out of the vector
        for (int i = v.size() - 1; i >= 0; i--) {
            top.tokenStack.push(v.get(i));
        }
    }

    //-------------------------------
    //First checks to see if tokenStack has any tokens put there from macro
    //calls.  If so, it just pops a token and returns it.
    //Otherwise, it returns a Token corresponding to the next non-comment token
    //in the "stream".  If there is a scanning error, it returns a Token
    //whose isLegal field is false.  A Token of type EOF is repeatedly
    //returned at the end of the input.
    //
    public Token getNextToken() throws AssemblyException.InvalidTokenException, AssemblyException.SyntaxError
    {
        //check tokenStack for tokens from macro calls
        if (!top.tokenStack.empty()) {
            return top.tokenStack.pop();
        }

        StringBuilder buffer = new StringBuilder();
        int currentState = START;

        //hold the start of the token in these values so if an error occurs,
        //we can figure out easily which token it was
        int startingLineNumber = top.prevLineNumber;
        int startingColumnNumber = top.prevColumnNumber;
        int startingOffset = top.prevOffset;

        //the current character needed is already in place from initialization
        //or the last time getNextToken() was called

        //loop until the next token is found
        while (top.currentChar != (char) EOF &&
                nextState[currentState][charType[top.currentChar]] != ILLEGAL) {
            //move to the next state using the current state
            //and character as indices into nextState matrix
            currentState = nextState[currentState][charType[top.currentChar]];
            
            //do any state actions
            if (charType[top.currentChar] != C_WHITESPACE ||
                    currentState == COMMENT ||
                    currentState == OPENDQUOTE ||
                    currentState == CHARSQUOTE) {
                //we want to save the white space in comments and pathnames
                //as well as in regular tokens
                buffer.append(top.currentChar);
            }
            else {
                //skip white space, but still inc the *starting* column number
                //and offset
                startingColumnNumber++;
                startingOffset++;
            }

            //move to the next character
            top.currentChar = getNextChar();
        }

        if (isFinalState(currentState)) {
//            if (buffer.toString().equals(".global")){
//                System.out.println();
//            }
            Token.Type ttype = getTokenType(buffer.toString(), currentState);
            return new Token(top.filename, ttype, startingLineNumber,
                    startingColumnNumber, startingOffset,
                    buffer.toString(), true);
        }
        else if (top.currentChar == (char) EOF && currentState == START) {
            //return an end of file token to mark the end of the token stream.
            return new Token(top.filename, Token.Type.EOF, startingLineNumber,
                    startingColumnNumber, startingOffset, "", true);
        }
        else {
            //the token is illegal
            buffer.append(top.currentChar);

            //advance one character before returning the illegal token
            if (top.currentChar != (char) EOF) {
                top.currentChar = getNextChar();
            }


            Token.Type tokenType = getTokenType(buffer.toString(), currentState);
            return new Token(top.filename, tokenType, startingLineNumber,
                    startingColumnNumber, startingOffset, buffer.toString(), false);
            //mark as illegal
        }
    }//end getNextToken

//    public void setCommentPseudoAndLabelchars(char c, char p, char l)
//    {
//        commentChar = c;
//        pseudoChar = p;
//        labelChar = l;
//        initializeCharTypeArray();
//        initializeNextStateArray();
//    }
//
//    public String getCommentPseudoAndLabelChars()
//    {
//        return "" + commentChar + pseudoChar + l  abelChar;
//    }
}
