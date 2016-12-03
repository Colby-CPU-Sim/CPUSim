package cpusim.gui.editmicroinstruction;

import com.google.common.base.Joiner;
import cpusim.gui.editmodules.EditModulesController;
import cpusim.gui.util.ControlButtonController;
import cpusim.model.Machine;
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

    private MicroinstructionTableController<T> microinsController;

    /**
     * Default constructor for JavaFX SceneBuilder.
     */
    MicroinstructionControlButtonController() {
        this(null);
    }

    MicroinstructionControlButtonController(MicroinstructionTableController<T> microinsController) {
        super(false, microinsController);
        this.microinsController = microinsController;
    
        loadFXML();
    }

    @Override
    protected void initialize() {
        super.initialize();

        microinsController.getSelectionModel().selectedItemProperty().addListener(getButtonChangeListener());
    }

    @Override
    protected boolean checkDelete(T toDelete) {
        boolean shouldDelete = true;
        
        final Optional<Machine> machineOpt = microinsController.getMachine();
        if (machineOpt.isPresent()) {
            final Machine machine = machineOpt.get();
    
            //now test to see if it is used by any instructions that use the micro and if so,
            //warn the user that those micros will be deleted too.
            final List<MachineInstruction> instrsThatUseIt = machine.getInstructionsThatUse(toDelete);
            if (!instrsThatUseIt.isEmpty()) {
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
        }

        return shouldDelete;
    }

    public Optional<MicroinstructionTableController<T>> getMicroinsController() {
        return Optional.ofNullable(microinsController);
    }

    public void setMicroinsController(MicroinstructionTableController<T> microinsController) {
        this.microinsController = microinsController;
    }
}