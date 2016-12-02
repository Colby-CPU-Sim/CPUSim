package cpusim.model.microinstruction;

import cpusim.model.Machine;
import cpusim.model.Module;
import cpusim.model.iochannel.FileChannel;
import cpusim.model.iochannel.IOChannel;
import cpusim.model.iochannel.StreamChannel;
import cpusim.model.module.Register;
import cpusim.model.util.IdentifiedObject;
import cpusim.model.util.ValidationException;
import cpusim.model.util.units.ArchType;
import cpusim.xml.HtmlEncoder;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.UUID;

import static com.google.common.base.Preconditions.*;

/**
 * Reads/writes to an {@link IOChannel}.
 */
public class IO extends Microinstruction<IO> {
    
    private SimpleStringProperty type; // FIXME Enum
    private SimpleObjectProperty<Register> buffer;
    private SimpleStringProperty direction; // FIXME Enum
    private IOChannel connection;

    /**
     * Constructor
     * creates a new {@link IOChannel} object with input values.
     *
     * @param name name of the microinstruction.
     * @param machine the machine that the microinstruction belongs to.
     * @param type type of logical microinstruction.
     * @param buffer the source1 register.
     * @param direction the destination register.
     * @param connection the IOChannel used
     */
    public IO(String name, Machine machine,
              String type,
              Register buffer,
              String direction,
              IOChannel connection){
        this(name, IdentifiedObject.generateRandomID(), machine, type, buffer, direction, connection);
    }
    
    /**
     * Constructor
     * creates a new Increment object with input values.
     *
     * @param name name of the microinstruction.
     * @param machine the machine that the microinstruction belongs to.
     * @param type type of logical microinstruction.
     * @param buffer the source1 register.
     * @param direction the destination register.
     * @param connection the IOChannel used
     */
    public IO(String name,
              UUID id,
              Machine machine,
              String type,
              Register buffer,
              String direction,
              IOChannel connection){
        super(name, id, machine);
        this.type = new SimpleStringProperty(type);
        this.buffer = new SimpleObjectProperty<>(buffer);
        this.direction = new SimpleStringProperty(direction);
        this.connection = connection;
    }

    /**
     * Constructor
     * creates a new Increment object with input values.
     *
     * @param name name of the microinstruction.
     * @param type type of logical microinstruction.
     * @param buffer the source1 register.
     * @param direction the destination register.
     */
    public IO(String name, Machine machine, String type, Register buffer, String direction) {
        this(name, IdentifiedObject.generateRandomID(), machine, type, buffer, direction, new StreamChannel());
    }
    
    /**
     * Copy constructor
     * @param other instance to copy from
     */
    public IO(IO other) {
        this(other.getName(),
                other.machine,
                other.getType(),
                other.getBuffer(),
                other.getDirection(),
                other.getConnection());
    }

    /**
     * returns the register to be calculated.
     * @return the name of the register.
     */
    public Register getBuffer(){
        return buffer.get();
    }

    /**
     * updates the register used by the microinstruction.
     * @param newBuffer the new source register for the logical microinstruction.
     */
    public void setBuffer(Register newBuffer){
        buffer.set(newBuffer);
    }

    /**
     * returns the register to put result.
     * @return the name of the register.
     */
    public String getDirection(){
        return direction.get();
    }

