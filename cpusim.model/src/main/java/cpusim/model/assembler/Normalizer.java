///////////////////////////////////////////////////////////////////////////////
// File:    	Normalizer.java
// Type:    	java application file
// Author:		Raymond H. Mazza III and Dale Skrien
// Project: 	CPU Sim 3.0
// Date:    	June, 2000
//
// Description:
//		this class replaces all variables with their corresponding constants.
//		Label references are replaced with their cell address, and EQUs are
//		replaced with their correct values.


///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

package cpusim.model.assembler;


///////////////////////////////////////////////////////////////////////////////
// the libraries we need to import

import cpusim.model.Field;
import cpusim.model.FieldValue;
import cpusim.model.Machine;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;

///////////////////////////////////////////////////////////////////////////////
// the Normalizer class

public class Normalizer
{
    private HashMap<Token, Token> labelHash; //key = label, value = memory address
    private Machine machine;  //has global EQU's to handle


    //-------------------------------
    // constructor
    public Normalizer(Machine machine)
    {
        this.machine = machine;
        labelHash = new HashMap<>();

    }

    //-------------------------------
    // returns a list of the parsedInstructions in normalized form.
    // "Normalized form" means all EQUs have been replaced with their values,
    //    and all variables have been replaced with their values.  In other
    //    words, all operands have been replaced by their numerical value.
    // It assumes the instructions begin at the given starting address.
    public List<InstructionCall> normalize(
            List<InstructionCall> parsedInstructions,
            HashMap<Token, Token> parsedEqus, int startingAddress) throws AssemblyException
    {
    	    	
        this.replaceEqusWithValues(parsedInstructions, parsedEqus);
        this.processLabels(parsedInstructions, startingAddress);
        this.replaceVars(parsedInstructions, startingAddress);

        return parsedInstructions;
    }

    //-------------------------------
    //for debugging purposes only
//    public HashMap getLabelHash()
//    {
//        return this.labelHash;
//    }


    //***************************************
    //EQU Replacer methods
    //***************************************

    //-------------------------------

    private void replaceEqusWithValues(
            List<InstructionCall> instructions,
            HashMap<Token, Token> equs)
    {
        //first add in the global EQU's if there is not already a local EQU
        //of the same name
        ObservableList<EQU> globalEqus = machine.getEQUs();
        for (EQU globalEqu : globalEqus) {
            String key = globalEqu.getName();
            long value = globalEqu.getValue();
            Token tokenKey = new Token(null, Token.Type.VAR, -1, -1, -1, key, true);
            Token tokenValue = new Token(null, Token.Type.CONSTANT, -1, -1, -1,
                    value + "", true);
            equs.putIfAbsent(tokenKey, tokenValue);
        }

        //now replace any EQU values in the EQU hashtable that might actually
        //be EQUs of something else
        fillInEqus(equs);

        //now make replacements in the operands of each instruction
        fillInInstructions(equs, instructions);
    }

    //-------------------------------
    // If a value is another key, replace that key with its value.
    // The parser ensures that the second key has already previously
    // been defined, so there is no need to error-check here.
    private void fillInEqus(HashMap<Token, Token> equs)
    {
        for (Token key : equs.keySet()) {
            Token value = equs.get(key);
            while (value.type == Token.Type.VAR) {
                //if it is not a constant, then it is the key to another equ,
                //so use it as the key to get that value, and continue until
                //the value is a constant.
                //the parser checks to be sure that the value is defined
                //before hand
                value = equs.get(value);
            }
            
            equs.put(key, value);
        }
    }

    //-------------------------------
    //go through all the operands in the instructions vector and make the
    //necessary replacements of the EQUs with their values.
    private void fillInInstructions(HashMap equs,
                                    List<InstructionCall> instructions)
    {
        for (InstructionCall instrCall : instructions) {
            List<Token> operands = instrCall.operands;

            for (int i = 0; i < operands.size(); i++) {
                Token op = operands.get(i);
                if (op.type == Token.Type.VAR && equs.get(op) != null) {
                    //if the token is a variable and a valid key, replace it
                    Token tkn = (Token) (equs.get(op));
                    //make sure to keep the variable's line and column number
                    //the same
                    operands.set(i, new Token(op.filename,
                            tkn.type, op.lineNumber, op.columnNumber,
                            op.offset, tkn.contents, tkn.isLegal));
//                    System.out.print(op+" : "+operands.get(i)+"               ");
                }
            }
        }
    }

    //***************************************
    //Label Processor/Replacer methods
    //***************************************

    //-------------------------------
    //cycles through the specified list of instructions and assigns
    //a memory address to every label.  The address will be the sum of the
    //lengths of the instructions up to that point divided by the
    // memory cell size and rounded down.
    // It assumes that the instructions will be
    //loaded into memory starting at the given address

    private void processLabels(List<InstructionCall> instructions,
                               int startingAddress) throws AssemblyException.NameSpaceError, AssemblyException.InvalidOperandError
    {
        checkState(machine.getCodeStore().isPresent());
        labelHash.clear();
        int cellSize = machine.getCodeStore().get().getCellSize();
        int currAddressInBits = startingAddress*cellSize;
        //location in the program in bits from the start of the code store RAM

        for (InstructionCall instrCall : instructions) {
            for (Token label : instrCall.labels) {
            	
                //can't really say what line they were on here,
                //but there is no need to
                Token labelAddress = new Token(null, Token.Type.CONSTANT, -11, 0, 0,
                        currAddressInBits/cellSize + "", true);
                //we must remove the colon from the end of its contents
                //because it isn't referenced with the colon in the code.
                //We fromRootController a clone since all tokens are final.
                Token clone = new Token(label.filename,
                        label.type,
                        label.lineNumber,
                        label.columnNumber,
                        label.offset,
                        label.contents.substring(0,
                                 label.contents.length() - 1),
                        label.isLegal);
                //System.out.println(clone + " : "+labelAddress+"\n");
                //check that the label hasn't already been used elsewhere
                if (labelHash.get(clone) != null) {
                    throw new AssemblyException.NameSpaceError("The label \"" +
                            clone.contents +
                            "\" is used earlier in the program " +
                            "and so cannot be used here",
                            clone);
                }
                //put all the labels into the hashtable
                labelHash.put(clone, labelAddress);
            }

            currAddressInBits += instrCall.getLength(); // in bits
        }
    }

