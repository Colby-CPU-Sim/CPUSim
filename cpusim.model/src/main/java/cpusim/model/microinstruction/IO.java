package cpusim.model.microinstruction;

import cpusim.model.Machine;
import cpusim.model.iochannel.FileChannel;
import cpusim.model.iochannel.IOChannel;
import cpusim.model.module.Module;
import cpusim.model.module.Register;
import cpusim.model.util.MachineComponent;
import cpusim.model.util.ValidationException;
import cpusim.model.util.units.ArchType;
import cpusim.model.util.units.ArchValue;
import cpusim.xml.HtmlEncoder;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static cpusim.model.util.Validate.getOptionalProperty;

/**
 * Reads/writes to an {@link IOChannel}.
 */
public class IO extends Microinstruction<IO> {

    public enum Type {
        Integer,
        ASCII,
        Unicode
    }

    private final ObjectProperty<Type> type;
    private final ObjectProperty<IODirection> direction;

    @DependantComponent
    private final ObjectProperty<Register> buffer;

    private final ObjectProperty<IOChannel> connection;

    private final ReadOnlySetProperty<MachineComponent> dependencies;

    private static final Logger logger = LogManager.getLogger(IO.class);

    /**
     * Constructor
     * creates a new Increment object with input values.
     *  @param name name of the microinstruction.
     * @param machine the machine that the microinstruction belongs to.
     * @param type type of logical microinstruction.
     * @param buffer the source1 register.
     * @param direction the destination register.
     * @param connection the IOChannel used
     */
    public IO(String name,
              UUID id,
              Machine machine,
              Type type,
              @Nullable Register buffer,
              IODirection direction,
              IOChannel connection){
        super(name, id, machine);
        this.type = new SimpleObjectProperty<>(this, "type", checkNotNull(type));
        this.buffer = new SimpleObjectProperty<>(this, "buffer", buffer);
        this.direction = new SimpleObjectProperty<>(this, "direction", checkNotNull(direction));
        this.connection = new SimpleObjectProperty<>(this, "connection", connection);

        this.dependencies = MachineComponent.collectDependancies(this)
                .buildSet(this, "dependantComponents");
    }
    
    /**
     * Copy constructor
     * @param other instance to copy from
     */
    public IO(IO other) {
        this(other.getName(),
                UUID.randomUUID(),
                other.getMachine(),
                other.getType(),
                other.getBuffer().orElse(null),
                other.getDirection(),
                other.getConnection().orElse(null));
    }

    @Override
    public ReadOnlySetProperty<MachineComponent> getDependantComponents() {
        return dependencies;
    }

    /**
     * returns the register to be calculated.
     * @return the name of the register.
     */
    public Optional<Register> getBuffer(){
        return Optional.ofNullable(buffer.get());
    }

    /**
     * updates the register used by the microinstruction.
     * @param newBuffer the new source register for the logical microinstruction.
     */
    public void setBuffer(Register newBuffer){
        buffer.set(newBuffer);
    }

    public ObjectProperty<Register> bufferProperty() {
        return buffer;
    }

    /**
     * returns the register to put result.
     * @return the name of the register.
     */
    public IODirection getDirection(){
        return direction.get();
    }

    /**
     * updates the register used by the microinstruction.
     * @param newDirection the new destination for the logical microinstruction.
     */
    public void setDirection(IODirection newDirection){
        direction.set(newDirection);
    }

    public ObjectProperty<IODirection> directionProperty() {
        return direction;
    }

    /**
     * returns the type of shift.
     * @return type of shift as a string.
     */
    public Type getType(){
        return type.get();
    }

    /**
     * updates the type used by the microinstruction.
     * @param newType the new string of type.
     */
    public void setType(Type newType){
        type.set(newType);
    }

    public ObjectProperty<Type> typeProperty() {
        return type;
    }

    /**
     * getter for the IOChannel
     * @return the IOChannel
     */
    public Optional<IOChannel> getConnection(){
        return Optional.ofNullable(this.connection.getValue());
    }

    /**
     * setter for the IOChannel
     * @param newConnection new Channel to be set
     */
    public void setConnection(IOChannel newConnection){
        this.connection.setValue(newConnection);
    }
    
    public ObjectProperty<IOChannel> connectionProperty() {
        return connection;
    }
    
