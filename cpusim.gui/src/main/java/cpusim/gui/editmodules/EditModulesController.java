/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Removed one try catch and allOkay variable and replaced it with a try catch for
 * a validation exception
 */
package cpusim.gui.editmodules;

import com.google.common.collect.ImmutableList;
import cpusim.model.Machine;
import cpusim.Mediator;
import cpusim.model.Microinstruction;
import cpusim.model.Module;
import cpusim.gui.desktop.DesktopController;
import cpusim.gui.editmodules.arrayregisters.EditArrayRegistersController;
import cpusim.gui.help.HelpController;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.RAM;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import cpusim.model.util.ValidationException;
import cpusim.util.Dialogs;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * This class is the controller for the dialog box that is used for
 * editing the properties of each register in a register array.
 */
public class EditModulesController implements Initializable {
    @FXML
    private ComboBox<String> moduleCombo;

    @FXML
    private Pane tablePane;

    @FXML
    private Button newButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button duplicateButton;

    @FXML
    private Button okButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button helpButton;

    @FXML
    private Button propertiesButton;

    private Mediator mediator;
    private Machine machine;
    private DesktopController desktop;

    private Module<?> selectedSet = null;
    private TableView<Module<?>> activeTable;
    private ChangeTable tableMap;
    private ContentChangeListener contentChangeListener;
    
    private static final String CURRENT_NAME = "Current";

    public EditModulesController(Mediator mediator, DesktopController desktop) {
        this.mediator = mediator;
        this.machine = mediator.getMachine();
        this.desktop = desktop;
        activeTable = null;
        tableMap = new ChangeTable(mediator);
        contentChangeListener = new ContentChangeListener();
    }

