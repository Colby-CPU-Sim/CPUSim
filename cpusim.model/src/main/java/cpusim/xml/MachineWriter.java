///////////////////////////////////////////////////////////////////////////////
// File:    	MachineWriter.java
// Author:		Dale Skrien
// Project: 	CPU Sim
// Date:    	June, 2001
//
// Last Modified: 6/3/13
//
// Description:
//   This file contains the class for writing Machines created using
//   CPU Sim to a file in XML format, using the CPUSimMachine.dtd
//
// Things to do:
//  1.


///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

/**
 * Jinghui Yu, Ben Borchard and Michael Goldenberg made the following modifications to
 * this class on 11/11/13:
 *
 * 1.) Modified the DTD string so it write the initial value and read-only properties
 * of register to machine.
 *
 */

package cpusim.xml;


import cpusim.model.Field;
import cpusim.model.Machine;
import cpusim.model.MachineInstruction;
import cpusim.model.microinstruction.Microinstruction;
import cpusim.model.Module;
import cpusim.model.assembler.EQU;
import cpusim.model.assembler.PunctChar;
import cpusim.model.iochannel.FileChannel;
import cpusim.model.iochannel.IOChannel;
import cpusim.model.microinstruction.Comment;
import cpusim.model.microinstruction.IO;
import cpusim.model.module.RegisterRAMPair;
import javafx.collections.ObservableList;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import cpusim.gui.*;
//import cpusim.scrollabledesktop.BaseInternalFrame;

///////////////////////////////////////////////////////////////////////////////
// the MachineWriter class

