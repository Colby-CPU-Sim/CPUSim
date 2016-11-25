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
import cpusim.model.Machine;
import cpusim.model.microinstruction.Microinstruction;
import cpusim.model.microinstruction.*;
import cpusim.model.util.ValidationException;
import cpusim.gui.help.HelpController;
import cpusim.gui.util.DragTreeCell;
import cpusim.util.Dialogs;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

/**
 * This class is the controller for the dialog box that is used for
 * editing microinstructions.
 */
public class EditMicroinstructionsController implements Initializable {
    
    @FXML
    private ComboBox<String> microinstructionCombo;
    
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

    private Mediator mediator;

    private Microinstruction selectedSet = null;
    private MicroController<? extends Microinstruction> activeTable;
    private ContentChangeListener listener;
    private DragTreeCell parent;
    private ImmutableMicroControllerMap typesMap;
    
    public EditMicroinstructionsController(Mediator mediator) {
        this(mediator, null);
    }

    public EditMicroinstructionsController(Mediator mediator, DragTreeCell parent) {
        this.mediator = mediator;
        activeTable = null;
        listener = new ContentChangeListener();
        this.parent = parent;
        typesMap = new ImmutableMicroControllerMap(mediator);
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

        microinstructionCombo.setVisibleRowCount(14); // show all micros at once

        setParents(tablePane);
        tablePane.getChildren().clear();
        activeTable = typesMap.getController(TransferRtoR.class);
        tablePane.getChildren().add(activeTable);

        activeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        newButton.setDisable(!((MicroController)activeTable).newMicrosAreAllowed());

        activeTable.getSelectionModel().selectedItemProperty().addListener(listener);

        // resizes the width and height of the table to match the dialog
        tablePane.widthProperty().addListener((observableValue, oldValue, newValue) ->
                    activeTable.setPrefWidth((Double) newValue)
        );
        tablePane.heightProperty().addListener((observableValue, oldValue, newValue) ->
                    activeTable.setPrefHeight((Double)newValue)
        );

        // listen for changes to the instruction combo box selection and update
        // the displayed micro instruction table accordingly.
        microinstructionCombo.getSelectionModel().selectedItemProperty().addListener(
                (selected, oldType, newType) -> {
                    activeTable.getSelectionModel().selectedItemProperty().removeListener(listener);
                    typesMap.get(oldType).getSelectionModel().clearSelection();
                    tablePane.getChildren().clear();
                    tablePane.getChildren().add(typesMap.get(newType));

                    activeTable = typesMap.getController(Machine.getMicroClasses()
                            .stream().filter(c -> c.getSimpleName().equals(newType))
                            .findAny().get());
                    activeTable.setPrefWidth(tablePane.getWidth());
                    activeTable.setPrefHeight(tablePane.getHeight());
                    activeTable.setColumnResizePolicy(TableView
                            .CONSTRAINED_RESIZE_POLICY);


                    newButton.setDisable(!((MicroController) activeTable).newMicrosAreAllowed());

                    selectedSet = null;
                    deleteButton.setDisable(true);
                    duplicateButton.setDisable(true);

                    activeTable.getSelectionModel().selectedItemProperty().
                            addListener(listener);
                });

        // Define an event filter for the ComboBox for Mouse_released events
        EventHandler validityFilter = new EventHandler<InputEvent>() {
            public void handle(InputEvent event) {
                try{
                    ((MicroController<?>)activeTable).checkValidity();

                } catch (ValidationException ex){
                    Dialogs.createErrorDialog(tablePane.getScene().getWindow(),
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
        activeTable.createNewEntry();
    }

    /**
     * deletes an existing set instruction when clicking on Delete button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    public void onDeleteButtonClick(ActionEvent e) {
        int selected = activeTable.getSelectionModel().getSelectedIndex();
        activeTable.deleteEntry(selected);
    }

    /**
     * duplicates the selected set instruction when clicking on Duplicate button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    public void onDuplicateButtonClick(ActionEvent e) {
        //add a new item at the end of the list.
        activeTable.createDuplicateEntry();
    }

    /**
     * save the current changes and close the window when clicking on OK button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    public void onOKButtonClick(ActionEvent e) {
        //get the current edited clones
        try{
            getCurrentController().checkValidity();
            //update the machine with the new values
            updateMachine();
            //get a handle to the stage.
            Stage stage = (Stage) okButton.getScene().getWindow();
            //close window.
            stage.close();

            if (parent != null)
                parent.updateDisplay();
        } catch (ValidationException ex){
            Dialogs.createErrorDialog(tablePane.getScene().getWindow(),
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
        return typesMap.get(Machine.getMicroClassByName(microinstructionCombo.getValue()).get());
    }
    
    /**
     * Called whenever the dialog is exited via the 'ok' button
     * and the machine needs to be updated based on the changes
     * made while the dialog was open (JRL)
     */
    protected void updateMachine() {
        for (MicroController<?> controller : typesMap.values()) {
            controller.updateMachineFromItems();
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
                selectedSet = null;
                deleteButton.setDisable(true);
                duplicateButton.setDisable(true);
            }
            else {
                selectedSet = newMicro;
                deleteButton.setDisable(false);
                duplicateButton.setDisable(false);
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