    //***************************************
    //Var Processor/Replacer methods
    //***************************************
    
    //-------------------------------
    //This method takes a Vector of instructions and a Hashtable of Labels and
    // memory addresses for the instruction calls following those labels.  At this
    // point, the only variables left in the instruction Vector should be
    // (a) labels and (b) field value names, if the fields have restricted values.
    // This method uses the Hashtable to find and replace the labels with the
    // memory address they refer to and replaces the field value names with the
    // field values.
    // When this method returns, the InstructionCalls in the instructions list
    // will have operands consisting only of tokens representing integer constants.
    
    
    private void replaceVars(
            List<InstructionCall> instructions, int startingAddress) throws NumberFormatException, AssemblyException.InvalidOperandError
    {
        checkState(machine.getCodeStore().isPresent());
        int cellSize = machine.getCodeStore().get().getCellSize();
        int currAddressInBits = startingAddress * cellSize;
        for (InstructionCall instrCall : instructions) {
            if (instrCall.machineInstruction == null) {
                //it's a .data or .ascii pseudoinstruction
                List<Token> operands = instrCall.operands;
                for (int i = 0; i < operands.size(); i++) {
                    Token op = operands.get(i);
                    if (op.type == Token.Type.VAR && labelHash.get(op) != null) {
                        //if the token is a variable and a valid key, replace it
                        Token tkn = labelHash.get(op);
                        //make sure to keep the variable's line and column number
                        //the same
                        
                        operands.set(i, new Token(op.filename,
                                tkn.type, op.lineNumber, op.columnNumber,
                                op.offset, tkn.contents, tkn.isLegal));
                    }
//                    System.out.print("PI: "+op+" => "+operands.get(i)+"\n");
                    

                }
            } else { //it's a regular instruction
                List<Token> operands = instrCall.operands;
                List<Field> fields = instrCall.machineInstruction.getInstructionFields();
                int opIndex = 0;

                for (int i = 1; i < fields.size(); i++) { //i=0 <==> opcode field
                    Field field = fields.get(i);
                    //System.out.print(field+"; \n");
                    if (field.getNumBits() == 0)
                        continue;
                    Token op = operands.get(opIndex);
                    if (op.type == Token.Type.VAR && labelHash.get(op) != null) {
                        //if the token is a variable and the name of a label,
                        //replace the contents of the op token with the
                        //value of the label or the value of the label minus
                        //the current address, if pcRelative addressing is used.
                        //Since tokens are final, we need to fromRootController a whole new token.
                        //If the token is not a valid label, the token is not
                        //replaced and the code generator will catch the error.
                        Token opValue = labelHash.get(op);
                        String newContents = opValue.contents;
                        //System.out.print("(MI) "+op+": "+newContents);
                        if (field.getRelativity() ==
                                Field.Relativity.pcRelativePreIncr) {
                            newContents = "" + (Integer.parseInt(newContents) -
                                    currAddressInBits/cellSize);
                            
                        } else if (field.getRelativity() ==
                                Field.Relativity.pcRelativePostIncr) {
                            newContents = "" + (Integer.parseInt(newContents) -
                                    (currAddressInBits +
                                            instrCall.getLength())/cellSize);
                        }
                        //System.out.print(" => "+newContents);

                        //make newContents at least as long as op's contents
                        //for positioning of highlighting if an error occurs
                        for (int j = newContents.length(); j < op.contents.length();
                                j++)
                            newContents = ' ' + newContents;
                        Token newOpToken = new Token(
                                op.filename, op.type,
                                op.lineNumber, op.columnNumber, op.offset,
                                newContents, true);
                        operands.set(opIndex, newOpToken);
                    } else if (op.type == Token.Type.VAR &&
                            field.getValue(op.contents) != null) {
                        //the var is one of the acceptable values for the field.
                        
                        Optional<FieldValue> value = field.getValue(op.contents);
                        if (!value.isPresent()) {
                           throw new IllegalStateException(op.contents + " does not exist in Field");
                        }
                        
                        String newContents = Long.toString(value.get().getValue());
                        //make newContents at least as long as op's contents
                        //for positioning of highlighting if an error occurs
                        for (int j = newContents.length(); j < op.contents.length(); j++)
                            newContents = '0' + newContents;
                        Token newOpToken = new Token(
                                op.filename, op.type,
                                op.lineNumber, op.columnNumber, op.offset,
                                newContents, true);
                        operands.set(opIndex, newOpToken);
                    } else if (op.type == Token.Type.VAR &&
                            field.getValues().size() != 0) {
                        //the var is not one of the acceptable values for the field.
                        throw new AssemblyException.InvalidOperandError("Error: The value \"" +
                                op.contents + "\" is not one of the acceptable " +
                                "field values", op);
                    }
                    opIndex++;
                }
            }
            currAddressInBits += instrCall.getLength();
                                       // changed getLength to return # bits
        }
    }

}  //end of class Normalizer
       




