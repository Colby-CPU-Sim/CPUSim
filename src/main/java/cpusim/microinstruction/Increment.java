/**
 * Author: Jinghui Yu
 * Last editing date: 6/6/2013
 */

package cpusim.microinstruction;

import cpusim.Machine;
import cpusim.Microinstruction;
import cpusim.Module;
import cpusim.module.ConditionBit;
import cpusim.module.Register;
import cpusim.util.CPUSimConstants;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.math.BigInteger;

/**
 * The increment microinstrucion adds an integer constant to the contents of a register.
 */
public class Increment extends Microinstruction{
    private SimpleObjectProperty<Register> register;
    private SimpleObjectProperty<ConditionBit> overflowBit;
    private SimpleObjectProperty<ConditionBit> carryBit;
    private SimpleLongProperty delta;

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
    public Increment(String name, Machine machine,
                     Register register,
                     ConditionBit overflowBit,
                     ConditionBit carryBit,
                     Long delta){
        super(name, machine);
        this.register = new SimpleObjectProperty<>(register);
        this.overflowBit = new SimpleObjectProperty<>(overflowBit);
        this.carryBit = new SimpleObjectProperty<>(carryBit);
        this.delta = new SimpleLongProperty(delta);
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
    public Long getDelta(){
        return delta.get();
    }

    /**
     * updates the fixed value stored in the set microinstruction.
     * @param newDelta the new value for the field.
     */
    public void setDelta(Long newDelta){
        delta.set(newDelta);
    }
    
    /**
     * returns the class of the microinstruction
     * @return the class of the microinstruction
     */
    @Override
    public String getMicroClass(){
        return "increment";
    }

    /**
     * duplicate the set class and return a copy of the original Set class.
     * @return a copy of the Set class
     */
    public Object clone(){
        return new Increment(getName(),machine,getRegister(),getOverflowBit(),getCarryBit(),getDelta());
    }

    /**
     * copies the data from the current micro to a specific micro
     * @param oldMicro the micro instruction that will be updated
     */
    public void copyDataTo(Microinstruction oldMicro)
    {
        assert oldMicro instanceof Increment :
                "Passed non-Increment to Increment.copyDataTo()";
        Increment newIncr = (Increment) oldMicro;
        newIncr.setName(getName());
        newIncr.setRegister(getRegister());
        newIncr.setOverflowBit(getOverflowBit());
        newIncr.setCarryBit(getCarryBit());
        newIncr.setDelta(getDelta());
    }

    /**
     * execute the micro instruction from machine
     */
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
    public String getXMLDescription(){
        return "<Increment name=\"" + getHTMLName() +
                "\" register=\"" + getRegister().getID() +
                (overflowBit.get() != CPUSimConstants.NO_CONDITIONBIT ?
                        "\" overflowBit=\"" + getOverflowBit().getID() : "") +
                (carryBit.get() != CPUSimConstants.NO_CONDITIONBIT ?
                "\" carryBit=\"" + getCarryBit().getID() : "") +
                "\" delta=\"" + getDelta() +
                "\" id=\"" + getID() + "\" />";
    }

    /**
     * returns the HTML description
     * @return the HTML description
     */
    public String getHTMLDescription(){
        return "<TR><TD>" + getHTMLName() + "</TD><TD>" + getRegister().getHTMLName() +
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
    public boolean uses(Module m){
        return (m == register.get() ||
                m == overflowBit.get() ||
                m == carryBit.get());
    }
}
