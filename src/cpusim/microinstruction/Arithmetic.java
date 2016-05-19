/**
 * Author: Jinghui Yu
 * Last Editing date: 6/6/2013
 */

package cpusim.microinstruction;

import cpusim.ExecutionException;
import cpusim.Machine;
import cpusim.Microinstruction;
import cpusim.Module;
import cpusim.module.ConditionBit;
import cpusim.module.Register;
import cpusim.util.CPUSimConstants;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.math.BigInteger;

/**
 * The arithmetic microinstruction use three registers and optionally two condition
 * bits.
 */
public class Arithmetic extends Microinstruction {
    private SimpleStringProperty type;
    private SimpleObjectProperty<Register> source1;
    private SimpleObjectProperty<Register> source2;
    private SimpleObjectProperty<Register> destination;
    private SimpleObjectProperty<ConditionBit> overflowBit;
    private SimpleObjectProperty<ConditionBit> carryBit;

    /**
     * Constructor
     * creates a new Increment object with input values.
     *
     * @param name name of the microinstruction.
     * @param machine machine that the microinstruction belongs to.
     * @param type type of logical microinstruction.
     * @param source1 the source1 register.
     * @param source2 the source2 register.
     * @param destination the destination register.
     */
    public Arithmetic(String name, Machine machine,
                   String type,
                   Register source1,
                   Register source2,
                   Register destination,
                   ConditionBit overflowBit,
                   ConditionBit carryBit){
        super(name, machine);
        this.type = new SimpleStringProperty(type);
        this.source1 = new SimpleObjectProperty<>(source1);
        this.source2 = new SimpleObjectProperty<>(source2);
        this.destination = new SimpleObjectProperty<>(destination);
        this.overflowBit = new SimpleObjectProperty<>(overflowBit);
        this.carryBit = new SimpleObjectProperty<>(carryBit);
    }

    /**
     * returns the register to be calculated.
     * @return the name of the register.
     */
    public Register getSource1(){
        return source1.get();
    }

    /**
     * updates the register used by the microinstruction.
     * @param newSource1 the new source register for the logical microinstruction.
     */
    public void setSource1(Register newSource1){
        source1.set(newSource1);
    }

    /**
     * returns the register to be calculated.
     * @return the name of the register.
     */
    public Register getSource2(){
        return source2.get();
    }

    /**
     * updates the register used by the microinstruction.
     * @param newSource2 the new source register for the logical microinstruction.
     */
    public void setSource2(Register newSource2){
        source2.set(newSource2);
    }

    /**
     * returns the register to put result.
     * @return the name of the register.
     */
    public Register getDestination(){
        return destination.get();
    }

    /**
     * updates the register used by the microinstruction.
     * @param newDestination the new destination for the logical microinstruction.
     */
    public void setDestination(Register newDestination){
        destination.set(newDestination);
    }

    /**
     * returns the type of shift.
     * @return type of shift as a string.
     */
    public String getType(){
        return type.get();
    }

    /**
     * updates the type used by the microinstruction.
     * @param newType the new string of type.
     */
    public void setType(String newType){
        type.set(newType);
    }

    /**
     * returns the overflowBit.
     * @return the overflowBit.
     */
    public ConditionBit getOverflowBit(){
        return overflowBit.get();
    }

    /**
     * updates the overflowBit value.
     * @param newOverflowBit the new value
     */
    public void setOverflowBit(ConditionBit newOverflowBit){
        overflowBit.set(newOverflowBit);
    }

    /**
     * returns the carryBit value.
     * @return the carryBit
     */
    public ConditionBit getCarryBit(){
        return carryBit.get();
    }

    /**
     * updates the carryBit
     * @param newCarryBit the new carry bit
     */
    public void setCarryBit(ConditionBit newCarryBit){
        carryBit.set(newCarryBit);
    }

