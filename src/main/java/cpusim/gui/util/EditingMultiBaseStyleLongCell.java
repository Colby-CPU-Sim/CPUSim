/**
 * author: Jinghui Yu
 * last edit data: 6/3/2013
 */

/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 11/6/13
 * with the following changes:
 * 
 * 1.) added capability for the cell to handle the Unsigned Decimal and Ascii bases
 */

package cpusim.gui.util;

import cpusim.assembler.EQU;
import cpusim.gui.desktop.FontData;
import cpusim.module.RAMLocation;
import cpusim.module.Register;
import cpusim.util.Convert;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * An editable cell class that allows the user to modify the integer in the cell.
 */

public class EditingMultiBaseStyleLongCell<T> extends TableCell<T, Long> {
    private TextField textField;
    private Base base;
    private FontData styleInfo;
    private int cellSize;

    /**
     * constructor
     * @param base the base in which the data is to be edited
     * @param style the font and background info for displaying the cell.
     *              If style=null, the default font and background are used.
     */
    public EditingMultiBaseStyleLongCell(Base base, FontData style) {
        this.base = base;
        this.styleInfo = style;
    }

    public Base getBase(){
        return base;
    }

    /**
     * What happens when the user starts to edit the table cell.  Namely that 
     * a text field is created 
     */
    @Override
    public void startEdit() {
        if (!isEmpty() && ! isReadOnlyRegisterValue()) {
            super.startEdit();
            createTextField();
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }
    }

    private boolean isReadOnlyRegisterValue() {
        if ((getTableRow().getItem() instanceof Register)) {
            Register register = (Register) getTableRow().getItem();
            return register.getReadOnly();
        }
        else {
            return false;
        }
    }

