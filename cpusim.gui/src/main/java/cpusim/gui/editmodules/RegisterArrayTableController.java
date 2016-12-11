package cpusim.gui.editmodules;

import com.google.common.base.Joiner;
import cpusim.gui.util.ControlButtonController;
import cpusim.gui.util.FXMLLoaderFactory;
import cpusim.gui.util.table.EditingNonNegativeIntCell;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import cpusim.model.util.Validate;
import cpusim.util.Dialogs;
import cpusim.util.ValidateControllers;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The controller for editing the Register arrays in the EditModules dialog.
 */
public class RegisterArrayTableController extends ModuleTableController<RegisterArray> {

    static final String FX_ID = "registerArraysTab";

    @FXML @SuppressWarnings("unused")
    private TableColumn<RegisterArray, Integer> length;

    @FXML @SuppressWarnings("unused")
    private TableColumn<RegisterArray, Integer> width;

    private ConditionBitTableController bitController;

    /**
     * Constructor
     */
    RegisterArrayTableController(){
        super("RegisterArrayTable.fxml", RegisterArray.class);
    
        loadFXML();
    }

    @Override
    public void initialize() {
        super.initialize();

        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        name.prefWidthProperty().bind(prefWidthProperty().divide(100/40.0));
        length.prefWidthProperty().bind(prefWidthProperty().divide(100/30.0));
        width.prefWidthProperty().bind(prefWidthProperty().divide(100/30.0));

        Callback<TableColumn<RegisterArray,Integer>,TableCell<RegisterArray,Integer>> cellIntFactory =
                setIntegerTableColumn -> new EditingNonNegativeIntCell<>();

        length.setCellValueFactory(new PropertyValueFactory<>("length"));
        width.setCellValueFactory(new PropertyValueFactory<>("width"));

        //Add for Editable Cell of each field, in String or in Integer
        length.setCellFactory(cellIntFactory);
        length.setOnEditCommit(text -> text.getRowValue().setLength(text.getNewValue()));

        width.setCellFactory(cellIntFactory);
        width.setOnEditCommit(text -> text.getRowValue().setWidth(text.getNewValue()));
    }
    
    /**
     * Sets the {@link ConditionBitTableController} stored.
     * @param ctrl Sets the {@link ConditionBitTableController}
     */
    void setConditionBitController(ConditionBitTableController ctrl) {
        bitController = checkNotNull(ctrl);
    }

    @Override
    protected ControlButtonController<RegisterArray> createControlButtonController() {
        return new ModuleControlButtonController<RegisterArray>(this, true) {
            @Override
            protected void onPropertiesButtonClick(final ActionEvent e) {

                // FIXME this method is nasty
                EditArrayRegistersController controller = new EditArrayRegistersController();
                controller.machineProperty().bind(machineProperty());
                controller.currentArrayProperty().bind(getSelectionModel().selectedItemProperty());

                Pane dialogRoot;
                final FXMLLoader fxmlLoader = FXMLLoaderFactory.fromController(controller, "EditRegisters.fxml");
                try {
                     dialogRoot = fxmlLoader.load();
                } catch (IOException ex) {
                    // should never happen
                    throw new IllegalStateException("Unable to load file: EditRegisters.fxml", ex);
                }

                Stage dialogStage = new Stage();
                Scene dialogScene = new Scene(dialogRoot);
                dialogStage.setScene(dialogScene);
                dialogStage.initOwner(RegisterArrayTableController.this.getScene().getWindow());
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.setTitle("Edit Register Arrays");

                dialogScene.addEventFilter(
                        KeyEvent.KEY_RELEASED, event -> {
                            if (event.getCode().equals(KeyCode.ESCAPE)) {
                                if (dialogStage.isFocused()) {
                                    dialogStage.close();
                                }
                            }
                        });

                dialogStage.show();
            }
    
            @Override
            protected boolean checkDelete(final RegisterArray toDelete) {
                boolean shouldDelete = super.checkDelete(toDelete);
                if (!shouldDelete) return false; // short circuit
    
                //see if a RegisterArray is used for a ConditionBit and,
                //if so, warn the user and return false
                List<ConditionBit> cBitsThatUseIt = bitController.getBitClonesThatUse(toDelete);
    
                if (cBitsThatUseIt.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Register Array ");
                    sb.append(toDelete.getName());
                    sb.append(" is used by the following condition bits: \n  ");
        
                    Joiner.on(", ").appendTo(sb, cBitsThatUseIt);
                    sb.append(".\nYou need to delete those condition bits first.");
                    Dialogs.createErrorDialog(getScene().getWindow(),
                            "Deletion Error",
                            sb.toString()).showAndWait();
                    shouldDelete = false;
                }
    
                return shouldDelete;
            }
        };
    }
    
    @Override
    public BooleanBinding propertiesButtonEnabledBinding() {
        return selectedItemIsNotNullBinding();
    }
    
    /**
     * getter for prototype of the right subclass
     * @return the prototype of the subclass
     */
    @Override
    public Supplier<RegisterArray> supplierBinding() {
        return () -> new RegisterArray("???", UUID.randomUUID(), getMachine(),
                        4, 32, 0, Register.Access.readWrite());
    }

    /**
     * returns a string of the types of the controller
     * @return a string of the types of the controller
     */
    @Override
    public String toString()
    {
        return "RegisterArray";
    }
    
    /**
     * Check validity of array of Objects' properties.
     */
    public void checkValidity()
    {
        List<RegisterArray> registerArrays = getItems();
        //buildSet up a HashMap of old registers and new widths
        final Map<Register, Integer> regWidths = registerArrays.stream()
                .flatMap(r -> r.getRegisters().stream()) // get all of the registers in the arrays
                .distinct() // remove any duplicates (possibility)
                .collect(Collectors.toMap(Function.identity(), Register::getWidth)); // map them to their widths
        
        //now do all the tests
        Validate.registerWidthsAreOkayForMicros(machineProperty().get(), regWidths);
        
        ValidateControllers.registerArrayWidthsAreOkay(bitController, registerArrays);
        ValidateControllers.registerArrayWidthsAreOkayForTransferMicros(machineProperty().get(), registerArrays);
    }
    
    /**
     * Runs through all of the known {@link RegisterArray} components and searches for a copy of the original
     * {@link Register} passed. If the value is found, it returns {@link Optional#of(Object)}.
     *
     * @param original The original {@link Register} from the {@link cpusim.model.Machine}.
     * @return {@link Optional#of(Object)} or {@link Optional#empty()} if not found.
     */
    Optional<Register> getRegisterClone(Register original) {
        return getItems().stream()
                .flatMap(arr -> arr.getRegisters().stream())
                .filter(r -> r.equals(original))
                .findFirst();
    }
    
    /**
     * get the ID of the corresponding help page
     * @return the ID of the page
     */
    public String getHelpPageID()
    {
        return "Register Arrays";
    }

}
