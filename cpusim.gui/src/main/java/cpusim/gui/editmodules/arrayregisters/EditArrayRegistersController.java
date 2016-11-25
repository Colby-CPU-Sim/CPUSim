/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Removed one try catch and allOkay variable and replaced it with a try catch for a validation exception
 */
package cpusim.gui.editmodules.arrayregisters;

import cpusim.Mediator;
import cpusim.gui.editmodules.RegisterArrayTableController;
import cpusim.gui.editmodules.RegistersTableController;
import cpusim.gui.help.HelpController;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import cpusim.model.util.NamedObject;
import cpusim.model.util.Validatable;
import cpusim.model.util.ValidationException;
import cpusim.util.Dialogs;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class EditArrayRegistersController implements Initializable {
    @FXML
    private ComboBox<String> arrayCombo;
    @FXML
    private Pane tablePane;
    @FXML
    private Button okButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button helpButton;

    private Mediator mediator;
    private RegistersTableController registerController;
    private ObservableList<RegisterArray> registerArrays;
    private RegisterArrayTableView activeTable;
    private ChangeTable tableMap;
    private String selection;

    public EditArrayRegistersController(Mediator mediator,
                                        RegistersTableController registerTableController,
                                        RegisterArrayTableController controller,
                                        String selected) {
        this.mediator = mediator;
        this.registerController = registerTableController;
        this.registerArrays = controller.getItems();
        activeTable = null;

        selection = selected;
    }

    public EditArrayRegistersController(Mediator mediator,
                                        RegistersTableController registerTableController,
                                        RegisterArrayTableController controller) {
        this.mediator = mediator;
        this.registerController = registerTableController;
        this.registerArrays = controller.getItems();
        activeTable = null;

        selection = registerArrays.get(0).getName();
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

        arrayCombo.getItems().addAll(registerArrays.stream().map(
                RegisterArray::getName).collect(Collectors.toList()));
        arrayCombo.getSelectionModel().select(selection);

        tableMap = new ChangeTable(registerArrays);
        tableMap.setParents(tablePane);
        tablePane.getChildren().clear();
        tablePane.getChildren().add(tableMap.getMap().get(selection));

        activeTable = tableMap.getMap().get(selection);
        activeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // resizes the width and height of the content panes
        tablePane.widthProperty().addListener((observableValue, oldValue, newValue) ->
                        activeTable.setPrefWidth((Double) newValue)
        );
        tablePane.heightProperty().addListener((observableValue, oldValue, newValue) ->
                        activeTable.setPrefHeight((Double) newValue)
        );

        // listen for changes to the instruction combo box selection and update
        // the displayed micro instruction table accordingly.
        arrayCombo.getSelectionModel().selectedItemProperty().addListener(
                (selected, oldType, newType) -> {
                    activeTable.setClones(activeTable.getItems());
                    activeTable = tableMap.getMap().get(newType);
                    tableMap.getMap().get(oldType).getSelectionModel().clearSelection();
                    tablePane.getChildren().clear();
                    tablePane.getChildren().add(tableMap.getMap().get(newType));
                    activeTable.setPrefWidth(tablePane.getWidth());
                    activeTable.setPrefHeight(tablePane.getHeight());
                    activeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

                });

        // Define an event filter for the ComboBox for Mouse_released events
        EventHandler validityFilter = (EventHandler<InputEvent>) event -> {
            try {
                ObservableList<Register> list = FXCollections.observableArrayList();
                list.addAll(registerController.getItems());
                for (RegisterArrayTableView r : tableMap.getMap().values()) {
                    list.addAll(r.getItems());
                }

                Validatable.all(list);
            } catch (ValidationException ex) {
                Dialogs.createErrorDialog(tablePane.getScene().getWindow(),
                        "Registers Error", ex.getMessage()).showAndWait();
                event.consume();
            }
        };
        arrayCombo.addEventFilter(MouseEvent.MOUSE_RELEASED, validityFilter);
    }

    /**
     * save the current changes and close the window when clicking on OK button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML @SuppressWarnings("unused")
    public void onOKButtonClick(ActionEvent e) {
        //get the current edited clones
        ObservableList<Register> objList = activeTable.getItems();
        try {
            ObservableList<Register> list = FXCollections.observableArrayList();
            list.addAll(registerController.getItems());
            for (RegisterArrayTableView r : tableMap.getMap().values()) {
                list.addAll(r.getItems());
            }

            Validatable.all(objList);
            //update the machine with the new values
            updateRegisters();
            //get a handle to the stage.
            Stage stage = (Stage) okButton.getScene().getWindow();
            //close window.
            stage.close();
        } catch (Exception ex) {
            Dialogs.createErrorDialog(tablePane.getScene().getWindow(),
                    "Registers Error", ex.getMessage()).showAndWait();
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
        String startString = "Hardware Modules";
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
     * gets the table view object that is current being edited in the window.
     *
     * @return the table view object that is current being edited in the window.
     */
    private RegisterArrayTableView getCurrentController() {
        return tableMap.getMap().get(arrayCombo.getValue());
    }

    /**
     * Called whenever the dialog is exited via the 'ok' button
     * and the machine needs to be updated based on the changes
     * made while the dialog was open (JRL)
     */
    private void updateRegisters() {
        // and the machine needs to be updated based on the changes made.
        getCurrentController().setClones(activeTable.getItems());
        for (RegisterArrayTableView t : tableMap.getMap().values()) {
            for (RegisterArray ra : registerArrays) {
                if (ra.getName().equals(t.getArrayName())) {
                    ra.registers().setAll(t.createNewModulesList());
                }
            }
        }

    }


    /**
     * a class that holds the current register array class
     */
    class ChangeTable {
        Map<String, RegisterArrayTableView> typesMap;

        /**
         * Constructor
         *
         * @param arrays the current register array
         */
        public ChangeTable(ObservableList<RegisterArray> arrays) {
            // an hashmap that holds types as the keys and sub fxml names as values.
            typesMap = buildMap(arrays);

        }

        /**
         * build the map to store all the controllers
         *
         * @param arrays a list holds all the register array objects
         * @return the map that contains all the controllers
         */
        public Map buildMap(ObservableList<RegisterArray> arrays) {
            Map<String, RegisterArrayTableView> map = new HashMap();
            for (RegisterArray ra : arrays) {
                map.put(ra.getName(), new RegisterArrayTableView(ra.getName(), ra
                        .registers()));
            }
            return map;
        }

        /**
         * returns the map of controllers.
         *
         * @return the map of controllers.
         */
        public Map<String, RegisterArrayTableView> getMap() {
            return typesMap;
        }

        /**
         * sets the parents frame for each controller.
         *
         * @param tables the controller to be edited.
         */
        public void setParents(Node tables) {
            for (RegisterArrayTableView moduleController : typesMap.values()) {
                moduleController.setParentFrame(tables);
            }
        }
    }


}
