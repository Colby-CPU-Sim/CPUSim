package cpusim.model.microinstruction;

import cpusim.model.Machine;
import cpusim.model.Module;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.Register;
import cpusim.model.util.IdentifiedObject;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.math.BigInteger;
import java.util.UUID;

import static com.google.common.base.Preconditions.*;

/**
 * The increment microinstrucion adds an integer constant to the contents of a {@link Register}.
 *
 * @since 2013-06-06
 */
public class Increment extends Microinstruction<Increment> {
    
    private SimpleObjectProperty<Register> register;
    private SimpleObjectProperty<ConditionBit> overflowBit;
    private SimpleObjectProperty<ConditionBit> carryBit;
    private SimpleLongProperty delta;

    /**
     * Constructor
     * creates a new Increment object with input values.
     *
     * @param name name of the microinstruction.
     * @param id Unique ID for the machine
     * @param machine the machine that the microinstruction belongs to.
     * @param register the register whose value is to be incremented.
     * @param overflowBit a condition bit.
     * @param delta the integer value what will be added to the register contents.
     */
    public Increment(String name,
                     UUID id,
                     Machine machine,
                     Register register,
                     ConditionBit overflowBit,
                     ConditionBit carryBit,
                     long delta){
        super(name, id, machine);
        this.register = new SimpleObjectProperty<>(register);
        this.overflowBit = new SimpleObjectProperty<>(overflowBit);
        this.carryBit = new SimpleObjectProperty<>(carryBit);
        this.delta = new SimpleLongProperty(delta);
    }
    
    /**
     * Constructor
     * creates a new Increment object with input values.
     *
     * @param name name of the microinstruction.
     * @param machine the machine that the microinstruction belongs to.
     * @param register the register whose value is to be incremented.
     * @param overflowBit a condition bit.
     * @param delta the integer value what will be added to the register contents.
     */
    public Increment(String name,
                     Machine machine,
                     Register register,
                     ConditionBit overflowBit,
                     ConditionBit carryBit,
                     long delta){
        this(name, IdentifiedObject.generateRandomID(), machine, register, overflowBit, carryBit, delta);
    }
    
    /**
     * Copy constructor
     * @param other instance to copy from
     */
    public Increment(Increment other) {
        this(other.getName(),
                other.machine,
                other.getRegister(),
                other.getOverflowBit(),
                other.getCarryBit(),
                other.getDelta());
    }
    
    /**
     * returns the register to be incremented.
     * @return the name of the register.
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
     * returns the status of recording overflowBit.
     *
     * @return the status of recording overflowBit as a string.
     */
    public ConditionBit getOverflowBit(){
        return overflowBit.get();
    }

    /**
     * updates the status of whether recording the overflow.
     *
     * @param newOverflowBit the new string for the status.
     */
    public void setOverflowBit(ConditionBit newOverflowBit){
        overflowBit.set(newOverflowBit);
    }

    /**
     * returns the status of recording carryBit.
     *
     * @return the status of recording carryBit as a string.
     */
    public ConditionBit getCarryBit() { return carryBit.get(); }

    /**
     * updates the status of the carryBit.
     *
     * @param newCarryBit the new string for the status.
     */
    public void setCarryBit(ConditionBit newCarryBit) { carryBit.set(newCarryBit);}

    /**
     * returns the fixed value stored in the set microinstruction.
     * @return the integer value of the field.
     */
    public long getDelta(){
        return delta.get();
    }

    /**
     * updates the fixed value stored in the set microinstruction.
     * @param newDelta the new value for the field.
     */
    public void setDelta(long newDelta){
        delta.set(newDelta);
    }
    
    @Override
    public <U extends Increment> void copyTo(final U newIncr) {
        checkNotNull(newIncr);
        newIncr.setName(getName());
        newIncr.setRegister(getRegister());
        newIncr.setOverflowBit(getOverflowBit());
        newIncr.setCarryBit(getCarryBit());
        newIncr.setDelta(getDelta());
    }
    
    @Override
    protected void validateState() {
        
    }
    
    /**
     * execute the micro instruction from machine
     */
    @Override
    public void execute()
    {
        BigInteger bigValue = BigInteger.valueOf(register.get().getValue());
        BigInteger bigDelta = BigInteger.valueOf(delta.get());
        BigInteger bigResult;

        bigResult = bigValue.add(bigDelta);

        //handle overflow
        int width = register.get().getWidth();
        BigInteger twoToWidthMinusOne = BigInteger.valueOf(2).pow(width - 1);
        if (bigResult.compareTo(twoToWidthMinusOne) >= 0 ||
                bigResult.compareTo(twoToWidthMinusOne.negate()) < 0)
            overflowBit.get().set(1);
        else
            overflowBit.get().set(0);

        if ((bigValue.intValue() < 0 && bigDelta.intValue() < 0) ||
                        (bigValue.intValue() < 0 && bigDelta.intValue() >= -bigValue.intValue()) ||
                        (bigDelta.intValue() < 0 && bigValue.intValue() >= -bigDelta.intValue()))
            carryBit.get().set(1);
        else
            carryBit.get().set(0);

        //set destination's value to the result
        long result = bigResult.longValue();
        register.get().setValue((result << (64 - width)) >> (64 - width));
    }

    /**
     * returns the XML description
     * @return the XML description
     */
    @Override
    public String getXMLDescription(String indent){
        return indent + "<Increment name=\"" + getHTMLName() +
                "\" register=\"" + getRegister().getID() +
                (overflowBit.get() != ConditionBit.none() ?
                        "\" overflowBit=\"" + getOverflowBit().getID() : "") +
                (carryBit.get() != ConditionBit.none() ?
                "\" carryBit=\"" + getCarryBit().getID() : "") +
                "\" delta=\"" + getDelta() +
                "\" id=\"" + getID() + "\" />";
    }

    /**
     * returns the HTML description
     * @return the HTML description
     */
    @Override
    public String getHTMLDescription(String indent) {
        return indent + "<TR><TD>" + getHTMLName() + "</TD><TD>" + getRegister().getHTMLName() +
                "</TD><TD>" + getOverflowBit().getHTMLName() +
                "</TD><TD>" + getCarryBit().getHTMLName() +
                "</TD><TD>" + getDelta() + "</TD></TR>";
    }

    /**
     * returns true if this microinstruction uses m
     * (so if m is modified, this micro may need to be modified.
     * @param m the module that holds the microinstruction
     * @return boolean value true if this micro used the module
     */
    @Override
    public boolean uses(Module<?> m){
        return (m == register.get() ||
                m == overflowBit.get() ||
                m == carryBit.get());
    }
}
