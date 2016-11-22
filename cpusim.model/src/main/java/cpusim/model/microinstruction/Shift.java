/**
 * Author: Jinghui Yu
 * last editing date: 6/6/2013
 */

/*
 * Michael Goldenberg, Ben Borchard, and Jinghui Yu made the following changes in 11/6/13
 * 
 * 1.) Modified the getMicroClass() method so that it returns "shift" instead of "clone"
 * 
 */

package cpusim.model.microinstruction;

import cpusim.model.Machine;
import cpusim.model.module.Register;
import cpusim.model.util.Copyable;
import cpusim.model.util.ValidationException;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import static com.google.common.base.Preconditions.*;

/**
 * The shift microinstruction performs a bit-wise shift of the contents of the
 * specified source register to either the left ot the right and places the result
 * in the destination register.
 */
public class Shift extends Transfer<Register, Register> implements Copyable<Shift> {
    
    // TODO Enumeration
    private SimpleStringProperty type;
    
    // TODO Enumeration
    private SimpleStringProperty direction;
    private SimpleIntegerProperty distance;

    /**
     * Constructor
     * creates a new Increment object with input values.
     *
     * @param name name of the microinstruction.
     * @param machine the machine that the microinstruction belongs to.
     * @param source the source register.
     * @param destination the destination register.
     * @param type type of shift.
     * @param direction left or right shift.
     * @param distance number of bits to be shifted.
     */
    public Shift(String name, Machine machine,
                 Register source,
                 Register destination,
                 String type,
                 String direction,
                 int distance){
        super(name, machine, source, 0, destination, 0, source.getWidth());
        
        this.type = new SimpleStringProperty(type);
        this.direction = new SimpleStringProperty(direction);
        this.distance = new SimpleIntegerProperty(distance);
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
     * returns the type of shift.
     * @return type of shift as a string.
     */
    public String getDirection(){
        return direction.get();
    }

    /**
     * updates the type used by the microinstruction.
     * @param newDirection the new string of type.
     */
    public void setDirection(String newDirection){
        direction.set(newDirection);
    }

    /**
     * returns the value of distance.
     * @return the distance as an integer.
     */
    public int getDistance(){
        return distance.get();
    }

    /**
     * updates the value of distance.
     * @param newDistance the new integer of distance.
     */
    public void setDistance(int newDistance){
        distance.set(newDistance);
    }
    
    /**
     * returns the class of the microinstruction
     * @return the class of the microinstruction
     */
    @Override
    public String getMicroClass(){
        return "shift";
    }
    
    @Override
    public <U extends Shift> void copyTo(final U other) {
        checkNotNull(other);
    
        copyToHelper(other);
    
        other.setType(getType());
        other.setDirection(getDirection());
        other.setDistance(getDistance());
    }
    
    @Override
    protected void validateState() {
        super.validateState();
    
        // checks if the two registers specified in the shift microinstructions have the same width
        if (getSource().getWidth() !=
                getDest().getWidth()) {
            throw new ValidationException("The microinstruction " + getName() +
                    " has different-sized registers designated " +
                    "for source and destination.\nBoth registers " +
                    "must have the same number of bits.");
        }
    
        // checks the array of Shift micros to make sure none have
        // a negative shift distances
        if (getDistance() <= 0) {
            throw new ValidationException("The microinstruction \"" + getName() +
                    "\" has a negative or zero shift distance.\nShift distances " +
                    "must be positive.");
        }
    }
    
    /**
     * execute the micro instruction from machine
     */
    @Override
    public void execute()
    {
        long width = source.get().getWidth();
        long value = source.get().getValue() << (64 - width);

        if (type.get().equals("logical") && direction.get().equals("left")) {
            value = value << distance.get();
            value = value >> (64 - width);
        }
        else if (type.get().equals("logical") && direction.get().equals("right")) {
            value = value >>> (64 - width);
            value = value >>> distance.get();
        }
        else if (type.get().equals("arithmetic") && direction.get().equals("left")) {
            value = value << distance.get();
            value = value >> (64 - width);
        }
        else if (type.get().equals("arithmetic") && direction.get().equals("right")) {
            value = value >> (64 - width);
            value = value >> distance.get();
        }
        else if (type.get().equals("cyclic") && direction.get().equals("left")) {
            long temp = value;
            value = value << distance.get();
            temp = temp >>> (width - distance.get());
            value = value | temp;
            value = value >> (64 - width);
        }
        else {
            assert type.get().equals("cyclic") &&
                    direction.get().equals("right") : "Illegal type " +
                    "or direction in Shift micro " +
                    getName();
            long temp = value;
            value = value >>> distance.get();
            temp = temp << (width - distance.get());
            value = value | temp;
            value = value >> (64 - width);
        }
        dest.get().setValue(value);
    }

    /**
     * returns the XML description
     * @return the XML description
     */
    @Override
    public String getXMLDescription(String indent){
        return indent + "<Shift name=\"" + getHTMLName() +
                "\" type=\"" + getType() +
                "\" source=\"" + getSource().getID() +
                "\" destination=\"" + getDest().getID() +
                "\" direction=\"" + getDirection() +
                "\" distance=\"" + getDistance() +
                "\" id=\"" + getID() + "\" />";
    }

    /**
     * returns the HTML description
     * @return the HTML description
     */
    @Override
    public String getHTMLDescription(String indent){
        return indent + "<TR><TD>" + getHTMLName() + "</TD><TD>" + getSource().getHTMLName() +
                "</TD><TD>" + getDest().getHTMLName() + "</TD><TD>" + getType() +
                "</TD><TD>" + getDirection() +
                "</TD><TD>" + getDistance() + "</TD></TR>";
    }
}
