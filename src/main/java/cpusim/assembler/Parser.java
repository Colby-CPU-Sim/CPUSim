///////////////////////////////////////////////////////////////////////////////
// File:    	Parser.java
// Type:    	java application file
// Author:		Raymond H. Mazza III and Dale Skrien
// Project: 	CPU Sim 3.0
// Date:    	June, 2000
//
// Description:
//   The parser that takes tokens and builds a vector of instruction calls.

///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

package cpusim.assembler;

/*
 * Michael Goldenberg, Ben Borchard, and Jinghui Yu made the following changes in 12/4/13
 * 
 * 1.) Added two stacks, includeFileNames and includeLabelHashStack.  The former has the
 * current file being parsed on top, the latter has the hash of original label tokens and 
 * new label tokens with a unique name
 * 2.) Made the Include() method have a first pass to find label tokens and fromRootController new
 * label tokens with unique names
 * 3.) Changed the advance() method so that it replaces the token just received with a new
 * token with a unique name if appropriate (if it is in an include file and working with a
 * label token or a label variable token)
 * 4.) Made a new advanceWithoutReplacingLabels() method that is used when making the
 * first pass
 * through the included file
 * 5.) Added a hashmap of globals and a corresponding boolean that is true if the
 * labels have already
 * been declared
 * 6.) Chaged the includeFistPass() method and Instr() method to allow funtionality for
 * global labels
 * 
 */

///////////////////////////////////////////////////////////////////////////////
// the libraries we need to import

import cpusim.model.Field;
import cpusim.model.Field.Type;
import cpusim.model.FieldValue;
import cpusim.model.Machine;
import cpusim.model.MachineInstruction;
import cpusim.assembler.AssemblyException.*;
import cpusim.util.Convert;
import cpusim.util.SourceLine;

import java.util.*;

///////////////////////////////////////////////////////////////////////////////
// the Parser class

public class Parser {

    private Scanner scanner;
    private Token token;            //holds the current token
    private HashMap<Token, Token> equs;            //containing name-value pairs
    private List<InstructionCall> instructions;
    private HashMap<Token, MacroDef> macros;        //key = token for macro name,
    //value = MacroDef object

    //stack of a hash of labels that need to be replaced in the include file
    //key = original label, value = label with unique name
    private Stack<HashMap<Token, Token>> includeLabelHashStack;
    //stack of the filenames included using the include pseudo
    private Stack<String> includeFileNames;
    private HashMap<Token, Boolean> globals;      //hash of global labels and whether
    //they have been declared
    private int uniqueMacroLabelNumber;            //incremented each time it is used
    private int uniqueIncludeLabelNumber;    //incremented each time it is used

    private Machine machine;        //contains global EQU's the parser needs


    //-------------------------------
    // constructor
    public Parser(Scanner s, Machine machine) {
        this.scanner = s;
        this.equs = new HashMap<Token, Token>();
        this.instructions = new ArrayList<InstructionCall>();
        this.macros = new HashMap<Token, MacroDef>();
        this.includeFileNames = new Stack<>();
        this.includeLabelHashStack = new Stack<>();
        this.globals = new HashMap<>();
        this.machine = machine;
    }

    //-------------------------------
    //advances parser once
    //and initializes its modules
    public void initialize() throws AssemblyException {
        this.instructions.clear();
        this.macros.clear();
        this.equs.clear();
        this.uniqueMacroLabelNumber = 0;
        this.uniqueIncludeLabelNumber = 0;

        this.advance();
    }

    //-------------------------------
    //parses the whole program
    public void parse() throws AssemblyException {
        Program();
    }


    //-------------------------------
    //only used for debugging in the Assembler class
    public List<InstructionCall> getInstructions() {
        return instructions;
    }

    //-------------------------------
    //also only used for debugging in the Assembler class
    public HashMap<Token, Token> getEqus() {
        return equs;
    }


    //-------------------------------
    // NOTE:	having an EOF token returned as the last token (instead of null)
    //			automatically stops the parsing without making any changes in
    //			Parser because once the EOF token is found, no part of the
    //			parser advances past it, and no part of the parser recognizes it,
    //			but it's legal, so the parser just finishes it's cycle through
    //			all of its methods without doing anything.
    private void advance() throws AssemblyException.InvalidTokenException, SyntaxError {
        //grab the next token
        token = scanner.getNextToken();

        //update the include stacks
        updateIncludeStacks();

        //give unique labels to any tokens that need it if in an include file
        if (!this.includeFileNames.isEmpty()) {
            giveUniqueLabelName();
        }

        //check if the token is legal
        if (token != null && !token.isLegal()) {
            throw new AssemblyException.InvalidTokenException("Error: non-sensical " +
                    "token was found: \"" +
                    token.contents + "\"",
                    token);
        }
    }

