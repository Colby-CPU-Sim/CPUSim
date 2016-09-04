///////////////////////////////////////////////////////////////////////////////
// File:    	RAM.java
// Type:    	java application file
// Author:		Dale Skrien
// Project: 	CPU Sim
// Date:    	June, 1999
//
// Last Modified: 6/4/13
//
// Description:
//   This file contains the code for the RAM module.
//
// Things to do:
//   1.  Why doesn't the ModuleDialog let you specify a negative length for a RAM
//		 but does let you specify a negative value for a Register?
//
///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

package cpusim.model.module;

import cpusim.ExecutionException;
import cpusim.model.Module;
import cpusim.assembler.AssembledInstructionCall;
import cpusim.util.LoadException;
import cpusim.util.SourceLine;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * This class models RAM.  All addressable units ("cells") have the same
 * number of bits, but that number can be any value from 1 to 64.
 */
public class RAM extends Module
        implements cpusim.util.CPUSimConstants
{
    /** the data stored in the ram cells */
    private ObservableList<RAMLocation> data;
    /** data value that is changed in debug mode and used for backupManager only */
    private SimpleListProperty<RAMLocation> changedData;
    /** number of bits per cell.  The first 64-cellSize bits of each data value
     * will be all 0's. That is, the value is not stored in 64-bit 2's complement. */
    private SimpleIntegerProperty cellSize;
    /** the number of cells in ram */
    private SimpleIntegerProperty length;
    /** set to true when CPU Sim is running in debug mode */
    private boolean haltAtBreaks;
    /** all 0s except the rightmost cellSize bits, which are 1s. */
    private long cellMask;

    /**
     * Constructor
     * @param name name of the ram
     * @param length a positive base-10 integer that specifies the number
     *               of cells in the RAM.
     */
    public RAM(String name, int length) {
        this(name, length, 5);
    }

    /**
     * Constructor
     * @param name name of the ram
     * @param length a positive integer that specifies the number
     *               of cells in the RAM.
     * @param cellSize the number of bits per cell. This must be a positive
     *                 integer up to 64.
     */
    public RAM(String name, int length, int cellSize) {
        super(name);
        this.cellSize = new SimpleIntegerProperty(cellSize);
        this.length = new SimpleIntegerProperty(length);
        this.data = FXCollections.observableArrayList();
        this.changedData = new SimpleListProperty<>(this,"RAM data",null);
        for (int i=0; i<length; i++){
            this.data.add(new RAMLocation(i, 0, this, false, "", null));
        }
        this.haltAtBreaks = false; //can only fromRootController RAM when not in debug mode
        cellMask = 0;
        for (int i = 0; i < cellSize; i++)
            cellMask = (cellMask << 1) + 1;
    }

    //------------------------
    // utility methods

    /**
     * returns the data field
     * @return the data in the RAM
     */
    public ObservableList<RAMLocation> data(){
        return data;
    }

    /**
     * returns the value at address addr.
     * the value is returned as a 64-bit 2's complement integer.
     * Used by getValueAt in RAMTableModel, so should not signal a break
     * even if there is a break on that row of the table
     * @param addr address of the data
     * @return the data as a long object
     */
    public long getData(int addr) {
        if (addr < 0 || addr >= data.size())
            throw new ExecutionException("Attempted to access RAM " +
                    getName() + " at address " + addr +
                    " which is out of range");
        return (data.get(addr).getValue() << (64 - cellSize.get())) >> (64 - cellSize.get());
    }

    /**
     * returns the long value consisting of the given number of bits
     * starting at address addr, as a 2's complement number.  It
     * gets the high order bits from the first cell first and, if necessary,
     * continues getting bits from successive cells.
     * @param addr address of the data
     * @param numBits number of bits for the value
     * @return data as a lone object
     */
    public long getData(int addr, int numBits) {
        int numCells = (numBits + cellSize.get() - 1) / cellSize.get(); //ceil(numBits/cellSize)
        if (addr < 0 || addr + numCells > data.size())
            throw new ExecutionException("Attempted to access RAM " +
                    getName() + " at addresses " + addr + " to " +
                    (addr + numCells - 1) + " which is out of range");
        assert numBits > 0 && numBits <= 64 :
                "RAM.getData() was called with numBits = " + numBits;
        long value = 0;
        for (int i = addr; i < addr + numCells - 1; i++)
            // Is the "& cellMask" in the next line necessary?
            value = (value << cellSize.get()) + (data.get(i).getValue() & cellMask);
        // now add the bits from the last cell
        int numBitsLeft = numBits % cellSize.get() == 0 ? cellSize.get() : numBits % cellSize.get();
        long tempMask = (1L << numBitsLeft) - 1;
        value = (value << numBitsLeft) +
                (data.get(addr + numCells - 1).getValue() & tempMask);
        // now add sign extension
        value = value << (64 - numBits) >> (64 - numBits);
        return value;
    }

    /**
     * sets the given number of bits starting at address addr
     * with the given long value.  If there are too many bits for the
     * cell at the given address, the bits are stored in consecutive cells.
     * The value is stored big-endian
     * and is right justified within the given number of bits.
     * This means that if there are more bits than needed, the
     * first few bits will just contain 0's if value >= 0 and
     * 1's if value < 0.
     * @param addr address in the RAM of the data
     * @param value value of the data in long
     * @param numBits number of bits of the data
     */
    public void setData(final int addr, long value, final int numBits) {
        int numCells = (numBits + cellSize.get() - 1) / cellSize.get(); //ceil(numBits/cellSize)
        if (addr < 0 || addr + numCells > data.size())
            throw new ExecutionException("Attempt to access data in RAM " +
                    getName() + " at addresses " + addr + " to " +
                    (addr + numCells - 1) + " which are out of range");
        assert numBits > 0 :
                "RAM.setData() was called with numBits = " + numBits;

        //save old values of data for the purpose of backing up
        final ObservableList<RAMLocation> savedData = FXCollections.observableArrayList();
        for (int j = 0; j < numCells; j++)
            savedData.add(data.get(addr + j));
        changedData.set(savedData);

        //set the new values of data starting with the low-order bits.
        if (numBits % numCells != 0) {
            long tempMask = (1L << (numBits % numCells)) - 1;
            long tempValue = value & tempMask;
            int tempIndex = addr + numCells - 1;
            data.get(tempIndex).setValue(data.get(tempIndex).getValue() &
                    (-1 - (tempMask << (cellSize.get() - numBits % numCells))));
            data.get(tempIndex).setValue(data.get(tempIndex).getValue() |
                    (tempValue << (cellSize.get() - numBits % numCells)));
            value >>= numBits % numCells;
            for (int j = numCells - 2; j >= 0; j--) {
                data.get(addr + j).setValue(value & cellMask);
                value >>= cellSize.get();
            }
        } else {
            for (int j = numCells - 1; j >= 0; j--) {
                data.get(addr + j).setValue(value & cellMask);
                value >>= cellSize.get();
            }
        }
    }

    /**
     * set the value of the data at address addr
     * If the value doesn't fit, only the low-order cellSize bits
     * are saved.
     * @param addr address in the RAM of the data
     * @param value value of the data in long
     */
    public void setData(final int addr, long value) {
        final long[] oldValue = {data.get(addr).getValue()};
        data.get(addr).setValue(value & cellMask);
    }

    /**
     * getter for the data simple list property object
     * @return the data simple list property object
     */
    public SimpleListProperty dataProperty(){
        return changedData;
    }

    /**
     * getter for the assembly language comment in the data
     * @param index index of the comment line
     * @return a string of comment
     */
    public String getComment(int index) {
        return data.get(index).getComment();
    }

    /**
     * setter for the assembly language comment in the data
     * @param i index of the comment line
     * @param value value of the comment as a string
     */
    public void setComment(int i, String value) {
        //change all tabs to spaces for compactness
        data.get(i).setComment(value.replace('\t', ' '));
    }

    /**
     * get the source line from the assembled instructions
     * @param index index of the source line in the instructions
     * @return the source line at the given index
     */
    public SourceLine getSourceLine(int index) {
        if (0 <= index && index < data.size())
            return data.get(index).getSourceLine();
        else
            return null;
    }

    /**
     * setter of the source line
     * @param index index of the source line in the instructions
     * @param sourceLine the new source line to be set
     */
    public void setSourceLine(int index, SourceLine sourceLine) {
        assert 0 <= index && index < data.size() : "index out of range" +
                "in RAM.setsourceLine";
        this.data.get(index).setSourceLine(sourceLine);
    }

    /**
     * getter for the break line
     * @param index index of the break line
     * @return boolean if the line is break
     */
    public boolean getBreak(int index) {
        return data.get(index).getBreak();
    }

    /**
     * break the ram at the given address
     * @param addr address of the line to break
     * @return boolean value telling if it's break
     */
    public boolean breakAtAddress(int addr) {
        return haltAtBreaks && data.get(addr).getBreak();
    }

    /**
     * set the break at the given index
     * @param i index in the RAM
     * @param value boolean value to tell if break
     */
    public void setBreak(int i, boolean value) {
        data.get(i).setBreak(value);
    }

    /**
     * getter for the cell size
     * @return the cell size
     */
    public int getCellSize() {
        return cellSize.get();
    }

    /**
     * change the size (the number of bits) of the cells to the new value.
     * effect: if we reduce the cell size, some high order bits are lost.
     * if we increase the cell size, the new high order bits are zeros.
     *
     * @param newSize the new number of bits per cell
     */
    public void setCellSize(int newSize) {
        int oldCellSize = cellSize.get();
        cellSize.set(newSize);
        cellMask = 0;
        for (int i = 0; i < cellSize.get(); i++)
            cellMask = (cellMask << 1) + 1;
        // now update all the values in the data array
        for (int i = 0; i < data.size(); i++)
            data.get(i).setValue(data.get(i).getValue() & cellMask);
    }

    public int getLength() {
        return data.size();
    }

    /**
     * set the length of the data in ram
     * @param newLength new length of the data
     */
    public void setLength(int newLength) {
        length.set(newLength);
        int oldLength = data.size();
        if (newLength == oldLength)
            return;  //no changes need to be made

        //update the data
        if (newLength > oldLength) //add new empty RAMLocations
            for (int i = oldLength; i < newLength; i++) {
                data.add(new RAMLocation(i,0,this,false,"",null));
            }
        else { //new length is shorter so remove extra RAMLocations
            data.remove(newLength,oldLength);
        }
    }

    /**
     * loads the given vector of assembled instructions into the
     * given RAM starting at the given starting address.
     * returns true if the loading was successful.
     * It concatenates the bits in all the instrs to form one long list
     * of bits and then inserts them in the RAM starting at the given
     * address.
     * If the memory is too small, it displays an error message.
     * @param instrs lists of the assembled instruction
     * @param address address to be loaded to
     */
    public void loadAssembledInstructions(
            List<AssembledInstructionCall> instrs, int address) {
        int totalNumBits = 0;
        int nextAddr = address;
        int cellIndex = 0;
        long cellValue = 0;

        for (AssembledInstructionCall nextInstr : instrs) {
            int instrIndex = 0;
            int instrLength = nextInstr.length();
            long instrValue = nextInstr.getValue();
            //remove any sign bits
            instrValue = (instrValue << (64 - instrLength)) >>> (64 - instrLength);

            totalNumBits += instrLength;
            if ((totalNumBits + cellSize.get() - 1) / cellSize.get() + address
                    > getLength()) {
                throw new LoadException("There is not enough " +
                        "room in RAM " + getName() + " to load the instructions " +
                        "starting at address " + address, this, instrs);
            }

            //save comments & SourceLines
            String c = getComment(nextAddr); //get current comment on that line
            setComment(nextAddr, (c.length() == 0 ? "" : c + " | ") +
                    nextInstr.getComment().trim());
            if (getSourceLine(nextAddr) == null)
                // if the line doesn't already have a SourceLine, add one
                setSourceLine(nextAddr, nextInstr.getSourceLine());

            // fill up as many cells as you can
            while (instrLength - instrIndex >= cellSize.get() - cellIndex) {
                // add instr bits to successive cells
                long value = instrValue << instrIndex;
                value >>>= instrLength - (cellSize.get() - cellIndex);
                cellValue |= (value & cellMask);
                setData(nextAddr, ~cellValue); // to invoke an ChangeListener
                setData(nextAddr, cellValue);
                instrIndex += cellSize.get() - cellIndex;
                nextAddr++;
                cellIndex = 0;
                cellValue = 0;
            }
            if (instrLength - instrIndex > 0) {
                // there are a few more bits to store and
                // those bits fit in the current cell
                long value = instrValue << (64 - (instrLength - instrIndex));
                value >>>= (64 - (instrLength - instrIndex));
                value <<= (cellSize.get() - cellIndex) - (instrLength - instrIndex);
                cellValue |= (value & cellMask);
                cellIndex += instrLength - instrIndex;
            }
        }
    }



    /**
     * clone the whole object
     * @return a clone of this object
     */
    public Object clone() {
        return new RAM(getName(), data.size(), cellSize.get());
    }

    /**
     * copies the data from the current module to a specific module
     * @param comp the micro instruction that will be updated
     */
    public void copyDataTo(Module comp) {
        assert comp instanceof RAM :
                "Passed non-RAM to RAM.copyDataTo()";
        RAM newRAM = (RAM) comp;
        newRAM.setName(getName());
        newRAM.setLength(getLength());
        newRAM.setCellSize(getCellSize());
    }

    /**
     * returns the XML description
     * @return the XML description
     */
    public String getXMLDescription() {
        return "<RAM name=\"" + getHTMLName() + "\" length=\"" + getLength()
                + "\" cellSize=\"" + getCellSize() + "\" id=\"" + getID()
                + "\" />";
    }

    /**
     * returns the HTML description
     * @return the HTML description
     */
    public String getHTMLDescription() {
        return "<TR><TD>" + getHTMLName() + "</TD><TD>" + getLength() +
                "</TD><TD>" + getCellSize() + "</TD></TR>";
    }

    /**
     * clear erases the data, comments, and breakpoints of this ram
     */
    public void clear() {
        for (int i = 0; i < getLength(); i++) {
            setData(i, 0);
            setComment(i, "");
            setSourceLine(i, null);
        }
    }

    /**
     * setter for the halt at breaks
     * @param b if true then halt at breaks
     */
    public void setHaltAtBreaks(boolean b) {
        haltAtBreaks = b;
    }

    /**
     * getter for the length of address bits
     * @return the length of address bits
     */
    public int getNumAddrBits() {
        return 31-Integer.numberOfLeadingZeros(data.size());
    }

    /**
     * clear all the breakpoints for ram locations.
     */
    public void clearAllBreakpoints() {
        for (RAMLocation rLoc : this.data()) {
            rLoc.setBreak(false);
        }
    }
}
