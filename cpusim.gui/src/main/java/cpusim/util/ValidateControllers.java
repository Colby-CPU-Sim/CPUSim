/**
 * 
 */
package cpusim.util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import cpusim.gui.editmodules.ConditionBitTableController;
import cpusim.gui.editmodules.RegisterArrayTableController;
import cpusim.model.Machine;
import cpusim.model.Microinstruction;
import cpusim.model.microinstruction.TransferAtoR;
import cpusim.model.microinstruction.TransferRtoA;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import cpusim.model.module.RegisterRAMPair;
import cpusim.model.util.ValidationException;

import javafx.collections.ObservableList;

/**
 * Contains methods for Validation in the User Interface.
 *  
 * @author Kevin Brightwell
 * @since 2016-09-20
 */
public class ValidateControllers {

	/**
     * check if the given registers' widths work for all conditionbits
     * that use them.
     * 
     * @param bitController the conditionBit controller that holds conditionbit modules
     * @param registers a list of register that is being checked
     * 
     * @since 2015-02-12
     */
    public static void registerWidthsAreOkay(ConditionBitTableController bitController,
                                             List<? extends Register> registers)
    {
        for (Register register : registers) {
            List<ConditionBit> bits = bitController.getBitClonesThatUse(register);
            for (ConditionBit bit: bits) {
                if (bit.getBit() >= register.getWidth()) {
                    throw new ValidationException("ConditionBit " + bit.getName() +
                            " refers to bit " + bit.getBit() + " of register " +
                            register + ",\nso you can't make that register " +
                            "narrower than " + (bit.getBit() + 1) + " bits.");
                }
            }
        }
    }
    
    /**
     * checks if the given register arrays' widths work for all conditionbits
     * that use them.  That is, it throws a validationException if some condition bit refers
     * to a bit that is no longer part of the register due to a narrowing of
     * the register.
     * @param bitController the conditionBit controller that holds conditionbit modules
     * @param arrays an array that holds all the register arrays
     * 
     * @since 2015-02-12
     */
    public static void registerArrayWidthsAreOkay(ConditionBitTableController bitController,
                                                  List<? extends RegisterArray> arrays)
    {
        for (RegisterArray array : arrays) {
            for (Register register : array) {
                List<ConditionBit> bits = bitController.getBitClonesThatUse(register);
                for (ConditionBit bit : bits) {
                    if (bit.getBit() >= register.getWidth()) {
                        throw new ValidationException("ConditionBit " + bit.getName() + " refers to bit " +
                                bit.getBit() + " of register " + register + ",\nso you can't make the array " +
                                "narrower than " + (bit.getBit() + 1) + " bits.");
                    }
                }
            }
        }
    }
    
    /**
     * check if all registers have appropriate width for modules.
     * @param arrays an array of RegisterArray clones with new widths
     */
    public static void registerArrayWidthsAreOkayForTransferMicros(
            Machine machine,
            List<? extends RegisterArray> arrays,
            RegisterArrayTableController controller)
    {
        //make a HashMap of old arrays as keys and new widths as
        //Integer values
        final Map<RegisterArray, Integer> newWidths = new HashMap<>();

        newWidths.putAll(
                machine.getModule("registerArrays", RegisterArray.class).stream()
                    .collect(Collectors.toMap(Function.identity(), RegisterArray::getWidth)));


        //now adjust the HashMap to use the new proposed widths
        for (RegisterArray array : arrays) {
            RegisterArray oldArray = controller.getCurrentFromClone(array);
            if (oldArray != null) {
                newWidths.put(oldArray, array.getWidth());
            }
        }

        //now go through all transfers to see if width changes will make them
        //invalid
        ObservableList<TransferAtoR> transferAtoRs = machine.getMicros(TransferAtoR.class);
        for (TransferAtoR t : transferAtoRs) {
            int sourceWidth = newWidths.get(t.getSource());
            if (sourceWidth < t.getSrcStartBit() + t.getNumBits()) {
                throw new ValidationException("The new width " + sourceWidth +
                        " of register array " + t.getSource() +
                        "\ncauses microinstruction " + t + " to be invalid.");
            }
        }

        ObservableList<TransferRtoA> transferRtoAs = machine.getMicros(TransferRtoA.class);
        for (TransferRtoA t : transferRtoAs) {
            final int destWidth = newWidths.get(t.getDest());
            if (destWidth < t.getDestStartBit() + t.getNumBits()) {
                throw new ValidationException("The new width " + destWidth + " of register array " + t.getDest() +
                        "\ncauses microinstruction " + t + " to be invalid.");
            }
        }

    }
    
    /**
     * Check if all RegisterRAMPairs are unique and throw ValidationException
     * if there are duplicated pairs.
     * @param data the list of RegisterRAMPairs.
     */
    public static void allRegisterRAMPairAreUnique(List<RegisterRAMPair> data){
        
        Set<RegisterRAMPair> set = new HashSet<>(data);
        if (set.size() != data.size()) {
            // Now check all of them to find the first duplicate
            
            for (int i = 0; i < data.size() - 1; i++) {
                for (int j = i + 1; j < data.size(); j++) {
                    final RegisterRAMPair pair1 = data.get(i);
                    final RegisterRAMPair pair2 = data.get(j);
                    
                    if (pair1.equals(pair2)) {
                        throw new ValidationException("The Register " +
                                pair1.getRegister().getName() +
                                " and the RAM " + pair1.getRam().getName() +
                                " pair has been set twice.");
                    }
                }
            }
        }
    }

	
}