    /**
     * This is the same as the normal advance method except that it doesn't try to
     * give unique labels
     *
     * @throws cpusim.assembler.AssemblyException.InvalidTokenException
     * @throws cpusim.assembler.AssemblyException.SyntaxError
     */
    private void advanceWithoutReplacingLabels() throws InvalidTokenException,
            SyntaxError {
        //grab the next token
        token = scanner.getNextToken();

        //check if the token is legal
        if (token != null && !token.isLegal()) {
            throw new AssemblyException.InvalidTokenException("Error: nonsensical token" +
                    " was found: \"" +
                    token.contents + "\"",
                    token);
        }
    }

    /**
     * updates the two include stacks if it is necessary
     */
    private void updateIncludeStacks() {
        //check if we are getting a token from an include file
        if (!this.includeFileNames.isEmpty()) {
            //if the file we are scanning is not on the top of our stack, pop that
            // filename
            //off the stack alon with the unique label hashmap
            if (!scanner.thisFileIsCurrentlyBeingScanned(this.includeFileNames.peek())) {
                this.includeFileNames.pop();
                this.includeLabelHashStack.pop();
            }
        }
    }

    /**
     * gives the token a unique name if it is a key in the hashmap at the top of the
     * includeLabelHashStack
     */
    private void giveUniqueLabelName() {
        if (this.includeLabelHashStack.peek().containsKey(token)) {
            token = this.includeLabelHashStack.peek().get(token);
        }
    }

    private boolean currentTokenHasOneOfTypes(Token.Type[] types) {
        for (Token.Type type : types)
            if (token.type == type) {
                return true;
            }
        return false;
    }


    //-------------------------------
    //Program -> [Comments-and-EOLs] Equ-and-include-and-macro-and-global-part
    // Instr-part EOF
    private void Program() throws AssemblyException {
        //Comments-and-EOLs
        //put this in an if statement because the Comments_and_EOLs expects to see
        //a comment or EOL
        if (token.type == Token.Type.COMMENT || token.type == Token.Type.EOL) {
            Comments_and_EOLs();
        }

        //Equ-and-include-and-macro-and-global-part
        Equ_and_Include_and_macro_and_global_part();

        //Instr-part
        Instr_part();

        //only thing left should be EOF
        if (!currentTokenHasOneOfTypes(new Token.Type[]{Token.Type.EOF})) {
            throw new AssemblyException.SyntaxError("No EOF token found...  " +
                    "program seemed to end prematurely." +
                    "\n       This probably means there is something " +
                    "missing or something extra at the following location," +
                    "\n       or there is some other type of faulty " +
                    "structure to your program...",
                    token);
        }
    }

    //-------------------------------
    private void Include() throws AssemblyException.SyntaxError, AssemblyException
            .InvalidTokenException, ImportError, NameSpaceError {
        // This is where, if the .include is on the last line with no comment,
        // the advance goes to the path token, and the getNextToken before
        // returning the path token chambers the next char, which then happens
        // to be the EOF, but, our getNextChar overrides that and pushes the EOF
        // onto the token stack while returning a newline character for
        // top.currentchar

        advance(); //past the include token, the next should be a path
        if (!currentTokenHasOneOfTypes(new Token.Type[]{Token.Type.QUOTEDSTRING})) {
            throw new AssemblyException.SyntaxError("There needs to be a path in " +
                    "quotes or in angle brackets <...> after the " +
                    " .include pseudo-instruction",
                    token);
        }

        Token pathToken = token; //save the token for its line and column numbers

        // advance to make the current token be the EOL of the .include line
        // (current char may already be EOF for the scanner because it gets set
        // in getNextToken())
        advance(); //advance over the QUOTEDSTRING token

        if (token.type == Token.Type.COMMENT) {
            advance();
        }

        // start scanning the included file
        scanner.startScanning(pathToken);

        // get the first token in the new file
        this.advanceWithoutReplacingLabels();

        // push filename onto the stack
        this.includeFileNames.push(token.filename);

        // make initial pass through the included file to construct a Map of all unique
        // labels in the file.
        includeFirstPass(token.filename);

        // scan the file again now that the uniqueLabelsHash and filename has been
        // pushed onto their
        // respective include stacks
        scanner.startScanning(pathToken);
        advance(); // grab the first token
    }

