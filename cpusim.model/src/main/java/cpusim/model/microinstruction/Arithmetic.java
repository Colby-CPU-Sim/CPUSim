package cpusim.model.microinstruction;

import com.google.common.base.Strings;
import cpusim.model.ExecutionException;
import cpusim.model.Machine;
import cpusim.model.Module;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.Register;
import cpusim.model.util.IdentifiedObject;
import cpusim.model.util.ValidationException;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.math.BigInteger;
import java.util.UUID;

import static com.google.common.base.Preconditions.*;

/**
 * The arithmetic microinstruction use three registers and optionally two condition
 * bits performing arithmetic operations (e.g. add, subtract).
 *
 * @since 2013-06-06
 */
public class Arithmetic extends Microinstruction<Arithmetic> {
    
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
        this(name, IdentifiedObject.generateRandomID(),
                machine, type,
                source1, source2,
                destination, overflowBit,
                carryBit);
    }
    
    /**
     * Constructor
     * creates a new Increment object with input values.
     *
     * @param name name of the microinstruction.
     * @param id UUID for the microinstruction
     * @param machine machine that the microinstruction belongs to.
     * @param type type of logical microinstruction.
     * @param source1 the source1 register.
     * @param source2 the source2 register.
     * @param destination the destination register.
     */
    public Arithmetic(String name,
                      UUID id,
                      Machine machine,
                      String type,
                      Register source1,
                      Register source2,
                      Register destination,
                      ConditionBit overflowBit,
                      ConditionBit carryBit){
        super(name, id, machine);
        this.type = new SimpleStringProperty(type);
        this.source1 = new SimpleObjectProperty<>(source1);
        this.source2 = new SimpleObjectProperty<>(source2);
        this.destination = new SimpleObjectProperty<>(destination);
        this.overflowBit = new SimpleObjectProperty<>(overflowBit);
        this.carryBit = new SimpleObjectProperty<>(carryBit);
    }
    
    /**
     * Copy constructor.
     *
     * @param other Copied from.
     */
    public Arithmetic(Arithmetic other) {
        this(other.getName(),
                other.machine,
                other.getType(),
                other.getSource1(),
                other.getSource2(),
                other.getDestination(),
                other.getOverflowBit(),
                other.getCarryBit());
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
    @Override
    public void execute() {
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
        else if (type.get().equals("DIVIDE")) {
            if (op2.compareTo(BigInteger.ZERO) == 0) {
                throw new ExecutionException("There was an attempt to divide by 0.");
            } else {
                result = op1.divide(op2);
            }
        } else {
        	throw new ExecutionException("Unknown arithmetic operation.");
        }

        //set overflow bit if necessary
        if (result.compareTo(twoToWidthMinusOne) >= 0 ||
                result.compareTo(twoToWidthMinusOne.negate()) < 0)
            overflowBit.get().set(1);
        else
            overflowBit.get().set(0);

        //set the carry bit if necessary
        if (type.get().equals("ADD") &&
                ((value1 < 0 && value2 < 0) ||
                        (value1 < 0 && value2 >= -value1) ||
                        (value2 < 0 && value1 >= -value2)))
            carryBit.get().set(1);
        else
            carryBit.get().set(0);

        //save the result
        long longResult = result.longValue();
        destination.get().setValue((longResult << (64 - width)) >> (64 - width));
    }
    
    @Override
    public <U extends Arithmetic> void copyTo(final U newArithmetic) {
        checkNotNull(newArithmetic);
        
        newArithmetic.setName(getName());
        newArithmetic.setType(getType());
        newArithmetic.setSource1(getSource1());
        newArithmetic.setSource2(getSource2());
        newArithmetic.setDestination(getDestination());
        newArithmetic.setOverflowBit(getOverflowBit());
        newArithmetic.setCarryBit(getCarryBit());
    }
    
    @Override
    public void validateState() {
        if (Strings.isNullOrEmpty(type.getValue())) {
            throw new ValidationException("The arithmetic operation is not specified.");
        }
        
        
    }
    
    /**
     * returns the XML description
     * @return the XML description
     */
    @Override
    public String getXMLDescription(String indent) {
        return indent + "<Arithmetic name=\"" + getHTMLName() +
                "\" type=\"" + getType() +
                "\" source1=\"" + getSource1().getID() +
                "\" source2=\"" + getSource2().getID() +
                "\" destination=\"" + getDestination().getID() +
                (overflowBit.get() !=  ConditionBit.none() ?
                        "\" overflowBit=\"" + getOverflowBit().getID() : "") +
                (carryBit.get() != ConditionBit.none() ?
                        "\" carryBit=\"" + getCarryBit().getID() : "") +
                "\" id=\"" + getID() + "\" />";
    }

    @Override
    public String getHTMLDescription(String indent){
        return indent + "<TR><TD>" + getHTMLName() + "</TD><TD>" + getType() +
                "</TD><TD>" + getSource1().getHTMLName() + "</TD><TD>" +
                getSource2().getHTMLName() +
                "</TD><TD>" + getDestination().getHTMLName() + "</TD><TD>" +
                getOverflowBit().getHTMLName() +
                "</TD><TD>" + getCarryBit().getHTMLName() + "</TD></TR>";
    }

    @Override
    public boolean uses(Module<?> m){
        return (m == getSource1() || m == getSource2() || m == getDestination()
                || m == getCarryBit() || m == getOverflowBit());
    }
}
