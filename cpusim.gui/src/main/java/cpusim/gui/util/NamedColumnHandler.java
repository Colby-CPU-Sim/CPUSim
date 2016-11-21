package cpusim.gui.util;

import cpusim.model.util.NamedObject;
import cpusim.model.util.ValidationException;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Implements an {@link EventHandler} for the {@link TableColumn.CellEditEvent} for handling
 * {@link NamedObject}-based types.
 */
public class NamedColumnHandler<T extends NamedObject>
        implements EventHandler<TableColumn.CellEditEvent<T, String>> {

    public interface HasUpdateTable<T extends NamedObject> {

        void updateTable(TableView<T> table);

    }

    private final HasUpdateTable<T> updater;
    private final TableView<T> table;

    public NamedColumnHandler(TableView<T> table, HasUpdateTable<T> updater) {
        this.table = table;
        this.updater = updater;
    }

    public <V extends TableView<T> & HasUpdateTable<T>> NamedColumnHandler(V both) {
        this.table = both;
        this.updater = both;
    }

    @Override
    public void handle(final TableColumn.CellEditEvent<T, String> text) {
        String newName = text.getNewValue();
        String oldName = text.getOldValue();
        text.getRowValue().setName(newName);

        try {
            NamedObject.validateUniqueAndNonempty(table.getItems());
        } catch (ValidationException ex) {
            text.getRowValue().setName(oldName);
            updater.updateTable(table);
        }
    }
}
