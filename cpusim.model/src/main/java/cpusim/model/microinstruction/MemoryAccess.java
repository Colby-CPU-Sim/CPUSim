package cpusim.model.microinstruction;

import cpusim.model.Machine;
import cpusim.model.Module;
import cpusim.model.module.RAM;
import cpusim.model.module.Register;
import cpusim.model.util.IdentifiedObject;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.UUID;

import static com.google.common.base.Preconditions.*;

/**
 * Reads data from a {@link RAM} location and places it into a {@link Register}.
 *
 * @since 2015-02-12
 */
public class MemoryAccess extends Microinstruction<MemoryAccess> {
	
    private SimpleStringProperty direction;
    private SimpleObjectProperty<RAM> memory;
    private SimpleObjectProperty<Register> data;
    private SimpleObjectProperty<Register> address;


    /**
     * Constructor
     * creates a new Increment object with input values.
     *
     * @param name name of the microinstruction.
     * @param id Unique ID for microinstruction
     * @param machine the machine that the microinstruction belongs to.
     * @param direction type of logical microinstruction.
     * @param memory the RAM memory.
     * @param data the register storing the data.
     * @param address the register storing the address.
     */
    public MemoryAccess(String name,
                        UUID id,
                        Machine machine,
                        String direction,
                        RAM memory,
                        Register data,
                        Register address){
        super(name, id, machine);
        this.direction = new SimpleStringProperty(direction);
        this.memory = new SimpleObjectProperty<>(memory);
        this.data = new SimpleObjectProperty<>(data);
        this.address = new SimpleObjectProperty<>(address);
    }
    
    
    /**
     * Constructor
     * creates a new Increment object with input values.
     *
     * @param name name of the microinstruction.
     * @param machine the machine that the microinstruction belongs to.
     * @param direction type of logical microinstruction.
     * @param memory the RAM memory.
     * @param data the register storing the data.
     * @param address the register storing the address.
     */
    public MemoryAccess(String name, Machine machine,
                        String direction,
                        RAM memory,
                        Register data,
                        Register address){
        this(name, IdentifiedObject.generateRandomID(), machine, direction, memory, data, address);
    }
    
    /**
     * Copy constructor
     * @param other instance to copy
     */
    public MemoryAccess(MemoryAccess other) {
        this(other.getName(), other.machine,
                other.getDirection(), other.getMemory(),
                other.getData(), other.getAddress());
    }
    
    /**
     * returns the register to be calculated.
     * @return the name of the register.
     */
    public RAM getMemory(){
        return memory.get();
    }

    /**
     * updates the register used by the microinstruction.
     * @param newMemory the new source register for the logical microinstruction.
     */
    public void setMemory(RAM newMemory){
        memory.set(newMemory);
    }

    /**
     * returns the register to be calculated.
     * @return the name of the register.
     */
    public Register getData(){
        return data.get();
    }

    /**
     * updates the register used by the microinstruction.
     * @param newData the new source register for the logical microinstruction.
     */
    public void setData(Register newData){
        data.set(newData);
    }

    /**
     * returns the register to be calculated.
     * @return the name of the register.
     */
    public Register getAddress(){
        return address.get();
    }

    /**
     * updates the register used by the microinstruction.
     * @param newAddress the new source register for the logical microinstruction.
     */
    public void setAddress(Register newAddress){
        address.set(newAddress);
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
     * execute the micro instruction from machine
     */
    @Override
    public void execute()
    {
        int addressValue = (int) address.get().getValue();
        if( addressValue < 0 ) {
            //change it to positive if address width < 32 bits
            //note:  We don't need to worry about addresses with width = 32
            //       since Java arrays (which are used to store the bytes of RAM)
            //       can have size at most 2^31-1, so if an address register
            //       has width 32, only positive values are legal.
            int addrWidth = address.get().getWidth();
            if( addrWidth < 32 ) {
                addressValue += (1 << addrWidth);
            }
        }
        int numBits = data.get().getWidth();
        if (direction.get().equals("read")) {
            long value = memory.get().getData(addressValue, numBits);
            data.get().setValue(value);
//            if( memory.get().breakAtAddress(addressValue))
//                throw new BreakException("Break in " + memory.get().getName() +
//                        " read at address " + addressValue,
//                        addressValue, memory.get());
        }
        else {
            assert direction.get().equals("write") : "Illegal direction " +
                    direction.get() + " in MemoryAccess micro " + getName();
            long value = data.get().getValue();
            memory.get().setData(addressValue, value, numBits);
            //it would seem like the next statement should be added to the
            //end of RAM.setData, but that method is called when the user
            //manually edits the data in the RAM and we don't want a break
            //to be called in that case(?).  But there should be a better way
            //to do this.
//            if (memory.get().breakAtAddress(addressValue))
//                throw new BreakException("Break in " + memory.get().getName() +
//                        " write at address " + addressValue,
//                        addressValue, memory.get());
        }
    }

    @Override
    protected void validateState() {
        // currently no-op
    }

    /**
     * returns the XML description
     * @return the XML description
     */
    @Override
    public String getXMLDescription(String indent) {
        return indent + "<MemoryAccess name=\"" + getHTMLName() +
                "\" direction=\"" + getDirection() +
                "\" memory=\"" + getMemory().getID() +
                "\" data=\"" + getData().getID() +
                "\" address=\"" + getAddress().getID() +
                "\" id=\"" + getID() + "\" />";
    }

    /**
     * returns the HTML description
     * @return the HTML description
     */
    @Override
    public String getHTMLDescription(String indent) {
        return indent + "<TR><TD>" + getHTMLName() +
                "</TD><TD>" + getDirection() +
                "</TD><TD>" + getMemory().getHTMLName() +
                "</TD><TD>" + getData().getHTMLName() +
                "</TD><TD>" + getAddress().getHTMLName() +
                "</TD></TR>";
    }
    
    @Override
    public <U extends MemoryAccess> void copyTo(final U newMemoryAccess) {
        checkNotNull(newMemoryAccess);
        
        newMemoryAccess.setName(getName());
        newMemoryAccess.setDirection(getDirection());
        newMemoryAccess.setMemory(getMemory());
        newMemoryAccess.setData(getData());
        newMemoryAccess.setAddress(getAddress());
    }
    
    /**
     * returns true if this microinstruction uses m
     * (so if m is modified, this micro may need to be modified.
     * @param m the module that holds the microinstruction
     * @return boolean value true if this micro used the module
     */
    @Override
    public boolean uses(Module<?> m){
        return (m == memory.get() || m == data.get() || m == address.get());
    }
}