    /**
     * execute the micro instruction from machine
     */
    public void execute()
    {
        long value1 = source1.get().getValue();
        long value2 = source2.get().getValue();
        BigInteger op1 = BigInteger.valueOf(value1);
        BigInteger op2 = BigInteger.valueOf(value2);
        int width = destination.get().getWidth();
        BigInteger twoToWidthMinusOne = BigInteger.valueOf(2).pow(width-1);
        BigInteger result = null;

        if (type.get().equals("ADD"))
            result = op1.add(op2);
        else if (type.get().equals("SUBTRACT"))
            result = op1.subtract(op2);
        else if (type.get().equals("MULTIPLY"))
            result = op1.multiply(op2);
        else if (type.get().equals("DIVIDE"))
            if (op2.compareTo(BigInteger.ZERO) == 0)
                throw new ExecutionException("There was an " +
                        "attempt to divide by 0.");
            else
                result = op1.divide(op2);

        //set overflow bit if necessary
        if (result.compareTo(twoToWidthMinusOne) >= 0 ||
                result.compareTo(twoToWidthMinusOne.negate()) < 0)
            overflowBit.get().set(1);

        //set the carry bit if necessary
        if (type.get().equals("ADD") &&
                ((value1 < 0 && value2 < 0) ||
                        (value1 < 0 && value2 >= -value1) ||
                        (value2 < 0 && value1 >= -value2)))
            carryBit.get().set(1);

        //save the result
        long longResult = result.longValue();
        destination.get().setValue((longResult << (64 - width)) >> (64 - width));
    }
    
    /**
     * returns the class of the microinstruction
     * @return the class of the microinstruction
     */
    @Override
    public String getMicroClass() {
        return "arithmetic";
    }

    /**
     * duplicate the set class and return a copy of the original Set class.
     * @return a copy of the Set class
     */
    public Object clone(){
        return new Arithmetic(getName(), machine, getType(), getSource1(),getSource2(),
                getDestination(),getOverflowBit(),getCarryBit());
    }

    /**
     * copies the data from the current micro to a specific micro
     * @param oldMicro the micro instruction that will be updated
     */
    public void copyDataTo(Microinstruction oldMicro)
    {
        assert oldMicro instanceof Arithmetic :
                "Passed non-Arithmetic to Arithmetic.copyDataTo()";
        Arithmetic newArithmetic = (Arithmetic) oldMicro;
        newArithmetic.setName(getName());
        newArithmetic.setType(getType());
        newArithmetic.setSource1(getSource1());
        newArithmetic.setSource2(getSource2());
        newArithmetic.setDestination(getDestination());
        newArithmetic.setOverflowBit(getOverflowBit());
        newArithmetic.setCarryBit(getCarryBit());
    }

    /**
     * returns the XML description
     * @return the XML description
     */
    public String getXMLDescription(){
        return "<Arithmetic name=\"" + getHTMLName() +
                "\" type=\"" + getType() +
                "\" source1=\"" + getSource1().getID() +
                "\" source2=\"" + getSource2().getID() +
                "\" destination=\"" + getDestination().getID() +
                (overflowBit.get() != CPUSimConstants.NO_CONDITIONBIT ?
                        "\" overflowBit=\"" + getOverflowBit().getID() : "") +
                (carryBit.get() != CPUSimConstants.NO_CONDITIONBIT ?
                        "\" carryBit=\"" + getCarryBit().getID() : "") +
                "\" id=\"" + getID() + "\" />";
    }

    /**
     * returns the HTML description
     * @return the HTML description
     */
    public String getHTMLDescription(){
        return "<TR><TD>" + getHTMLName() + "</TD><TD>" + getType() +
                "</TD><TD>" + getSource1().getHTMLName() + "</TD><TD>" +
                getSource2().getHTMLName() +
                "</TD><TD>" + getDestination().getHTMLName() + "</TD><TD>" +
                getOverflowBit().getHTMLName() +
                "</TD><TD>" + getCarryBit().getHTMLName() + "</TD></TR>";
    }

    /**
     * returns true if this microinstruction uses m
     * (so if m is modified, this micro may need to be modified.
     * @param m the module that holds the microinstruction
     * @return boolean value true if this micro used the module
     */
    public boolean uses(Module m){
        return false;
    }
}
