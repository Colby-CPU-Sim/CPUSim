///////////////////////////////////////////////////////////////////////////////
// File:    	MachineHTMLWriter.java
// Author:		Dale Skrien
// Project: 	CPU Sim
// Date:    	June, 2001
//
// Description:
//   This file contains the class for writing Machines created using
//   CPU Sim to a file in HTML format
//
// Things to do:
//  1.

/*
 * Michael Goldenberg, Ben Borchard, and Jinghui Yu made the following changes in 11/11/13
 * 
 * 1.) Modified various aspects of the modules display so that the the initial value 
 * and read only properties of the register will be displayed in the table.  Also so
 * that the all the registers within each register array will be displayed within the register
 * array table
 * 
 */


///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

package cpusim.xml;


///////////////////////////////////////////////////////////////////////////////
// the libraries we need to import

import cpusim.assembler.EQU;
import cpusim.assembler.PunctChar;
import cpusim.model.*;
import javafx.collections.ObservableList;

import java.io.PrintWriter;
import java.util.List;

///////////////////////////////////////////////////////////////////////////////
// the MachineHTMLWriter class

public class MachineHTMLWriter
{
    private final String HEADER_PREFIX =
            "<TR VALIGN=\"middle\"><TD bgcolor=\"#C08060\" COLSPAN=\"";
    private final String HEADER_MIDDLE =
            "\" HEIGHT=\"35\"><FONT " + //
            " SIZE=\"+2\"><B>&nbsp;";
    private final String HEADER_SUFFIX = "</B></font></TD></TR>";
    private final String COL_HEADER_PREFIX = "<TR><TD><B>Name</B></TD><TD><B>";
    private final int[] MODULE_COLUMNS = {4, 4, 4, 3};
    private final int[] MICRO_COLUMNS = {5, 7, 5, 6, 5, 7, 2, 6, 9, 9, 2, 3, 5, 5};

    private String[] moduleHeaders, moduleColumnHeaders,
    microHeaders, microColumnHeaders;

