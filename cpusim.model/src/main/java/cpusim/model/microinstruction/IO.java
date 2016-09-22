/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 *
 * 1) Added undoExecute to back up the IOChannel.
 */

package cpusim.model.microinstruction;

import cpusim.iochannel.FileChannel;
import cpusim.iochannel.IOChannel;
import cpusim.model.Machine;
import cpusim.model.Microinstruction;
import cpusim.model.Module;
import cpusim.model.module.Register;
import cpusim.util.CPUSimConstants;
import cpusim.xml.HtmlEncoder;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * The logical microinstructions perform the bit operations of AND, OR, NOT, NAND,
 * NOR, or XOR on the specified registers.
 */
public class IO
        extends Microinstruction implements CPUSimConstants{
    private SimpleStringProperty type;
    private SimpleObjectProperty<Register> buffer;
    private SimpleStringProperty direction;
    private IOChannel connection;

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
    public IO(String name, Machine machine,
              String type,
              Register buffer,
              String direction,
              IOChannel connection){
        super(name, machine);
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
    public IO(String name, Machine machine, String type, Register buffer, String direction)
    {
        super(name, machine);
        this.type = new SimpleStringProperty(type);
        this.buffer = new SimpleObjectProperty<>(buffer);
        this.direction = new SimpleStringProperty(direction);
        this.connection = CONSOLE_CHANNEL;
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
    
    /**
     * returns the class of the microinstruction
     * @return the class of the microinstruction
     */
    @Override
    public String getMicroClass(){
        return "io";
    }

    /**
     * duplicate the set class and return a copy of the original Set class.
     * @return a copy of the Set class
     */
    public Object clone(){
        return new IO(getName(),machine,getType(), getBuffer(),getDirection(),getConnection());
    }

    /**
     * copies the data from the current micro to a specific micro
     * @param oldMicro the micro instruction that will be updated
     */
    public void copyTo(Microinstruction oldMicro)
    {
        assert oldMicro instanceof IO :
                "Passed non-IO to IO.copyDataTo()";
        IO newIO = (IO) oldMicro;
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
            char c = connection.readUnicode();
            buffer.get().setValue((int) c);
        }
        else if (type.get().equals("integer") && direction.get().equals("output")) {
            connection.writeLong(buffer.get().getValue());
        }
        else if (type.get().equals("ascii") && direction.get().equals("output")) {
            connection.writeAscii(buffer.get().getValue());
        }
        else if (type.get().equals("unicode") && direction.get().equals("output")) {
            connection.writeUnicode(buffer.get().getValue());
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
     * returns the XML description
     * @return the XML description
     */
    public String getXMLDescription(){
        return "<IO name=\"" + getHTMLName() +
                "\" direction=\"" + getDirection() +
                "\" type=\"" + getType() +
                "\" buffer=\"" + getBuffer().getID() +
                "\" connection=\"" + getConnection() +
                "\" id=\"" + getID() + "\" />";
    }

    /**
     * returns the HTML description
     * @return the HTML description
     */
    public String getHTMLDescription(){
        return "<TR><TD>" + getHTMLName() +
                "</TD><TD>" + getDirection() +
                "</TD><TD>" + getType() +
                "</TD><TD>" + getBuffer().getHTMLName() +
                "</TD><TD>" + HtmlEncoder.sEncode(getConnection().toString()) +
                "</TD></TR>";
    }

    /**
     * returns true if this microinstruction uses m
     * (so if m is modified, this micro may need to be modified.
     * @param m the module that holds the microinstruction
     * @return boolean value true if this micro used the module
     */
    public boolean uses(Module m){
        return (m == buffer.get());
    }
}