    /**
     * This method runs through a file and finds all the label tokens and gives
     * them new names and stores all of it in a hashmap which it then pushes onto the
     * includeLabelHashStack
     *
     * @param filename the filename of the file to run through
     * @throws cpusim.assembler.AssemblyException.InvalidTokenException
     * @throws cpusim.assembler.AssemblyException.SyntaxError
     * @throws cpusim.assembler.AssemblyException.NameSpaceError
     */
    private void includeFirstPass(String filename) throws InvalidTokenException,
            SyntaxError, NameSpaceError {

        //initialize the includeLabelHash so that labels within the include get a unique
        //label name
        HashMap<Token, Token> includeLabelHash = new HashMap<>();

        //run through the file
        while (scanner.thisFileIsCurrentlyBeingScanned(filename)) {
            //don't replace globals
            if (token.type == Token.Type.LABEL && !globals.containsKey(token)) {

                //check that the label isn't duplicated in the macro body
                if (includeLabelHash.containsKey(token)) {
                    throw new AssemblyException.NameSpaceError("The label \"" +
                            token.contents + "\" is used twice in the macro " +
                            "and so cannot be used here",
                            token);
                }

                //make a variable label token (without the labelChar)
                Token vt = new Token(token.filename, Token.Type.VAR, token
                        .columnNumber, token.columnNumber,
                        token.offset, token.contents.substring(0, token.contents.length
                        () - 1), token.isLegal);

                //make a label token with a unique name
                Token nt = new Token(token.filename, token.type, token.columnNumber,
                        token.columnNumber,
                        token.offset, "IL$" + this.uniqueIncludeLabelNumber + machine
                        .getLabelChar(),
                        token.isLegal);

                //make a variable label token (without the labelChar) with a unique name
                Token nvt = new Token(token.filename, Token.Type.VAR, token
                        .columnNumber, token.columnNumber,
                        token.offset, "IL$" + this.uniqueIncludeLabelNumber, token
                        .isLegal);

                //increment the number to preserve uniqueness
                this.uniqueIncludeLabelNumber++;

                //add the label with and without its label char (as a LABEL and VAR)
                includeLabelHash.put(token, nt);
                includeLabelHash.put(vt, nvt);
            }
            //get the next token, but do not deal with labels
            advanceWithoutReplacingLabels();
        }
        //put the hash onto the stack
        this.includeLabelHashStack.push(includeLabelHash);
    }


    private void Global() throws InvalidTokenException, SyntaxError {

        //advance past the global token
        advance();

        //make sure the next token is a variable token
        if (!currentTokenHasOneOfTypes(new Token.Type[]{Token.Type.VAR})) {
            throw new AssemblyException.SyntaxError("There needs to be a label " +
                    "after the .global pseudo-instruction",
                    token);
        }

        //add a label token to the list of globals
        globals.put(new Token(token.filename, Token.Type.LABEL, token.lineNumber, token
                .columnNumber,
                token.offset, token.contents + machine.getLabelChar(), token.isLegal),
                false);

        //advanced passed the global label
        advance();
    }

    //-------------------------------
    //these are the comments to be tossed since they do not appear on the same
    //line as on instruction
    //Comments-and-EOLs -> ([Comment] EOL)+
    private void Comments_and_EOLs() throws AssemblyException.SyntaxError,
            InvalidTokenException {

        do {
            if (!currentTokenHasOneOfTypes(
                    new Token.Type[]{Token.Type.COMMENT, Token.Type.EOL, Token.Type
                            .EOF})) {
                throw new AssemblyException.SyntaxError("There was supposed to be " +
                        "either a comment or an\n" +
                        "       end of line or end of file, " +
                        (token.contents.equals("") ? "" :
                                "not \"" + token.contents + "\""),
                        token);
            }
            advance();
        }
        while (currentTokenHasOneOfTypes(new Token.Type[]{Token.Type.COMMENT, Token
                .Type.EOL}));

    }

    //-------------------------------
    //Equ-and-Include-and-macro-and-global-part ->
    //					((Equ-decl | Include | Macro-decl | global) Comments-and-EOLs)*
    private void Equ_and_Include_and_macro_and_global_part() throws AssemblyException {
        //(Equ-decl Comments-and-EOLs)*
        while (currentTokenHasOneOfTypes(new Token.Type[]{Token.Type.VAR, Token.Type
                .MACRO,
                Token.Type.INCLUDE, Token.Type.GLOBAL})) {
            if (token.type == Token.Type.INCLUDE) {
                Include();
                // if the included file doesn't start with an EOL or comment
                //      then we want to skip over the Comments_and_EOLs at the
                //      end of this while loop
                if(token.type != Token.Type.EOL && token.type != Token.Type.COMMENT)
                	continue;
            }
            else if (token.type == Token.Type.GLOBAL) {
                Global();
            }
            else if (token.type == Token.Type.MACRO) {
                Macro_decl();
            }
            else if (macros.get(token) != null) {
                //it's a macro call in the instruction part, so exit
                break;
            }
            else {
                Equ_decl();
            }

            Comments_and_EOLs();
        }
    }

