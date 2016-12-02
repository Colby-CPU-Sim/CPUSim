package cpusim.model.microinstruction;

import cpusim.model.Machine;
import cpusim.model.Module;
import cpusim.model.module.Sized;
import cpusim.model.util.IdentifiedObject;
import cpusim.model.util.ValidationException;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Denotes common behavior for a {@link cpusim.model.module.Register} transfer
 *
 * @since 2016-11-14
 */
abstract class Transfer<From extends Module<From> & Sized<From>, To extends Module<To> & Sized<To>,
        Sub extends Transfer<From, To, Sub>>
        extends Microinstruction<Sub> {
    
    protected SimpleObjectProperty<From> source;
    protected SimpleIntegerProperty srcStartBit;
    
    protected SimpleObjectProperty<To> dest;
    protected SimpleIntegerProperty destStartBit;
    protected SimpleIntegerProperty numBits;
    
    
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
    public Transfer(String name,
                    UUID id,
                    Machine machine,
                    From source,
                    int srcStartBit,
                    To dest,
                    int destStartBit,
                    int numBits){
        super(name, id, machine);
        this.source = new SimpleObjectProperty<>(source);
        this.srcStartBit = new SimpleIntegerProperty(srcStartBit);
        this.dest = new SimpleObjectProperty<>(dest);
        this.destStartBit = new SimpleIntegerProperty(destStartBit);
        this.numBits = new SimpleIntegerProperty(numBits);
    }
    
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
    public Transfer(String name,
                    Machine machine,
                    From source,
                    int srcStartBit,
                    To dest,
                    int destStartBit,
                    int numBits){
        super(name, IdentifiedObject.generateRandomID(), machine);
        this.source = new SimpleObjectProperty<>(source);
        this.srcStartBit = new SimpleIntegerProperty(srcStartBit);
        this.dest = new SimpleObjectProperty<>(dest);
        this.destStartBit = new SimpleIntegerProperty(destStartBit);
        this.numBits = new SimpleIntegerProperty(numBits);
    }
    
    /**
     * Copy constructor
     * @param other Copied instance
     */
    public Transfer(final Transfer<From, To, Sub> other) {
        this(checkNotNull(other).getName(),
                other.machine,
                other.getSource(),
                other.getSrcStartBit(),
                other.getDest(),
                other.getDestStartBit(),
                other.getNumBits());
    }
    
    /**
     * returns the name of the set microinstruction as a string.
     *
     * @return the name of the set microinstruction.
     */
    public final From getSource(){
        return source.get();
    }
    
    /**
     * updates the register used by the microinstruction.
     *
     * @param newSource the new selected register for the set microinstruction.
     */
    public final void setSource(From newSource){
        source.set(newSource);
    }
    
    /**
     * returns the index of the start bit of the microinstruction.
     *
     * @return the integer value of the index.
     */
    public final int getSrcStartBit(){
        return srcStartBit.get();
    }
    
    /**
     * updates the index of the start bit of the microinstruction.
     *
     * @param newSrcStartBit the new index of the start bit for the set microinstruction.
     */
    public final void setSrcStartBit(int newSrcStartBit){
        srcStartBit.set(newSrcStartBit);
    }
    
    /**
     * returns the name of the set microinstruction as a string.
     *
     * @return the name of the set microinstruction.
     */
    public final To getDest(){
        return dest.get();
    }
    
    /**
     * updates the register used by the microinstruction.
     *
     * @param newDest the new selected register for the set microinstruction.
     */
    public final void setDest(To newDest){
        dest.set(newDest);
    }
    
    /**
     * returns the index of the start bit of the microinstruction.
     *
     * @return the integer value of the index.
     */
    public final int getDestStartBit(){
        return destStartBit.get();
    }
    
    /**
     * updates the index of the start bit of the microinstruction.
     *
     * @param newDestStartBit the new index of the start bit for the set microinstruction.
     */
    public final void setDestStartBit(int newDestStartBit){
        destStartBit.set(newDestStartBit);
    }
    
    /**
     * returns the number of bits of the value.
     *
     * @return the integer value of the number of bits.
     */
    public final int getNumBits(){
        return numBits.get();
    }
    
    /**
     * updates the number of bits of the value.
     *
     * @param newNumbits the new value of the number of bits.
     */
    public final void setNumBits(int newNumbits){
        numBits.set(newNumbits);
    }
    
    /**
     * returns true if this microinstruction uses m
     * (so if m is modified, this micro may need to be modified.
     * @param m the module that holds the microinstruction
     * @return boolean value true if this micro used the module
     */
    @Override
    public boolean uses(Module<?> m){
        return (m == source.get() || m == dest.get());
    }
    
    protected final <U extends Sub> void copyToHelper(final U other) {
        checkNotNull(other);
    
        other.setName(getName());
        other.setSource(getSource());
        other.setSrcStartBit(getSrcStartBit());
        other.setDest(getDest());
        other.setDestStartBit(getDestStartBit());
        other.setNumBits(getNumBits());
    }
    
    @Override
    protected void validateState() {
        String boundPhrase = "";
    
        int srcStartBit = getSrcStartBit();
        int destStartBit = getDestStartBit();
        int numBits = getNumBits();
    
        if (srcStartBit < 0 || destStartBit < 0 || numBits < 0) {
            throw new ValidationException("You have a negative value for one of the " +
                    "start bits or the number of bits\nin the " +
                    "microinstruction \"" + getName() + "\".");
        }
    
        if (srcStartBit > getSource().getWidth()) {
            boundPhrase = "srcStartBit";
        } else if (destStartBit > getDest().getWidth()) {
            boundPhrase = "destStartBit";
        }
    
        if (!boundPhrase.isEmpty()) {
            throw new ValidationException(boundPhrase + " has an invalid value for the " +
                    "specified register in instruction " + getName() +
                    ".\nIt must be non-negative, and less than the " +
                    "register's length.");
        }
    
        if (srcStartBit + numBits > getSource().getWidth() ||
                destStartBit + numBits > getDest().getWidth()) {
            throw new ValidationException("The number of bits being transferred is " +
                    "too large to fit in the source register or the " +
                    "destination array.\n" +
                    "Please specify a new start bit or a smaller number " +
                    "of bits to copy in the microinstruction \"" +
                    getName() + ".\"");
        }
    }
}
