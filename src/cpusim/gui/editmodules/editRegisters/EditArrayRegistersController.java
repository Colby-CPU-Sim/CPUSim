
/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Removed one try catch and allOkay variable and replaced it with a try catch for a validation exception
 */
package cpusim.gui.editmodules.editRegisters;

import cpusim.Mediator;
import cpusim.Module;
import cpusim.gui.editmodules.RegisterArrayTableController;
import cpusim.gui.editmodules.RegistersTableController;
import cpusim.gui.help.HelpController;
import cpusim.module.Register;
import cpusim.module.RegisterArray;
import cpusim.util.Dialogs;
import cpusim.util.Validate;
import cpusim.util.ValidationException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class EditArrayRegistersController implements Initializable {
    @FXML
    BorderPane scene;
    @FXML
    ComboBox<String> registerCombo;
    @FXML
    Pane tables;
    @FXML
    Button okButton;
    @FXML
    Button cancelButton;
    @FXML
    Button helpButton;

    private Mediator mediator;
    private RegistersTableController registerController;
    private ObservableList<RegisterArray> registerArrays;
    private TableView activeTable;
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

        selection = (registerArrays.get(0)).getName();
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

        tableMap = new ChangeTable(registerArrays);
        registerCombo.setVisibleRowCount(registerArrays.size());
        registerCombo.getSelectionModel().select(selection);

        tableMap.setParents(tables);
        tables.getChildren().clear();
        tables.getChildren().add(tableMap.getMap().get(selection));

        activeTable = tableMap.getMap().get(selection);

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
                        activeTable.setPrefHeight(newHeight - 100);
                    }
                }
        );

        // listen for changes to the instruction combo box selection and update
        // the displayed micro instruction table accordingly.
        registerCombo.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> selected,
                                        String oldType, String newType) {
                        ((TableController) activeTable).setClones(activeTable.getItems());

                        tableMap.getMap().get(oldType).getSelectionModel().clearSelection();
                        tables.getChildren().clear();
                        tables.getChildren().add(
                                tableMap.getMap().get(newType));

                        activeTable = tableMap.getMap().get(newType);
                        activeTable.setPrefSize(
                                scene.getWidth(), scene.getHeight() - 100);
                    }
                });

        // Define an event filter for the ComboBox for Mouse_released events
        EventHandler validityFilter = new EventHandler<InputEvent>() {
            public void handle(InputEvent event) {
                try {
                    ObservableList<Register> list = FXCollections.observableArrayList();
                    list.addAll(registerController.getItems());
                    for (TableController r : tableMap.getMap().values()) {
                        list.addAll(r.getItems());
                    }

                    Validate.allNamesAreUnique(list.toArray());
                    ((TableController) activeTable).checkValidity(list);
                    event.consume();
                } catch (ValidationException ex) {
                    Dialogs.createErrorDialog(tables.getScene().getWindow(),
                            "Registers Error", ex.getMessage()).showAndWait();
                }
            }
        };
        registerCombo.addEventFilter(MouseEvent.MOUSE_RELEASED, validityFilter);
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
        try {
            ObservableList<Register> list = FXCollections.observableArrayList();
            list.addAll(registerController.getItems());
            for (TableController r : tableMap.getMap().values()) {
                list.addAll(r.getItems());
            }
            Validate.allNamesAreUnique(list.toArray());
            getCurrentController().checkValidity(objList);
            //update the machine with the new values
            updateRegisters();
            //get a handle to the stage.
            Stage stage = (Stage) okButton.getScene().getWindow();
            //close window.
            stage.close();
        } catch (Exception ex) {
            Dialogs.createErrorDialog(tables.getScene().getWindow(),
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
    public TableController getCurrentController() {
        return tableMap.getMap().get(registerCombo.getValue());
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
    protected void updateRegisters() { // and the machine needs to be updated based on the changes
        // ma
        getCurrentController().setClones(activeTable.getItems());
        for (TableController t : tableMap.getMap().values()) {
            ObservableList list = t.getItems();
            for (RegisterArray ra : registerArrays) {
                if (ra.getName().equals(t.getArrayName())) {
                    ra.registers().setAll(t.createNewModulesList(t.getClones()));
                }
            }
        }

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
     * a class that holds the current register array class
     */
    class ChangeTable {
        Map<String, TableController> typesMap;

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
            Map<String, TableController> map = new HashMap();
            for (RegisterArray ra : arrays) {
                map.put(ra.getName(), new TableController(ra.getName(), ra.registers()));
                registerCombo.getItems().add(ra.getName());
            }
            return map;
        }

        /**
         * returns the map of controllers.
         *
         * @return the map of controllers.
         */
        public Map<String, TableController> getMap() {
            return typesMap;
        }

        /**
         * sets the parents frame for each controller.
         *
         * @param tables the controller to be edited.
         */
        public void setParents(Node tables) {
            for (TableController moduleController : typesMap.values()) {
                moduleController.setParentFrame(tables);
            }
        }
    }


}
