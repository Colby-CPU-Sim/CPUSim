package cpusim.gui.util.table;

import cpusim.model.microinstruction.Shift;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.util.Callback;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Cellfactory to display and select {@link Enum} values.
 *
 * @since 2016-12-07
 */
public class EnumCellFactory<S, E extends Enum<E>> implements Callback<TableColumn<S,E>, TableCell<S,E>> {

    private final Class<E> enumClass;

    private final Comparator<E> sorter;

    public EnumCellFactory(Class<E> enumClass) {
        this.enumClass = enumClass;
        this.sorter = Comparator.comparing(Enum::toString);
    }

    public EnumCellFactory(Class<E> enumClass, Comparator<E> sorter) {
        this.enumClass = enumClass;
        this.sorter = checkNotNull(sorter);
    }

    @Override
    public TableCell<S, E> call(TableColumn<S, E> param) {
        ObservableList<E> list = FXCollections.observableArrayList(
                EnumSet.allOf(enumClass).stream()
                        .sorted(sorter)
                        .collect(Collectors.toList()));
        return new ComboBoxTableCell<>(list);
    }
}