    //-------------------------------
    //Equ-decl -> Symbol "EQU" Operand
    private void Equ_decl() throws AssemblyException.SyntaxError, NameSpaceError,
            InvalidTokenException, UndefinedOperandError {
        //Symbol
        checkEquNotAlreadyUsed(token);
        Token key = token;    //we know this is a symbol (var) since it was checked
        //in the calling method
        advance();

        //"EQU"
        if (!currentTokenHasOneOfTypes(new Token.Type[]{Token.Type.EQU})) {
            throw new AssemblyException.SyntaxError("This looks like an EQU " +
                    "declaration,\nbut there is no 'EQU' following the " +
                    "word \"" + key + "\".",
                    key);
        }
        advance();

        //Operand
        if (!currentTokenHasOneOfTypes(new Token.Type[]{Token.Type.VAR, Token.Type
                .CONSTANT}))
        //make sure the token is an operand
        {
            throw new AssemblyException.SyntaxError("This is an unfinished EQU " +
                    "declaration",
                    token);
        }

        //make sure this is a previously defined value in the EQU list (as a constant)
        if (token.type == Token.Type.VAR) {
            checkEquDefined(token);
        }

        equs.put(key, token);
        advance();                    //call advance first
    }

    //-------------------------------
    //Macro-declaration ->
    //	Macro Symbol [Symbol ([","] Symbol)*] Comments-and-EOLs
    //                                                     Instruction-part Endm
    private void Macro_decl() throws AssemblyException.SyntaxError, AssemblyException
            .NameSpaceError, InvalidTokenException, ImportError {
        //Macro ...
        advance(); //advance past the "MACRO" token

        //... Symbol or opcode...
        //(We allow the user to redefine an opcode using macros)
        if (!currentTokenHasOneOfTypes(new Token.Type[]{Token.Type.VAR, Token.Type
                .OPCODE})) {
            throw new AssemblyException.SyntaxError("This macro needs a name",
                    token);
        }

        Token macroName = token;
        if (macros.get(macroName) != null) {
            throw new AssemblyException.NameSpaceError("A macro by the name \"" +
                    macroName + "\" already exists.",
                    token);
        }
        advance();

        //... [Symbol ([","] Symbol)*] ...
        List<Token> macroParameters = new ArrayList<Token>();
        while (token.type == Token.Type.VAR) {
            macroParameters.add(token);
            advance();

            if (token.type == Token.Type.COMMA) {
                advance();
            }
        }

        //Comments-and-EOLs
        Comments_and_EOLs();

        //Instruction-part Endm
        //Note: this instruction part has to be kept as the body of the macro
        List<Token> macroBody = new ArrayList<Token>();
        while (token.type != Token.Type.ENDM && token.type != Token.Type.EOF) {
            //if an include is found in a macro body, it is scanned from during
            //the parse rather than included as part of the macro body.
            //In this way, an include file could finish a macro
            //definition if desired.
            //(but why someone would desire this...  I don't know)
            if (token.type == Token.Type.INCLUDE) {
                Include();
                advance();  //get rid of EOL token from end of .include statement
            }

            macroBody.add(token);
            advance();
        }

        if (token.type == Token.Type.EOF) {
            throw new AssemblyException.SyntaxError("Error: The macro declaration for " +
                    "\"" +
                    macroName + "\" is missing the \"ENDM\" keyword.",
                    macroName);
        }
        else {
            advance(); //pass the ENDM token
            macros.put(macroName, new MacroDef(macroName, macroParameters,
                    macroBody));
        }

    }


    //-------------------------------
    //Instr-part -> (InstructionCall Comments-and-EOLs)*
    private void Instr_part() throws AssemblyException.InvalidTokenException,
            SyntaxError, ImportError, NameSpaceError, UndefinedOperandError, TypeError,
            AssemblyException.InvalidOperandError {
        //VAR token would be a macro call
        while (token.type == Token.Type.INCLUDE || token.type == Token.Type.LABEL ||
                token.type == Token.Type.OPCODE || token.type == Token.Type.DATA ||
                token.type == Token.Type.VAR || token.type == Token.Type.ASCII) {

            if (token.type == Token.Type.INCLUDE) {
                Include();
                // if the included file doesn't start with an EOL or comment
                //      then we want to skip over the Comments_and_EOLs at the
                //      end of this while loop
                if(token.type != Token.Type.EOL && token.type != Token.Type.COMMENT)
                    continue;
            }
            else {
                Instr();
            }
            Comments_and_EOLs();
        }
    }

