/**
 * Author: Jinghui Yu
 * LastEditingDate: 6/7/2013
 */

package cpusim.model.microinstruction;

import cpusim.ExecutionException;
import cpusim.model.Machine;
import cpusim.model.Microinstruction;
import cpusim.model.Module;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * The TransferRtoA microinstruction transfers data from a register to a register array.
 */
public class TransferAtoR extends Microinstruction {
    private SimpleObjectProperty<RegisterArray> source;
    private SimpleIntegerProperty srcStartBit;
    private SimpleObjectProperty<Register> dest;
    private SimpleIntegerProperty destStartBit;
    private SimpleIntegerProperty numBits;
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
    public TransferAtoR(String name, Machine machine,
                        RegisterArray source,
                        Integer srcStartBit,
                        Register dest,
                        Integer destStartBit,
                        Integer numBits,
                        Register index,
                        Integer indexStart,
                        Integer indexNumBits){
        super(name, machine);
        this.source = new SimpleObjectProperty<>(source);
        this.srcStartBit = new SimpleIntegerProperty(srcStartBit);
        this.dest = new SimpleObjectProperty<>(dest);
        this.destStartBit = new SimpleIntegerProperty(destStartBit);
        this.numBits = new SimpleIntegerProperty(numBits);
        this.index = new SimpleObjectProperty<>(index);
        this.indexStart = new SimpleIntegerProperty(indexStart);
        this.indexNumBits = new SimpleIntegerProperty(indexNumBits);
    }

    /**
     * returns the name of the set microinstruction as a string.
     *
     * @return the name of the set microinstruction.
     */
    public RegisterArray getSource(){
        return source.get();
    }

    /**
     * updates the register used by the microinstruction.
     *
     * @param newSource the new selected register for the set microinstruction.
     */
    public void setSource(RegisterArray newSource){
        source.set(newSource);
    }

    /**
     * returns the index of the start bit of the microinstruction.
     *
     * @return the integer value of the index.
     */
    public Integer getSrcStartBit(){
        return srcStartBit.get();
    }

    /**
     * updates the index of the start bit of the microinstruction.
     *
     * @param newSrcStartBit the new index of the start bit for the set microinstruction.
     */
    public void setSrcStartBit(Integer newSrcStartBit){
        srcStartBit.set(newSrcStartBit);
    }

    /**
     * returns the name of the set microinstruction as a string.
     *
     * @return the name of the set microinstruction.
     */
    public Register getDest(){
        return dest.get();
    }

    /**
     * updates the register used by the microinstruction.
     *
     * @param newDest the new selected register for the set microinstruction.
     */
    public void setDest(Register newDest){
        dest.set(newDest);
    }

    /**
     * returns the index of the start bit of the microinstruction.
     *
     * @return the integer value of the index.
     */
    public Integer getDestStartBit(){
        return destStartBit.get();
    }

    /**
     * updates the index of the start bit of the microinstruction.
     *
     * @param newDestStartBit the new index of the start bit for the set microinstruction.
     */
    public void setDestStartBit(Integer newDestStartBit){
        destStartBit.set(newDestStartBit);
    }

    /**
     * returns the number of bits of the value.
     *
     * @return the integer value of the number of bits.
     */
    public Integer getNumBits(){
        return numBits.get();
    }

    /**
     * updates the number of bits of the value.
     *
     * @param newNumbits the new value of the number of bits.
     */
    public void setNumBits(Integer newNumbits){
        numBits.set(newNumbits);
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
    public Integer getIndexStart(){
        return indexStart.get();
    }

    /**
     * updates the index of the start bit of the microinstruction.
     *
     * @param newIndexStart the new index of the start bit for the set microinstruction.
     */
    public void setIndexStart(Integer newIndexStart){
        indexStart.set(newIndexStart);
    }

    /**
     * returns the number of bits of the value.
     *
     * @return the integer value of the number of bits.
     */
    public Integer getIndexNumBits(){
        return indexNumBits.get();
    }

    /**
     * updates the number of bits of the value.
     *
     * @param newIndexNumBits the new value of the number of bits.
     */
    public void setIndexNumBits(Integer newIndexNumBits){
        indexNumBits.set(newIndexNumBits);
    }

    /**
     * duplicate the set class and return a copy of the original Set class.
     *
     * @return a copy of the Set class
     */
    public Object clone(){
        return new TransferAtoR(getName(),machine,getSource(),getSrcStartBit(),
                getDest(),getDestStartBit(),getNumBits(),
                getIndex(),getIndexStart(),getIndexNumBits());
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
     * @param oldMicro the micro instruction that will be updated
     */
    public void copyDataTo(Microinstruction oldMicro)
    {
        assert oldMicro instanceof TransferAtoR :
                "Passed non-TransferAtoR to TransferAtoR.copyDataTo()";
        TransferAtoR newTransferAtoR = (TransferAtoR) oldMicro;
        newTransferAtoR.setName(getName());
        newTransferAtoR.setSource(getSource());
        newTransferAtoR.setIndex(getIndex());
        newTransferAtoR.setSrcStartBit(getSrcStartBit());
        newTransferAtoR.setDest(getDest());
        newTransferAtoR.setDestStartBit(getDestStartBit());
        newTransferAtoR.setNumBits(getNumBits());
        newTransferAtoR.setIndexStart(getIndexStart());
        newTransferAtoR.setIndexNumBits(getIndexNumBits());
    }

    /**
     * execute the micro instruction from machine
     */
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
    public String getXMLDescription(){
        return "<TransferAtoR name=\"" + getHTMLName() +
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
    public String getHTMLDescription(){
        return "<TR><TD>" + getHTMLName() +
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
    public boolean uses(Module m){
        return (m == source.get() || m == dest.get() || m == index.get());
    }

}
