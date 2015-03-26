/*
 * Ben Borchard
 * 
 * Last Modified 6/4/13s
 */
package cpusim.gui.desktop;

import cpusim.gui.util.*;
import cpusim.module.Register;
import cpusim.util.Dialogs;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * This class is the controller for a pane in the Desktop that contains registers.
 * There is one such controller for the default registers pane and one controller for
 * each of the register array panes.
 */
public class RegisterTableController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    AnchorPane anchorPane;
    @FXML
    TitledPane titledPane;
    @FXML
    TableView<Register> table;
    @FXML
    TableColumn<Register, String> name;
    @FXML
    TableColumn<Register, Integer> width;
    @FXML
    TableColumn<Register, Long> data;

    ObservableList<Register> registers;
    private DesktopController desktop;
    String title;
    Base base;

    public RegisterTableController(DesktopController d, ObservableList<Register> regs, String t) {
    	desktop = d;
        registers = regs;
        this.title = t;
    }

    /**
     * Initializes all of the various gui properties and variables
     * @param url unused
     * @param rb unused
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        titledPane.setText(title);

        AnchorPane.setTopAnchor(titledPane, 0.0);
        AnchorPane.setRightAnchor(titledPane, 2.0);
        AnchorPane.setLeftAnchor(titledPane, 0.0);
        AnchorPane.setBottomAnchor(titledPane, 0.0);
        
        base = new Base("Dec");
        FontData styleInfo = desktop.getRegisterTableFontData();

        table.getSelectionModel().selectedItemProperty().addListener((ov, t, t1) -> {
            updateTable();
        });
        
        Callback<TableColumn<Register,Long>,TableCell<Register,Long>> cellMultiBaseLongFactory =
                column -> {
                    final EditingMultiBaseStyleLongCell<Register> a =
                            new EditingMultiBaseStyleLongCell<>(base, styleInfo);
                    return a;
                };
        
        Callback<TableColumn<Register,String>,TableCell<Register,String>> cellStringFactory =
                column -> new EditingStrStyleCell<>(styleInfo);
        
        Callback<TableColumn<Register,Integer>,TableCell<Register,Integer>> cellIntegerFactory =
                column -> new EditingIntStyleCell<>(styleInfo);
        data.setCellFactory(cellMultiBaseLongFactory);
        name.setCellFactory(cellStringFactory);
        width.setCellFactory(cellIntegerFactory);

        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        name.prefWidthProperty().bind(table.widthProperty().divide(100 / 25.0));
        width.prefWidthProperty().bind(table.widthProperty().divide(100 / 15.0));
        data.prefWidthProperty().bind(table.widthProperty().divide(100 / 60.0));

        name.setCellValueFactory(new PropertyValueFactory<Register, String>("name"));
        width.setCellValueFactory(new PropertyValueFactory<Register, Integer>("width"));
        data.setCellValueFactory(new PropertyValueFactory<Register, Long>("value"));

        data.setOnEditCommit(text -> {
                    Register register = text.getRowValue();
                    if(! register.getReadOnly())
                        register.setValue(text.getNewValue());
                }
        );

        table.setItems(registers);

        // Right clicks on table
        ContextMenu cm = new ContextMenu();
        MenuItem edit = new MenuItem("Edit Hardware");
        edit.setOnAction(e -> {
            ObservableList<RegisterTableController> RTCs =
                    desktop.getRegisterControllers();
            for (RegisterTableController RTC : RTCs) {
                if (RTC == RegisterTableController.this) {
                    if (RTC.title.equals("Registers")) {
                        desktop.openHardwareModulesDialog(0);
                    }
                    else {
                        desktop.openHardwareModulesDialog(1);
                    }
                }
            }
        });
        edit.disableProperty().bind(desktop.modifyMenu.disableProperty());
        cm.getItems().add(edit);
        table.setContextMenu(cm);
    }

    /**
     * sets the base in which the data is represented
     * @param newBase the new base (decimal, hex, binary)
     */
    void setDataBase(String newBase) {
        base.setBase(newBase);

        updateTable();
    }

    public void outlineRows(Set<Register> registerSet) {
        HashSet<Integer> rowSet = new HashSet<Integer>();

        for (int i = 0; i < registers.size(); i++) {
            if (registerSet.contains(registers.get(i)))
                rowSet.add(i);
        }
        setOutlineRows(rowSet);
    }

    public void setOutlineRows(Set<Integer> rowSet){
        for (Node n : table.lookupAll("EditingMultiBaseStyleLongCell")){
            if (n instanceof EditingMultiBaseStyleLongCell){
                EditingMultiBaseStyleLongCell cell = (EditingMultiBaseStyleLongCell) n;
                if (rowSet.contains(cell.getIndex()) ){
                    cell.getStyleClass().add("Outline");
                }
                else {
                    cell.getStyleClass().remove("Outline");
                }
            }
        }
    }
    
    /**
     * updates the values in the table.
     * This is using a hack.
     */
    public void updateTable(){
        data.setVisible(false);
        data.setVisible(true);
    }
}
