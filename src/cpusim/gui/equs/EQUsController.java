
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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import java.util.ResourceBundle;

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
    private ObservableList<Base> bases;

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
        ArrayList<String> names = getNames();
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

        updateButtonClickables();
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

        updateButtonClickables();
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
            ArrayList<String> names = getNames();
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
        updateButtonClickables();
    }

    ///////////////// Update which buttons are disabled /////////////////

    /**
     * Used to control the disabling/enabling of
     * the buttons within the EQUs window.
     * Calling this method should update
     * the New, Delete, and Duplicate buttons according
     * to the state of the window.
     */
    public void updateButtonClickables() {
        if (equsTable.getItems().isEmpty()) {
            deleteButton.setDisable(true);
            duplicateButton.setDisable(true);
        }
        else {
            if (selectedSet == null) {
                deleteButton.setDisable(true);
                duplicateButton.setDisable(true);
            }
            else {
                if (equsTable.getItems().indexOf(selectedSet) >= 0) {
                    deleteButton.setDisable(false);
                    duplicateButton.setDisable(false);
                }
                else {
                    deleteButton.setDisable(true);
                    duplicateButton.setDisable(true);
                }
            }
        }
    }

    ///////////////// Initializer Help Methods /////////////////

    /**
     * Sets up the table of EQUs.
     */
    private void setUpEqusTable() {

        // Accounts for width changes.
        equsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // updates selectedSet
        updateButtonClickables();
        equsTable.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<EQU>() {
                    @Override
                    public void changed(ObservableValue<? extends EQU> selected,
                                        EQU oldSet, EQU newSet) {
                        selectedSet = newSet;
                        updateButtonClickables();
                    }
                });

        // Callbacks
        Callback<TableColumn<EQU, String>, TableCell<EQU, String>> cellStrFactory =
                setStringTableColumn -> new EditingStrCell<>();

        Callback<TableColumn<EQU, Long>, TableCell<EQU, Long>> cellLongFactory =
                setLongTableColumn -> {
                    EditingMultiBaseStyleLongCell<EQU> a = new
                            EditingMultiBaseStyleLongCell<>(base, new FontData());
                    a.setTooltip(new Tooltip());
                    a.tooltipProperty().get().textProperty().bind(a
                            .tooltipStringProperty);
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

        valueColumn.setCellFactory(cellLongFactory);
        valueColumn.setOnEditCommit(
                text -> {
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
    public void setUpBaseComboBox() {
        bases = FXCollections.observableArrayList(new Base(Base.DECIMAL),
                new Base(Base.BINARY), new Base(Base.HEX));
        baseComboBox.setItems(bases);
        baseComboBox.setValue(bases.get(0));

        baseComboBox.getSelectionModel().selectedIndexProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> arg0,
                                        Number old_value, Number new_value) {
                        if (new_value.equals(0)) {
                            base.setBase(Base.DECIMAL);
                        }
                        else if (new_value.equals(1)) {
                            base.setBase(Base.BINARY);
                        }
                        else {
                            base.setBase(Base.HEX);
                        }
                        refreshTable();
                    }
                });
    }

    ///////////////// Other Private Help Methods /////////////////

    /**
     * Refreshes the EQUs table.
     */
    public void refreshTable() {
        // New way to refresh table that doesn't change column width
        ObservableList<EQU> items = equsTable.getItems();
        ObservableList<EQU> copy = FXCollections.observableArrayList();
        for (EQU equ : items) {
            copy.add((EQU) (equ.clone()));
        }
        items.removeAll(items);
        items.addAll(copy);
    }

    /**
     * Returns an ArrayList which contains all the
     * names of the items in the table.
     *
     * @return ArrayList which contains all the
     * names of the items in the table.
     */
    private ArrayList<String> getNames() {
        ObservableList<EQU> data = equsTable.getItems();
        ArrayList<String> al = new ArrayList<>();
        if (data.isEmpty()) {
            return al;
        }

        for (EQU equ : data) {
            al.add(equ.getName());
        }
        return al;
    }

    private void updateTable() {
        nameColumn.setVisible(false);
        nameColumn.setVisible(true);
        double w = equsTable.getWidth();
        equsTable.setPrefWidth(w - 1);
        equsTable.setPrefWidth(w);
    }

}