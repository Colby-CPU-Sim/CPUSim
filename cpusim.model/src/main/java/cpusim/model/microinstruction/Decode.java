package cpusim.model.microinstruction;

import cpusim.model.ExecutionException;
import cpusim.model.Machine;
import cpusim.model.MachineInstruction;
import cpusim.model.Module;
import cpusim.model.module.Register;
import cpusim.model.util.IdentifiedObject;
import cpusim.model.util.ValidationException;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.*;

/**
 * Decodes an instruction and stores it in a {@link Register}.
 *
 * @since 2013-06-06
 */
public class Decode extends Microinstruction<Decode> {
    
    private SimpleObjectProperty<Register> ir;
    
    /**
     * Constructor
     * creates a new Branch object with input values.
     *
     * @param name name of the microinstruction.
     * @param machine the machine that the microinstruction belongs to.
     * @param ir size of the relative jump.
     */
    public Decode(String name,
                  UUID id,
                  Machine machine,
                  Register ir){
        super(name, id, machine);
        this.ir = new SimpleObjectProperty<>(ir);
    }
    
    /**
     * Copy consturctor
     * @param other Instance to copy from
     */
    public Decode(Decode other) {
        this(other.getName(), IdentifiedObject.generateRandomID(), other.machine, other.getIr());
    }

    /**
     * returns the fixed value stored in the set microinstruction.
     *
     * @return the integer value of the field.
     */
    public Register getIr(){
        return ir.get();
    }

    public ObjectProperty<Register> irProperty() {
        return ir;
    }

    /**
     * updates the fixed value stored in the set microinstruction.
     *
     * @param newIr the new value for the field.
     */
    public void setIr(Register newIr){
        ir.set(newIr);
    }
    
    /**
     * execute the micro instruction from machine
     */
    @Override
    public void execute() {
        List<MachineInstruction> instructions = machine.getInstructions();
        int width = ir.get().getWidth();
        long value = ir.get().getValue();
        for (int i = 1; i <= width; i++) {
            //??????
            value = (value << (64 - width)) >>> (64 - width);
            
            long opcode = value >>> (width - i);
            for (MachineInstruction instr : instructions) {
                if (opcode == instr.getOpcode() &&
                        i == instr.getInstructionFields().get(0).getNumBits()) {
                    machine.getControlUnit().setMicroIndex(0);
                    machine.getControlUnit().setCurrentInstruction(instr);
                    return;
                }
            }
        }
        //if we get this far, there was no machine instruction found
        throw new ExecutionException("No opcode matched the bits in " +
                "the register: " + ir.get() + ".");
    }

    @Override
    public void validate() {
        super.validate();

        if (ir.getValue() == null) {
            throw new ValidationException("No IR register set for Decode instruction, " + getName());
        }
    }
    
    @Override
    public <U extends Decode> void copyTo(final U other) {
        checkNotNull(other);
        
        other.setName(getName());
        other.setIr(getIr());
    }
    
    @Override
    public String getXMLDescription(String indent) {
        return indent + "<Decode name=\"" + getHTMLName() +
                "\" ir=\"" + getIr().getID() +
                "\" id=\"" + getID() + "\" />";
    }
    
    @Override
    public String getHTMLDescription(String indent) {
        return indent + "<TR><TD>" + getHTMLName() +
                "</TD><TD>" + getIr().getHTMLName() +
                "</TD></TR>";
    }

    @Override
    public boolean uses(Module<?> m){
        return (m == ir.get());
    }
}