    //-------------------------------
    //Instr ->
    //  (Label Comments-and-EOLs)* [Label] Symbol [Operand ([","] Operand)*] [Comment]
    // In this case, Symbol is either an OPCODE, DATA or a VAR (representing a macro call)
    private void Instr() throws AssemblyException.NameSpaceError, AssemblyException
            .SyntaxError, InvalidTokenException, UndefinedOperandError, TypeError,
            AssemblyException.InvalidOperandError {
        InstructionCall node = new InstructionCall(
                machine.getCodeStore().getCellSize());

        //handle (Label Comments-and-EOLs)*
        while (token.type == Token.Type.LABEL) {
            //make sure the label isn't the name of an opcode, which is illegal
            List<MachineInstruction> instructions =
                    scanner.getMachine().getInstructions();
            for (MachineInstruction instruction : instructions) {
                if (token.contents.equals(instruction.getName() +
                        machine.getLabelChar())) {
                    throw new AssemblyException.NameSpaceError("Opcode names cannot " +
                            "be used as label names",
                            token);
                }
            }
            //check if the label is global and see if it has been declared
            if (globals.containsKey(token) && !globals.get(token)) {
                globals.put(token, true);
            }
            //throw a namespace exception if it has already been declared
            else if (globals.containsKey(token) && globals.get(token)) {
                throw new AssemblyException.NameSpaceError("This label is global"
                        + " and was already declared in this or an included file", token);
            }
            //the label is legal so add it to the list of labels for this instr
            node.labels.add(token);
            node.comment += token.contents + " "; //save the label in the comment
            advance();    //call advance first
            if (token.type == Token.Type.COMMENT || token.type == Token.Type.EOL) {
                Comments_and_EOLs();    //ditch any comments
                node.comment = "";
            }
        }

        //should be a .data, .ascii, macro call, or regular instruction
        if (!currentTokenHasOneOfTypes(new Token.Type[]{Token.Type.OPCODE, Token.Type.DATA,
                                                        Token.Type.VAR, Token.Type.ASCII})
                || ((token.type == Token.Type.VAR) && !(macros.containsKey(token)))) {
            //you only get here after a label
            throw new AssemblyException.SyntaxError("There must be a data " +
                    "pseudo-instruction, a macro call, an ascii " +
                    "pseudo-instruction or a normal " +
                    "instruction after a label.",
                    token);
        }

        if (macros.containsKey(token)) { //it is a macro call
            node.comment = ""; //don't add the label in front of the macro call
            macroCall(); //this gets the macro and sticks in the args, then
            //shoves all the body onto the token stack
            //push labels in front of the macro call
            this.scanner.pushTokens(node.labels);
            return; //return here, because the macro will start a new instruction
        }
        else if (token.type == Token.Type.ASCII) {
            parseAsciiPseudoinstruction(node);
        }
        else if (token.type == Token.Type.DATA) {
            parseDataPseudoinstruction(node);
        }
        else { //token.type == Token.Type.OPCODE, so it is a regular instruction
            parseRegularInstruction(node);
        }

        //handle ...[Comment]...
        if (token.type == Token.Type.COMMENT) {
            node.comment += " " + token.contents;
        }

        instructions.add(node);    //add the completed instruction object
    }

    private void parseAsciiPseudoinstruction(InstructionCall node) throws
            AssemblyException.SyntaxError, InvalidTokenException, AssemblyException
            .InvalidOperandError {
        node.sourceLine = new SourceLine(token.lineNumber, token.filename);
        if (node.comment.equals("")) {
            node.comment = "\t"; //indent lines with no labels
        }
        node.comment += ".ascii ";
        advance(); //past the ascii token, the next should be a quoted string
        node.machineInstruction = null; //just for clarification,
        //ascii pseudoinstructions have no machine instruction
        if (!currentTokenHasOneOfTypes(new Token.Type[]{Token.Type.QUOTEDSTRING})
                || token.contents.charAt(0) == '<') {
            throw new AssemblyException.SyntaxError("There needs to be a quoted" +
                    " string in the .ascii pseudo-instruction",
                    token);
        }

        String contentsWithEscapes = Convert.tokenContentsToStringWithEscapes(token);

        node.comment += contentsWithEscapes; //save the string in the comment


        //save the quoted string characters as individual integer tokens
        // (operands), like in a .data pseudoinstruction
        int numChars = contentsWithEscapes.length() - 2;
        int cellSize = machine.getCodeStore().getCellSize();
        int numCellsPerChar = (8 + cellSize - 1) / cellSize;
        node.operands.add(new Token(null, Token.Type.CONSTANT, -1, -1, -1,
                numChars * numCellsPerChar + "", true)); //number of cells of data
        node.operands.add(new Token(null, Token.Type.CONSTANT, -1, -1, -1,
                numCellsPerChar + "", true)); //num cells per operand
        for (int i = 1; i <= numChars; i++)
            node.operands.add(new Token(null, Token.Type.CONSTANT, -1, -1,
                    -1, ((int) contentsWithEscapes.charAt(i)) + "", true));

        advance(); //advance over the QUOTEDSTRING token
    }

