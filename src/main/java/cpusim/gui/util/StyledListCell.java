/**
 * File: StyledListCell
 * User: djskrien
 * Date: 3/28/15
 */
package cpusim.gui.util;

import cpusim.model.Microinstruction;
import cpusim.model.microinstruction.Comment;
import javafx.scene.control.ListCell;

public class StyledListCell<T> extends ListCell<T> {

    public StyledListCell() {

    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
            setTooltip(null);
        }
        else if (isEditing()) { // not editable
        }
        else if (item instanceof Comment) {
            setText(((Microinstruction) item).getName());
            setStyle("-fx-font-family:Courier; -fx-text-fill:gray; " +
                    "-fx-font-style:italic;");
        }
        else {
            setText(((Microinstruction) item).getName());
            setStyle("-fx-font-family:Courier");
        }
    }
}
