package cpusim.gui.editmodules;

import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import cpusim.model.util.Validate;
import cpusim.util.ValidateControllers;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
                .flatMap(r -> r.getRegisters().stream()) // get all of the registers in the arrays
                .distinct() // remove any duplicates (possibility)
                .collect(Collectors.toMap(Function.identity(), Register::getWidth)); // map them to their widths

        //now do all the tests
        Validate.registerWidthsAreOkayForMicros(getMachine(), regWidths);

        ValidateControllers.registerArrayWidthsAreOkay(getBitController(), registerArrays);
        ValidateControllers.registerArrayWidthsAreOkayForTransferMicros(getMachine(), registerArrays);
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