    /**
     * initializes the dialog window after its root element has been processed.
     * contains a listener to the combo box, so that the content of the table will
     * change according to the selected type of microinstruction.
     *
     * @param url the location used to resolve relative paths for the root
     *            object, or null if the location is not known.
     * @param rb  the resources used to localize the root object, or null if the root
     *            object was not localized.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        moduleCombo.setVisibleRowCount(4); // show all modules at once

        tableMap.setParents(tablePane);
        tablePane.getChildren().clear();
        activeTable = tableMap.get(Register.class);
        tablePane.getChildren().add(activeTable);

        activeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        activeTable.getSelectionModel().selectedItemProperty().addListener
                (contentChangeListener);

        // resizes the width and height of the content panes
        tablePane.widthProperty().addListener((observableValue, oldValue, newValue) ->
                        activeTable.setPrefWidth((Double) newValue)
        );
        tablePane.heightProperty().addListener((observableValue, oldValue, newValue) ->
                        activeTable.setPrefHeight((Double) newValue)
        );

        // listen for changes to the instruction combo box selection and update
        // the displayed micro instruction table accordingly.
        moduleCombo.getSelectionModel().selectedItemProperty().addListener(
                (selected, oldType, newType) -> {
                    ((ModuleController) activeTable).setClones(activeTable.getItems());

                    activeTable.getSelectionModel().selectedItemProperty().
                            removeListener(contentChangeListener);
                    tableMap.getMap().get(oldType).getSelectionModel().clearSelection();
                    tablePane.getChildren().clear();
                    tablePane.getChildren().add(
                            tableMap.getMap().get(newType));

                    activeTable = tableMap.getMap().get(newType);
                    activeTable.setPrefWidth(tablePane.getWidth());
                    activeTable.setPrefHeight(tablePane.getHeight());
                    activeTable.setColumnResizePolicy(TableView
                            .CONSTRAINED_RESIZE_POLICY);

                    newButton.setDisable(!((ModuleController) activeTable)
                            .newModulesAreAllowed());

                    if (oldType.equals("Register") || oldType.equals("RegisterArray")) {
                        ((ConditionBitTableController) tableMap.getMap().get
                                ("ConditionBit")).updateRegisters();
                    }

                    propertiesButton.setDisable(!newType.equals("RegisterArray") ||
                                                activeTable.getItems().size() == 0);

                    selectedSet = null;
                    deleteButton.setDisable(true);
                    duplicateButton.setDisable(true);

                    //listen for changes to the table selection and update the
                    // status of buttons.
                    activeTable.getSelectionModel().selectedItemProperty().
                            addListener(contentChangeListener);
                });

        // Define an event filter for the ComboBox for Mouse_released events
        EventHandler validityFilter = new EventHandler<InputEvent>() {
            public void handle(InputEvent event) {
                try {
                    ((ModuleController) activeTable).checkValidity();

                } catch (ValidationException ex) {
                    Dialogs.createErrorDialog(tablePane.getScene().getWindow(),
                            "Modules Error", ex.getMessage()).showAndWait();
                    event.consume();
                }
            }
        };
        moduleCombo.addEventFilter(MouseEvent.MOUSE_RELEASED, validityFilter);
    }

    /**
     * creates a new instruction when clicking on New button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    protected void onNewButtonClick(ActionEvent e) {
        //add a new item at the end of the list.
        String uniqueName = createUniqueName(activeTable.getItems(), "?");
        Module<?> newObject = getControllerUnchecked(currentController()).getPrototype().cloneOf();
        newObject.setName(uniqueName);

        activeTable.getItems().add(activeTable.getItems().size(), newObject);
        ((ModuleController) activeTable).updateTable();
        activeTable.getSelectionModel().select(newObject);
        if (activeTable instanceof RegisterArrayTableController) {
            propertiesButton.setDisable(false);
        }
    }

    /**
     * deletes an existing instruction when clicking on Delete button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    public void onDeleteButtonClick(ActionEvent e) {
        int selected = activeTable.getSelectionModel().getSelectedIndex();

        //first see if it is a register used for a ConditionBit and,
        //if so, warn the user and return.
        if ((selectedSet instanceof Register) ||
                (selectedSet instanceof RegisterArray)) {
            ConditionBitTableController ctrl = (ConditionBitTableController)tableMap.get(ConditionBit.class);
            List<ConditionBit> cBitsThatUseIt;
            if (selectedSet instanceof Register) {
                cBitsThatUseIt = ctrl.getBitClonesThatUse((Register) selectedSet);
            } else {
                cBitsThatUseIt = ctrl.getBitClonesThatUse((RegisterArray) selectedSet);
            }

            if (cBitsThatUseIt.size() > 0) {
                String message = selectedSet + " is used by the " +
                        "following condition bits: \n  ";
                for (int i = 0; i < cBitsThatUseIt.size(); i++)
                    message += cBitsThatUseIt.get(i) +
                            (i == cBitsThatUseIt.size() - 1 ? "" : ",  ");
                message += ".\nYou need to delete those condition bits first.";
                Dialogs.createErrorDialog(tablePane.getScene().getWindow(), "Deletion " +
                        "Error", message).showAndWait();
                return; //don't delete anything
            }
        }

        //now test to see if it is used by any micros and if so,
        //warn the user that those micros will be deleted too.
        Module<?> oldModule = getControllerUnchecked(currentController()).getCurrentFromClone(selectedSet);
        if (oldModule != null) {
            Map<Microinstruction, ObservableList<Microinstruction>> microsThatUseIt =
                    machine.getMicrosThatUse(oldModule);
            if (microsThatUseIt.size() > 0) {
                String[] options = {"Yes, delete it", "Cancel"};
                StringBuilder message = new StringBuilder(selectedSet.toString());
                message.append(" is used by the following microinstructions: \n  ");

                Set<Microinstruction> s = microsThatUseIt.keySet();
                Iterator it = s.iterator();
                while (it.hasNext()) {
                    message.append(it.next());
                    message.append(it.hasNext() ? ",  " : "");
                }

                message.append(".\n  If you delete it, all these microinstructions will also be deleted.  ");
                message.append("Really delete it?");
                Optional<ButtonType> result = Dialogs.createConfirmationDialog
                        (tablePane.getScene().getWindow(), "Confirm Deletion", message.toString())
                        .showAndWait();
                if (result.get() == ButtonType.CANCEL ||
                        result.get() == ButtonType.NO ||
                        result.get() == ButtonType.CLOSE) {
                    return; //don't delete anything
                }
            }
        }

        activeTable.getItems().remove(activeTable.getItems().indexOf(selectedSet));

        if (selected == 0) {
            activeTable.getSelectionModel().select(0);
        }
        else {
            activeTable.getSelectionModel().select(selected - 1);
        }
        if (activeTable instanceof RegisterArrayTableController &&
                ((RegisterArrayTableController) activeTable).getTable().getItems().size
                        () == 0) {
            propertiesButton.setDisable(true);
        }
    }

    /**
     * duplicates the selected instruction when clicking on Duplicate button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    public void onDuplicateButtonClick(ActionEvent e) {
        //add a new item at the end of the list.
        Module<?> newObject = selectedSet.cloneOf();
        String uniqueName = createUniqueDuplicatedName(activeTable.getItems(),
                newObject.getName());
        newObject.setName(uniqueName);
        activeTable.getItems().add(0, newObject);
        //update display
        ((ModuleController) activeTable).updateTable();
        activeTable.scrollTo(1);
        activeTable.getSelectionModel().select(0);
    }

    /**
     * edits the selected register array
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    public void onPropertiesButtonClick(ActionEvent e) {


        EditArrayRegistersController controller;
        if (activeTable.getSelectionModel().getSelectedIndex() == -1) {
            controller = new EditArrayRegistersController(mediator,
                    (RegistersTableController) tableMap.get(Register.class),
                    (RegisterArrayTableController) tableMap.get(RegisterArray.class));
        }
        else {
            controller = new EditArrayRegistersController(mediator,
                    (RegistersTableController) tableMap.get(Register.class),
                    (RegisterArrayTableController) tableMap.get(RegisterArray.class),
                    (activeTable.getItems().get(
                            activeTable.getSelectionModel().getSelectedIndex()
                    )).getName()
            );
        }

        //controller
        FXMLLoader fxmlLoader = new FXMLLoader(mediator.getClass().getResource(
                "gui/editmodules/arrayregisters/EditRegisters.fxml"));
        fxmlLoader.setController(controller);

        final Stage dialogStage = new Stage();
        Pane dialogRoot = null;
        try {
            dialogRoot = fxmlLoader.load();
        } catch (IOException ex) {
            // should never happen
            assert false : "Unable to load file: EditRegisters.fxml";
        }
        Scene dialogScene = new Scene(dialogRoot);
        dialogStage.setScene(dialogScene);
        dialogStage.initOwner(propertiesButton.getScene().getWindow());
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setTitle("Edit Register Arrays");

        dialogScene.addEventFilter(
                KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent event) {
                        if (event.getCode().equals(KeyCode.ESCAPE)) {
                            if (dialogStage.isFocused()) {
                                dialogStage.close();
                            }
                        }
                    }
                });
        dialogStage.show();
    }

    /**
     * save the current changes and close the window when clicking on OK button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    public void onOKButtonClick(ActionEvent e) {
        //get the current edited clones
        //ObservableList objList = activeTable.getItems();
        try {

//            String[] controllerStrings = {"Register", "RegisterArray", "ConditionBit",
//                    "RAM"};
            List<Class<? extends Module<?>>> values = ImmutableList.of(Register.class, RegisterArray.class,
                    ConditionBit.class, RAM.class);
            for (Class<? extends Module<?>> controller : values) {
                getControllerUnchecked(controller).checkValidity();
            }

            //update the machine with the new values
            updateMachine();
            //get a handle to the stage.
            Stage stage = (Stage) okButton.getScene().getWindow();
            //close window.
            stage.close();

            mediator.addPropertyChangeListenerToAllModules(mediator.getBackupManager());
            desktop.getHighlightManager().updatePairsForNewRegistersAndRAMs();
        } catch (ValidationException ex) {
            Dialogs.createErrorDialog(tablePane.getScene().getWindow(), "Modules " +
                    "Error", ex.getMessage()).showAndWait();
        }

    }

    /**
     * close the window without saving the changes.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    public void onCancelButtonClick(ActionEvent e) {
        //get a handle to the stage.
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        //close window.
        stage.close();
    }

    /**
     * open a help window when clicking on the help button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    public void onHelpButtonClick(ActionEvent e) {
        String startString = ((ModuleController) activeTable).getHelpPageID();
        if (mediator.getDesktopController().getHelpController() == null) {
            HelpController helpController = HelpController.openHelpDialog(
                    mediator.getDesktopController(), startString);
            mediator.getDesktopController().setHelpController(helpController);
        }
        else {
            HelpController hc = mediator.getDesktopController().getHelpController();
            hc.getStage().toFront();
            hc.selectTreeItem(startString);
        }
    }
    
    /**
     * Get the class representing the current controller. The options are:
     * <ul>
     *     <li>{@code Register} -> {@link Register}</li>
     *     <li>{@code RegisterArray} -> {@link RegisterArray}</li>
     *     <li>{@code ConditionBit} -> {@link ConditionBit}</li>
     *     <li>{@code RAM} -> {@link RAM}</li>
     * </ul>
     *
     * @return the {@link Class} representing the currently requested controller
     */
    private Class<? extends Module<?>> currentController() {
        switch (moduleCombo.getValue()) {
        case "Register":
            return Register.class;
    
        case "RegisterArray":
            return RegisterArray.class;
    
        case "ConditionBit":
            return ConditionBit.class;
        
        case "RAM":
            return RAM.class;
        
        default:
            throw new IllegalArgumentException("Unknown module specified: " + moduleCombo.getValue());
        }
    }
    
