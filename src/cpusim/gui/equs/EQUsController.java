
/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) removed the checkValidity() method
 * 2.) removed the validate check from the onOKButtonClicked() and moved it to
 * the onEditCommit method for the name column
 */

package cpusim.gui.equs;

import cpusim.Mediator;
import cpusim.assembler.EQU;
import cpusim.gui.desktop.FontData;
import cpusim.gui.help.HelpController;
import cpusim.gui.util.Base;
import cpusim.gui.util.EditingMultiBaseStyleLongCell;
import cpusim.gui.util.EditingStrCell;
import cpusim.util.Validate;
import cpusim.util.ValidationException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 11/7/13
 * with the following changes:
 * 
 * 1.) changed the cell in the value class to EditingMultiBaseStyleLongCell instead of 
 * EditingMultiBaseLongCell and got rid of the EditingMultiBaseLongCell.  This does not
 * mean that the EQU table can be styled, as a blank color string is passed in
 */

public class EQUsController implements Initializable {

    @FXML
    private Button helpButton;
    @FXML
    private Button okButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button newButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button duplicateButton;

    @FXML
    private TableView<EQU> equsTable;
    @FXML
    private TableColumn<EQU, String> nameColumn;
    @FXML
    private TableColumn<EQU, Long> valueColumn;

    @FXML
    private ComboBox<Base> baseComboBox;

    private EQU selectedSet;
    private Mediator mediator;
    private Base base;

    ///////////////// Constructor and Initializer /////////////////

    /**
     * Constructor which passes in the mediator.
     *
     * @param m The mediator for the machine
     *          and assembler information.
     */
    public EQUsController(Mediator m) {
        mediator = m;
    }