    @Override
    public IO cloneFor(IdentifierMap oldToNew) {
        checkNotNull(oldToNew);

        return new IO(getName(), UUID.randomUUID(), getMachine(),
                getType(),
                oldToNew.get(getBuffer().orElse(null)),
                getDirection(),
                getConnection().orElse(null));
    }

    @Override
    public <U extends IO> void copyTo(U newIO) {
        checkNotNull(newIO);

        newIO.setName(getName());
        newIO.setDirection(getDirection());
        newIO.setType(getType());
        newIO.setBuffer(getBuffer().orElse(null));
        newIO.setConnection(getConnection().orElse(null));
    }

    /**
     * execute the micro instruction from machine
     */
    @Override
    public void execute() {
        int numBits = buffer.get().getWidth();

        IODirection dir = getDirection();
        Type type = getType();
        
        IOChannel connection = this.connection.getValue();

        switch (dir) {
            case Read: {
                long value;
                switch (type) {
                    case Integer:
                        value = connection.readLong(numBits);
                        break;
                    case ASCII:
                        value = connection.readAscii();
                        break;
                    case Unicode:
                        value = connection.readUnicode();
                        break;

                    default:
                        throw new IllegalStateException("Unsupported Type received: " + type);
                }

                buffer.get().setValue(value);
            } break;

            case Write: {
                final long value = buffer.get().getValue();
                switch (type) {
                    case Integer:
                        connection.writeLong(value);
                        break;

                    case ASCII:
                        connection.writeAscii((char)(value & ArchType.Byte.getMask(1)));
                        break;

                    case Unicode:
                        connection.writeUnicode((int)value);
                        break;

                    default:
                        throw new IllegalStateException("Unsupported Type received: " + type);
                }
            } break;

            default:
                throw new IllegalStateException("Unsupported direction: " + dir);
        }

        // FIXME Is it necessary?
        //added on 3/20 by Ben Borchard because outputs wouldn't print to the console.
        //I am not sure that this is the correct thing to do...
        connection.flush(true);
    }

    /**
     * undo the execution of the micro instruction from machine
     */
    public void undoExecute() {
        IOChannel connection = this.connection.get();
        if (connection instanceof FileChannel){
            FileChannel connect = (FileChannel) connection;

            Type type = this.type.get();

            switch (direction.get()) {
                case Read: {
                    switch (type) {
                        case Integer:
                        case Unicode:
                            connect.unReadLong();
                            break;

                        case ASCII:
                            connect.unReadOneChar();
                            break;

                        default:
                            throw new IllegalStateException("Unknown type: " + type);
                    }
                } break;

                case Write: {
                    switch (type) {
                        case Integer:
                        case Unicode:
                            connect.unWriteLong();
                            break;

                        case ASCII:
                            connect.unWriteOneChar();
                            break;

                        default:
                            throw new IllegalStateException("Unknown type: " + type);
                    }
                } break;

                default:
                    throw new IllegalStateException("Unknown direction: " + direction);
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
//                "\" buffer=\"" + getBuffer().getID() +
                "\" connection=\"" + getConnection() +
                "\" id=\"" + getID() + "\" />";
	}

	@Override
	public String getHTMLDescription(String indent) {
		return indent + "<TR><TD>" + getHTMLName() +
                "</TD><TD>" + getDirection() +
                "</TD><TD>" + getType() +
//                "</TD><TD>" + getBuffer().getHTMLName() +
                "</TD><TD>" + HtmlEncoder.sEncode(getConnection().toString()) +
                "</TD></TR>";
	}

    /**
     * check if the ios of type ascii have 8-bit-wide
     * (or greater) buffers and ios of type unicode have
     * 16-bit-wide (or greater) buffers.
     */
    @Override
    public void validate() {
        super.validate();

        ArchValue width;
        switch (getType()) {
            case ASCII:
                width = ArchType.Byte.of(Character.BYTES);
                break;

            case Unicode:
                width = ArchType.Byte.of(Short.BYTES);
                break;

            case Integer:
                width = ArchType.Byte.of(Long.BYTES);
                break;

            default:
                throw new IllegalStateException("Unknown type: " + getType());

        }

        Register buffer = getOptionalProperty(this, IO::bufferProperty);
        
        if (buffer.getWidth() < width.as(ArchType.Byte)) {
            throw new ValidationException("IO \"" + this + "\" is of type " + getType()+ " and so needs a\n" +
                    "buffer register at least " + width.as(ArchType.Bit) + " bits wide.");
        }
    }
}
