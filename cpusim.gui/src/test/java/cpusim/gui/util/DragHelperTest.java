package cpusim.gui.util;

import com.google.common.collect.Lists;
import cpusim.model.Machine;
import cpusim.model.microinstruction.Branch;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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
    private Branch branchMicro;
    private DragHelper.HandleDragBehaviour handler;

    private final UUID branchUUID = UUID.randomUUID();
    private final int dragIndex = 2;
    private final List<File> files = Lists.newArrayList(Paths.get(".").toAbsolutePath().toFile());


    @Override
    public void start(Stage stage) throws Exception {
        StackPane pane = new StackPane();

        HBox layout = new HBox();
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
        children.add(dropLabel);

        pane.getChildren().add(layout);

        Scene scene = new Scene(pane, 200, 50);
        stage.setScene(scene);
        stage.show();
    }

    @Before
    public void setupMock() {
        branchMicro = mock(Branch.class);
        when(branchMicro.getID()).thenReturn(branchUUID);
        machineProperty.getValue().getMicros(Branch.class).add(branchMicro);

        handler = mock(DragHelper.HandleDragBehaviour.class);
    }

    @Test
    public void insertIntoDragboardMicro() throws Exception {
        drag("#startMicro", MouseButton.PRIMARY).dropTo("#end");

        verify(branchMicro, atLeast(1)).getID();

        verify(handler).onDragMicro(branchMicro);
    }

    @Test
    public void insertIntoDragboardIndex() throws Exception {
        drag("#startIndex", MouseButton.PRIMARY).dropTo("#end");

        verify(handler).onDragIndex(dragIndex);
    }

    @Test
    public void insertOther() throws Exception {
        drag("#other", MouseButton.PRIMARY).dropTo("#end");

        verify(handler).onOther(DataFormat.FILES, files);
    }
}