    private void parseRegularInstruction(InstructionCall node) throws AssemblyException
            .SyntaxError, InvalidTokenException, TypeError {
        node.sourceLine = new SourceLine(token.lineNumber, token.filename);

        //find the instruction in the list of machineInstructions and
        //stick it in the InstructionCall node.
        List<MachineInstruction> instructions =
                scanner.getMachine().getInstructions();
        for (MachineInstruction instruction : instructions) {
            if (token.contents.equals(instruction.getName())) {
                node.machineInstruction = instruction;
                break;
            }
        }

        if (node.comment.equals("")) {
            node.comment = "        "; //indent lines with no labels
        }
        node.comment += token.contents + " "; //opcode and a space
        advance(); //advance past the opcode

        List<Field> fields = node.machineInstruction.getAssemblyFields();
        for (int i = 1; i < fields.size(); i++) {
            //process the next field
            Field field = fields.get(i);
            if (field.getNumBits() == 0
                    && field.getType() == Type.required) {
                check(field.getName().equals(token.contents), token,
                        "The token should be \"" +
                                field.getName() + "\"");
                addTokenToComments(node, token);
                advance();
            }
            else if (field.getNumBits() == 0) { // type = optional)
                if (field.getName().equals(token.contents)) {
                    addTokenToComments(node, token);
                    advance();
                }
            }
            else if (!(field.getType() == Type.required)) {
                node.operands.add(new Token(token.filename, Token.Type.CONSTANT,
                        token.lineNumber, token.columnNumber,
                        token.offset, "" + field.getDefaultValue(),
                        true));
            }
//            else if(! field.isRequired()) {
//                check(false, token, "Optional fields of positive length" +
//                        " are not allowed in this version");
//            }
            else if (field.getValues().size() == 0) { //no special values
                check(currentTokenHasOneOfTypes(new Token.Type[]{Token.Type.VAR,
                                Token.Type.CONSTANT}),
                        token, "The token \"" + token.contents
                                + "\" is not a legal operand here");
                node.operands.add(token);
                addTokenToComments(node, token);
                advance();
            }
            else {
                check(currentTokenHasOneOfTypes(new Token.Type[]{Token.Type.VAR,
                                Token.Type.CONSTANT}),
                        token, "The token \"" + token.contents
                                + "\" is not a legal operand here");
                //check that the token is one of the legal values for this field
                boolean found = false;
                for (FieldValue value : field.getValues()) {
                    if (value.getName().equals(token.contents)) {
                        found = true;
                    }
                }
                check(found, token, "The token \"" + token.contents
                        + "\" is not one of the legal" +
                        " values for this field");
                node.operands.add(token);
                addTokenToComments(node, token);
                advance();
            }
        }

        //now check for illegal leftover tokens on the line
        check(currentTokenHasOneOfTypes(new Token.Type[]{Token.Type.COMMENT, Token.Type
                        .EOL, Token.Type.EOF}),
                token, "The token \"" + token.contents + "\" is illegal here");
    }

    private void addTokenToComments(InstructionCall node, Token token) {
        if (Character.isLetterOrDigit(node.comment.charAt(
                node.comment.length() - 1))
                && Character.isLetterOrDigit(token.contents.charAt(0))) {
            node.comment += " " + token.contents;
        }
        else {
            node.comment += token.contents;
        }
    }

    private void check(boolean condition, Token token, String message) throws
            AssemblyException.TypeError {
        if (!condition) {
            throw new AssemblyException.TypeError(message, token);
        }
    }

    private void parseDataPseudoinstruction(InstructionCall node) throws
            AssemblyException.SyntaxError, InvalidTokenException, TypeError {
        node.sourceLine = new SourceLine(token.lineNumber, token.filename);
        if (node.comment.equals("")) {
            node.comment = "\t"; //indent lines with no labels
        }
        node.comment += ".data";
        advance();    //advance past the ".data" token
        node.machineInstruction = null; //just for clarification, data has
        //no machine instruction

        //first operand
        check(currentTokenHasOneOfTypes(new Token.Type[]{Token.Type.VAR, Token.Type
                        .CONSTANT}),
                token, "Error: The data pseudo-instruction" +
                        " must have an integer as first operand");

        node.operands.add(token);
        node.comment += " " + token.contents;
        advance();
        if (token.contents.equals(",")) {
            advance(); //ignore commas
        }

        //second operand
        if (currentTokenHasOneOfTypes(new Token.Type[]{Token.Type.VAR, Token.Type
                .CONSTANT})) {
            //second operand
            node.operands.add(token);
            node.comment += " " + token.contents;
            advance();
        }
        else if (token.contents.equals("[")) {
            node.operands.add(new Token(null, Token.Type.CONSTANT, -1, -1, -1,
                    "1", true)); //1 cell per operand
        }
        else {
            throw new AssemblyException.SyntaxError("The data pseudo-instruction " +
                    "has an illegal second operand", token);
        }

        //remaining operands
        if (token.contents.equals("[")) { //there are more operands
            node.comment += " [";
            advance();
            while (token.type == Token.Type.VAR || token.type == Token.Type.CONSTANT) {
                node.operands.add(token);
                node.comment += " " + token.contents;
                advance();
                if (token.contents.equals(",")) {
                    advance();
                }
            }
            if (!token.contents.equals("]")) {
                throw new AssemblyException.SyntaxError("Closing bracket ']' " +
                        "expected", token);
            }
            node.comment += "]";
            advance();
        }
    }