public class MachineWriter
{
    private String[] moduleHeaders, microHeaders;
    private String ls = System.getProperty("line.separator");
    private String internalDTD = "<!DOCTYPE Machine [" + ls +
            "<!ELEMENT Machine (PunctChar*," +
            " Field*, FileChannel*, Register*, RegisterArray*," +
            " ConditionBit*, RAM*, Set*, Test*, Increment*, Shift*, Logical*," +
            " Arithmetic*, Branch*, TransferRtoR*, TransferRtoA*, TransferAtoR*," +
            " Decode*, SetCondBit*, IO*, MemoryAccess*, End, Comment*, EQU*, FetchSequence," +
            " MachineInstruction*, HighlightingInfo?, LoadingInfo?, IndexingInfo?," +
            " ProgramCounterInfo?, ModuleWindowsInfo?) >" + ls +
            "<!ATTLIST Machine name CDATA \"unnamed\">" + ls +
            "<!ELEMENT PunctChar EMPTY>" + ls +
            "<!ATTLIST PunctChar char CDATA #REQUIRED use " +
            " (symbol|token|label|comment|pseudo|illegal) #REQUIRED>" + ls +
            "<!ELEMENT Field (FieldValue*)>" + ls +
            "<!ATTLIST Field name CDATA #REQUIRED type " +
            " (required|optional|ignored) #REQUIRED numBits CDATA #REQUIRED" +
            " relativity (absolute|pcRelativePreIncr|pcRelativePostIncr)" +
            " #REQUIRED defaultValue CDATA #REQUIRED signed (true|false)" +
            " #REQUIRED id ID #REQUIRED>" + ls +
            "<!ELEMENT FieldValue EMPTY>" + ls +
            "<!ATTLIST FieldValue name CDATA #REQUIRED value CDATA #REQUIRED>" + ls +
            "<!ELEMENT FileChannel EMPTY>" + ls +
            "<!ATTLIST FileChannel file CDATA #REQUIRED id CDATA #REQUIRED>" + ls +
            "<!ELEMENT Register EMPTY>" + ls +
            "<!ATTLIST Register name CDATA #REQUIRED width CDATA #REQUIRED initialValue" +
            " CDATA #REQUIRED readOnly (true|false) \"false\" id ID" +
            " #REQUIRED>" + ls +
            "<!ELEMENT RegisterArray (Register+)>" + ls +
            "<!ATTLIST RegisterArray name CDATA #REQUIRED width CDATA #REQUIRED length" +
            " CDATA #REQUIRED id ID #REQUIRED>" + ls +
            "<!ELEMENT ConditionBit EMPTY>" + ls +
            "<!ATTLIST ConditionBit name CDATA #REQUIRED bit CDATA #REQUIRED register" +
            " IDREF #REQUIRED halt (true|false) \"false\" id ID #REQUIRED>" + ls +
            "<!ELEMENT RAM EMPTY>" + ls +
            "<!ATTLIST RAM name CDATA #REQUIRED length CDATA #REQUIRED id ID" +
            " #REQUIRED cellSize CDATA \"8\">" + ls +
            "<!ELEMENT Increment EMPTY>" + ls +
            "<!ATTLIST Increment name CDATA #REQUIRED register IDREF #REQUIRED" +
            " overflowBit IDREF #IMPLIED delta CDATA #REQUIRED id ID #REQUIRED>" + ls +
            "<!ELEMENT Arithmetic EMPTY>" + ls +
            "<!ATTLIST Arithmetic name CDATA #REQUIRED type (ADD|SUBTRACT|MULTIPLY|" +
            "DIVIDE) #REQUIRED source1 IDREF #REQUIRED source2 IDREF #REQUIRED" +
            " destination IDREF #REQUIRED overflowBit IDREF #IMPLIED  carryBit IDREF" +
            " #IMPLIED  id ID #REQUIRED>" + ls +
            "<!ELEMENT TransferRtoR EMPTY>" + ls +
            "<!ATTLIST TransferRtoR name CDATA #REQUIRED source IDREF #REQUIRED" +
            " srcStartBit CDATA #REQUIRED dest IDREF #REQUIRED destStartBit CDATA" +
            " #REQUIRED numBits CDATA #REQUIRED id ID #REQUIRED>" + ls +
            "<!ELEMENT TransferRtoA EMPTY>" + ls +
            "<!ATTLIST TransferRtoA name CDATA #REQUIRED source IDREF #REQUIRED" +
            " srcStartBit CDATA #REQUIRED dest IDREF #REQUIRED destStartBit" +
            " CDATA #REQUIRED numBits CDATA #REQUIRED index IDREF #REQUIRED" +
            " indexStart CDATA #IMPLIED indexNumBits CDATA #IMPLIED id ID" +
            " #REQUIRED>" + ls +
            "<!ELEMENT TransferAtoR EMPTY>" + ls +
            "<!ATTLIST TransferAtoR name CDATA #REQUIRED source IDREF #REQUIRED" +
            " srcStartBit CDATA #REQUIRED dest IDREF #REQUIRED destStartBit" +
            " CDATA #REQUIRED numBits CDATA #REQUIRED index IDREF #REQUIRED" +
            " indexStart CDATA #IMPLIED indexNumBits CDATA #IMPLIED id ID" +
            " #REQUIRED>" + ls +
            "<!ELEMENT Shift EMPTY>" + ls +
            "<!ATTLIST Shift name CDATA #REQUIRED source IDREF #REQUIRED destination" +
            " IDREF #REQUIRED type (logical | arithmetic | cyclic) #REQUIRED" +
            " direction (right | left) #REQUIRED distance CDATA #REQUIRED id ID" +
            " #REQUIRED>" + ls +
            "<!ELEMENT Branch EMPTY>" + ls +
            "<!ATTLIST Branch name CDATA #REQUIRED amount CDATA #REQUIRED id ID" +
            " #REQUIRED>" + ls +
            "<!ELEMENT Logical EMPTY>" + ls +
            "<!ATTLIST Logical name CDATA #REQUIRED source1 IDREF #REQUIRED source2" +
            " IDREF #REQUIRED destination IDREF #REQUIRED type (AND | OR | NAND |" +
            " NOR | XOR | NOT) #REQUIRED id ID #REQUIRED>" + ls +
            "<!ELEMENT Set EMPTY>" + ls +
            "<!ATTLIST Set name CDATA #REQUIRED register IDREF #REQUIRED start" +
            " CDATA #REQUIRED numBits CDATA #REQUIRED value CDATA #REQUIRED id ID" +
            " #REQUIRED>" + ls +
            "<!ELEMENT Test EMPTY >" + ls +
            "<!ATTLIST Test name CDATA #REQUIRED" +
            " register IDREF #REQUIRED start CDATA #REQUIRED numBits" +
            " CDATA #REQUIRED comparison (EQ | NE | LT | GT | LE | GE ) #REQUIRED" +
            " value CDATA #REQUIRED omission CDATA #REQUIRED id ID #REQUIRED>" + ls +
            "<!ELEMENT Decode EMPTY >" + ls +
            "<!ATTLIST Decode name CDATA #REQUIRED ir IDREF #REQUIRED id ID" +
            " #REQUIRED>" + ls +
            "<!ELEMENT IO EMPTY >" + ls +
            "<!ATTLIST IO name CDATA #REQUIRED direction (input | output) #REQUIRED" +
            " type (integer | ascii | unicode) #REQUIRED buffer IDREF #REQUIRED" +
            " connection CDATA #IMPLIED id ID #REQUIRED>" + ls +
            "<!ELEMENT MemoryAccess EMPTY >" + ls +
            "<!ATTLIST MemoryAccess name CDATA #REQUIRED direction (read | write )" +
            " #REQUIRED memory IDREF #REQUIRED data IDREF #REQUIRED address IDREF" +
            " #REQUIRED id ID #REQUIRED>" + ls +
            "<!ELEMENT SetCondBit EMPTY >" + ls +
            "<!ATTLIST SetCondBit name CDATA #REQUIRED bit IDREF #REQUIRED value" +
            " (0 | 1) #REQUIRED id ID #REQUIRED>" + ls +
            "<!ELEMENT End EMPTY>" + ls +
            "<!ATTLIST End id ID #REQUIRED>" + ls +
            "<!ELEMENT Comment EMPTY>" + ls +
            "<!ATTLIST Comment name CDATA #REQUIRED id ID #REQUIRED>" + ls +
            "<!ELEMENT Microinstruction EMPTY>" + ls +
            "<!ATTLIST Microinstruction microRef IDREF #REQUIRED>" + ls +
//            "<!ELEMENT FieldLength EMPTY>" + ls +
//            "<!ATTLIST FieldLength length CDATA #REQUIRED>" + ls +
            "<!ELEMENT MachineInstruction (Microinstruction*)>" + ls +
            "<!ATTLIST MachineInstruction name CDATA #REQUIRED opcode CDATA" +
            " #REQUIRED instructionFormat CDATA #REQUIRED assemblyFormat CDATA"+
            " #REQUIRED instructionColors CDATA #REQUIRED assemblyColors CDATA"
            + " #REQUIRED>" + ls +
//            the next 2 lines aren't needed since the fields list is
//            constructed from the instruction's format
//            "<!ELEMENT FieldRef EMPTY>" + ls +
//            "<!ATTLIST FieldRef ref IDREF #REQUIRED>" + ls +
            "<!ELEMENT FetchSequence (Microinstruction*) >" + ls +
            "<!ELEMENT EQU EMPTY>" + ls +
            "<!ATTLIST EQU name CDATA #REQUIRED value CDATA #REQUIRED>" + ls +
            "<!ELEMENT HighlightingInfo (RegisterRAMPair*)>" + ls +
            "<!ELEMENT RegisterRAMPair EMPTY>" + ls +
            "<!ATTLIST RegisterRAMPair register IDREF #REQUIRED ram IDREF #REQUIRED" +
            " dynamic (true|false) #REQUIRED>" + ls +
            "<!ELEMENT LoadingInfo EMPTY>" + ls +
            "<!ATTLIST LoadingInfo ram IDREF #IMPLIED startingAddress CDATA \"0\">" + ls +
            "<!ELEMENT IndexingInfo EMPTY>" + ls +
            "<!ATTLIST IndexingInfo indexFromRight CDATA \"false\">" + ls +
            "<!ELEMENT ProgramCounterInfo EMPTY>" + ls +
            "<!ATTLIST ProgramCounterInfo programCounter IDREF #REQUIRED>" + ls +
            "<!ELEMENT ModuleWindowsInfo ((RegisterWindowInfo |" +
            " RegisterArrayWindowInfo | RAMWindowInfo)*) >" + ls +
            "<!ELEMENT RegisterWindowInfo EMPTY>" + ls +
            "<!ATTLIST RegisterWindowInfo top CDATA \"50\" left CDATA \"50\" width" +
            " CDATA \"300\" height CDATA \"150\" base (Decimal|Binary|Hexadecimal|Ascii|UnsignedDec|Unicode) " +
            " \"Decimal\">" + ls +
            "<!ELEMENT RegisterArrayWindowInfo EMPTY>" + ls +
            "<!ATTLIST RegisterArrayWindowInfo array IDREF #REQUIRED top CDATA" +
            " \"50\" left CDATA \"50\" width CDATA \"300\" height CDATA \"150\"" +
            " base (Decimal|Binary|Hexadecimal|Ascii|UnsignedDec|Unicode) \"Decimal\">" + ls +
            "<!ELEMENT RAMWindowInfo EMPTY>" + ls +
            "<!ATTLIST RAMWindowInfo ram IDREF #REQUIRED cellSize CDATA \"1\" top" +
            " CDATA \"50\" left CDATA \"50\" width CDATA \"450\" height CDATA" +
            " \"450\" contentsbase (Decimal|Binary|Hexadecimal|Ascii|UnsignedDec|Unicode) \"Decimal\"" +
            " addressbase (Decimal|Binary|Hexadecimal) \"Decimal\">" + ls + "]>";


