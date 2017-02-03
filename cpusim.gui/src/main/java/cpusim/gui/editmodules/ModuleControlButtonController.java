package cpusim.gui.editmodules;

import com.google.common.base.Joiner;
import cpusim.gui.util.ControlButtonController;
import cpusim.model.Machine;
import cpusim.model.module.Module;
import cpusim.model.microinstruction.Microinstruction;
import cpusim.util.Dialogs;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;

import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of {@link ControlButtonController} for the {@link EditModulesController} window.
 * @param <T>
 */
class ModuleControlButtonController<T extends Module<T>> extends ControlButtonController<T> {

    private ModuleTableController<T> moduleController;

    /**
     * Default constructor for JavaFX Scene Builder.
     */
    ModuleControlButtonController() {
        this(null, true);
    }

    ModuleControlButtonController(ModuleTableController<T> moduleController,
                                  final boolean hasExtendedProperties) {
        super(hasExtendedProperties, false);
        this.moduleController = moduleController;
    
        loadFXML();

        setInteractionHandler(moduleController);
    }

    @Override
    protected boolean checkDelete(T toDelete) {
        
        boolean shouldDelete = true;
        //now test to see if it is used by any micros and if so,
        //warn the user that those micros will be deleted too.
        
        Machine machine = moduleController.getMachine();
        Map<Microinstruction<?>, ObservableList<Microinstruction<?>>> microsThatUseIt
                    = machine.getMicrosThatUse(toDelete);
            
        if (!microsThatUseIt.isEmpty()) {
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

    /**
     * Get the {@link ModuleTableController} this is attached to.
     * @return Optional because it may be {@code null} if uninitialized.
     */
    public Optional<ModuleTableController<T>> getModuleController() {
        return Optional.ofNullable(moduleController);
    }

    /**
     * Set the referenced {@link ModuleTableController} instance.
     * @param moduleController Non-{@code null} controller.
     */
    public void setModuleController(ModuleTableController<T> moduleController) {
        this.moduleController = checkNotNull(moduleController);
    }
}