    /**
     * Get the {@link ModuleController} for the required {@link Module} class.
     *
     * @param controllerClazz the {@link Class} of the {@link ModuleController}.
     * @return the table view object that is current being edited in the window.
     */
    private <T extends Module<T>> ModuleController<T> getController(Class<T> controllerClazz) {
        return tableMap.get(controllerClazz);
    }
    
    /**
     * Unchecked version of {@link #getController(Class)}
     * @param controllerClazz
     * @return
     */
    private ModuleController<?> getControllerUnchecked(Class<? extends Module<?>> controllerClazz) {
        return tableMap.getMap().get(controllerClazz);
    }

    /**
     * returns a String that is different from all names of
     * existing objects in the given list.  It checks whether proposedName
     * is unique and if so, it returns it.  Otherwise, it
     * proposes a new name of proposedName + "?" and tries again.
     *
     * @param list         list of existing objects
     * @param proposedName a given proposed name
     * @return the unique name
     */
    public String createUniqueName(ObservableList list, String proposedName) {
        String oldName;
        for (Object obj : list) {
            oldName = obj.toString();
            if (oldName != null && oldName.equals(proposedName)) {
                return createUniqueName(list, proposedName + "?");
            }
        }
        return proposedName;
    }

    /**
     * returns a String that is different from all names of
     * existing objects in the given list.  It checks whether proposedName
     * is unique and if so, it returns it.  Otherwise, it
     * proposes a new name of proposedName + "copy" and tries again.
     *
     * @param list         list of existing objects
     * @param proposedName a given proposed name
     * @return the unique name
     */
    protected String createUniqueDuplicatedName(ObservableList list,
                                                String proposedName) {
        int i = 1;
        String s = proposedName + "_copy1";

        for (Object aList : list) {
            String oldName = aList.toString();
            // Duplicating name properly
            if (oldName != null && oldName.equals(s)) {
                i++;
                s = s.substring(0, s.length() - 1) + String.valueOf(i);
            }
        }
        return s;
    }

