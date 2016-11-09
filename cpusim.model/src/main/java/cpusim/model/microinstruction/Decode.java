/**
 * Author: Jinghui Yu
 * Last editing date: 6/6/2013
 */

package cpusim.model.microinstruction;

import java.util.List;

import cpusim.model.ExecutionException;
import cpusim.model.Machine;
import cpusim.model.MachineInstruction;
import cpusim.model.Microinstruction;
import cpusim.model.Module;
import cpusim.model.module.Register;

import javafx.beans.property.SimpleObjectProperty;

/**
 * The branch microinstruction is identical to the Test microinstruction except
 * that it is an unconditional jump.
 */
public class Decode extends Microinstruction
{
    private SimpleObjectProperty<Register> ir;

    /**
     * Constructor
     * creates a new Branch object with input values.
     *
     * @param name name of the microinstruction.
     * @param machine the machine that the microinstruction belongs to.
     * @param ir size of the relative jump.
     */
    public Decode(String name, Machine machine,
                  Register ir){
        super(name, machine);
        this.ir = new SimpleObjectProperty<>(ir);
    }

    /**
     * returns the fixed value stored in the set microinstruction.
     *
     * @return the integer value of the field.
     */
    public Register getIr(){
        return ir.get();
    }

    /**
     * updates the fixed value stored in the set microinstruction.
     *
     * @param newIr the new value for the field.
     */
    public void setIr(Register newIr){
        ir.set(newIr);
    }

    public Machine getMachine(){
        return this.machine;
    }

    public void setMachine(Machine newMachine){
        this.machine = newMachine;
    }
    
    /**
     * returns the class of the microinstruction
     * @return the class of the microinstruction
     */
    @Override
    public String getMicroClass(){
        return "decode";
    }

    /**
     * duplicate the set class and return a copy of the original Set class.
     *
     * @return a copy of the Set class
     */
    public Object clone(){
        return new Decode(getName(),machine,getIr());
    }

    /**
     * execute the micro instruction from machine
     */
    public void execute()
    {
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

    /**
     * copies the data from the current micro to a specific micro
     * @param oldMicro the micro instruction that will be updated
     */
    public void copyTo(Microinstruction oldMicro)
    {
        assert oldMicro instanceof Decode :
                "Passed non-Decode to Decode.copyDataTo()";
        Decode newDecode = (Decode) oldMicro;
        newDecode.setName(getName());
        newDecode.setIr(getIr());
        newDecode.machine = machine;
    }

    /**
     * returns the XML description
     * @return the XML description
     */
    @Override
    public String getXMLDescription(String indent) {
        return indent + "<Decode name=\"" + getHTMLName() +
                "\" ir=\"" + getIr().getID() +
                "\" id=\"" + getID() + "\" />";
    }

    /**
     * returns the HTML description
     * @return the HTML description
     */
    @Override
    public String getHTMLDescription(String indent) {
        return indent + "<TR><TD>" + getHTMLName() +
                "</TD><TD>" + getIr().getHTMLName() +
                "</TD></TR>";
    }

    /**
     * returns true if this microinstruction uses m
     * (so if m is modified, this micro may need to be modified.
     * @param m the module that holds the microinstruction
     * @return boolean value true if this micro used the module
     */
    public boolean uses(Module<?> m){
        return (m == ir.get());
    }
}
