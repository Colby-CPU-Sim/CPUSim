package cpusim.gui.editmodules;

import com.google.common.base.Joiner;
import cpusim.gui.util.ControlButtonController;
import cpusim.model.Module;
import cpusim.model.microinstruction.Microinstruction;
import cpusim.util.Dialogs;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;

import java.util.Map;
import java.util.Optional;

/**
 * Implementation of {@link ControlButtonController} for the {@link EditModulesController} window.
 * @param <T>
 */
class ModuleControlButtonController<T extends Module<T>> extends ControlButtonController<T> {

    private final ModuleTableController<T> moduleController;

    ModuleControlButtonController(ModuleTableController<T> moduleController,
                                  final boolean hasExtendedProperties) {
        super(hasExtendedProperties, moduleController);
        this.moduleController = moduleController;
    
        loadFXML();
    }

    @Override
    protected void initializeSubclass() {
        moduleController.getSelectionModel().selectedItemProperty().addListener(getButtonChangeListener());
    }

    @Override
    protected boolean checkDelete(T toDelete) {
        
        boolean shouldDelete = true;
        //now test to see if it is used by any micros and if so,
        //warn the user that those micros will be deleted too.
        
        Map<Microinstruction, ObservableList<Microinstruction>> microsThatUseIt =
                moduleController.getMachine().getMicrosThatUse(toDelete);
        if (microsThatUseIt.size() > 0) {
            StringBuilder message = new StringBuilder(toDelete.toString());
            message.append(" is used by the following microinstructions: \n  ");
        
            Joiner.on(", ").appendTo(message, microsThatUseIt.keySet());
        
            message.append(".\n  If you delete it, all these microinstructions will also be deleted.  ");
            message.append("Really delete it?");
            Optional<ButtonType> result = Dialogs.createConfirmationDialog(moduleController.getScene().getWindow(),
                    "Confirm Deletion", message.toString()).showAndWait();
            shouldDelete = !(result.get() == ButtonType.CANCEL ||
                    result.get() == ButtonType.NO ||
                    result.get() == ButtonType.CLOSE);
        }
        
        return shouldDelete;
    }
    


}