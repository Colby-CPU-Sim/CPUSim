package cpusim.gui.editmachineinstruction;

import cpusim.gui.harness.FXHarness;
import cpusim.gui.util.DragHelper;
import cpusim.model.Field;
import cpusim.model.Machine;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.testfx.matcher.control.ListViewMatchers;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 *
 * @since 2016-12-12
 */
public class FieldListControlTest extends FXHarness {

    private FieldListControl underTest;

    private DragHelper.HandleDragBehaviour handler;

    @Before
    public void setup() {
        Machine machine = getMachine();

        Field f1 = new Field("f1", UUID.randomUUID(), machine, 8,
                Field.Relativity.absolute, null, 0x00,
                Field.SignedType.Unsigned, Field.Type.required);
        Field f2 = new Field("f2", UUID.randomUUID(), machine, 4,
                Field.Relativity.absolute, null, 0x00,
                Field.SignedType.Signed, Field.Type.required);

        machine.getFields().addAll(Arrays.asList(f1, f2));

        underTest.machineProperty().bind(machineProperty());

        handler = mock(DragHelper.HandleDragBehaviour.class);
    }

    @Override
    public void start(Stage stage) throws Exception {
        underTest = new FieldListControl(true, true);

        BorderPane pane = new BorderPane();
        pane.setLeft(underTest);

        // Drop this side

        VBox right = new VBox();

        Label dropLabel = new Label("Drop here.");
        dropLabel.setId("end");
        dropLabel.setOnDragOver(ev -> {
            ev.acceptTransferModes(TransferMode.ANY);
            ev.consume();
        });
        dropLabel.setOnDragDropped(ev -> {
            DragHelper helper = new DragHelper(machineProperty(), ev.getDragboard());
            helper.visit(handler);

            ev.setDropCompleted(true);
            ev.consume();
        });
        right.getChildren().add(dropLabel);

        pane.setRight(right);

        Scene scene = new Scene(pane);

        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void elementsPresent() {
        ListView<Field> list = lookup("#fieldListView").query();

        for (Field f : getMachine().getFields()) {
            MatcherAssert.assertThat(list, ListViewMatchers.hasListCell(f));
        }
    }

    /**
     * Verify that when you drag the values over, it sets up the handler properly to have a field.
     */
    @Test
    public void dragTest() {
        Set<ListCell<Field>> cells = lookup(".list-cell")
                .<ListCell<Field>>match(c -> c != null && !c.isEmpty())
                .queryAll();

        Label end = lookup("#end").query();

        for (ListCell<Field> cell : cells) {
            drag(cell, MouseButton.PRIMARY).dropTo(end);

            verify(handler).onDragField(cell.getItem());
        }

        ListCell<Field> emptyCell = lookup(".list-cell")
                .<ListCell<Field>>match(c -> c != null && c.isEmpty())
                .query();

        drag(emptyCell, MouseButton.PRIMARY).dropTo(end);

        verify(handler, never());
    }

    @Test
    public void addNewItem() {

    }
}