    //-------------------------------
    private void macroCall() throws AssemblyException.InvalidTokenException,
            SyntaxError, UndefinedOperandError, NameSpaceError {
        //we must make all the labels in macros unique from one another.
        //Each new unique label will consist of an uppercase 'L' followed
        //by a dollar sign '$' followed by a unique constant determined by the
        //instance variable uniqueLabelNumber which is incremented each time
        //a new unique label is made.  Ex:  L$19
        HashMap<Token, Token> uniqueLabelHash = new HashMap<Token, Token>();
        Token macroDefKey = token;
        advance();

        //make a new vector with clones of all the tokens of the macro body,
        //so that the original MacroDef body can be left alone
        List<Token> macroArgs = getMacroArgs();
        MacroDef mdef = getMdef(macroDefKey, macroArgs);

        //match up the parameter with its corresponding argument so it may be
        //referenced from the body
        HashMap<Token, Token> parameterArgHash = getParameterArgHash(macroArgs, mdef);
        List<Token> specializedMacroBody = specializedMacroBody(mdef, parameterArgHash);

        List<Token> localLabels = new ArrayList<Token>();
        //REMINDER: LABELS all have colons, and references to labels are of type VAR
        //first pass through, see which labels are defined in the macro, so we
        //know to only change those
        for (int j = 0; j < specializedMacroBody.size(); j++) {
            firstPass(specializedMacroBody, localLabels, j);
        }

        //second pass, replace all local labels by verifying with the contains()
        //method on the localLabels vector.  Now go through the specialized
        //macro body and every time a local label token is found,
        //add it to the local unique label HashMap with a unique label
        //L$ + uniqueLabelNumber, and reference that when it is seen again
        for (int j = 0; j < specializedMacroBody.size(); j++) {
            secondPass(uniqueLabelHash, specializedMacroBody, localLabels, j);
        }
        this.scanner.pushTokens(specializedMacroBody);
    }

    private void secondPass(HashMap<Token, Token> uniqueLabelHash,
                            List<Token> specializedMacroBody,
                            List<Token> localLabels,
                            int j) {
        if (localLabels.contains(specializedMacroBody.get(j)))
        //if the token is a label to replace
        {
            char labelChar = machine.getLabelChar();
            Token toReplace = specializedMacroBody.get(j);
            Token t = specializedMacroBody.get(j);

            if (t.contents.charAt(t.contents.length() - 1) == labelChar) {
                //we must remove the colon from the end of its contents
                //because it isn't referenced with the colon in the code
                //fromRootController a clone since all tokens are final
                t = new Token(t.filename, t.type, t.lineNumber,
                        t.columnNumber, t.offset, t.contents.substring(0,
                        t.contents.length() - 1), t.isLegal);
            }

            //if the hash does not contain this token as a key, then make it
            //one and make a corresponding new unique label.  Make sure to
            //add ones with and without colons so they get replaced
            //correctly for the normalizer
            if (!uniqueLabelHash.containsKey(t)) {
                String uniqueLabelContents = "L$" + this.uniqueMacroLabelNumber;

                //add the labels without the colons
                //(as label references --> VAR type)
                Token uniqueLabelToken1 =
                        new Token(t.filename, Token.Type.VAR, t.lineNumber,
                                t.columnNumber, t.offset,
                                uniqueLabelContents, t.isLegal());
                uniqueLabelHash.put(t, uniqueLabelToken1);

                //and with the colons
                t = new Token(t.filename, Token.Type.LABEL, t.lineNumber,
                        t.columnNumber, t.offset,
                        t.contents + labelChar, t.isLegal);
                uniqueLabelContents += labelChar;
                Token uniqueLabelToken2 = new Token(t.filename, Token.Type.LABEL,
                        t.lineNumber, t.columnNumber, t.offset,
                        uniqueLabelContents, t.isLegal());
                uniqueLabelHash.put(t, uniqueLabelToken2);

                uniqueMacroLabelNumber++;
            }

            //now replace this label with its unique value
            specializedMacroBody.set(j, uniqueLabelHash.get(toReplace));
        }
    }

    private void firstPass(List<Token> specializedMacroBody,
                           List<Token> localLabels, int j) throws AssemblyException
            .NameSpaceError {
        Token t = specializedMacroBody.get(j);
        //if the token is a label, remember it because it is a local label
        if (t.type == Token.Type.LABEL) {
            //check that the label isn't duplicated in the macro body
            if (localLabels.contains(t)) {
                throw new AssemblyException.NameSpaceError("The label \"" +
                        t.contents + "\" is used twice in the macro " +
                        "and so cannot be used here",
                        t);
            }

            //add the label with and without its label char (as a LABEL and VAR)
            localLabels.add(t);
            localLabels.add(new Token(t.filename, Token.Type.VAR,
                    t.lineNumber, t.columnNumber, t.offset,
                    t.contents.substring(0, t.contents.length() - 1),
                    t.isLegal));
        }
    }