    /**
     * Called whenever the dialog is exited via the 'ok' button
     * and the machine needs to be updated based on the changes
     * made while the dialog was open (JRL)
     */
    protected void updateMachine() { // and the machine needs to be updated based on
    // the changes
        // ma
        getControllerUnchecked(currentController()).setClones(activeTable.getItems());

        machine.setRAMs(sortModulesByName(
                tableMap.get(RAM.class).createNewModulesList(tableMap.get(RAM.class).getClones())));
        machine.setRegisters(sortModulesByName(
                tableMap.get(Register.class).createNewModulesList(tableMap.get(Register.class).getClones())));
        machine.setRegisterArrays(sortModulesByName(
                tableMap.get(RegisterArray.class).createNewModulesList(tableMap.get(RegisterArray.class).getClones())));
        machine.setConditionBits(sortModulesByName(
                tableMap.get(ConditionBit.class).createNewModulesList(tableMap.get(ConditionBit.class).getClones())));
        
        List<RAM> rams = machine.getModule("rams", RAM.class);
        if (!rams.contains(machine.getCodeStore())) {
            //the code store was deleted so set a different
            //RAM to be the code store
            if (rams.size() != 0) {
                machine.setCodeStore(rams.get(0));
            }
            else {
                machine.setCodeStore(null);
            }
        }
        desktop.adjustTablesForNewModules();
        mediator.setMachineDirty(true);
        mediator.clearRAMs();
        mediator.clearRegisterArrays();
        mediator.clearRegisters();
    }

