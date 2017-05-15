package cpusim.model.microinstruction;

import cpusim.model.Machine;
import cpusim.model.module.Module;
import cpusim.model.module.RAM;
import cpusim.model.module.Register;
import cpusim.model.util.MachineComponent;
import cpusim.model.util.ObservableCollectionBuilder;
import javafx.beans.property.*;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Reads data from a {@link RAM} location and places it into a {@link Register}.
 *
 * @since 2015-02-12
 */
public class MemoryAccess extends Microinstruction<MemoryAccess> {

    @DependantComponent
    private final ObjectProperty<IODirection> direction;
    @DependantComponent
    private final ObjectProperty<RAM> memory;
    @DependantComponent
    private final ObjectProperty<Register> data;
    @DependantComponent
    private final ObjectProperty<Register> address;

    private final ReadOnlySetProperty<MachineComponent> dependants;


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
                        IODirection direction,
                        @Nullable RAM memory,
                        @Nullable Register data,
                        @Nullable Register address){
        super(name, id, machine);
        this.direction = new SimpleObjectProperty<>(this, "direction", direction);
        this.memory = new SimpleObjectProperty<>(this, "memory", memory);
        this.data = new SimpleObjectProperty<>(this, "data", data);
        this.address = new SimpleObjectProperty<>(this, "address", address);

        this.dependants = (new ObservableCollectionBuilder<MachineComponent>())
                .add(this.memory)
                .add(this.data)
                .add(this.address)
                .buildSet(this, "dependants");
    }
    
    /**
     * Copy constructor
     * @param other instance to copy
     */
    public MemoryAccess(MemoryAccess other) {
        this(other.getName(), UUID.randomUUID(), other.getMachine(),
                other.getDirection(), other.getMemory().orElse(null),
                other.getData().orElse(null), other.getAddress().orElse(null));
    }

    @Override
    public ReadOnlySetProperty<MachineComponent> getDependantComponents() {
        return dependants;
    }

    /**
     * returns the register to be calculated.
     * @return the name of the register.
     */
    public Optional<RAM> getMemory(){
        return Optional.ofNullable(memory.get());
    }

    /**
     * updates the register used by the microinstruction.
     * @param newMemory the new source register for the logical microinstruction.
     */
    public void setMemory(RAM newMemory){
        memory.set(newMemory);
    }

    public ObjectProperty<RAM> memoryProperty() {
        return memory;
    }

    /**
     * returns the register to be calculated.
     * @return the name of the register.
     */
    public Optional<Register> getData(){
        return Optional.ofNullable(data.get());
    }

    /**
     * updates the register used by the microinstruction.
     * @param newData the new source register for the logical microinstruction.
     */
    public void setData(Register newData){
        data.set(newData);
    }

    public ObjectProperty<Register> dataProperty() {
        return data;
    }

    /**
     * returns the register to be calculated.
     * @return the name of the register.
     */
    public Optional<Register> getAddress(){
        return Optional.ofNullable(address.get());
    }

    /**
     * updates the register used by the microinstruction.
     * @param newAddress the new source register for the logical microinstruction.
     */
    public void setAddress(Register newAddress){
        address.set(newAddress);
    }

    public ObjectProperty<Register> addressProperty() {
        return address;
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
        switch (direction.get()) {
            case Read: {
                long value = memory.get().getData(addressValue, numBits);
                data.get().setValue(value);

//            if( memory.get().breakAtAddress(addressValue))
//                throw new BreakException("Break in " + memory.get().getName() +
//                        " read at address " + addressValue,
//                        addressValue, memory.get());
            } break;

            case Write: {
                long value = data.get().getValue();
                memory.get().setData(addressValue, value, numBits);

                // FIXME this could be done via listeners
                //it would seem like the next statement should be added to the
                //end of RAM.setData, but that method is called when the user
                //manually edits the data in the RAM and we don't want a break
                //to be called in that case(?).  But there should be a better way
                //to do this.
//            if (memory.get().breakAtAddress(addressValue))
//                throw new BreakException("Break in " + memory.get().getName() +
//                        " write at address " + addressValue,
//                        addressValue, memory.get());

            } break;

            default: {
                throw new IllegalArgumentException("Illegal direction " + direction.get()
                        + " in MemoryAccess micro " + getName());
            }
        }
    }

    /**
     * returns the XML description
     * @return the XML description
     */
    @Override
    public String getXMLDescription(String indent) {
        return indent + "<MemoryAccess name=\"" + getHTMLName() +
                "\" direction=\"" + getDirection() +
//                "\" memory=\"" + getMemory().getID() +
//                "\" data=\"" + getData().getID() +
//                "\" address=\"" + getAddress().getID() +
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
//                "</TD><TD>" + getMemory().getHTMLName() +
//                "</TD><TD>" + getData().getHTMLName() +
//                "</TD><TD>" + getAddress().getHTMLName() +
                "</TD></TR>";
    }

    @Override
    public MemoryAccess cloneFor(IdentifierMap oldToNew) {
        return new MemoryAccess(getName(), UUID.randomUUID(), oldToNew.getNewMachine(),
                getDirection(),
                oldToNew.get(getMemory().orElse(null)),
                oldToNew.get(getData().orElse(null)),
                oldToNew.get(getAddress().orElse(null)));
    }

    @Override
    public <U extends MemoryAccess> void copyTo(U other) {
        checkNotNull(other);

        other.setName(getName());
        other.setDirection(getDirection());
        other.setMemory(getMemory().orElse(null));
        other.setData(getData().orElse(null));
        other.setAddress(getAddress().orElse(null));
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
