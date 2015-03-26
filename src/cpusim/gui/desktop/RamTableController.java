/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cpusim.gui.desktop;

import cpusim.gui.util.Base;
import cpusim.gui.util.EditingMultiBaseStyleLongCell;
import cpusim.module.RAM;
import cpusim.module.RAMLocation;
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
import java.util.ResourceBundle;
import java.util.Set;

/**
 * FXML Controller class
 *
 * @author Ben Borchard
 */
public class RamTableController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML AnchorPane anchorPane;
    @FXML TitledPane titledPane;
    @FXML TableView<RAMLocation> table;
    @FXML TableColumn<RAMLocation,Long> address;
    @FXML TableColumn<RAMLocation,Long> data;
    
    RAM ram;
    ObservableList<RAMLocation> ramLocations;

    String title;
    
    Base valBase;
    Base addrBase;

    int breakRow;

    private DesktopController desktop;

    public RamTableController(DesktopController d, RAM ram, String title) {
    	desktop = d;
        this.ram = ram;
        this.ramLocations = ram.data();
        this.title = title;
        breakRow = -1;
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
        AnchorPane.setRightAnchor(titledPane, 0.0);
        AnchorPane.setLeftAnchor(titledPane, 0.0);
        AnchorPane.setBottomAnchor(titledPane, 0.0);
        
        valBase = new Base("Dec");
        addrBase = new Base("Dec");

        FontData styleInfo = desktop.getRamTableFontData();

        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        table.getSelectionModel().selectedItemProperty().addListener(
                (ov, t, t1) -> updateTable());


        Callback<TableColumn<RAMLocation,Long>,TableCell<RAMLocation,Long>> cellValMultiBaseLongFactory =
                setStringTableColumn -> {
                    EditingMultiBaseStyleLongCell<RAMLocation> a =
                            new EditingMultiBaseStyleLongCell<>(valBase, styleInfo);
                    return a;
                };

        Callback<TableColumn<RAMLocation,Long>,TableCell<RAMLocation,Long>> cellAddrMultiBaseLongFactory =
                setStringTableColumn -> {
                    EditingMultiBaseStyleLongCell<RAMLocation> a =
                            new EditingMultiBaseStyleLongCell<>(addrBase, styleInfo);
                    return a;
                };
        
        address.setCellFactory(cellAddrMultiBaseLongFactory);
        cellAddrMultiBaseLongFactory.call(address);

        data.setCellFactory(cellValMultiBaseLongFactory);
        cellValMultiBaseLongFactory.call(data);

        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        address.prefWidthProperty().bind(table.widthProperty().divide(100/34.0));
        data.prefWidthProperty().bind(table.widthProperty().divide(100/66.0));
        
                
        address.setCellValueFactory(new PropertyValueFactory<>("address"));
        data.setCellValueFactory(new PropertyValueFactory<>("value"));

        data.setOnEditCommit(text -> {
                    text.getTableView().getItems().get(
                            text.getTablePosition().getRow()).
                            setValue(text.getNewValue());
                    table.getStyleClass().add("tableRowCell");
                }
        );

        table.setItems(ramLocations);

        // Right clicks on table
        ContextMenu cm = new ContextMenu();
        MenuItem options = new MenuItem("Options");
        options.setOnAction(e -> desktop.openOptionsDialog(2));
        MenuItem edit = new MenuItem("Edit Hardware");
        edit.setOnAction(e -> desktop.openHardwareModulesDialog(3));
        
        // bind disabled properties to whether their MenuItem is disabled
        options.disableProperty().bind(desktop.executeMenu.getItems().get(11).disableProperty());
        edit.disableProperty().bind(desktop.modifyMenu.disableProperty());
        
    	cm.getItems().addAll(options, edit);
    	table.setContextMenu(cm);


    }

    /**
     * Converts a set of addresses taken as a parameter into a set of indexes
     * so that the cell renderer can outline those indexes.
     * @param addresses a Set of Integers which contains the adresses of
     *                  all the changed ram
     */
    //added by Charlie and Mike 11/06
    public void outlineRows(Set<Integer> addresses)
    {
        setOutlineRows(addresses);
    }

    /**
     * Converts a set of addresses taken as a parameter into a set of indexes
     * so that the cell renderer can outline those indexes.
     * @param rowSet a Set of Integers which contains the adresses of
     *                  all the changed ram
     */
    public void setOutlineRows(Set<Integer> rowSet){
        for (Node n : table.lookupAll("EditingMultiBaseStyleLongCell")){
            if (n instanceof EditingMultiBaseStyleLongCell){
                EditingMultiBaseStyleLongCell cell = (EditingMultiBaseStyleLongCell) n;
                if (rowSet.contains(cell.getIndex()) && cell.getBase().equals(valBase) ){
                    cell.getStyleClass().add("Outline");
                }
                else {
                    cell.getStyleClass().remove("Outline");
                }
            }
        }
    }
    
    /**
     * sets the base in which the address is displayed
     * @param newBase the new base (binary, decimal, hex)
     */
    public void setAddressBase(String newBase) {
        addrBase.setBase(newBase);
        
        updateTable();
    }
    
    /**
     * sets the base in which the data is displayed
     * @param newBase the new base (binary, decimal, hex)
     */
    public void setDataBase(String newBase) {
        valBase.setBase(newBase);
        
        updateTable();
    }

    /**
     * updates the table using a hack
     */
    public void updateTable(){
        data.setVisible(false);
        data.setVisible(true);

        address.setVisible(false);
        address.setVisible(true);
    }

    public TableView getTable(){
        return table;
    }

    public RAM getRam(){
        return this.ram;
    }

    public void setBreakRow(int row)
    {
        breakRow = row;
    }

    public void clearBreakRow()
    {
        breakRow = -1;
    }

    //------------------------------
    //	highlights the rows with the given addresses
    //  If an address is out of range, it does nothing with that value.

    public void highlightRows(int[] addresses)
    {
        assert addresses != null : "addresses was null in " +
                "RAMWindow.highlightRows()";

        table.getSelectionModel().clearSelection();
        int length = ram.getLength();


        for (int address : addresses)
            if (address >= 0 && address < length) {
                table.getSelectionModel().select(address);
                table.scrollTo(address-4>=0? address-4 : 0);

            }
    }
    
    public void highlightBreak(int breakAddress)
    {
        if(breakAddress >= 0 && breakAddress < ram.getLength()) {
            int row = breakAddress;
            setBreakRow(row);
            table.scrollTo(row);
        }
    }
    
}
