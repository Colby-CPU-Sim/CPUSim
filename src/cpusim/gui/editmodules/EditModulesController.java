/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Removed one try catch and allOkay variable and replaced it with a try catch for a validation exception
 */
package cpusim.gui.editmodules;

import cpusim.Machine;
import cpusim.Mediator;
import cpusim.Module;
import cpusim.gui.desktop.DesktopController;
import cpusim.gui.editmodules.editRegisters.EditArrayRegistersController;
import cpusim.gui.help.HelpController;
import cpusim.module.RAM;
import cpusim.module.Register;
import cpusim.module.RegisterArray;
import cpusim.util.Dialogs;
import cpusim.util.ValidationException;
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
import javafx.scene.layout.BorderPane;
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
    BorderPane scene;
    @FXML
    ComboBox<String> moduleCombo;
    @FXML
    Pane tables;
    @FXML
    Button newButton;
    @FXML
    Button deleteButton;
    @FXML
    Button duplicateButton;
    @FXML
    Button okButton;
    @FXML
    Button cancelButton;
    @FXML
    Button helpButton;
    @FXML
    Button propertiesButton;

    Mediator mediator;
    Machine machine;
    DesktopController desktop;

    private Module seletedSet = null;
    private TableView activeTable;
    private ChangeTable tableMap;
    private ContentChangeListener listener;

    public static final String CURRENT = "Current";

    public EditModulesController(Mediator mediator, DesktopController desktop) {
        this.mediator = mediator;
        this.machine = mediator.getMachine();
        this.desktop = desktop;
        activeTable = null;
        tableMap = new ChangeTable(mediator);
        listener = new ContentChangeListener();
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

        moduleCombo.setVisibleRowCount(4);

        tableMap.setParents(tables);
        tables.getChildren().clear();
        tables.getChildren().add(tableMap.getMap().get("Register"));

        activeTable = tableMap.getMap().get("Register");

        activeTable.getSelectionModel().selectedItemProperty().addListener(listener);

        // resizes the width and height of the content panes

        // listens for changes to the size of the dialog window and updates the
        // content panes.
        scene.widthProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(
                            ObservableValue<? extends Number> observableValue,
                            Number oldValue, Number newValue) {
                        Double newWidth = (Double) newValue;
                        activeTable.setPrefWidth(newWidth);
                    }
                }
        );
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue,
                                Number oldValue, Number newValue) {
                Double newHeight = (Double) newValue;
                activeTable.setPrefHeight(newHeight - 130);
            }
        });

        // listen for changes to the instruction combo box selection and update
        // the displayed micro instruction table accordingly.
        moduleCombo.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> selected,
                                        String oldType, String newType) {
                        ((ModuleController) activeTable).setClones(activeTable.getItems());

                        activeTable.getSelectionModel().selectedItemProperty().
                                removeListener(listener);
                        tableMap.getMap().get(oldType).getSelectionModel().clearSelection();
                        tables.getChildren().clear();
                        tables.getChildren().add(
                                tableMap.getMap().get(newType));

                        activeTable = tableMap.getMap().get(newType);

                        activeTable.setPrefWidth(scene.getWidth());
                        activeTable.setPrefHeight(scene.getPrefHeight() - 130);

                        if (!((ModuleController) activeTable).newModulesAreAllowed()) {
                            newButton.setDisable(true);
                        } else {
                            newButton.setDisable(false);
                        }

                        if (oldType.equals("Register") || oldType.equals("RegisterArray"))
                            ((ConditionBitTableController) tableMap.getMap().get("ConditionBit")).updateRegisters();

                        if (newType.equals("RegisterArray") && machine.getModule("registerArrays").size() > 0) {
                            propertiesButton.setVisible(true);
                            propertiesButton.setDisable(false);
                        } else
                            propertiesButton.setVisible(false);

                        seletedSet = null;
                        deleteButton.setDisable(true);
                        duplicateButton.setDisable(true);

                        //listen for changes to the table selection and update the
                        // status of buttons.
                        activeTable.getSelectionModel().selectedItemProperty().
                                addListener(listener);
                    }
                });

        // Define an event filter for the ComboBox for Mouse_released events
        EventHandler validityFilter = new EventHandler<InputEvent>() {
            public void handle(InputEvent event) {
                try {
                    ((ModuleController) activeTable).checkValidity();

                } catch (ValidationException ex) {
                    Dialogs.createErrorDialog(tables.getScene().getWindow(), "Modules Error", ex.getMessage()).showAndWait();
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
        Object newObject = getController(CURRENT).getNewObject(uniqueName);

        // A really ugly hack to create a unique opcode
        // required by InstructionDialog
        /**
         if (EditDialog.this instanceof InstructionDialog) {
         MachineInstruction instr = (MachineInstruction) newObject;
         long uniqueOpcode =
         ((MachineInstrFactory) getCurrentFactory()
         ).createUniqueOpcode(model.getAllObjects());
         instr.setOpcode(uniqueOpcode);
         }*/
        activeTable.getItems().add(0, newObject);
        ((ModuleController) activeTable).updateTable();
        if (activeTable instanceof RegisterArrayTableController)
            propertiesButton.setDisable(false);
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
        if ((seletedSet instanceof Register) ||
                (seletedSet instanceof RegisterArray)) {
            Vector cBitsThatUseIt =
                    ((ConditionBitTableController) tableMap.getMap().
                            get("ConditionBit")).getBitClonesThatUse(seletedSet);
            if (cBitsThatUseIt.size() > 0) {
                String message = seletedSet + " is used by the " +
                        "following condition bits: \n  ";
                for (int i = 0; i < cBitsThatUseIt.size(); i++)
                    message += cBitsThatUseIt.elementAt(i) +
                            (i == cBitsThatUseIt.size() - 1 ? "" : ",  ");
                message += ".\nYou need to delete those condition bits first.";
                Dialogs.createErrorDialog(tables.getScene().getWindow(), "Deletion Error", message).showAndWait();
                return; //don't delete anything
            }
        }

        //now test to see if it is used by any micros and if so,
        //warn the user that those micros will be deleted too.
        Module oldModule =
                getController(CURRENT).getCurrentFromClone(seletedSet);
        if (oldModule != null) {
            HashMap microsThatUseIt = machine.getMicrosThatUse(oldModule);
            if (microsThatUseIt.size() > 0) {
                String[] options = {"Yes, delete it", "Cancel"};
                String message = seletedSet + " is used by the " +
                        "following microinstructions: \n  ";

                Set s = microsThatUseIt.keySet();
                Iterator it = s.iterator();
                while (it.hasNext()) {
                    message += it.next() +
                            (it.hasNext() ? ",  " : "");
                }
                message += ".\n  If you delete it, all these " +
                        "microinstructions will also be deleted.  " +
                        "Really delete it?";
                Optional<ButtonType> result = Dialogs.createConfirmationDialog(tables.getScene().getWindow(), "Confirm Deletion", message).showAndWait();
                if (result.get() == ButtonType.CANCEL ||
                        result.get() == ButtonType.NO ||
                        result.get() == ButtonType.CLOSE)
                    return; //don't delete anything
            }
        }

        activeTable.getItems().remove(activeTable.getItems().indexOf(seletedSet));

        if (selected == 0) {
            activeTable.getSelectionModel().select(0);
        } else {
            activeTable.getSelectionModel().select(selected - 1);
        }
        if (activeTable instanceof RegisterArrayTableController &&
                ((RegisterArrayTableController) activeTable).getTable().getItems().size() == 0)
            propertiesButton.setDisable(true);
    }

    /**
     * duplicates the selected instruction when clicking on Duplicate button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    public void onDuplicateButtonClick(ActionEvent e) {
        //add a new item at the end of the list.
        Module newObject = (Module) seletedSet.clone();
        String uniqueName = createUniqueDuplicatedName(activeTable.getItems(), newObject.getName());
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
        FXMLLoader fxmlLoader = new FXMLLoader(
                mediator.getClass().getResource("gui/editmodules/editRegisters/EditRegisters.fxml"));
        EditArrayRegistersController controller;
        if (activeTable.getSelectionModel().getSelectedIndex() == -1) {
            controller = new EditArrayRegistersController(mediator,
                    (RegistersTableController) tableMap.getMap().get("Register"),
                    (RegisterArrayTableController) tableMap.getMap().get("RegisterArray"));
        } else {
            controller = new EditArrayRegistersController(mediator,
                    (RegistersTableController) tableMap.getMap().get("Register"),
                    (RegisterArrayTableController) tableMap.getMap().get("RegisterArray"),
                    ((RegisterArray) activeTable.getItems().get(
                            activeTable.getSelectionModel().getSelectedIndex()
                    )).getName()
            );
        }

        //controller
        fxmlLoader.setController(controller);

        final Stage dialogStage = new Stage();
        Pane dialogRoot = null;
        try {
            dialogRoot = (Pane) fxmlLoader.load();
        } catch (IOException ex) {
            //TODO: something...
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

            String[] controllerStrings = {"Register", "RegisterArray", "ConditionBit", "RAM"};
            for (String controller : controllerStrings) {
                getController(controller).checkValidity();
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
            Dialogs.createErrorDialog(tables.getScene().getWindow(), "Modules Error", ex.getMessage()).showAndWait();
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
        } else {
            HelpController hc = mediator.getDesktopController().getHelpController();
            hc.getStage().toFront();
            hc.selectTreeItem(startString);
        }
    }

    /**
     * gets the table view object specified by the input String. The options are
     * "Register", "RegisterArray", "ConditionBit", "RAM", and CURRENT (a static string that
     * will return the currently active table).
     *
     * @param controller the String representation of the desired controller
     * @return the table view object that is current being edited in the window.
     */
    public ModuleController getController(String controller) {
        if (controller.equals(CURRENT)) {
            return tableMap.getMap().get(moduleCombo.getValue());
        } else {
            return tableMap.getMap().get(controller);
        }
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
            if (oldName != null && oldName.equals(proposedName))
                return createUniqueName(list, proposedName + "?");
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
    protected void updateMachine() { // and the machine needs to be updated based on the changes
        // ma
        getController(CURRENT).setClones(activeTable.getItems());

        machine.setRAMs(sortVectorByName(
                tableMap.getMap().get("RAM").createNewModulesList(
                        tableMap.getMap().get("RAM").getClones())));
        machine.setRegisters(sortVectorByName(
                tableMap.getMap().get("Register").createNewModulesList(
                        tableMap.getMap().get("Register").getClones())));
        machine.setRegisterArrays(sortVectorByName(
                tableMap.getMap().get("RegisterArray").createNewModulesList(
                        tableMap.getMap().get("RegisterArray").getClones())));
        machine.setConditionBits(sortVectorByName(
                tableMap.getMap().get("ConditionBit").createNewModulesList(
                        tableMap.getMap().get("ConditionBit").getClones())));
        if (!machine.getModule("rams").contains(machine.getCodeStore())) {
            //the code store was deleted so set a different
            //RAM to be the code store
            if (machine.getModule("rams").size() != 0)
                machine.setCodeStore((RAM) machine.getModule("rams").get(0));
            else
                machine.setCodeStore(null);
        }
        desktop.adjustTablesForNewModules();
        mediator.setMachineDirty(true);
        mediator.clearRAMs();
        mediator.clearRegisterArrays();
        mediator.clearRegisters();
    }

    //sorts the given Vector of Modules in place by name
    //using Selection Sort.  It returns the modified vector.
    private Vector sortVectorByName(Vector modules) {
        for (int i = 0; i < modules.size() - 1; i++) {
            //find the smallest from positions i to the end
            String nameOfSmallest = ((Module) modules.elementAt(i)).getName();
            int indexOfSmallest = i;
            for (int j = i + 1; j < modules.size(); j++) {
                Module next = (Module) modules.elementAt(j);
                if (next.getName().compareTo(nameOfSmallest) < 0) {
                    indexOfSmallest = j;
                    nameOfSmallest = next.getName();
                }
            }
            //swap smallest into position i
            Object temp = modules.elementAt(i);
            modules.setElementAt(modules.elementAt(indexOfSmallest), i);
            modules.setElementAt(temp, indexOfSmallest);
        }
        return modules;
    }

    /**
     * a listener listening for changes to the table selection and
     * update the status of buttons.
     */
    class ContentChangeListener implements ChangeListener<Module> {

        @Override
        public void changed(ObservableValue<? extends Module> selected,
                            Module oldMicro,
                            Module newMicro) {
            if (newMicro == null) {
                seletedSet = null;
                deleteButton.setDisable(true);
                duplicateButton.setDisable(true);
            } else {
                seletedSet = newMicro;
                deleteButton.setDisable(false);
                duplicateButton.setDisable(false);
            }
        }
    }

    /**
     * a class that holds the current microinstruction class
     */
    class ChangeTable {
        Map<String, ModuleController> typesMap;

        /**
         * Constructor
         *
         * @param mediator the current mediator
         */
        public ChangeTable(Mediator mediator) {
            // an hashmap that holds types as the keys and sub fxml names as values.
            typesMap = buildMap(mediator);

        }

        /**
         * build the map to store all the controllers
         *
         * @param mediator mediator that holds all the information
         * @return the map that contains all the controllers
         */
        public Map buildMap(Mediator mediator) {
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

            Map<String, ModuleController> map = new HashMap<String, ModuleController>() {{
                put("Register", registerTableController);
                put("RegisterArray", registerArrayTableController);
                put("ConditionBit", conditionBitTableController);
                put("RAM", ramTableController);
            }};
            return map;
        }

        /**
         * returns the map of controllers.
         *
         * @return the map of controllers.
         */
        public Map<String, ModuleController> getMap() {
            return typesMap;
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