    //sorts the given Vector of Modules in place by name
    //using Selection Sort.  It returns the modified vector.
    private <T extends Module<T>> List<T> sortModulesByName(final List<T> modules) {

        Collections.sort(modules, new Module.NameComparator());

//        for (int i = 0; i < modules.size() - 1; i++) {
//            //find the smallest from positions i to the end
//            String nameOfSmallest = ((Module) modules.get(i)).getName();
//            int indexOfSmallest = i;
//            for (int j = i + 1; j < modules.size(); j++) {
//                Module next = (Module) modules.get(j);
//                if (next.getName().compareTo(nameOfSmallest) < 0) {
//                    indexOfSmallest = j;
//                    nameOfSmallest = next.getName();
//                }
//            }
//            //swap smallest into position i
//            Module<?> temp = modules.get(i);
//            modules.set(i, modules.get(indexOfSmallest));
//            modules.set(indexOfSmallest, temp);
//        }

        return modules;
    }

    /**
     * a listener listening for changes to the table selection and
     * update the status of buttons.
     */
    private class ContentChangeListener implements ChangeListener<Module> {

        @Override
        public void changed(ObservableValue<? extends Module> selected,
                            Module oldModule,
                            Module newModule) {
            if (newModule == null) {
                selectedSet = null;
                deleteButton.setDisable(true);
                duplicateButton.setDisable(true);
            }
            else {
                selectedSet = newModule;
                deleteButton.setDisable(false);
                duplicateButton.setDisable(false);
            }
        }
    }

    /**
     * a class that holds the current microinstruction class
     */
    private class ChangeTable {
        private Map<Class<? extends Module<?>>, ModuleController<?>> typesMap;

        /**
         * Constructor
         *
         * @param mediator the current mediator
         */
        ChangeTable(Mediator mediator) {
            // an hashmap that holds types as the keys and sub fxml names as values.
            typesMap = buildMap(mediator);

        }

        /**
         * build the map to store all the controllers
         *
         * @param mediator mediator that holds all the information
         * @return the map that contains all the controllers
         */
        private Map<Class<? extends Module<?>>, ModuleController<?>> buildMap(Mediator mediator) {
            final RegistersTableController registerTableController
                    = new RegistersTableController(mediator);
            final RegisterArrayTableController registerArrayTableController
                    = new RegisterArrayTableController(mediator);
            final ConditionBitTableController conditionBitTableController
                    = new ConditionBitTableController(mediator,
                    registerTableController,
                    registerArrayTableController);
            final RAMsTableController ramTableController
                    = new RAMsTableController(mediator);
            registerTableController.setBitController(conditionBitTableController);
            registerArrayTableController.setBitController(conditionBitTableController);

            Map<Class<? extends Module<?>>, ModuleController<?>> map = new HashMap<Class<? extends Module<?>>, ModuleController<?>>() {{
                put(Register.class, registerTableController);
                put(RegisterArray.class, registerArrayTableController);
                put(ConditionBit.class, conditionBitTableController);
                put(RAM.class, ramTableController);
            }};

            return map;
        }

        /**
         * returns the map of controllers.
         *
         * @return the map of controllers.
         */
        public Map<Class<? extends Module<?>>, ModuleController<?>> getMap() {
            return typesMap;
        }

        @SuppressWarnings("unchecked")
        public <T extends Module<T>> ModuleController<T> get(Class<T> clazz) {
            return (ModuleController<T>)typesMap.get(clazz);
        }
    
        /**
         * Get the same result as {@link #get(Class)} but returns an unchecked version.
         * @param clazz
         * @return
         */
        public ModuleController<?> getUnchecked(Class<? extends Module<?>> clazz) {
            return typesMap.get(clazz);
        }

        /**
         * sets the parents frame for each controller.
         *
         * @param tables the controller to be edited.
         */
        public void setParents(Node tables) {
            for (ModuleController moduleController : typesMap.values()) {
                moduleController.setParentFrame(tables);
            }
        }
    }

    /**
     * selects the section that will be shown
     *
     * @param indexInComboBox the index of the section in the combo box
     */
    public void selectSection(int indexInComboBox) {
        if (0 <= indexInComboBox && indexInComboBox <= 3) {
            moduleCombo.getSelectionModel().select(indexInComboBox);
        }
    }
}
