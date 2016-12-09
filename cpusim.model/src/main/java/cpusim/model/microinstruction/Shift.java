package cpusim.model.microinstruction;

import cpusim.model.Machine;
import cpusim.model.module.Register;
import cpusim.model.util.MachineComponent;
import cpusim.model.util.ValidationException;
import javafx.beans.property.*;

import java.util.UUID;

import static com.google.common.base.Preconditions.*;

/**
 * The shift microinstruction performs a bit-wise shift of the contents of the
 * specified source register to either the left ot the right and places the result
 * in the destination register.
 */
public class Shift extends Transfer<Register, Register, Shift> {

    /**
     * Type of shift
     */
    public enum ShiftType {
        Arithmetic,
        Logical,
        Cyclic
    }

    public enum ShiftDirection {
        Left, Right
    }

    private final ObjectProperty<ShiftType> type;
    private final ObjectProperty<ShiftDirection> direction;
    private final IntegerProperty distance;

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
    public Shift(String name,
                 UUID id,
                 Machine machine,
                 Register source,
                 Register destination,
                 ShiftType type,
                 ShiftDirection direction,
                 int distance){
        super(name, id, machine, source, 0, destination, 0, source.getWidth());
        
        this.type = new SimpleObjectProperty<>(this, "type", checkNotNull(type));
        this.direction = new SimpleObjectProperty<>(this, "direction", checkNotNull(direction));
        this.distance = new SimpleIntegerProperty(this, "distance", distance);

        this.dependencies = MachineComponent.collectDependancies(this)
                .buildSet(this, "dependencies");
    }
    
    /**
     * Constructor
     * creates a new Increment object with input values.
     *
     * @param other instance to copy
     */
    public Shift(Shift other){
        this(other.getName(), UUID.randomUUID(), other.getMachine(),
                other.getSource(), other.getDest(), other.getType(),
                other.getDirection(), other.getDistance());
    }

    /**
     * returns the type of shift.
     * @return type of shift as a string.
     */
    public ShiftType getType(){
        return type.get();
    }

    /**
     * updates the type used by the microinstruction.
     * @param newType the new string of type.
     */
    public void setType(ShiftType newType){
        type.set(checkNotNull(newType));
    }

    public ObjectProperty<ShiftType> typeProperty() {
        return type;
    }

    /**
     * returns the type of shift.
     * @return type of shift as a string.
     */
    public ShiftDirection getDirection(){
        return direction.get();
    }

    /**
     * updates the type used by the microinstruction.
     * @param newDirection the new string of type.
     */
    public void setDirection(ShiftDirection newDirection){
        direction.set(checkNotNull(newDirection));
    }

    public ObjectProperty<ShiftDirection> directionProperty() {
        return direction;
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

    public IntegerProperty distanceProperty() {
        return distance;
    }
    
    @Override
    public void validate() {
        super.validate();
    
        // checks if the two registers specified in the shift microinstructions have the same width
        if (getSource().getWidth() != getDest().getWidth()) {
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
    public void execute() {
        long width = source.get().getWidth();
        long value = source.get().getValue() << (64 - width);

        long distance = this.distance.get();

        switch (direction.get()) {
            case Left: {
                switch (type.get()) {

                    case Logical:
                    case Arithmetic:  {
                        value <<= distance;
                        value >>= 64 - width;
                    } break;

                    case Cyclic: {
                        long temp = value;
                        value <<= distance;
                        temp >>>= width - distance;
                        value |= temp;
                        value >>= (64 - width);
                    } break;

                    default: {
                        throw new IllegalStateException("Unknown shift type: " + type.get());
                    }
                }
            } break;

            case Right: {
                switch (type.get()) {

                    case Logical: {
                        value >>>= 64 - width;
                        value >>>= distance;
                    } break;

                    case Arithmetic:  {
                        value >>= 64 - width;
                        value >>= distance;
                    } break;

                    case Cyclic: {
                        long temp = value;
                        value >>>= distance;
                        temp <<= width - distance;
                        value |= temp;
                        value >>= 64 - width;
                    } break;

                    default: {
                        throw new IllegalStateException("Unknown shift type: " + type.get());
                    }
                }
            } break;

            default:
                throw new IllegalStateException("Unknown shift direction: " + direction.get());
        }

        dest.get().setValue(value);
    }


    @Override
    public Shift cloneFor(IdentifierMap oldToNew) {
        return new Shift(getName(), UUID.randomUUID(), getMachine(),
                oldToNew.get(getSource()), oldToNew.get(getDest()),
                getType(), getDirection(), getDistance());
    }

    @Override
    public <U extends Shift> void copyTo(U other) {
        super.copyTo(other);

        other.setType(getType());
        other.setDirection(getDirection());
        other.setDistance(getDistance());
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
