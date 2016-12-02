package cpusim.model.microinstruction;

import cpusim.model.Machine;
import cpusim.model.Module;
import cpusim.model.module.Register;
import cpusim.model.util.IdentifiedObject;
import cpusim.model.util.Validate;
import cpusim.model.util.ValidationException;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.UUID;

import static com.google.common.base.Preconditions.*;

/**
 * The Set microinstruction allows the computer to set the contents
 * of any contiguous set of bits in any register to any fixed value.
 *
 * @since 2013-06-07
 */
public class CpusimSet extends Microinstruction<CpusimSet> {

    private SimpleObjectProperty<Register> register;
    private SimpleIntegerProperty start;
    private SimpleIntegerProperty numBits;
    private SimpleLongProperty value;

    /**
     * Constructor
     * creates a new Set object with input values.
     *
     * @param name name of the microinstruction.
     * @param register the register whose bits are to be set.
     * @param machine the machine that the microinstruction belongs to.
     * @param start the leftmost or rightmost bit of the register that is to be set.
     * @param numBits the number of consecutive bits in the register to be set.
     * @param value the base-10 value to which the bits are to be set.
     */
    public CpusimSet(String name, Machine machine,
                     Register register,
                     int start,
                     int numBits,
                     long value){
		this(name,
                IdentifiedObject.generateRandomID(),
                machine,
                register,
                start,
                numBits,
                value);
    }
    
    /**
     * Constructor
     * creates a new Set object with input values.
     *
     * @param name name of the microinstruction.
     * @param register the register whose bits are to be set.
     * @param machine the machine that the microinstruction belongs to.
     * @param start the leftmost or rightmost bit of the register that is to be set.
     * @param numBits the number of consecutive bits in the register to be set.
     * @param value the base-10 value to which the bits are to be set.
     */
    public CpusimSet(String name,
                     UUID id,
                     Machine machine,
                     Register register,
                     int start,
                     int numBits,
                     long value){
        super(name, id, machine);
        
        this.register = new SimpleObjectProperty<>(register);
        this.start = new SimpleIntegerProperty(start);
        this.numBits = new SimpleIntegerProperty(numBits);
        this.value = new SimpleLongProperty(value);
    }

    
    /**
     * Copy constructor
     * @param other Implementation to copy from.
     * 
     * @throws NullPointerException if <code>other</code> is <code>null</code>
     */
    public CpusimSet(final CpusimSet other) {
    	this(checkNotNull(other).getName(), other.machine, other.getRegister(), 
    			other.getStart(), other.getNumBits(), other.getValue());
    }


    /**
     * @return the Register in the set microinstruction.
     */
    public Register getRegister(){
        return register.get();
    }

    /**
     * updates the register used by the microinstruction.
     * @param newRegister the new selected Register for the set microinstruction.
     */
    public void setRegister(Register newRegister){
        register.set(newRegister);
    }

    /**
     * returns the index of the start bit of the microinstruction.
     * @return the integer value of the index.
     */
    public int getStart(){
        return start.get();
    }

    /**
     * updates the index of the start bit of the microinstruction.
     * @param newStart the new index of the start bit for the set microinstruction.
     * @since 2016-09-20
     */
    public void setStart(int newStart){
        start.set(newStart);
    }
    
    /**
     * returns the number of bits of the value.
     * @return the integer value of the number of bits.
     */
    public int getNumBits(){
        return numBits.get();
    }

    /**
     * updates the number of bits of the value.
     * @param newNumbits the new value of the number of bits.
     */
    public void setNumBits(int newNumbits){
        numBits.set(newNumbits);
    }

    /**
     * returns the fixed value stored in the set microinstruction.
     * @return the integer value of the field.
     */
    public long getValue(){
        return value.get();
    }

    /**
     * updates the fixed value stored in the set microinstruction.
     * @param newValue the new value for the field.
     */
    public void setValue(final long newValue){
        value.set(newValue);
    }
    
