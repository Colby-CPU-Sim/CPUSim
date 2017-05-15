package cpusim.gui.util.list;

import cpusim.model.microinstruction.Microinstruction;
import cpusim.model.microinstruction.Comment;
import javafx.scene.control.ListCell;

/**
 * Styled cell that changes the format of a cell based on the item.
 * @since 2015-03-28
 */
public class StyledListCell<T extends Microinstruction<?>> extends ListCell<T> {

    public StyledListCell() {

    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        textProperty().unbind();
        if (empty) {
            setText(null);
            setGraphic(null);
            setTooltip(null);
        } else {
            textProperty().bind(item.nameProperty());

            if (item instanceof Comment) {
                setStyle("-fx-font-family:Courier; -fx-text-fill:gray; " +
                        "-fx-font-style:italic;");
            }
            else {
                setStyle("-fx-font-family:Courier");
            }
        }
    }
}
