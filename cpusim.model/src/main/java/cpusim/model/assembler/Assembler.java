///////////////////////////////////////////////////////////////////////////////
// File:    	Assembler.java
// Type:    	java application file
// Author:		Raymond H. Mazza III and Dale Skrien
// Project: 	CPU Sim 3.0
// Date:    	June, 2000
//
// Description:
//   This file contains the code for the Assembler, which takes a text file
//   and constructs an executable file from it.


///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

package cpusim.model.assembler;


///////////////////////////////////////////////////////////////////////////////
// the packages we need to import

import cpusim.model.Machine;
import cpusim.model.assembler.AssemblyException.InvalidOperandError;

import java.util.List;
import java.util.ArrayList;

///////////////////////////////////////////////////////////////////////////////
// the Assembler class

public class Assembler
{
    private Scanner scanner; 			//scans & creates tokens for the parser
    private Parser parser;			//packages instructions
    private Normalizer normalizer;			//replaces variables with constants
    private CodeGenerator generator;	//generates numeric code

    private List<AssembledInstructionCall> assembledInstructions;
    //filled in by the assemble() method

    //-------------------------------
    // constructor
    public Assembler(Machine machine)
    {
        scanner = new Scanner(machine);
        parser = new Parser(scanner, machine);
        normalizer = new Normalizer(machine);
        generator = new CodeGenerator(machine);
        assembledInstructions = new ArrayList<>();
        // in case the user calls getAssembledInstructions before calling assemble
    }

    //-------------------------------
    // getAssembledInstructions:
    // returns the assembled instructions, that is, a Vector
    // of AssembledInstructionCall objects, or null
    // if assemble hasn't been called or if an error occurred.
    public List<AssembledInstructionCall> getAssembledInstructions()
    {
        return assembledInstructions;
    }

    //-------------------------------
    // assemble:  assembles the code found in the text file with
    // full pathname of progamFileName.  It assumes that the
    // starting address of the first assembled instruction will be
    // the given address.  If an error occurs during
    // assembly, it throws an AssemblyException and returns false.
    // If all goes well, it puts the assembled instructions into
    // the instance variable assembledInstructions and returns true.
    public void assemble(String programFileName, int startingAddress)
            throws AssemblyException
    {
        //set up the scanner and get the first char
        scanner.startScanning(programFileName);
        //set up the parser and grab the first token
        parser.initialize();
        //parse the whole programFileName
        parser.parse();
        
        /*for (InstructionCall ic : parser.getInstructions()){
        	/*
        	System.out.println(ic.getLength());
        	System.out.println(ic.getOpcode());
        	System.out.println(ic.labels);
        	System.out.println("\n\n");
		  }	*/

        
        List<InstructionCall> instructionsWithNoVars =
                normalizer.normalize(parser.getInstructions(), parser.getEqus(),
                        startingAddress);

        assembledInstructions = generator.generateCode(instructionsWithNoVars);
    }

    /**
     * replaces the scanner and parser with new ones that properly scan
     * and parse for the given machine.
     * Used when the machine has been edited in the Preferences dialog.
     * @param machine The Machine whose programs are to be assembled
     */
    public void updateScannerAndParser(Machine machine)
    {
        scanner = new Scanner(machine);
        parser = new Parser(scanner, machine);
    }

} //end of class Assembler
