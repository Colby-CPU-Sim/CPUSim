/**
 * auther: Jinghui Yu
 * last edit date: 6/5/2013
 */

/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Removed one try catch and allOkay variable and replaced it with a try catch for a validation exception
 */
package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.Microinstruction;
import cpusim.gui.help.HelpController;
import cpusim.gui.util.DragTreeCell;
import cpusim.util.CPUSimConstants;
import cpusim.util.Dialogs;
import cpusim.util.ValidationException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;

/**
 * This class is the controller for the dialog box that is used for
 * editing microinstructions.
 */
public class EditMicroinstructionsController implements Initializable {
    @FXML
    BorderPane scene;
    @FXML
    ComboBox<String> microinstructionCombo;
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

    Mediator mediator;

    private Microinstruction seletedSet = null;
    private TableView activeTable;
    private ChangeTable tableMap;
    private ContentChangeListener listener;
    private DragTreeCell parent;
    
    public EditMicroinstructionsController(Mediator mediator) {
        this.mediator = mediator;
        activeTable = null;
        tableMap = new ChangeTable();
        listener = new ContentChangeListener();
    }

    public EditMicroinstructionsController(Mediator mediator, DragTreeCell parent) {
        this.mediator = mediator;
        activeTable = null;
        tableMap = new ChangeTable();
        listener = new ContentChangeListener();
        this.parent = parent;
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

        microinstructionCombo.setVisibleRowCount(14);

        tableMap.setParents(tables);
        tables.getChildren().clear();
        tables.getChildren().add(tableMap.getMap().get("TransferRtoR"));

        activeTable = tableMap.getMap().get("TransferRtoR");

        if (!((MicroController)activeTable).newMicrosAreAllowed()) {
            newButton.setDisable(true);
        }
        else {
            newButton.setDisable(false);
        }
        activeTable.getSelectionModel().selectedItemProperty().addListener(listener);

        // resizes the width and height of the content panes

        // listens for changes to the size of the dialog window and updates the
        // content panes.
        scene.widthProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(
                            ObservableValue<? extends Number> observableValue,
                            Number oldValue,
                            Number newValue) {
                        Double newWidth = (Double) newValue;
                        activeTable.setPrefWidth(newWidth);
                    }
                }
        );
        scene.heightProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(
                            ObservableValue<? extends Number> observableValue,
                            Number oldValue,
                            Number newValue) {
                        Double newHeight = (Double) newValue;
                        activeTable.setPrefHeight(newHeight - 130);
                    }
                }
        );

        // listen for changes to the instruction combo box selection and update
        // the displayed micro instruction table accordingly.
        microinstructionCombo.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> selected,
                                        String oldType, String newType) {
                        activeTable.getSelectionModel().selectedItemProperty().
                                removeListener(listener);
                        tableMap.getMap().get(oldType).getSelectionModel().clearSelection();
                        tables.getChildren().clear();
                        tables.getChildren().add(
                                tableMap.getMap().get(newType));

                        activeTable = tableMap.getMap().get(newType);
                        activeTable.setPrefSize(
                                scene.getWidth(), scene.getHeight() - 130);

                        if (!((MicroController)activeTable).newMicrosAreAllowed()) {
                            newButton.setDisable(true);
                        }
                        else {
                            newButton.setDisable(false);
                        }

                        seletedSet = null;
                        deleteButton.setDisable(true);
                        duplicateButton.setDisable(true);

                        activeTable.getSelectionModel().selectedItemProperty().
                                addListener(listener);
                    }
                });

        // Define an event filter for the ComboBox for Mouse_released events
        EventHandler validityFilter = new EventHandler<InputEvent>() {
            public void handle(InputEvent event) {
                try{
                    ((MicroController)activeTable).checkValidity(activeTable.getItems());

                } catch (ValidationException ex){
                    Dialogs.createErrorDialog(tables.getScene().getWindow(),
                            "Microinstruction Error", ex.getMessage()).showAndWait();

                    event.consume();
                }
            }
        };
        microinstructionCombo.addEventFilter(MouseEvent.MOUSE_RELEASED, validityFilter);
    }

    /**
     * creates a new set instruction when clicking on New button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    protected void onNewButtonClick(ActionEvent e) {
        //add a new item at the end of the list.
        String uniqueName = createUniqueName(activeTable.getItems(), "?");
        Object newObject = getCurrentController().getNewObject(uniqueName);

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
        ((MicroController) activeTable).updateTable();
    }

    /**
     * deletes an existing set instruction when clicking on Delete button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    public void onDeleteButtonClick(ActionEvent e) {
        int selected = activeTable.getSelectionModel().getSelectedIndex();
        Microinstruction theMicro =
                (Microinstruction) activeTable.getItems().get(selected);

        //first see if it is used by any machine instructions and,
        // if so, warn the user.
        Microinstruction oldInstr =
                ((MicroController)activeTable).getCurrentFromClone(theMicro);
        if (oldInstr != null) {
            Vector instrsThatUseIt =
                    mediator.getMachine().getInstructionsThatUse(oldInstr);
            if (instrsThatUseIt.size() > 0) {
                String message = theMicro + " is used by the " +
                        "following machine instructions: \n  ";
                for (int i = 0; i < instrsThatUseIt.size(); i++)
                    message += instrsThatUseIt.elementAt(i) + " ";
                message += ".\nReally delete it?";

                Alert dialog = Dialogs.createConfirmationDialog(tables.getScene().getWindow(),
                        "Confirm Deletion", message);
                Optional<ButtonType> result = dialog.showAndWait();
                if(result.get() == ButtonType.CANCEL ||
                        result.get() == ButtonType.NO ||
                        result.get() == ButtonType.CLOSE)
                    return; //don't delete anything
            }
        }

        activeTable.getItems().remove(activeTable.getItems().indexOf(seletedSet));

        if (selected == 0) {
            activeTable.getSelectionModel().select(0);
        }
        else{
            activeTable.getSelectionModel().select( selected - 1 );
        }
    }

    /**
     * duplicates the selected set instruction when clicking on Duplicate button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    public void onDuplicateButtonClick(ActionEvent e) {
        //add a new item at the end of the list.
        Microinstruction newObject = (Microinstruction) seletedSet.clone();
        String uniqueName = createUniqueDuplicatedName(activeTable.getItems(), newObject.getName());
        newObject.setName(uniqueName);
        activeTable.getItems().add(0, newObject);
        //update display
        ((MicroController) activeTable).updateTable();
        activeTable.scrollTo(1);
        activeTable.getSelectionModel().select(0);
    }

    /**
     * save the current changes and close the window when clicking on OK button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    public void onOKButtonClick(ActionEvent e) {
        //get the current edited clones
        ObservableList objList = activeTable.getItems();

        try{
            getCurrentController().checkValidity(objList);
            //update the machine with the new values
            updateMachine();
            //get a handle to the stage.
            Stage stage = (Stage) okButton.getScene().getWindow();
            //close window.
            stage.close();

            if (parent != null)
                parent.updateDisplay();
        } catch (ValidationException ex){
            Dialogs.createErrorDialog(tables.getScene().getWindow(),
                    "Microinstruction Error", ex.getMessage()).showAndWait();
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
    	String startString = ((MicroController)activeTable).getHelpPageID();
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
     * gets the table view object that is current being edited in the window.
     * @return  the table view object that is current being edited in the window.
     */
    public MicroController getCurrentController() {
        return tableMap.getMap().get(microinstructionCombo.getValue());
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
        String s = proposedName +"_copy1";

        for (Object aList : list) {
            String oldName = aList.toString();
            // Duplicating name properly
            if (oldName != null && oldName.equals(s)) {
                i++;
                s = s.substring(0, s.length()-1)+String.valueOf(i);
            }
        }
        return s;
    }

    /**
     * Called whenever the dialog is exited via the 'ok' button
     * and the machine needs to be updated based on the changes
     * made while the dialog was open (JRL)
     */
    protected void updateMachine() {
        for (MicroController controller : tableMap.getMap().values()) {
            ObservableList microList = controller.getItems();
            controller.setClones(microList);
            controller.updateCurrentMicrosFromClones();
        }
        mediator.setMachineDirty(true);
    }

    /**
     * a listener listening for changes to the table selection and
     * update the status of buttons.
     */
    class ContentChangeListener implements ChangeListener<Microinstruction> {

        @Override
        public void changed(ObservableValue<? extends Microinstruction> selected,
                            Microinstruction oldMicro,
                            Microinstruction newMicro) {
            if (newMicro == null) {
                seletedSet = null;
                deleteButton.setDisable(true);
                duplicateButton.setDisable(true);
            }
            else {
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
        Map<String, MicroController> typesMap;

        /**
         * Constructor
         */
        public ChangeTable() {
            // an hashmap that holds types as the keys and sub fxml names as values.
            typesMap = new HashMap<String, MicroController>() {{
                put("Set", new SetTableController(mediator));
                put("Test", new TestTableController(mediator));
                put("Increment", new IncrementTableController(mediator));
                put("Shift", new ShiftTableController(mediator));
                put("Logical", new LogicalTableController(mediator));
                put("Arithmetic", new ArithmeticTableController(mediator));
                put("Branch", new BranchTableController(mediator));
                put("TransferRtoR", new TransferRtoRTableController(mediator));
                put("TransferRtoA", new TransferRtoATableController(mediator));
                put("TransferAtoR", new TransferAtoRTableController(mediator));
                put("Decode", new DecodeTableController(mediator));
                put("SetCondBit", new SetCondBitTableController(mediator));
                put("IO", new IOTableController(mediator));
                put("MemoryAccess", new MemoryAccessTableController(mediator));
            }};

        }

        /**
         * returns the map of controllers.
         * @return the map of controllers.
         */
        public Map<String, MicroController> getMap() {
            return typesMap;
        }

        /**
         * sets the parents frame for each controller.
         * @param tables the controller to be edited.
         */
        public void setParents(Node tables) {
            for (MicroController microController : typesMap.values()) {
                microController.setParentFrame(tables);
            }
        }
    }

    /**
     * selects the section that will be shown
     * @param microInstr the index of the section in the combo box
     */
    public void selectSection(String microInstr) {
        int index = -1;
        for (String m : microinstructionCombo.getItems()){
            if (m.equals(microInstr)){
                index = microinstructionCombo.getItems().indexOf(m);
            }
        }
        if (index != -1){
            microinstructionCombo.getSelectionModel().select(index);
        }

    }

    /**
     * getter of the table shown in the dialog
     * @return the activetable item
     */
    public TableView getActiveTable(){
        return activeTable;
    }
}
