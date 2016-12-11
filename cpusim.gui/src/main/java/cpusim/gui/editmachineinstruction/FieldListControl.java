package cpusim.gui.editmachineinstruction;

import cpusim.gui.util.DragHelper;
import cpusim.gui.util.FXMLLoaderFactory;
import cpusim.model.Field;
import cpusim.model.Machine;
import cpusim.model.util.MachineBound;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fxmisc.easybind.EasyBind;

import java.io.IOException;

/**
 * Controller that stores a {@link javafx.scene.control.ListView} and {@link javafx.scene.control.Button}
 *
 * @since 2016-12-09
 */
public class FieldListControl extends VBox implements MachineBound {

    private static final String FXML_FILE = "FieldList.fxml";

    private final Logger logger = LogManager.getLogger(FieldListControl.class);

    @FXML @SuppressWarnings("unused")
    private ListView<Field> fieldListView;

    @FXML @SuppressWarnings("unused")
    private Button editFieldsButton;

    private ObjectProperty<Machine> machine;

    public FieldListControl() {

        this.machine = new SimpleObjectProperty<>(this, "machine", null);

        try {
            FXMLLoaderFactory.fromRootController(this, FXML_FILE).load();
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    @FXML @SuppressWarnings("unused")
    void initialize() {

        fieldListView.setCellFactory(param -> {
           ListCell<Field> cell = new ListCell<Field>() {
               @Override
               protected void updateItem(Field item, boolean empty) {
                   super.updateItem(item, empty);

                   if (empty || item == null) {
                       setText(null);
                   } else {
                       setText(item.getName());
                   }
               }
           };

            // Starting a drag
            cell.setOnDragDetected(event -> {
                if (!cell.isEmpty()) {
                    Field field = cell.getItem();
                    Dragboard db = cell.startDragAndDrop(TransferMode.COPY);
                    db.setDragView(cell.snapshot(null, null));

                    DragHelper helper = new DragHelper(machineProperty(), db);
                    helper.setFieldContent(field);

                    event.consume();
                    logger.trace("row.setOnDragDetected() started MOVE, field = \"{}\"@{}",
                            field.getName(), field.getID());
                }
            });

            // A drag entered the row
//            cell.setOnDragEntered(ev -> {});

            // Drag moved over, must set Transfer if used
//            cell.setOnDragOver(ev -> {});

            // Drag left the row
//            row.setOnDragExited(ev -> {});

            // This row started a drag, and is now done
            cell.setOnDragDone(ev -> {
                if (ev.getTransferMode() == TransferMode.COPY) {
                    logger.trace("Row Drag successfully completed: row.getItem() = {}", cell.getItem());
                    ev.consume();
                }
            });

           return cell;
        });

        EasyBind.subscribe(machine, newMachine -> {
            if (newMachine != null) {
                fieldListView.itemsProperty().bindBidirectional(getMachine().fieldsProperty());
            }
        });

    }

    @Override
    public ObjectProperty<Machine> machineProperty() {
        return machine;
    }

    @FXML
    public void handleEditFields(ActionEvent ev) {

    }
}
