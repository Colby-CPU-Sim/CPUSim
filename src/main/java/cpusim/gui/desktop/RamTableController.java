/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cpusim.gui.desktop;

import cpusim.gui.util.Base;
import cpusim.gui.util.EditingMultiBaseStyleLongCell;
import cpusim.module.RAM;
import cpusim.module.RAMLocation;
import javafx.collections.ObservableList;
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
    
    private RAM ram;
    private ObservableList<RAMLocation> ramLocations;

    private String title;
    
    private Base valBase;
    private Base addrBase;

    private DesktopController desktop;

    public RamTableController(DesktopController d, RAM ram, String title) {
    	desktop = d;
        this.ram = ram;
        this.ramLocations = ram.data();
        this.title = title;
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

        data.setCellFactory(cellValMultiBaseLongFactory);

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

    /**
     * highlights or unhighlights the row at the given breakAddress.
     * @param breakAddress the address of the cell to be highlighted or unhighlighted
     * @param select if true, select the cell otherwise unselect the cell
     */
    public void highlightBreakInRAM(int breakAddress, boolean select) {
        if(breakAddress >= 0 && breakAddress < ram.getLength()) {
            if( select) {
                table.getSelectionModel().select(breakAddress);
                table.scrollTo(breakAddress);
            }
            else {
                table.getSelectionModel().clearSelection(breakAddress);
            }
        }
    }
    
}
