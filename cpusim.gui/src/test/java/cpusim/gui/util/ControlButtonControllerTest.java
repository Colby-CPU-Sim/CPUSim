package cpusim.gui.util;

import cpusim.gui.harness.FXHarness;
import cpusim.model.util.Copyable;
import cpusim.model.util.NamedObject;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Test;
import org.testfx.matcher.base.ParentMatchers;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static cpusim.gui.harness.FXMatchers.forItem;
import static cpusim.model.harness.CPUSimMatchers.isNamed;
import static org.junit.Assert.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isDisabled;
import static org.testfx.matcher.base.NodeMatchers.isEnabled;
import static org.testfx.matcher.control.ListViewMatchers.*;

/**
 * Tests for the {@link ControlButtonController} class.
 *
 * @since 2019-12-12
 */
public class ControlButtonControllerTest extends FXHarness {

    private ListView<Data> listView;

    private ControlButtonController<Data> buttonController;

    private static final String SELECTOR_NEW = "#newButton";
    private static final String SELECTOR_DELETE = "#deleteButton";
    private static final String SELECTOR_DUPLICATE = "#duplicateButton";
    private static final String SELECTOR_PROPERTIES = "#propertiesButton";

    private final List<String> buttonSelectors = Arrays.asList(SELECTOR_NEW, SELECTOR_DELETE,
            SELECTOR_DUPLICATE, SELECTOR_PROPERTIES);

    private Button getButton(@Nonnull String selector) {
        return lookup(selector).query();
    }

    @Override
    public void start(Stage stage) throws Exception {
        buttonController = new DefaultControlButtonController<>(false);
        buttonController.setExtendedProperties(true);

        listView = new ListView<>();
        listView.setId("listView");

        VBox layout = new VBox();

        layout.getChildren().addAll(listView, buttonController);
        BorderPane pane = new BorderPane(layout);

        Scene scene = new Scene(pane, 400, 100);
        stage.setScene(scene);
        stage.show();
    }

    @Before
    public void setup() {

    }

    @Test
    public void toggleBindings() {
        BooleanProperty disabledProperty = new SimpleBooleanProperty(this, "enabled", true);

        buttonController.setInteractionHandler(new ControlButtonController.InteractionHandler<Data>() {
            @Override
            public void bindNewButtonDisabled(@Nonnull BooleanProperty toBind) {
                toBind.bind(disabledProperty);
            }

            @Override
            public void bindDeleteButtonDisabled(@Nonnull BooleanProperty toBind) {
                toBind.bind(disabledProperty);
            }

            @Override
            public void bindDuplicateButtonDisabled(@Nonnull BooleanProperty toBind) {
                toBind.bind(disabledProperty);
            }

            @Override
            public void bindPropertiesButtonDisabled(@Nonnull BooleanProperty toBind) {
                toBind.bind(disabledProperty);
            }

            @Override
            public void bindItems(@Nonnull Property<ObservableList<Data>> toBind) {
                checkNotNull(toBind);
            }

            @Override
            public void selectionModelBinding(@Nonnull ObjectProperty<SelectionModel<Data>> toBind) {
                checkNotNull(toBind);
            }

            @Override
            public Supplier<Data> getSupplier() {
                return null;
            }
        });

        buttonSelectors.forEach(selector -> assertTrue(getButton(selector).isDisabled()));
    }

    @Test
    public void hasPropertiesProperty() throws Exception {

        // Changing this property causes the UI to change, gotta wait for the event to trigger in the Queue
        interact(() -> buttonController.setExtendedProperties(true));
        verifyThat(buttonController, ParentMatchers.hasChildren(4));

        interact(() -> buttonController.setExtendedProperties(false));
        verifyThat(buttonController, ParentMatchers.hasChildren(3));
    }

    @Test
    public void prefIconOnlyProperty() throws Exception {
        interact(() -> buttonController.prefIconOnlyProperty().set(true));
        lookup(".button")
                .<Button>queryAll()
                .forEach(button -> assertEquals(button.getContentDisplay(), ContentDisplay.GRAPHIC_ONLY));

        interact(() -> buttonController.prefIconOnlyProperty().set(false));
        lookup(".button")
                .<Button>queryAll()
                .forEach(button -> assertNotEquals(button.getContentDisplay(), ContentDisplay.GRAPHIC_ONLY));
    }

    public ControlButtonController.InteractionHandler<Data> listHandler(Data supplied) {
        return new ControlButtonController.InteractionHandler<Data>() {
            private final ReadOnlyObjectProperty<Data> selectedItem = listView.getSelectionModel().selectedItemProperty();

            @Override
            public void bindNewButtonDisabled(@Nonnull BooleanProperty toBind) {
                toBind.bind(new ReadOnlyBooleanWrapper(false));
            }

            @Override
            public void bindDeleteButtonDisabled(@Nonnull BooleanProperty toBind) {
                toBind.bind(selectedItem.isNull());
            }

            @Override
            public void bindDuplicateButtonDisabled(@Nonnull BooleanProperty toBind) {
                toBind.bind(selectedItem.isNull());
            }

            @Override
            public void bindItems(@Nonnull Property<ObservableList<Data>> toBind) {
                toBind.bindBidirectional(listView.itemsProperty());
            }

            @Override
            public void selectionModelBinding(@Nonnull ObjectProperty<SelectionModel<Data>> toBind) {
                toBind.bind(listView.selectionModelProperty());
            }

            @Override
            public Supplier<Data> getSupplier() {
                return () -> supplied;
            }
        };
    }

    @Test
    public void onNewButtonClick() throws Exception {
        Data supplied = new Data("Test");
        buttonController.setInteractionHandler(listHandler(supplied));

        clickOn("#newButton");
        verifyThat(".list-view", hasListCell(supplied));
    }

    @Test
    public void onDeleteButtonClick() throws Exception {
        Data supplied = new Data("Test");
        listView.getItems().add(supplied);
        verifyThat(".list-view", hasListCell(supplied));

        buttonController.setInteractionHandler(listHandler(supplied));

        ListCell<Data> testCell = lookup(".list-cell")
                .<ListCell<Data>>match(forItem(isNamed("Test")))
                .query();
        clickOn(testCell);
        verifyThat("#deleteButton", isEnabled());

        clickOn("#deleteButton");
        verifyThat("#deleteButton", isDisabled());

        verifyThat(".list-view", isEmpty());
    }

    @Test
    public void onDuplicateButtonClick() throws Exception {
        Data supplied = new Data("Test");
        listView.getItems().add(supplied);
        verifyThat(".list-view", hasListCell(supplied));

        buttonController.setInteractionHandler(listHandler(supplied));

        ListCell<Data> testCell = lookup(".list-cell")
                .<ListCell<Data>>match(forItem(isNamed("Test")))
                .query();
        clickOn(testCell);
        verifyThat("#duplicateButton", isEnabled());

        clickOn("#duplicateButton");
        verifyThat("#duplicateButton", isEnabled());

        verifyThat(".list-view", hasItems(2));
    }

    @Test
    public void onPropertiesButtonClick() throws Exception {

    }

    static class Data implements NamedObject, Copyable<Data> {

        private StringProperty name;

        Data(String name) {
            this.name = new SimpleStringProperty(this, "name", name);
        }

        Data(Data other) {
            this(other.getName());
        }

        @Override
        public <U extends Data> void copyTo(U other) {
            checkNotNull(other);

            other.setName(name.getValue());
        }

        @Override
        public StringProperty nameProperty() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Data data = (Data) o;

            return getName().equals(data.getName());
        }

        @Override
        public int hashCode() {
            return getName().hashCode();
        }
    }
}