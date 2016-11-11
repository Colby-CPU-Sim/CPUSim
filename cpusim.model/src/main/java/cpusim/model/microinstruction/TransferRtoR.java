/**
 * author: Jinghui Yu
 * lastEditingDate: 6/7/2013
 */

package cpusim.model.microinstruction;

import cpusim.model.Machine;
import cpusim.model.Microinstruction;
import cpusim.model.Module;
import cpusim.model.module.Register;

import cpusim.model.util.Copyable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The TransferRtoR microinstruction transfers data from a register to a register.
 */
public class TransferRtoR extends Transfer<Register, Register> implements Copyable<TransferRtoR> {
    
    /**
     * Constructor
     * creates a new Transfer object with input values.
     *
     * @param name name of the microinstruction.
     * @param machine the machine that the microinstruction belongs to.
     * @param source the register whose value is to be tested.
     * @param srcStartBit an integer indicting the leftmost or rightmost bit to be transfered.
     * @param dest the destination register.
     * @param destStartBit an integer indicting the leftmost or rightmost bit to be changed.
     * @param numBits a non-negative integer indicating the number of bits to be tested.
     */
    public TransferRtoR(String name, Machine machine,
                Register source,
                int srcStartBit,
                Register dest,
                int destStartBit,
                int numBits){
        super(name, machine, source, srcStartBit, dest, destStartBit, numBits);
    }
    
    /**
     * Copy constructor
     * @param other copied instance
     */
    public TransferRtoR(TransferRtoR other) {
        super(other);
    }
    
    /**
     * returns the class of the microinstruction
     * @return the class of the microinstruction
     */
    @Override
    public String getMicroClass(){
        return "transferRtoR";
    }
    
    @Override
    public <U extends TransferRtoR> void copyTo(final U other) {
        copyToHelper(other);
    }

    /**
     * execute the micro instruction from machine
     */
    @Override
    public void execute()
    {
        //move the bit values of the registers to the far left end of the 64-bit long
        int srcFullShift = 64 - source.get().getWidth();
        int destFullShift = 64 - dest.get().getWidth();
        long sourceValue = source.get().getValue() << (srcFullShift);
        long destValue = dest.get().getValue() << (destFullShift);

        // get the shift amounts needed to extra the appropriate bits from src & dest
        int destRightShift;
        int destLeftShift;
        int srcRightShift;
        int srcLeftShift;
        if (machine.getIndexFromRight()) {
            destRightShift = destFullShift + destStartBit.get() + numBits.get();
            destLeftShift = dest.get().getWidth() - destStartBit.get();
            srcLeftShift = source.get().getWidth() - srcStartBit.get() - numBits.get();
            srcRightShift = dest.get().getWidth() - destStartBit.get() - numBits.get();
        }
        else {
            destRightShift = 64 - destStartBit.get();
            destLeftShift = destStartBit.get() + numBits.get();
            srcLeftShift = srcStartBit.get();
            srcRightShift = destStartBit.get();
        }

        // get the left and right parts of the dest register to save
        // and get the middle part of the src register to transfer.
        //NOTE: java won't allow shifts greater than 63, so if the shift is 64,
        //we bypass the actual shifting and just set the value to 0 since a
        //64 bit shift would result in 0 anyway
        long leftDestPart = 0;  // the bits in dest register's left part to save
        if(destRightShift < 64){ // zero out all but that left part
            leftDestPart = (destValue >>> destRightShift) << destRightShift;
        }

        long rightDestPart = 0; // the bits in dest register's right part to save
        if (destLeftShift < 64){ // zero out all but that right part
            rightDestPart = (destValue << destLeftShift) >>> destLeftShift;
        }

        // get the bits in the src register to be transferred
        int srcOffsetShift = 64 - numBits.get();
        long middlePart = (((sourceValue << srcLeftShift) >>> srcOffsetShift)
                << srcOffsetShift) >>> srcRightShift;

        long result = leftDestPart | middlePart | rightDestPart;
        result = result >> destFullShift;
        dest.get().setValue(result);
    }

    /**
     * returns the XML description
     * @return the XML description
     */
    @Override
    public String getXMLDescription(String indent){
        return indent + "<TransferRtoR name=\"" + getHTMLName() +
                "\" source=\"" + getSource().getID() +
                "\" srcStartBit=\"" + getSrcStartBit() +
                "\" dest=\"" + getDest().getID() +
                "\" destStartBit=\"" + getDestStartBit() +
                "\" numBits=\"" + getNumBits() +
                "\" id=\"" + getID() + "\" />";
    }

    /**
     * returns the HTML description
     * @return the HTML description
     */
    @Override
    public String getHTMLDescription(String indent){
        return indent + "<TR><TD>" + getHTMLName() +
                "</TD><TD>" + getSource().getHTMLName() +
                "</TD><TD>" + getSrcStartBit() +
                "</TD><TD>" + getDest().getHTMLName() +
                "</TD><TD>" + getDestStartBit() +
                "</TD><TD>" + getNumBits() +
                "</TD></TR>";
    }
}