    /**
     * updates the register used by the microinstruction.
     * @param newDirection the new destination for the logical microinstruction.
     */
    public void setDirection(String newDirection){
        direction.set(newDirection);
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
     * getter for the IOChannel
     * @return the IOChannel
     */
    public IOChannel getConnection(){
        return this.connection;
    }

    /**
     * setter for the IOChannel
     * @param newConnection new Channel to be set
     */
    public void setConnection(IOChannel newConnection){
        this.connection = newConnection;
    }
    
    @Override
    public <U extends IO> void copyTo(U newIO)
    {
        checkNotNull(newIO);
        newIO.setName(getName());
        newIO.setDirection(getDirection());
        newIO.setType(getType());
        newIO.setBuffer(getBuffer());
        newIO.setConnection(getConnection());
    }

    /**
     * execute the micro instruction from machine
     */
    public void execute()
    {
        int numBits = buffer.get().getWidth();

        if (type.get().equals("integer") && direction.get().equals("input")) {
            long inputLong = connection.readLong(numBits);
            buffer.get().setValue(inputLong);
        }
        else if (type.get().equals("ascii") && direction.get().equals("input")) {
            char c = connection.readAscii();
            buffer.get().setValue((int) c);
        }
        else if (type.get().equals("unicode") && direction.get().equals("input")) {
            int c = connection.readUnicode();
            buffer.get().setValue(c);
        }
        else if (type.get().equals("integer") && direction.get().equals("output")) {
            connection.writeLong(buffer.get().getValue());
        }
        else if (type.get().equals("ascii") && direction.get().equals("output")) {
            connection.writeAscii((char) (buffer.get().getValue() & ArchType.Byte.getMask(1)));
        }
        else if (type.get().equals("unicode") && direction.get().equals("output")) {
            connection.writeUnicode((int) buffer.get().getValue());
        }
        else
            assert false : "IO '" + getName() + "' has an illegal " +
                    "type or direction";
        
        //addded on 3/20 by Ben Borchard because outputs wouldn't print to the console.
        //I am not sure that this is the correct thing to do...
        connection.flush(true);

    }

    /**
     * undo the execution of the micro instruction from machine
     */
    public void undoExecute(){
        if (connection instanceof FileChannel){
            FileChannel connect = (FileChannel) connection;
            if (type.get().equals("integer") && direction.get().equals("input")) {
                connect.unReadLong();
            }
            else if ((type.get().equals("ascii")||type.get().equals("unicode"))
                    && direction.get().equals("input")) {
                connect.unReadOneChar();
            }
            else if (type.get().equals("integer") && direction.get().equals("output")) {
                connect.unWriteLong();
            }
            else if ((type.get().equals("ascii") ||type.get().equals("unicode"))
                    && direction.get().equals("output")) {
                connect.unWriteOneChar();
            }
        }

    }

    /**
     * returns true if this microinstruction uses m
     * (so if m is modified, this micro may need to be modified.
     * @param m the module that holds the microinstruction
     * @return boolean value true if this micro used the module
     */
    @Override
    public boolean uses(Module<?> m){
        return (m == buffer.get());
    }

    /**
     * returns the XML description
     * @return the XML description
     */
	@Override
	public String getXMLDescription(String indent) {
		return indent + "<IO name=\"" + getHTMLName() +
                "\" direction=\"" + getDirection() +
                "\" type=\"" + getType() +
                "\" buffer=\"" + getBuffer().getID() +
                "\" connection=\"" + getConnection() +
                "\" id=\"" + getID() + "\" />";
	}

	@Override
	public String getHTMLDescription(String indent) {
		return indent + "<TR><TD>" + getHTMLName() +
                "</TD><TD>" + getDirection() +
                "</TD><TD>" + getType() +
                "</TD><TD>" + getBuffer().getHTMLName() +
                "</TD><TD>" + HtmlEncoder.sEncode(getConnection().toString()) +
                "</TD></TR>";
	}

    /**
     * check if the ios of type ascii have 8-bit-wide
     * (or greater) buffers and ios of type unicode have
     * 16-bit-wide (or greater) buffers.
     */
    @Override
    protected void validateState() {
        if (getType().equals("ascii") && getBuffer().getWidth() < 1) {
            throw new ValidationException("IO \"" + this + "\" is of type " +
                    "ascii and so needs a\nbuffer register at least " +
                    "8 bits wide.");
        }
        else if (getType().equals("unicode") && getBuffer().getWidth() < 2) {
            throw new ValidationException("IO \"" + this + "\" is of type " +
                    "unicode and so needs a\nbuffer register at least " +
                    "16 bits wide.");
        }
    }
}
