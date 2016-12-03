package cpusim.model.microinstruction;

import cpusim.model.Machine;
import cpusim.model.Module;
import cpusim.model.module.Register;
import cpusim.model.util.IdentifiedObject;
import cpusim.model.util.ValidationException;
import cpusim.model.util.units.ArchType;
import cpusim.model.util.units.ArchValue;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.UUID;
import java.util.function.BiPredicate;

import static com.google.common.base.Preconditions.*;

/**
 * The Test microinstruction allows the computer to jump to other microinstructions
 * within the current fetch or execute sequence.
 *
 * @since 2013-06-04
 */
public class Test extends Microinstruction<Test> {
	
	public enum Operation {
		
		EQ(ArchValue::equals),
		
		NE(((BiPredicate<ArchValue, ArchValue>)ArchValue::equals).negate()),
		LT(ArchValue::lt),
		LE(ArchValue::lte),
		GT(ArchValue::gt),
		GE(ArchValue::gte);
		
		private final BiPredicate<ArchValue, ArchValue> checkFunc;
		
		private Operation(BiPredicate<ArchValue, ArchValue> checkFunc) {
			this.checkFunc = checkNotNull(checkFunc);
		}
		
		boolean check(long unsigned, long signed, ArchValue toCheck) {
			checkNotNull(toCheck);
			
			return checkFunc.test(ArchType.Bit.of(unsigned), toCheck);
		}
	}
	
    private final SimpleObjectProperty<Register> register;
    private final SimpleIntegerProperty start;
    private final SimpleIntegerProperty numBits;
    
    private final SimpleObjectProperty<Operation> comparison;
    private final SimpleLongProperty value;
    private final SimpleIntegerProperty omission;
    
    /**
     * Constructor
     * creates a new Test object with input values.
     *
     * @param name name of the microinstruction.
     * @param machine the machine that the microinstruction belongs to.
     * @param register the register whose value is to be tested.
     * @param start an integer indicating the leftmost or rightmost bit to be tested.
     * @param numBits a non-negative integer indicating the number of bits to be tested.
     * @param comparison the type of comparison be done.
     * @param value the integer to be compared with the part of the register.
     * @param omission an integer indicating the size of the relative jump.
     */
    public Test(String name,
                UUID id,
                Machine machine,
               Register register,
               int start,
               int numBits,
               Operation comparison,
               long value,
               int omission) {
    	super(name, id, machine);
        this.register = new SimpleObjectProperty<>(this, "register", register);
        this.start = new SimpleIntegerProperty(this, "start", start);
        this.numBits = new SimpleIntegerProperty(this, "numBits", numBits);
        this.comparison = new SimpleObjectProperty<>(this, "comparison", comparison);
        this.value = new SimpleLongProperty(this, "value", value);
        this.omission = new SimpleIntegerProperty(this, "omission", omission);
    }
    
    /**
     * Copy Constructor
     * creates a new Test object with input values.
     *
     * @param other Instance to copy
     */
    public Test(Test other) {
        this(other.getName(), IdentifiedObject.generateRandomID(), other.machine,
                other.getRegister(), other.getStart(),
                other.getNumBits(), other.getComparison(),
                other.getValue(), other.getOmission());
    }

    /**
     * returns the name of the set microinstruction as a string.
     * @return the name of the set microinstruction.
     */
    public Register getRegister(){
        return register.get();
    }

    /**
     * updates the register used by the microinstruction.
     * @param newRegister the new selected register for the set microinstruction.
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
     * returns the type of comparison of the test instruction.
     * @return the type of comparison of the test instruction as string.
     */
    public Operation getComparison(){
        return comparison.get();
    }

