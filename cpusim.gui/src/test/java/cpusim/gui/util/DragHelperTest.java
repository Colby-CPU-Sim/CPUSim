package cpusim.gui.util;

import com.google.common.collect.Lists;
import cpusim.model.Field;
import cpusim.model.Machine;
import cpusim.model.microinstruction.Branch;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * @since 2016-12-05
 */
public class DragHelperTest extends ApplicationTest {

    private ObjectProperty<Machine> machineProperty = new SimpleObjectProperty<>(new Machine("test"));
    private ObjectProperty<Machine> mockedMachineProperty = new SimpleObjectProperty<>(new Machine("test"));

    private Branch branchMicro;
    private DragHelper.HandleDragBehaviour handler;
    private Field field;

    private final UUID testUUID = UUID.randomUUID();
    private final int dragIndex = 2;
    private final List<File> files = Lists.newArrayList(Paths.get(".").toAbsolutePath().toFile());


    @Override
    public void start(Stage stage) throws Exception {
        BorderPane pane = new BorderPane();

        VBox layout = new VBox();
        ObservableList<Node> children = layout.getChildren();

        Label microLabel = new Label("micro!");
        microLabel.setId("startMicro");

        microLabel.setOnDragDetected(ev -> {
            Dragboard db = microLabel.startDragAndDrop(TransferMode.ANY);

            DragHelper helper = new DragHelper(machineProperty, db);
            helper.setMicroContent(branchMicro);

            ev.setDragDetect(true);
            ev.consume();
        });
        children.add(microLabel);

        Label indexLabel = new Label("index!");
        indexLabel.setId("startIndex");

        indexLabel.setOnDragDetected(ev -> {
            Dragboard db = indexLabel.startDragAndDrop(TransferMode.ANY);

            DragHelper helper = new DragHelper(machineProperty, db);
            helper.setIndexContent(dragIndex);

            ev.setDragDetect(true);
            ev.consume();
        });
        children.add(indexLabel);

        Label fieldLabel = new Label("field!");
        fieldLabel.setId("startField");

        fieldLabel.setOnDragDetected(ev -> {
            Dragboard db = fieldLabel.startDragAndDrop(TransferMode.ANY);

            DragHelper helper = new DragHelper(machineProperty, db);
            helper.setFieldContent(field);

            ev.setDragDetect(true);
            ev.consume();
        });
        children.add(fieldLabel);

        Label otherLabel = new Label("other?");
        otherLabel.setId("other");

        otherLabel.setOnDragDetected(ev -> {
            Dragboard db = otherLabel.startDragAndDrop(TransferMode.ANY);

            ClipboardContent cc = new ClipboardContent();
            cc.putFiles(files);
            db.setContent(cc);

            ev.setDragDetect(true);
            ev.consume();
        });
        children.add(otherLabel);

        pane.setLeft(layout);

        // Drop this side

        VBox right = new VBox();

        Label dropLabel = new Label("Drop here.");
        dropLabel.setOnDragOver(ev -> {
            ev.acceptTransferModes(TransferMode.ANY);
            ev.consume();
        });
        dropLabel.setOnDragDropped(ev -> {
            DragHelper helper = new DragHelper(machineProperty, ev.getDragboard());
            helper.visit(handler);

            ev.setDropCompleted(true);
            ev.consume();
        });
        dropLabel.setId("end");
        right.getChildren().add(dropLabel);

        pane.setRight(right);

        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
    }

    @Before
    public void setupMock() {
        branchMicro = mock(Branch.class);
        when(branchMicro.machineProperty()).thenReturn(mockedMachineProperty);
        when(branchMicro.getID()).thenReturn(testUUID);
        machineProperty.getValue().getMicros(Branch.class).add(branchMicro);

        field = mock(Field.class);
        when(field.machineProperty()).thenReturn(mockedMachineProperty);
        when(field.getID()).thenReturn(testUUID);

        machineProperty.getValue().getFields().add(field);

        handler = mock(DragHelper.HandleDragBehaviour.class);
    }

    @Test
    public void insertMicro() throws Exception {
        drag("#startMicro", MouseButton.PRIMARY).dropTo("#end");

        verify(branchMicro, atLeast(1)).getID();

        verify(handler).onDragMicro(branchMicro);
    }

    @Test
    public void insertIndex() throws Exception {
        drag("#startIndex", MouseButton.PRIMARY).dropTo("#end");

        verify(handler).onDragIndex(dragIndex);
    }

    @Test
    public void insertField() throws Exception {
        drag("#startField", MouseButton.PRIMARY).dropTo("#end");

        verify(field, atLeast(1)).getID();

        verify(handler).onDragField(field);
    }

    @Test
    public void insertOther() throws Exception {
        drag("#other", MouseButton.PRIMARY).dropTo("#end");

        verify(handler).onOther(DataFormat.FILES, files);
    }
}