    /**
     * Initialize the EQUs dialog box.
     */
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        base = new Base(Base.DECIMAL);
        setUpEqusTable();
        setUpBaseComboBox();
    }

    ///////////////// Button Events /////////////////

    /**
     * Brings up the help box.
     * Called when the "?" button is clicked.
     */
    public void onHelpButtonClicked() {
        String startString = "EQU Editor";
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
     * Saves all the current EQUs into the machine and
     * closes the window.
     * Called when the "OK" button is clicked.
     */
    public void onOkButtonClicked() {

        ObservableList<EQU> data = equsTable.getItems();

        // Saving EQUs
        ObservableList<EQU> vector = FXCollections.observableArrayList(new ArrayList<>
                ());
        for (EQU equ : data) {
            vector.add(equ);
        }
        mediator.getMachine().setEQUs(vector);
        mediator.setMachineDirty(true);

        //close window.
        ((Stage) (helpButton.getScene().getWindow())).close();
    }

    /**
     * Closes the window. Called when the
     * "Cancel" button is clicked.
     */
    public void onCancelButtonClicked() {
        //close window.
        ((Stage) (helpButton.getScene().getWindow())).close();
    }

    /**
     * Inserts a new entry into the table
     * of EQUs. Called when the "New" button is
     * clicked.
     */
    public void onNewButtonClicked() {
        // Make unique Name
        String s = "?";
        List<String> names = getNames();
        while (names.contains(s)) {
            s += "?";
        }
        ObservableList<EQU> data = equsTable.getItems();
        EQU newSet = new EQU(s, (long) 0);
        data.add(0, newSet);
        equsTable.setItems(data);

        // Select last and scroll
        equsTable.getSelectionModel().clearSelection();
        equsTable.getSelectionModel().selectFirst();
        equsTable.scrollTo(0);

        updateButtonEnabling();
    }

    /**
     * Deletes the currently selected item from
     * the table of EQUs, if there is one. Called
     * when the "Delete" button is clicked.
     */
    public void onDeleteButtonClicked() {
        ObservableList<EQU> data = equsTable.getItems();
        int index = data.indexOf(selectedSet);
        if (index >= 0) {
            data.remove(index);
            equsTable.setItems(data);
        }

        // Select Correctly
        int indexToSelect = index - 1 < 0 ? index : index - 1;
        if (equsTable.getItems().size() > 0) {
            equsTable.getSelectionModel().clearSelection();
            equsTable.getSelectionModel().select(indexToSelect);
        }

        updateButtonEnabling();
    }

    /**
     * Duplicates the currently selected item in
     * the table of EQUs, if there is one. Called
     * when the "Duplicate" button is clicked.
     */
    public void onDuplicateButtonClicked() {
        ObservableList<EQU> data = equsTable.getItems();
        int index = data.indexOf(selectedSet);
        if (index >= 0) {
            // Make new EQU and add to table
            EQU newSet = (EQU) (selectedSet.clone());
            String orig = newSet.getName() + "_copy";
            String s = newSet.getName() + "_copy";
            List<String> names = getNames();
            int i = 1;
            while (names.contains(s)) {
                s = s.substring(0, orig.length()) + String.valueOf(i);
                i++;
            }
            newSet.setName(s);
            data.add(0, newSet);

            // Select and Scroll to
            equsTable.getSelectionModel().clearSelection();
            equsTable.getSelectionModel().selectFirst();
            equsTable.scrollTo(0);
        }
        updateButtonEnabling();
    }

    ///////////////// Update which buttons are disabled /////////////////

    /**
     * Used to control the disabling/enabling of
     * the buttons within the EQUs window.
     * Calling this method should update
     * the New, Delete, and Duplicate buttons according
     * to the state of the window.
     */
    private void updateButtonEnabling() {
        if (equsTable.getItems().isEmpty()) {
            deleteButton.setDisable(true);
            duplicateButton.setDisable(true);
        }
        else if (selectedSet == null) {
            deleteButton.setDisable(true);
            duplicateButton.setDisable(true);
        }
        else if (equsTable.getItems().indexOf(selectedSet) >= 0) {
            deleteButton.setDisable(false);
            duplicateButton.setDisable(false);
        }
        else {
            deleteButton.setDisable(true);
            duplicateButton.setDisable(true);
        }
    }



    ///////////////// Initializer Help Methods /////////////////

    /**
     * Sets up the table of EQUs.
     */
    private void setUpEqusTable() {

        // Accounts for width changes.
        equsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        nameColumn.prefWidthProperty().bind(equsTable.widthProperty().subtract(4).multiply(.3));
        valueColumn.prefWidthProperty().bind(equsTable.widthProperty().subtract(4).multiply(.7));

        // updates selectedSet
        updateButtonEnabling();
        equsTable.getSelectionModel().selectedItemProperty().addListener(
                (selected, oldSet, newSet) -> {
                    selectedSet = newSet;
                    updateButtonEnabling();
                    updateTable();
                });

        // Callbacks
        Callback<TableColumn<EQU, String>, TableCell<EQU, String>> cellStrFactory =
                setStringTableColumn -> new EditingStrCell<>();

        Callback<TableColumn<EQU, Long>, TableCell<EQU, Long>> cellMultiBaseLongFactory =
                setLongTableColumn -> {
                    EditingMultiBaseStyleLongCell<EQU> a = new
                            EditingMultiBaseStyleLongCell<>(base, null);
                    return a;
                };

        // Set cellValue Factory

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        // Register Factories and setOnEditCommits
        nameColumn.setCellFactory(cellStrFactory);
        nameColumn.setOnEditCommit(
                text -> {
                    String newName = text.getNewValue();
                    String oldName = text.getOldValue();
                    (text.getRowValue()).setName(newName);
                    try {
                        Validate.EQUNames(equsTable.getItems(), mediator.getMachine
                                ());
                    } catch (ValidationException ex) {
                        (text.getRowValue()).setName(oldName);
                    }
                    updateTable();
                });

        valueColumn.setCellFactory(cellMultiBaseLongFactory);
        valueColumn.setOnEditCommit( text -> {
                    Long newValue = text.getNewValue();
                    text.getRowValue().setValue(newValue);
                });


        // Set current EQUs
        ObservableList<EQU> equs = mediator.getMachine().getEQUs();
        ObservableList<EQU> data = equsTable.getItems();
        for (EQU equ : equs) {
            data.add((EQU) (equ.clone()));
        }
        equsTable.setItems(data);
    }

    /**
     * Sets up the combo box with the appropriate
     * listeners to change the content of the
     * value column.
     */
    private void setUpBaseComboBox() {
        ObservableList<Base> bases = FXCollections.observableArrayList(
                new Base(Base.DECIMAL), new Base(Base.BINARY), new Base(Base.HEX));
        baseComboBox.setItems(bases);
        baseComboBox.setValue(bases.get(0));

        baseComboBox.getSelectionModel().selectedIndexProperty().addListener(
                (arg0, old_value, new_value) -> {
                    if (new_value.equals(0)) {
                        base.setBase(Base.DECIMAL);
                    }
                    else if (new_value.equals(1)) {
                        base.setBase(Base.BINARY);
                    }
                    else {
                        base.setBase(Base.HEX);
                    }
                    updateTable();
                });
    }

    ///////////////// Other Private Help Methods /////////////////

    /**
     * Returns an ArrayList which contains all the
     * names of the items in the table.
     *
     * @return ArrayList which contains all the
     * names of the items in the table.
     */
    private List<String> getNames() {
        return equsTable.getItems().stream().map(EQU::getName).collect(Collectors.toList());
    }

    private void updateTable() {
        valueColumn.setVisible(false);
        valueColumn.setVisible(true);
    }

}