    //-------------------------------
    //make a new vector with clones of all the tokens of the macro body,
    //and when there is a parameter, replace it with an argument.
    //The cloning is done so that the original MacroDef body can be left alone
    private List<Token> specializedMacroBody(MacroDef mdef,
                                             HashMap<Token, Token> parameterArgHash) {

        List<Token> specializedMacroBody = new ArrayList<Token>();
        for (Token token : mdef.body) {
            if (parameterArgHash.containsKey(token)) {
                //if this token is a parameter in the body, replace it with an
                //argument
                specializedMacroBody.add(parameterArgHash.get(
                        token));
            }
            else {
                //else just put the token into the specialized macro body
                specializedMacroBody.add(token);
            }
        }
        return specializedMacroBody;
    }

    //-------------------------------
    //match up the parameter with its corresponding argument so it may be
    //referenced from the body
    private HashMap<Token, Token> getParameterArgHash(List<Token> macroArgs,
                                                      MacroDef mdef) {
        HashMap<Token, Token> parameterArgHash = new HashMap<Token, Token>();
        for (int i = 0; i < mdef.parameters.size(); i++) {
            parameterArgHash.put(mdef.parameters.get(i),
                    macroArgs.get(i));
        }
        return parameterArgHash;
    }

    //-------------------------------
    private MacroDef getMdef(Token macroDefKey, List<Token> macroArgs) throws
            AssemblyException.UndefinedOperandError {
        MacroDef mdef = macros.get(macroDefKey);
        assert mdef != null : "null macro def in getMdef";

        if (mdef.parameters.size() != macroArgs.size()) {
            throw new AssemblyException.UndefinedOperandError("Wrong number of " +
                    "arguments " +
                    "in macro call \"" + macroDefKey.contents + "\"." +
                    "\n       Supposed to be " + mdef.parameters.size() +
                    " argument(s), not " + macroArgs.size(),
                    macroDefKey);
        }
        return mdef;
    }

    //-------------------------------
    //	make a new vector with clones of all the tokens of the macro body,
    //and when there is a parameter, replace it with an argument.
    //The cloning is done so that the original MacroDef body can be left alone
    private List<Token> getMacroArgs() throws AssemblyException.SyntaxError,
            InvalidTokenException {
        List<Token> macroArgs = new ArrayList<Token>();
        while (token.type == Token.Type.VAR || token.type == Token.Type.CONSTANT) {
            macroArgs.add(token); //gather all macro arguments into a vector
            advance();

            if (token.type == Token.Type.COMMA) {
                advance();
            }
        }
        return macroArgs;
    }

    //-------------------------------
    //checks for errors such as:
    //	one EQU 1
    //	one EQU 8
    private boolean checkEquNotAlreadyUsed(Token token) throws AssemblyException
            .NameSpaceError {
        boolean isValid = true;
        Set<Token> equSet = equs.keySet();
        /*while (equSet.hasMoreElements()) {
            Token currentKey = (Token) equSet.nextElement();
            if (token.equals(currentKey)) {
                isValid = false;
                throw new AssemblyException("Error: the EQU key \"" +
                        currentKey.contents + "\" has already been defined",
                        token);
            }
        }*/
        for (Token currentKey : equSet) {
            if (token.equals(currentKey)) {
                throw new AssemblyException.NameSpaceError("The EQU key \"" +
                        currentKey.contents + "\" has already been defined",
                        token);
            }
        }

        return isValid;
    }

    //-------------------------------
    //looks in the EQU HashMap to see if this EQU Variable (the right side)
    //is defined as a constant somewhere already, either in local EQU's or
    //in the global EQU's.  It throws an AssemblyException if it hasn't been
    //previously defined.
    //checks for errors such as:
    //	one EQU two (where two is not defined anywhere)
    //or:
    //	one EQU two
    //	two EQU 2
    //
    // --> must be the following to be correct:
    //	two EQU 2
    //	one EQU two
    private void checkEquDefined(Token token) throws AssemblyException
            .UndefinedOperandError {
        //check the global EQU's
        for (EQU globalEqu : machine.getEQUs()) {
            String key = globalEqu.getName();
            if (key.equals(token.contents)) {
                return;
            }
        }

        //check the local EQU's
        Set<Token> equSet = equs.keySet();
        for (Token key : equSet) {
            if (token.equals(key)) {
                return;
            }
        }

        //if we haven't returned, then an error has occurred.
        throw new AssemblyException.UndefinedOperandError("EQU value \"" + token
                .contents +
                "\" is not previously defined",
                token);
    }


}  //end of class Parser

