/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Changed the return value of checkValidity from a boolean to void (the functionality
 * enabled by that boolean value is now controlled by throwing ValidationException)
 * 2.) Changed the edit commit method on the name column so that it calls Validate.nameableObjects()
 * which throws a ValidationException in lieu of returning a boolean value
 * 3.) Moved rangesAreInBounds method to the Validate class and changed the return value to void
 * from boolean
 *
 * on 11/11/13:
 *
 * 1.) Added a column readOnly to decide if the value in that register is immutable
 * 2.) Changed initialize method so that it initializes cell factory of readOnly property
 * 3.) Changed checkValidity method so that it calls the Validate.readOnlyRegistersAreImmutable
 * to check if any read-only register is used as the destination register in microinstructions
 * transferAtoR and transferRtoR.
 *
 * on 12/2/2013:
 *
 * 1.) Changed checkValidity method so that it also checks if any read-only register is used as the
 * flag in microinstruction setCondBit.
 */
package cpusim.gui.editmodules;

import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import cpusim.model.util.IdentifiedObject;
import cpusim.model.util.Validate;
import cpusim.util.ValidateControllers;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The controller for editing the Registers in the EditModules dialog.
 */
public class RegistersForArrayTableController extends RegistersTableController {

    private final ObjectProperty<RegisterArray> currentArray;

    /**
     * Constructor
     */
    RegistersForArrayTableController() {
        super();

        currentArray = new SimpleObjectProperty<>(this, "currentArray", null);
    }

    public RegisterArray getCurrentArray() {
        return currentArray.get();
    }

    public ObjectProperty<RegisterArray> currentArrayProperty() {
        return currentArray;
    }

    /**
     * getter for prototype of the right subclass
     * @return the prototype of the subclass
     */
    @Override
    public Register createInstance() {
        return new Register("???",
                IdentifiedObject.generateRandomID(),
                machine.get(),
                16,
                0,
                Register.Access.readWrite());
    }

    /**
     * Check validity of array of Objects' properties.
     */
    @Override
    public void checkValidity() {
        // convert the array to an array of Registers

        super.checkValidity();

        List<RegisterArray> registerArrays = new ArrayList<>();
        registerArrays.add(currentArray.getValue());

        //buildSet up a HashMap of old registers and new widths
        final Map<Register, Integer> regWidths = registerArrays.stream()
                .flatMap(r -> r.registers().stream()) // get all of the registers in the arrays
                .distinct() // remove any duplicates (possibility)
                .collect(Collectors.toMap(Function.identity(), Register::getWidth)); // map them to their widths

        //now do all the tests
        Validate.registerWidthsAreOkayForMicros(machine.get(), regWidths);

        ValidateControllers.registerArrayWidthsAreOkay(getBitController(), registerArrays);
        ValidateControllers.registerArrayWidthsAreOkayForTransferMicros(machine.get(), registerArrays);
    }

    /**
     * get the ID of the corresponding help page
     * @return the ID of the page
     */
    public String getHelpPageID()
    {
        return "Registers";
    }

}