    /**
     * What happens when the user cancels while editing a table cell.  Namely the
     * graphic is set to null and the text is set to the proper value
     */
    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText(formatString(prepareString(String.valueOf(getItem()))));
        setGraphic(null);
    }

    /**
     * updates the Long in the table cell
     * @param item used for the parent method
     * @param empty used for the parent method
     */
    @Override
    public void updateItem(Long item, boolean empty) {
        super.updateItem(item, empty);

        if( styleInfo != null )
            styleInfo.setFontAndBackground(this);


        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            setTooltip(null);
        }
        else if (isEditing()) {
            if (textField != null) {
                textField.setText(formatString(prepareString(getString())));
            }
            setText(null);
            setGraphic(textField);
        }
        else {
            setTooltipsAndCellSize();
            setText(formatString(convertLong(Long.parseLong(getString()))));
            if(isReadOnlyRegisterValue()) {
                ImageView graphic = new ImageView(new Image("/images/icons/Lock.png"));
                graphic.setFitHeight(styleInfo == null ? this.getHeight() :
                                                   Integer.parseInt(styleInfo.fontSize));
                graphic.setSmooth(true);
                graphic.setPreserveRatio(true);
                graphic.setCache(true);
                setContentDisplay(ContentDisplay.RIGHT);
                setGraphic(graphic);
            }
            else
                setGraphic(null);
        }
    }

    /**
     * creates a text field with listeners so that that edits will be committed 
     * at the proper time
     */
    private void createTextField() {
        textField = new TextField(formatString(convertLong(Long.parseLong(getString()))));
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);

        // if user takes focus from the textField, check the validity of its
        // contents and, if valid, save the changes, else, put up an error dialog.
        // This listener actually does nothing, because
        textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0,
                                Boolean arg1, Boolean arg2) {
                if (!arg2) {
                    try {
                        commitEdit(convertString(prepareString(textField.getText())));
                    } catch (NumberFormatException exc) {
                        if (textField.getScene() != null) {
                            textField.setStyle("-fx-background-color:red;");
                            textField.setTooltip(new Tooltip(exc.getMessage()));
                            textField.requestFocus();
//                          Dialogs.createErrorDialog(textField.getScene().getWindow(),
//                                    "Number Format Error", errorMessage).showAndWait();
//                          cancelEdit();
                        }
                    }
                }
            }
        });

        // On ENTER , check for valid value and, if so,
        // save the changes, else, color the cell red.
        // On CANCEL (Escape), cancel the editing.
        textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (t.getCode() == KeyCode.ENTER) {
                    try {
                        commitEdit(convertString(prepareString(textField.getText())));
                    } catch (NumberFormatException exc) {
                        textField.setStyle("-fx-background-color:red;");
                        textField.setTooltip(new Tooltip(exc.getMessage()));
                        textField.requestFocus();
                        // TODO make this work some other way
                        //The following code crashes the program
                        //Dialogs.showErrorDialog(
                        //        (Stage)textField.getScene().getWindow(),
                        //        "This column requires integer values",
                        //        "Error Dialog", "title");
                        //cancelEdit();
                    }
                }
                else if (t.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
            }
        });
    }


    /**
     * Removes spaces and adds leading zeros to the string to make it the right
     * length for base 2 and base 16 numbers.
     * @param s the String to be displayed
     * @return the String with spaces removed and leading zeros added as needed.
     */
    private String prepareString(String s) {
        switch (base.getBase()) {
            case "Bin": {
                s = Convert.removeAllWhiteSpace(s);
                int zeroesNeeded = cellSize - s.length();
                String zeroString = "";
                for (int i = 0; i < zeroesNeeded; i++) {
                    zeroString += "0";
                }
                s = zeroString + s;
                break;
            }
            case "Hex": {
                s = Convert.removeAllWhiteSpace(s);
                int zeroesNeeded = cellSize / 4 - s.length() +
                                   cellSize % 4 == 0 ? 0 : 1;
                String zeroString = "";
                for (int i = 0; i < zeroesNeeded; i++) {
                    zeroString += "0";
                }
                s = zeroString + s;
                break;
            }
            case "Dec":
            case "Unsigned Dec":
                s = Convert.removeAllWhiteSpace(s);
        }

        return s;
    }

    private Long convertString(String s) {
        switch (base.getBase()) {
            case "Bin":
                return Convert.fromBinaryStringToLong(s, cellSize);
            case "Hex":
                return Convert.fromHexadecimalStringToLong(s, cellSize);
            case "Dec":
                return Convert.fromDecimalStringToLong(s, cellSize);
            case "Unsigned Dec":
                return Convert.fromUnsignedDecStringToLong(s, cellSize);
            default: // "Ascii"
                return Convert.fromAsciiStringToLong(s, cellSize,
                        (getItem()<<(64-cellSize))>>>(64-cellSize));
        }
    }

    private String convertLong(Long l) {
        switch (base.getBase()) {
            case "Bin":
                return Convert.fromLongToTwosComplementString(l, cellSize);
            case "Hex":
                return Convert.fromLongToHexadecimalString(l, cellSize);
            case "Dec":
                return String.valueOf(l);
            case "Unsigned Dec":
                return Convert.fromLongToUnsignedDecString(l, cellSize);
            default:
                return Convert.fromLongToAsciiString(l, cellSize);
        }
    }

    /**
     * Add spaces after every 4 characters in String s if s is in binary or hex.
     * @param s the String into which spaces are to be put
     * @return the new string with the spaces added.
     */
    private String formatString(String s) {
        if (base.getBase().equals("Bin") || base.getBase().equals("Hex")) {
            return Convert.insertSpacesInString(s, 4);
        }
        else {
            return s;
        }
    }

    public String allBasesToolTip() {
    	String toolTip = "";
        String originalBase = base.getBase();
        String newLine = System.getProperty("line.separator");

        // 1
        toolTip += "Decimal: ";
        base.setBase(Base.DECIMAL);
        toolTip += formatString(convertLong(Long.parseLong(getString())));
        toolTip += newLine;
        // 2
        toolTip += "Unsigned Decimal: ";
        base.setBase(Base.UNSIGNEDDECIMAL);
        toolTip += formatString(convertLong(Long.parseLong(getString())));
        toolTip += newLine;
        // 3
        toolTip += "Binary: ";
        base.setBase(Base.BINARY);
        toolTip += formatString(convertLong(Long.parseLong(getString())));
        toolTip += newLine;
        // 4
        toolTip += "Hexadecimal: ";
        base.setBase(Base.HEX);
        toolTip += formatString(convertLong(Long.parseLong(getString())));
        toolTip += newLine;
        // 5
        toolTip += "Ascii: ";
        base.setBase(Base.ASCII);
        toolTip += formatString(convertLong(Long.parseLong(getString())));

        base.setBase(originalBase);
        return toolTip;
    }

    /**
     * Sets the proper tool tips and cell size for the table cell
     */
    private void setTooltipsAndCellSize() {
        if (getTableRow() == null) {
            return;
    	}
        Object o = getTableRow().getItem();
        if (o instanceof Register) {
            cellSize = ((Register) o).getWidth();
            setTooltip(new Tooltip(allBasesToolTip()));
        }
        else if ((o instanceof RAMLocation) &&
                          getTableColumn().getText().equals("Data")) {
            RAMLocation ramLoc = (RAMLocation) o;
            cellSize = ramLoc.getRam().getCellSize();
            if (ramLoc.getSourceLine() != null) {
            	setTooltip(new Tooltip((ramLoc.getSourceLine().getLine()+1)
                        + ": " + ((RAMLocation) o).getComment()));
            }
            else {
                setTooltip(null);
            }
        }
        else if ((o instanceof RAMLocation) &&
                          getTableColumn().getText().equals("Addr")) {
            cellSize = 31 - Integer.numberOfLeadingZeros(
                                             getTableView().getItems().size());
            setTooltip(new Tooltip("Binary: " +
                    Long.toBinaryString(getItem()) +
                    System.getProperty("line.separator") +
                    "Decimal: " + getItem() +
                    System.getProperty("line.separator") +
                    "Hex: " + Long.toHexString(getItem())));
        }
        else if (o instanceof EQU) {
        	cellSize = 32;
            setTooltip(new Tooltip(allBasesToolTip()));
        }
        else {
            cellSize = 32;
        }
    }

    /**
     * returns a string value of the item in the table cell
     * @return a string value of the item in the table cell
     */
    private String getString() {
        return getItem() == null ? "" : getItem().toString();
    }

    /*public void setOutlineRows(Set<Integer> rowSet){
        if (rowSet.contains(getIndex())){
            setStyle("-fx-border-color: forestgreen; -fx-border-width: 1px;");
        }
        else{
            setStyle("-fx-border-width: 0px;");
        }
    }*/
}
