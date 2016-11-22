/**
 * Author: Jinghui Yu
 * LastEditingDate: 6/7/2013
 */

package cpusim.model.microinstruction;

import cpusim.model.ExecutionException;
import cpusim.model.Machine;
import cpusim.model.Module;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import cpusim.model.util.Copyable;
import cpusim.model.util.ValidationException;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * The TransferRtoA microinstruction transfers data from a register to a register array.
 */
public class TransferAtoR extends Transfer<RegisterArray, Register> implements Copyable<TransferAtoR> {
    
    private SimpleObjectProperty<Register> index;
    private SimpleIntegerProperty indexStart;
    private SimpleIntegerProperty indexNumBits;

    /**
     * Constructor
     * creates a new Test object with input values.
     *
     * @param name name of the microinstruction.
     * @param machine the machine that the microinstruction belongs to.
     * @param source the register whose value is to be tested.
     * @param srcStartBit an integer indicting the leftmost or rightmost bit to be tested.
     * @param dest the destination register.
     * @param destStartBit an integer indicting the leftmost or rightmost bit to be changed.
     * @param numBits a non-negative integer indicating the number of bits to be tested.
     */
    public TransferAtoR(String name,
                        Machine machine,
                        RegisterArray source,
                        int srcStartBit,
                        Register dest,
                        int destStartBit,
                        int numBits,
                        Register index,
                        int indexStart,
                        int indexNumBits){
        super(name, machine, source, srcStartBit, dest, destStartBit, numBits);
        this.index = new SimpleObjectProperty<>(index);
        this.indexStart = new SimpleIntegerProperty(indexStart);
        this.indexNumBits = new SimpleIntegerProperty(indexNumBits);
    }
    
    /**
     * Copy constructor
     * @param other copied instance
     */
    public TransferAtoR(TransferAtoR other) {
        super(other);
    }

    /**
     * returns the name of the set microinstruction as a string.
     *
     * @return the name of the set microinstruction.
     */
    public Register getIndex(){
        return index.get();
    }

    /**
     * updates the register used by the microinstruction.
     *
     * @param newIndex the new selected register for the set microinstruction.
     */
    public void setIndex(Register newIndex){
        index.set(newIndex);
    }

    /**
     * returns the index of the start bit of the microinstruction.
     *
     * @return the integer value of the index.
     */
    public int getIndexStart(){
        return indexStart.get();
    }

    /**
     * updates the index of the start bit of the microinstruction.
     *
     * @param newIndexStart the new index of the start bit for the set microinstruction.
     */
    public void setIndexStart(int newIndexStart){
        indexStart.set(newIndexStart);
    }

    /**
     * returns the number of bits of the value.
     *
     * @return the integer value of the number of bits.
     */
    public int getIndexNumBits(){
        return indexNumBits.get();
    }

    /**
     * updates the number of bits of the value.
     *
     * @param newIndexNumBits the new value of the number of bits.
     */
	public void setIndexNumBits(int newIndexNumBits){
        indexNumBits.set(newIndexNumBits);
    }
    
    /**
     * returns the class of the microinstruction
     * @return the class of the microinstruction
     */
    @Override
    public String getMicroClass(){
        return "transferAtoR";
    }

    /**
     * copies the data from the current micro to a specific micro
     * @param newTransferAtoR the micro instruction that will be updated
     */
    public <U extends TransferAtoR> void copyTo(U newTransferAtoR)
    {
        copyToHelper(newTransferAtoR);
        
        newTransferAtoR.setIndex(getIndex());
        newTransferAtoR.setIndexStart(getIndexStart());
        newTransferAtoR.setIndexNumBits(getIndexNumBits());
    }

