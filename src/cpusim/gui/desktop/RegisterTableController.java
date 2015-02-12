/*
 * Ben Borchard
 * 
 * Last Modified 6/4/13s
 */
package cpusim.gui.desktop;

import cpusim.gui.util.*;
import cpusim.module.Register;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
 * FXML Controller class
 *
 * @author Ben Borchard
 */

/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 11/6/13
 * with the following changes:
 * 
 * 1.) got rid of an unused ArrayList of EditingMultiBaseStyleCells called datas
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
    String color;

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
        color = desktop.getTableStyle().get();
        
        table.getSelectionModel().selectedItemProperty().addListener(new ChangeListener(){
            @Override
            public void changed(ObservableValue ov, Object t, Object t1) {
                updateTable();
            }
        });
        
        Callback<TableColumn<Register,Long>,TableCell<Register,Long>> cellMultiBaseLongFactory =
                new Callback<TableColumn<Register, Long>, TableCell<Register, Long>>() {
                    @Override
                    public TableCell<Register, Long> call(
                            TableColumn<Register, Long> setStringTableColumn) {
                        final EditingMultiBaseStyleLongCell<Register> a =
                                new EditingMultiBaseStyleLongCell<>(base, color);
                    	// Tooltip
                    	a.setTooltip(new Tooltip());
                    	a.tooltipProperty().get().textProperty().bind(a.tooltipStringProperty);
                    	return a;
                    }
                };
        
        Callback<TableColumn<Register,String>,TableCell<Register,String>> cellStringFactory =
                new Callback<TableColumn<Register, String>, TableCell<Register, String>>() {
                    @Override
                    public TableCell<Register, String> call(
                            TableColumn<Register, String> setStringTableColumn) {
                        final EditingStrStyleCell<Register> a = new EditingStrStyleCell<Register>(color);
                        return a;
                    }
                };
        
        Callback<TableColumn<Register,Integer>,TableCell<Register,Integer>> cellIntegerFactory =
                new Callback<TableColumn<Register, Integer>, TableCell<Register, Integer>>() {
                    @Override
                    public TableCell<Register, Integer> call(
                            TableColumn<Register, Integer> setStringTableColumn) {
                        final EditingIntStyleCell<Register> a = new EditingIntStyleCell<Register>(color);
                        return a;
                    }
                };
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

        data.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Register, Long>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Register, Long> text) {
                        text.getRowValue().setValue(text.getNewValue());
                    }
                }
        );

        table.setItems(registers);

        // Right clicks on table
        ContextMenu cm = new ContextMenu();
        MenuItem edit = new MenuItem("Edit Hardware");
        edit.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent e) {
        		ObservableList<RegisterTableController> RTCs = 
        				desktop.getRegisterController();
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
    
    /**
     * Sets the colors for the table
     * @param color style string that dictates the colors to be used at the table
     */
    public void setColor(String color){
        this.color = color;
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
