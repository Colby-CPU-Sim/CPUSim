// File:    	Main.java
// Author:		Dale Skrien
// Project: 	CPU Sim 3.0
// Date created: June, 2000
//
// Description:
//   This file contains the code for the Main class that gets things started.

/**
 * File: DialogChannel
 * Last update: December 2013
 * Authors: Stephen Morse, Ian Tibbits, Terrence Tan
 * Class: CS 361
 * Project 7
 * 
 * modified loadAndRunInCommandLineMode - concrete now buffered channel.
 */

package cpusim;

import cpusim.assembler.AssembledInstructionCall;
import cpusim.assembler.Assembler;
import cpusim.assembler.AssemblyException;
import cpusim.assembler.Token;
import cpusim.iochannel.BufferedChannel;
import cpusim.iochannel.CommandLineChannel;
import cpusim.iochannel.FileChannel;
import cpusim.model.Machine;
import cpusim.model.Microinstruction;
import cpusim.model.microinstruction.IO;
import cpusim.util.LoadException;
import cpusim.xml.MachineReader;
import javafx.application.Application;
import javafx.collections.ObservableList;
import org.xml.sax.SAXParseException;
import java.io.File;
import java.util.List;

public class Main {
    
	// Main entry point
    public static void main(String[] argv) {
        // deal with the command line params first
        String machineFileName = "";
        String textFileName = "";
        boolean commandLineMode = false;
        
        int i = 0;
        while(i < argv.length) {
            if(argv[i].equals("-m") && i + 1 < argv.length) {
                machineFileName = argv[i+1];
                i += 2;
            }
            else if (argv[i].equals("-t") && i + 1 < argv.length) {
                textFileName = argv[i+1];
                i += 2;
            }
            else if (argv[i].equals("-c")) {
                commandLineMode = true;
                i++;
            }
            else {
                System.out.println("Bad command line argument: " + argv[i]);
                return;
            }
        }

        // run it via the command line or via the JavaFX GUI
        if( commandLineMode ) {
        	loadAndRunInCommandLineMode(machineFileName, textFileName);
        }
        else {
        	Application.launch(cpusim.GUIMain.class,
                                             machineFileName, textFileName);
        }
    }

    /**
     * runs the given machine with the given program using command line io.
     * The JavaFX gui never appears.
     * @param machineFileName the name of the file containing the machine
     * @param textFileName the name of the file containing the program
     */
    private static void loadAndRunInCommandLineMode(String machineFileName, String
            textFileName) {
    	
    	// Give error when appropriate
        if(machineFileName.equals("") || textFileName.equals("")) {
            System.out.println("If you specify command line mode '-c', " +
                    "you must also specify a machine file with '-m'" +
                    " and a program file with '-t'.");
            return;
        }
        //load the machine in the machine file
        MachineReader reader = new MachineReader();
        File machineFile = new File(machineFileName);
        try {
            reader.parseDataFromFile(machineFile);
        } catch (Exception ex) {
            String errorMessage = "Error when reading the machine file \"" +
                    machineFileName + "\"";
            if (ex instanceof SAXParseException) {
                errorMessage += " at line " +
                        ((SAXParseException) ex).getLineNumber();
            }
            if (ex.getMessage() == null) {
                errorMessage += ".\nThe error type is unknown.";
            }
            else {
                errorMessage += ".\n" + ex.getMessage();
            }
            System.out.println(errorMessage);
            return;
        }
        final Machine machine = reader.getMachine();

        //assemble and load the program in the text file
        File programFile = new File(textFileName);
        Assembler assembler = new Assembler(machine);
        try {
            assembler.assemble(programFile.getAbsolutePath(),
                               machine.getStartingAddressForLoading());
        } catch (AssemblyException exc) {
            //exc.token.row & exc.token.col -->  where the error is
            String errorMessage = exc.getMessage();
            if (exc.token.type == Token.Type.EOF)
                errorMessage += "\n at the end of the file";
            else if (exc.token.columnNumber != -1 || exc.token.lineNumber != -1)
                errorMessage += "\nError is at line "
                        + (exc.token.lineNumber + 1) + " and column "
                        + exc.token.columnNumber;
            if (!exc.token.filename.equals(""))
                errorMessage += "\n       in file " + exc.token.filename;
            System.out.println(errorMessage);
            return;
        }
        List<AssembledInstructionCall> machineInstructions =
                                        assembler.getAssembledInstructions();
        try {
            machine.getCodeStore().loadAssembledInstructions(machineInstructions,
                                        machine.getStartingAddressForLoading());
        } catch (LoadException ex) {
            System.out.println(ex.getMessage());
            return;
        }

        //update all io micros with non-file channels
        //to input/output to the command line
        ObservableList<Microinstruction> ioMicros = machine.getMicros("io");
        BufferedChannel commandLineChannel = new BufferedChannel(new CommandLineChannel());
        for(Microinstruction io : ioMicros) {
            if(! (((IO) io).getConnection() instanceof FileChannel))
                ((IO) io).setConnection(commandLineChannel);
        }

        //run the program
        machine.execute(Machine.RunModes.COMMAND_LINE);
        System.exit(0);
    }
}