    //----------------------
    // constructor
    public MachineHTMLWriter()
    {
        moduleHeaders = new String[]{
            HEADER_PREFIX + "4" + HEADER_MIDDLE + "Registers" + HEADER_SUFFIX,
            HEADER_PREFIX + "4" + HEADER_MIDDLE + "Register Arrays" + HEADER_SUFFIX,
            HEADER_PREFIX + "4" + HEADER_MIDDLE + "Condition Bits" + HEADER_SUFFIX,
            HEADER_PREFIX + "3" + HEADER_MIDDLE + "RAMs" + HEADER_SUFFIX
        };
        moduleColumnHeaders = new String[]{
            COL_HEADER_PREFIX + "Width</B></TD><TD>" + "<B>Initial Value</B></TD><TD>"
                + "<B>Read Only</B></TD></TR>",
            COL_HEADER_PREFIX + "Length</B></TD><TD>" + "<B>Width</B></TD><TD>" + 
                "<B>Registers</B></TD></TR>",
            COL_HEADER_PREFIX + "Register</B></TD><TD><B>Bit</B></TD>" +
                                 "<TD><B>Halt</B></TD></TR>",
            COL_HEADER_PREFIX + "Length</B></TD><TD><B>Bits per Cell</B></TD></TR>"
        };
        microHeaders = new String[]{
            HEADER_PREFIX + "5" + HEADER_MIDDLE + "Set" + HEADER_SUFFIX,
            HEADER_PREFIX + "7" + HEADER_MIDDLE + "Test" + HEADER_SUFFIX,
            HEADER_PREFIX + "5" + HEADER_MIDDLE + "Increment" + HEADER_SUFFIX,
            HEADER_PREFIX + "6" + HEADER_MIDDLE + "Shift" + HEADER_SUFFIX,
            HEADER_PREFIX + "5" + HEADER_MIDDLE + "Logical" + HEADER_SUFFIX,
            HEADER_PREFIX + "7" + HEADER_MIDDLE + "Arithmetic" + HEADER_SUFFIX,
            HEADER_PREFIX + "2" + HEADER_MIDDLE + "Branch" + HEADER_SUFFIX,
            HEADER_PREFIX + "6" + HEADER_MIDDLE + "TransferRtoR" + HEADER_SUFFIX,
            HEADER_PREFIX + "9" + HEADER_MIDDLE + "TransferRtoA" + HEADER_SUFFIX,
            HEADER_PREFIX + "9" + HEADER_MIDDLE + "TransferAtoR" + HEADER_SUFFIX,
            HEADER_PREFIX + "2" + HEADER_MIDDLE + "Decode" + HEADER_SUFFIX,
            HEADER_PREFIX + "3" + HEADER_MIDDLE + "Set Condition Bit" + HEADER_SUFFIX,
            HEADER_PREFIX + "5" + HEADER_MIDDLE + "IO" + HEADER_SUFFIX,
            HEADER_PREFIX + "5" + HEADER_MIDDLE + "Memory Access" + HEADER_SUFFIX
        };
        microColumnHeaders = new String[]{
            COL_HEADER_PREFIX + "Register</B></TD>" + //set
                "<TD><B>Start Bit</B></TD>" +
                "<TD><B>Number of Bits</B></TD>" +
                "<TD><B>Value</B></TD></TR>",
            COL_HEADER_PREFIX + "Register</B></TD>" + //test
                "<TD><B>Start Bit</B></TD>" +
                "<TD><B>Number of Bits</B></TD>" +
                "<TD><B>Comparison</B></TD>" +
                "<TD><B>Value</B></TD>" +
                "<TD><B>Omission</B></TD></TR>",
            COL_HEADER_PREFIX + "Register</B></TD>" + //increment
                "<TD><B>Overflow Bit</B></TD>" +
                "<TD><B>Carry Bit</B></TD>" +
                "<TD><B>Delta</B></TD></TR>",
            COL_HEADER_PREFIX + "Source</B></TD>" + //shift
                "<TD><B>Destination</B></TD>" +
                "<TD><B>Type</B></TD>" +
                "<TD><B>Direction</B></TD>" +
                "<TD><B>Distance</B></TD></TR>",
            COL_HEADER_PREFIX + "Type</B></TD>" + //logical
                "<TD><B>Source1</B></TD>" +
                "<TD><B>Source2</B></TD>" +
                "<TD><B>Destination</B></TD></TR>",
            COL_HEADER_PREFIX + "Type</B></TD>" + //arithmetic
                "<TD><B>Source1</B></TD>" +
                "<TD><B>Source2</B></TD>" +
                "<TD><B>Destination</B></TD>" +
                "<TD><B>Overflow Bit</B></TD>" +
                "<TD><B>Carry Bit</B></TD></TR>",
            COL_HEADER_PREFIX + "Amount</B></TD></TR>", //branch
            COL_HEADER_PREFIX + "Source</B></TD>" + //transferRtoR
                "<TD><B>Src Start Bit</B></TD>" +
                "<TD><B>Destination</B></TD>" +
                "<TD><B>Dest Start Bit</B></TD>" +
                "<TD><B>Number of Bits</B></TD></TR>",
            COL_HEADER_PREFIX + "Source</B></TD>" + //transferRtoA
                "<TD><B>Src Start Bit</B></TD>" +
                "<TD><B>Destination</B></TD>" +
                "<TD><B>Dest Start Bit</B></TD>" +
                "<TD><B>Number of Bits</B></TD>" +
                "<TD><B>Index</B></TD>" +
                "<TD><B>Index Start Bit</B></TD>" +
                "<TD><B>Index Number of Bits</B></TD></TR>",
            COL_HEADER_PREFIX + "Source</B></TD>" + //transferAtoR
                "<TD><B>Src Start Bit</B></TD>" +
                "<TD><B>Destination</B></TD>" +
                "<TD><B>Dest Start Bit</B></TD>" +
                "<TD><B>Number of Bits</B></TD>" +
                "<TD><B>Index</B></TD>" +
                "<TD><B>Index Start Bit</B></TD>" +
                "<TD><B>Index Number of Bits</B></TD></TR>",
            COL_HEADER_PREFIX + "IR</B></TD></TR>", //decode
            COL_HEADER_PREFIX + "Bit</B></TD>" + //setcondbit
                "<TD><B>Value</B></TD></TR>",
            COL_HEADER_PREFIX + "Direction</B></TD>" + //io
                "<TD><B>Type</B></TD>" +
                "<TD><B>Buffer</B></TD>" +
                "<TD><B>Connection</B></TD></TR>",
            COL_HEADER_PREFIX + "Direction</B></TD>" + //memory access
                "<TD><B>Memory</B></TD>" +
                "<TD><B>Data</B></TD>" +
                "<TD><B>Address</B></TD></TR>"
        };
    }