    /**
     * execute the micro instruction from machine
     */
    @Override
    public void execute()
    {
        //manipulate variables depending on the indexing scheme
        int indexLeftShift;
        int indexRightShift = 64 - indexNumBits.get();
        if (!machine.getIndexFromRight()){
            indexLeftShift = 64 - index.get().getWidth() + indexStart.get();            
        }
        else{
            indexLeftShift = 64 - indexNumBits.get() - indexStart.get();
        }
        
        //trim the index register to the proper bits
        long indexValue = index.get().getValue();
        indexValue = (indexValue << indexLeftShift) >>> indexRightShift;
        
        //if indexValue is negative or too large, throw an exception.
        if (indexValue < 0 || indexValue >= source.get().getLength())
            throw new ExecutionException("Index value: " + indexValue +
                    " is out of range for choosing a register\nfrom array: " +
                    dest.get() + " in the transferAtoR microinstruction: " +
                    getName());
        
        Register sourceRegister = source.get().registers().get((int) indexValue);
        
        int srcFullShift = 64 - sourceRegister.getWidth();
        int destFullShift = 64 - dest.get().getWidth();
        
        //move the bit value of the register to the far left end of memory
        long sourceValue = sourceRegister.getValue() << (srcFullShift);
        long destValue = dest.get().getValue() << (destFullShift);

        //manipulate variables depending on the indexing scheme
        int destRightShift;
        int destLeftShift;
        int srcRightShift;
        int srcLeftShift;
        int srcOffsetShift = 64 - numBits.get();
        if(!machine.getIndexFromRight()){
            destRightShift = 64 - destStartBit.get();
            destLeftShift = destStartBit.get() + numBits.get();
            srcLeftShift = srcStartBit.get();
            srcRightShift = destStartBit.get();
        }
        else{
            destRightShift = srcFullShift + destStartBit.get() + numBits.get();
            destLeftShift = dest.get().getWidth() - destStartBit.get();
            srcLeftShift = sourceRegister.getWidth() - srcStartBit.get() - numBits.get();
            srcRightShift = dest.get().getWidth() - destStartBit.get() - numBits.get();
        }
        
        //shave of the middle part of the destination register by getting rid of
        //bits on either side.
        //NOTE: java won't allow shifts greater than 63, so if the shift is 64,
        //we bypass the actual shifting and just set the value to 0 since a
        //64 bit shift would result in 0 anyway
        long leftDestPart = 0;
        if(destRightShift != 64){
            leftDestPart = (destValue >>> (destRightShift))
                                << (destRightShift);
        }
        long rightDestPart = 0;
        if (destLeftShift != 64){
            rightDestPart = (destValue << (destLeftShift))
                                >>> (destLeftShift);
        }
        
        //trim the sides of the source register as dictated by the numbits and 
        //startbit fields (also the indexing scheme)
        long middlePart = (((sourceValue << srcLeftShift) >>> (srcOffsetShift))
                        << (srcOffsetShift)) >>> srcRightShift;
        long result = leftDestPart | middlePart | rightDestPart;
        result = result >> destFullShift;
        dest.get().setValue(result);
    }

    /**
     * returns the XML description
     * @return the XML description
     */
    @Override
    public String getXMLDescription(String indent) {
        return indent + "<TransferAtoR name=\"" + getHTMLName() +
                "\" source=\"" + getSource().getID() +
                "\" srcStartBit=\"" + getSrcStartBit() +
                "\" dest=\"" + getDest().getID() +
                "\" destStartBit=\"" + getDestStartBit() +
                "\" numBits=\"" + getNumBits() +
                "\" index=\"" + getIndex().getID() +
                "\" indexStart=\"" + getIndexStart() +
                "\" indexNumBits=\"" + getIndexNumBits() +
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
                "</TD><TD>" + getIndex().getHTMLName() +
                "</TD><TD>" + getIndexStart() +
                "</TD><TD>" + getIndexNumBits() +
                "</TD></TR>";
    }

    /**
     * returns true if this microinstruction uses m
     * (so if m is modified, this micro may need to be modified.
     * @param m the module that holds the microinstruction
     * @return boolean value true if this micro used the module
     */
    @Override
    public boolean uses(Module<?> m){
        return super.uses(m) || m == index.get();
    }
    
    @Override
    protected void validateState() {
        super.validateState();
    
        int indexNumBits = getIndexNumBits();
        int indexStart = getIndexStart();
    
        if (indexStart < 0) {
            throw new ValidationException("You have a negative value for one of the " +
                    "start bits or the number of bits\nin the " +
                    "microinstruction \"" + getName() + "\".");
        }
    
        if (indexStart > getIndex().getWidth()) {
            throw new ValidationException("indexStart has an invalid value for the " +
                    "specified register in instruction " + getName() +
                    ".\nIt must be non-negative, and less than the " +
                    "register's length.");
        }
    
        if (indexNumBits <= 0) {
            throw new ValidationException("A positive number of bits must be specified " +
                    "for the index register.");
        }
        else if (indexStart + indexNumBits > getIndex().getWidth()) {
            throw new ValidationException("The number of bits specified in the index " +
                    "register is " +
                    "too large to fit in the index register.\n" +
                    "Please specify a new start bit or a smaller number " +
                    "of bits in the microinstruction \"" +
                    getName() + ".\"");
        }
    }

}
