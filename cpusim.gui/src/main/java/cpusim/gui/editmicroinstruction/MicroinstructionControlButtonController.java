package cpusim.gui.editmicroinstruction;

import com.google.common.base.Joiner;
import cpusim.gui.editmodules.EditModulesController;
import cpusim.gui.util.ControlButtonController;
import cpusim.model.MachineInstruction;
import cpusim.model.microinstruction.Microinstruction;
import cpusim.util.Dialogs;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link ControlButtonController} for the {@link EditModulesController} window.
 * @param <T>
 */
class MicroinstructionControlButtonController<T extends Microinstruction<T>>
        extends ControlButtonController<T> {

    private final MicroinstructionTableController<T> microinsController;

    MicroinstructionControlButtonController(MicroinstructionTableController<T> microinsController) {
        super(false, microinsController);
        this.microinsController = microinsController;
    
        loadFXML();
    }

    @Override
    protected void initializeSubclass() {
        microinsController.getSelectionModel().selectedItemProperty().addListener(getButtonChangeListener());
    }

    @Override
    protected boolean checkDelete(T toDelete) {
        boolean shouldDelete = true;

        //now test to see if it is used by any instructions that use the micro and if so,
        //warn the user that those micros will be deleted too.
        List<MachineInstruction> instrsThatUseIt = microinsController.getMachine().getInstructionsThatUse(toDelete);
        if (instrsThatUseIt.size() > 0) {
            StringBuilder bld = new StringBuilder();
            bld.append(toDelete.getName());
            bld.append(" is used by the following machine instructions: \n  ");
            Joiner.on(", ").appendTo(bld, instrsThatUseIt);
            bld.append(".\nReally delete it?");

            Alert dialog = Dialogs.createConfirmationDialog(microinsController.getScene().getWindow(),
                    "Confirm Deletion", bld.toString());
            final Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent()) {
                final ButtonType res = result.get();
                shouldDelete = !(res == ButtonType.CANCEL ||
                        res == ButtonType.NO ||
                        res == ButtonType.CLOSE);
            }
        }
        
        return shouldDelete;
    }
    


}