    /**
     * execute the micro instruction from machine
     */
    @Override
    public void execute()
    {
        //shift all the bits in the register to the leftmost bits possible
        int fullShift = 64 - register.get().getWidth();
        long registerValue = register.get().getValue() << (fullShift);
        
        //set certain shift values depending on whether we are indexing from the
        //right or the left
        int rightOffsetShift;
        int leftOffsetShift;
        int valueRightShift;
        
        int numBits = this.numBits.get();
        int startBits = this.start.get();
        
        if (!machine.getIndexFromRight()){
            rightOffsetShift = 64 - startBits; 
            leftOffsetShift = startBits + numBits;
            valueRightShift = startBits;
        }
        else{
            rightOffsetShift = fullShift + numBits + startBits;
            leftOffsetShift = register.get().getWidth() - startBits;
            valueRightShift = register.get().getWidth() - startBits - numBits;
        }
        
        //NOTE: java doesn't allow shifts of greater than 63, so we manually set
        //the values of left and right part if the shift is 64
        
        //clear the right bits of the register
        long leftPart = 0;
        if (rightOffsetShift != 64)
            leftPart = (registerValue >>> rightOffsetShift) << rightOffsetShift;
        
        //put the value bits in the correct place
        long middlePart = (value.get() << (64 - numBits)) >>> valueRightShift;
        
        //clear the left bits of the register
        long rightPart = 0;
        if(leftOffsetShift != 64)
            rightPart = (registerValue << leftOffsetShift) >>> leftOffsetShift;
            
        //fromRootController the resulting value and puit it in the register
        long result = (leftPart | middlePart | rightPart) >> (fullShift);
        register.get().setValue(result);    
    }

    @Override
    public void copyTo(CpusimSet oldMicro){
    	checkNotNull(oldMicro);
    	
    	oldMicro.setName(getName());
    	oldMicro.setRegister(getRegister());
    	oldMicro.setStart(getStart());
    	oldMicro.setNumBits(getNumBits());
    	oldMicro.setValue(getValue());
    }

    /**
     * returns the XML description
     * @return the XML description
     */
    @Override
    public String getXMLDescription(String indent){
        return "<Set name=\"" + getHTMLName() +
                "\" register=\"" + getRegister().getID() +
                "\" start=\"" + getStart() +
                "\" numBits=\"" + getNumBits() +
                "\" value=\"" + getValue() +
                "\" id=\"" + getID() + "\" />";
    }

    /**
     * returns the HTML description
     * @return the HTML description
     */
	@Override
	public String getHTMLDescription(String indent) {
		return indent + "<TR><TD>" + getHTMLName() + "</TD><TD>" + getRegister().getHTMLName() +
	        "</TD><TD>" + getStart() + "</TD><TD>" + getNumBits() +
	        "</TD><TD>" + getValue() + "</TD></TR>";
	}

	/**
     * returns true if this microinstruction uses m
     * (so if m is modified, this micro may need to be modified.
     * @param m the module that holds the microinstruction
     * @return boolean value true if this micro used the module
     */
	@Override
	public boolean uses(Module<?> m) {
		return (m == register.get());
	}
    
    @Override
    protected void validateState() {
        // checks if Set objects with all ranges all in Bounds properly
        
        final int start = getStart();
        final int numBits = getNumBits();
//            final long value = getValue();
    
        Register register = getRegister();
    
        if (start < 0) {
            throw new ValidationException("You cannot specify a negative value for the " +
                    "start bit\n" +
                    "in the instruction " + getName() + ".");
        }
    
        if (numBits <= 0) {
            throw new ValidationException("You must specify a positive value for the " +
                    "bitwise width\nof the set range " +
                    "in the instruction " + getName() + ".");
        }
    
        final int regWidth = register.getWidth();
    
        if (start >= regWidth) {
            throw new ValidationException("Invalid start index for the specified register " +
                    "in the Set microinstruction " + getName() +
                    ".\nIt must be non-negative, and less than the " +
                    "register's length.");
        }
    
        if ((start + numBits) > regWidth) {
            throw new ValidationException("The bitwise width of the set area in the Set " +
                    "microinstruction " +
                    getName() + "\n is too large to fit in the register.");
        }
        
        // Then checks if the value of each Set microinstruction fits
        // in the given number of bits in the microinstruction.
        // The value is treated as either a 2's complement value or
        // and unsigned integer value and so the range of legal values
        // or n bits is -(2^(n-1)) to 2^n - 1.
    
        try {
            Validate.fitsInBits(getValue(), getNumBits());
        } catch (ValidationException e) {
            throw new ValidationException(e.getMessage() +
                    " in the microinstruction \"" + getName() + "\".", e);
        }
    }

}