    //----------------------
    // constructor
    public MachineWriter()
    {
        moduleHeaders = new String[]{
            "<!--............. registers .....................-->",
            "<!--............. register arrays ...............-->",
            "<!--............. condition bits ................-->",
            "<!--............. rams ..........................-->"
        };
        microHeaders = new String[]{
            "<!--............. set ...........................-->",
            "<!--............. test ..........................-->",
            "<!--............. increment .....................-->",
            "<!--............. shift .........................-->",
            "<!--............. logical .......................-->",
            "<!--............. arithmetic ....................-->",
            "<!--............. branch ........................-->",
            "<!--............. transferRtoR ..................-->",
            "<!--............. transferRtoA ..................-->",
            "<!--............. transferAtoR ..................-->",
            "<!--............. decode ........................-->",
            "<!--............. set condition bit .............-->",
            "<!--............. io ............................-->",
            "<!--............. memory access .................-->"
        };
    }

    //----------------------
    // sends the XML document containing the machine description to
    // the PrintWriter.
    // The name is the new name of the machine.
    // The commentPseudoLabelChars is a string of length 3 with the
    //      3 assembly language options
    // The rrPairs are the register/ram pairs for highlighting
    // The module windows are the open windows for registers and rams
    public void writeMachine(Machine machine, String name, ObservableList<RegisterRAMPair> rrPairs, PrintWriter out)
    {
        List<List<? extends Module<?>>> moduleVectors = machine.getAllModules();
        List<List<? extends Microinstruction>> microVectors = machine.getAllMicros();
        
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println(internalDTD);
        out.println();
        out.println("<Machine name=\"" + HtmlEncoder.sEncode(name) + "\" >");

        //print the punctuation characters and their uses
        out.println("\t<!--............. Punctuation Options .............-->");
        List<PunctChar> chars = machine.getPunctChars();
        if( chars.size() == 0 )
            out.println("\t<!-- none -->");
        else
            for(PunctChar c : chars)
                out.println(c.getXMLDescription("\t"));

        //print the fields for use by machine instructions
        out.println();
        out.println("\t<!--......... machine instruction fields ............-->");
        List<Field> fields = machine.getFields();
        if (fields.size() == 0)
            out.println("\t<!-- none -->");
        else
            for (Field field : fields) {
                out.println(field.getXMLDescription("\t"));
            }

        //get the file channels from the IO micros and print them out
        Set<FileChannel> fileChannelSet = new HashSet<>();
        out.println();
        out.println("\t<!--............. FileChannels .................-->");
        ObservableList<IO> ios = machine.getMicros(IO.class);
        for (IO io : ios) {
            IOChannel channel = io.getConnection();
            if (channel instanceof FileChannel)
                    fileChannelSet.add((FileChannel) channel);
        }
        if (fileChannelSet.size() == 0)
            out.println("\t<!-- none -->");
        else
        {
            for (FileChannel channel : fileChannelSet) {
                out.println("\t<FileChannel file=\"" +
                        HtmlEncoder.sEncode(channel.toString()) +
                        "\" id=\"" + HtmlEncoder.sEncode(channel.getID()) + "\" />");
            }
        }

        //print the modules
        for (int i = 0; i < moduleVectors.size(); i++) {
            out.println();
            out.println("\t" + moduleHeaders[i]);
            if (moduleVectors.get(i).size() == 0)
                out.println("\t<!-- none -->");
            else
                for (Module<?> m : moduleVectors.get(i)) {
                	out.println("\t" + m.getXMLDescription("\t\t"));
                }
        }

        //print the micros except for End
        for (int i = 0; i < microVectors.size(); i++) {
            out.println();
            out.println("\t" + microHeaders[i]);
            if (microVectors.get(i).size() == 0)
                out.println("\t<!-- none -->");
            else
            	for (Microinstruction m : microVectors.get(i)) {
                	out.println("\t" + m.getXMLDescription("\t\t"));
                }
        }

        //print the End micro
        out.println();
        out.println("\t<!--............. end ...........................-->");
        out.println("\t<End id=\"" + machine.getEnd().getID() + "\" />");

        //print the Comment micros
        out.println();
        out.println("\t<!--............. comment ...........................-->");
        List<Comment> comments = machine.getCommentMicros();
        if (comments.size() == 0)
            out.println("\t<!-- none -->");
        else
            for( Comment comment : comments)
                out.println("\t" + comment.getXMLDescription("\t\t"));

        //print the global EQUs
        out.println();
        out.println("\t<!--............. global equs ..................-->");

        ObservableList<EQU> EQUs = machine.getEQUs();
        if (EQUs.size() == 0)
            out.println("\t<!-- none -->");
        else
            for (int j = 0; j < EQUs.size(); j++)
                out.println("\t" + ((EQU)
                        EQUs.get(j)).getXMLDescription("\t\t"));

        //print the fetch sequence
        out.println();
        out.println("\t<!--............. fetch sequence ................-->");
        out.println("\t<FetchSequence>");
        MachineInstruction fetchSequence = machine.getFetchSequence();
        for (int i = 0; i < fetchSequence.getMicros().size(); i++) {
            out.println("\t\t<Microinstruction microRef=\"" +
                    fetchSequence.getMicros().get(i).getID() + "\" />");
        }
        out.println("\t</FetchSequence>");

        //print the machine instructions
        out.println();
        out.println("\t<!--............. machine instructions ..........-->");
        List<MachineInstruction> instructions = machine.getInstructions();
        if (instructions.size() == 0)
            out.println("\t<!-- none -->");
        else
            for (MachineInstruction instruction : instructions) {
                out.println();
                out.println(instruction.getXMLDescription("\t"));
            }

        //print the highlighting info
        out.println();
        out.println("\t<!--............. highlighting info .............-->");
        out.println("\t<HighlightingInfo>");
        for (RegisterRAMPair rrPair : rrPairs) {
            out.println("\t\t" + rrPair.getXMLDescription());
        }
        out.println("\t</HighlightingInfo>");

        //print the loading info
        out.println();
        out.println("\t<!--............. loading info ..................-->");
        if (machine.getCodeStore() != null) {
            out.println("\t<LoadingInfo ram=\"" +
                    machine.getCodeStore().getID() +
                    "\" startingAddress=\"" +
                    machine.getStartingAddressForLoading() + "\" />");
        }

        //print the indexing information
        out.println();
        out.println("\t<!--............. indexing info ............-->");
        out.println("\t<IndexingInfo indexFromRight=\"" +
                    machine.getIndexFromRight() +
                    "\" />");

        //print the program counter info
        out.println();
        out.println("\t<!--............. program counter info ..................-->");
        if (machine.getProgramCounter() != Machine.PLACE_HOLDER_REGISTER) {
            out.println("\t<ProgramCounterInfo programCounter=\"" +
                    machine.getProgramCounter().getID() + "\" />");
        }

        out.println();
        out.println("</Machine>");
        out.close();
    }


}