    /**
     * updates the type of comparison.
     * @param newComparison the new type of the comparison.
     */
    public void setComparison(Operation newComparison){
        comparison.set(newComparison);
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
    public void setValue(long newValue){
        value.set(newValue);
    }

    /**
     * returns the number of bits to jump.
     * @return the integer value of omission.
     */
    public int getOmission(){
        return omission.get();
    }

    /**
     * update the integer indicating the size of the relative jump.
     * @param newOmission the new number of bits to jump.
     */
    public void setOmission(int newOmission){
        omission.set(newOmission);
    }
    
    /**
     * execute the micro instruction from machine
     */
    @Override
    public void execute()
    {
        Operation comparison = this.comparison.get();
        long registerValue = register.get().getValue();
        final ArchValue width = ArchValue.bits(register.get().getWidth());
        final ArchValue numBits = ArchValue.bits(this.numBits.get());
        final ArchValue start = ArchValue.bits(this.start.get());
        
        final ArchValue bits64 = ArchType.Bit.of(64);
        ArchValue leftShift;
        final ArchValue rightShift = bits64.sub(numBits);
        
        //set leftShift differently depending on whether we are indexing from the
        //right or the left
        if (!machine.getIndexFromRight()){
            leftShift = bits64.sub(width).add(start);
        }
        else{
            leftShift = bits64.sub(start).sub(numBits);
        }
        
        //compare both the signed and unsigned values of the segment
        //of the register?  Does that make sense for GT, LT, GE, LE?
        //Just use both values for EQ and NE?
        long signedSegment = (registerValue << leftShift.as()) >> (rightShift.as());
        long unsignedSegment = signedSegment;
        if (numBits.lt(bits64) && signedSegment < 0) {
            unsignedSegment = signedSegment + (1L << numBits.as());
        }
        
        // TODO Replace with Functional approach using Enum
        if (comparison == Operation.EQ) {
            if (signedSegment == value.get() || unsignedSegment == value.get())
                machine.getControlUnit().incrementMicroIndex(omission.get());
        }
        else if (comparison == Operation.NE) {
            if (signedSegment != value.get() && unsignedSegment != value.get())
                machine.getControlUnit().incrementMicroIndex(omission.get());
        }
        else if (comparison == Operation.LT) {
            if (signedSegment < value.get())
                machine.getControlUnit().incrementMicroIndex(omission.get());
        }
        else if (comparison == Operation.GT) {
            if (signedSegment > value.get())
                machine.getControlUnit().incrementMicroIndex(omission.get());
        }
        else if (comparison == Operation.LE) {
            if (signedSegment <= value.get())
                machine.getControlUnit().incrementMicroIndex(omission.get());
        }
        else if(comparison == Operation.GE) {
            
            if (signedSegment >= value.get())
                machine.getControlUnit().incrementMicroIndex(omission.get());
        } else {
        	throw new IllegalStateException("Illegal comparison in Test microinstruction " + getName());
        }
    }
    
    @Override
    public void validate() {
        super.validate();

        final int start = getStart();
        final int numBits = getNumBits();
    
        if (start < 0 || numBits < 0) {
            throw new ValidationException("You cannot specify a negative value for the " +
                    "start bits,\nor the bitwise width of the test range\n" +
                    "in the microinstruction " + getName() + ".");
        }
        else if (start >= getRegister().getWidth()) {
            throw new ValidationException("The start bit in the microinstruction "
                    + getName() + " is out of range.\n" +
                    "It must be non-negative, and less than the " +
                    "register's length.");
        }
        else if ((start + numBits) > getRegister().getWidth()) {
            throw new ValidationException("The bits specified in the Test " +
                    "microinstruction " + getName() +
                    " are too large to fit in the register.");
        }
    }
    
    @Override
    public <U extends Test> void copyTo(U newTest)
    {
        checkNotNull(newTest);
        
        newTest.setName(getName());
        newTest.setRegister(getRegister());
        newTest.setStart(getStart());
        newTest.setNumBits(getNumBits());
        newTest.setComparison(getComparison());
        newTest.setValue(getValue());
        newTest.setOmission(getOmission());
    }

    /**
     * returns true if this microinstruction uses m
     * (so if m is modified, this micro may need to be modified.
     * @param m the module that holds the microinstruction
     * @return boolean value true if this micro used the module
     */
    @Override
    public boolean uses(Module<?> m){
        return (m == register.get());
    }

	@Override
	public String getXMLDescription(String indent) {
		return indent + "<Test name=\"" + getHTMLName() +
                "\" register=\"" + getRegister().getID() +
                "\" start=\"" + getStart() +
                "\" numBits=\"" + getNumBits() +
                "\" comparison=\"" + getComparison() +
                "\" value=\"" + getValue() +
                "\" omission=\"" + getOmission() +
                "\" id=\"" + getID() + "\" />";
	}

	@Override
	public String getHTMLDescription(String indent) {
		return indent + "<TR><TD>" + getHTMLName() + "</TD><TD>" + getRegister().getHTMLName() +
                "</TD><TD>" + getStart() + "</TD><TD>" + getNumBits() +
                "</TD><TD>" + getComparison() + "</TD><TD>" + getValue() +
                "</TD><TD>" + getOmission() + "</TD></TR>";
	}
}