    //----------------------
    // sends the HTML document containing the machine description to
    // the PrintWriter
    public void writeMachineInHTML(Machine machine, PrintWriter out)
    {
        ObservableList[]  moduleVectors = {
            machine.getModule("registers"),
            machine.getModule("registerArrays"),
            machine.getModule("conditionBits"),
            machine.getModule("rams")
        };
        ObservableList[] microVectors = {
            machine.getMicros("set"),
            machine.getMicros("test"),
            machine.getMicros("increment"),
            machine.getMicros("shift"),
            machine.getMicros("logical"),
            machine.getMicros("arithmetic"),
            machine.getMicros("branch"),
            machine.getMicros("transferRtoR"),
            machine.getMicros("transferRtoA"),
            machine.getMicros("transferAtoR"),
            machine.getMicros("decode"),
            machine.getMicros("setCondBit"),
            machine.getMicros("io"),
            machine.getMicros("memoryAccess")
        };

        out.println("<HTML><HEAD>");
        out.println("<TITLE>" + machine.getHTMLName() + "</TITLE></HEAD>");

        out.println("<BODY>");
        out.println("<CENTER><FONT COLOR=\"#804040\" SIZE=\"+3\"><B><I>");
        out.println(machine.getHTMLName());
        out.println("</I></B></FONT></CENTER>");

        //print the assembly language punctuation preferences
        out.println();
        out.println("<TABLE bgcolor=\"#FFC0A0\" BORDER=\"1\"" +
                " CELLPADDING=\"0\" CELLSPACING=\"3\" WIDTH=\"100%\">");
        out.println(HEADER_PREFIX + "2" + HEADER_MIDDLE +
                "Punctuation preferences" + HEADER_SUFFIX);
        out.println("<TR><TD><B>Character</B></TD><TD><B>Use</B></TD></TR>");
        PunctChar[] chars = machine.getPunctChars();
        if (chars.length == 0)
            out.println("<TR VALIGN=\"middle\"><TD COLSPAN=\"2\">" +
                    "<CENTER>(none)</CENTER></TD></TR>");
        else
            for (PunctChar c : chars)
                out.println(c.getHTMLDescription());
        out.println("</TABLE><P></P>");

        //print the modules
        for (int i = 0; i < moduleVectors.length; i++) {
            out.println();
            out.println("<TABLE bgcolor=\"#FFC0A0\" BORDER=\"1\"" +
                    "CELLPADDING=\"0\" CELLSPACING=\"3\" WIDTH=\"100%\">");
            out.println(moduleHeaders[i]);
            out.println(moduleColumnHeaders[i]);
            if (moduleVectors[i].size() == 0)
                out.println("<TR VALIGN=\"middle\"><TD COLSPAN=\"" +
                        MODULE_COLUMNS[i] +
                        "\"><CENTER>(none)</CENTER></TD></TR>");
            else
                for (int j = 0; j < moduleVectors[i].size(); j++)
                    out.println(((Module) moduleVectors[i].get(j)
                            ).getHTMLDescription());
            out.println("</TABLE><P></P>");
        }

        //print the register indexing direction (from the left or from the right)
        out.println();
        out.println("<TABLE bgcolor=\"#FFC0A0\" BORDER=\"1\"" +
                " CELLPADDING=\"0\" CELLSPACING=\"3\" WIDTH=\"100%\">");
        out.println(HEADER_PREFIX + "1" + HEADER_MIDDLE + "Indexing Order of Bits in Registers"
                + HEADER_SUFFIX);
        out.println("<TR><TD>From " + (machine.getIndexFromRight() ? " right to left" :
                " left to right") + "</TD></TR>");
        out.println("</TABLE><P></P>");

        //print the register chosen as the program counter
        out.println();
        out.println("<TABLE bgcolor=\"#FFC0A0\" BORDER=\"1\"" +
                " CELLPADDING=\"0\" CELLSPACING=\"3\" WIDTH=\"100%\">");
        out.println(HEADER_PREFIX + "1" + HEADER_MIDDLE + "Program Counter Register"
                + HEADER_SUFFIX);
        out.println("<TR><TD>" + machine.getProgramCounter() + "</TD></TR>");
        out.println("</TABLE><P></P>");

        //print the micros except for End
        for (int i = 0; i < microVectors.length; i++) {
            out.println();
            out.println("<TABLE bgcolor=\"#FFC0A0\" BORDER=\"1\"" +
                    " CELLPADDING=\"0\" CELLSPACING=\"3\" WIDTH=\"100%\">");
            out.println(microHeaders[i]);
            out.println(microColumnHeaders[i]);
            if (microVectors[i].size() == 0)
                out.println("<TR VALIGN=\"middle\"><TD COLSPAN=\"" +
                        MICRO_COLUMNS[i] +
                        "\"><CENTER>(none)</CENTER></TD></TR>");
            else
                for (int j = 0; j < microVectors[i].size(); j++)
                    out.println(((Microinstruction)
                            microVectors[i].get(j)).getHTMLDescription());
            out.println("</TABLE><P></P>");
        }

        //print the global EQUs
        out.println();
        out.println("<TABLE bgcolor=\"#FFC0A0\" BORDER=\"1\"" +
                " CELLPADDING=\"0\" CELLSPACING=\"3\" WIDTH=\"100%\">");
        out.println(HEADER_PREFIX + "2" + HEADER_MIDDLE + "EQUs"
                + HEADER_SUFFIX);
        out.println("<TR><TD><B>Name</B></TD><TD><B>Value</B></TD></TR>");
        out.println();
        ObservableList<EQU> EQUs = machine.getEQUs();
        if (EQUs.size() == 0)
            out.println("<TR VALIGN=\"middle\"><TD COLSPAN=\"2\">" +
                    "<CENTER>(none)</CENTER></TD></TR>");
        else
            for (int j = 0; j < EQUs.size(); j++)
                out.println(((EQU)
                        EQUs.get(j)).getHTMLDescription());
        out.println("</TABLE><P></P>");

        //print the fetch sequence
        out.println();
        out.println("<TABLE bgcolor=\"#FFC0A0\" BORDER=\"1\"" +
                " CELLPADDING=\"0\" CELLSPACING=\"3\" WIDTH=\"100%\">");
        out.println(HEADER_PREFIX + "1" + HEADER_MIDDLE + "Fetch Sequence"
                + HEADER_SUFFIX);
        out.println("<TR><TD><B>Microinstructions</B></TD></TR>");
        MachineInstruction fetchSequence = machine.getFetchSequence();
        out.println("<TR><TD>");
        for (int i = 0; i < fetchSequence.getMicros().size(); i++) {
            out.println(fetchSequence.getMicros().get(i).getHTMLName() +"<BR>");
        }
        out.println("</TD></TR>");
        out.println("</TABLE><P></P>");

        //print the fields
        out.println();
        out.println("<TABLE bgcolor=\"#FFC0A0\" BORDER=\"1\"" +
                " CELLPADDING=\"0\" CELLSPACING=\"3\" WIDTH=\"100%\">");
        out.println(HEADER_PREFIX + "7" + HEADER_MIDDLE +
                "Instruction Format Fields" + HEADER_SUFFIX);
        out.println("<TR><TD><B>Name</B></TD>" +
                "<TD><B>Type</B></TD>" +
                "<TD><B>Number of Bits</B></TD>" +
                "<TD><B>Relativity</B></TD>" +
                "<TD><B>Signed</B></TD>" +
                "<TD><B>Default Value</B></TD>" +
                "<TD><B>Values</B></TD>" +
                "</TR>");
        out.println();
        List<Field> fields = machine.getFields();
        if (fields.size() == 0)
            out.println("<TR VALIGN=\"middle\"><TD COLSPAN=\"8\">" +
                    "<CENTER>(none)</CENTER></TD></TR>");
        else
            for (Field field : fields)
                out.println(field.getHTMLDescription());
        out.println("</TABLE><P></P>");

        //print the machine instructions
        out.println();
        out.println("<TABLE bgcolor=\"#FFC0A0\" BORDER=\"1\"" +
                " CELLPADDING=\"0\" CELLSPACING=\"3\" WIDTH=\"100%\">");
        out.println(HEADER_PREFIX + "5" + HEADER_MIDDLE +
                "Machine Instructions" + HEADER_SUFFIX);
        out.println("<TR><TD><B>Name</B></TD>" +
                "<TD><B>Opcode (hex)</B></TD>" +
                "<TD><B>Instruction Format</B></TD>" +
                "<TD><B>Assembly Format</B></TD>" +
                "<TD><B>Microinstructions</B></TD>" +
                "</TR>");
        List<MachineInstruction> instructions = machine.getInstructions();
        if (instructions.size() == 0)
            out.println("<TR VALIGN=\"middle\"><TD COLSPAN=\"5\">" +
                    "<CENTER>(none)</CENTER></TD></TR>");
        else
            for (MachineInstruction instruction : instructions) {
                out.println();
                out.println(instruction.getHTMLDescription());
            }
        out.println("</TABLE><P></P>");

        out.println("</BODY></HTML>");
        out.close();
    }

}