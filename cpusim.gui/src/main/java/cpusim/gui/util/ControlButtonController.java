package cpusim.gui.util;

import cpusim.model.microinstruction.Microinstruction;
import cpusim.model.util.Copyable;
import cpusim.model.util.NamedObject;
import javafx.beans.NamedArg;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Labeled;
import javafx.scene.control.SelectionModel;
import javafx.scene.layout.HBox;
import org.fxmisc.easybind.EasyBind;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Controller for a "New", "Duplicate", and "Remove" button.
 * @since 2016-11-29
 */
public class ControlButtonController<T extends NamedObject & Copyable<T>> extends HBox {

    /**
     * Defines the FXML file used to load the controls.
     */
    private static final String FXML_PATH = "TableControlButtons.fxml";

    @FXML @SuppressWarnings("unused")
    private Button newButton;
    @FXML @SuppressWarnings("unused")
    private Button deleteButton;
    @FXML @SuppressWarnings("unused")
    private Button duplicateButton;
    @FXML @SuppressWarnings("unused")
    private Button propertiesButton;

    // all of the buttons
    private List<Button> allButtons;

    private final ListProperty<T> items;
    private final ObjectProperty<SelectionModel<T>> selectionModel;
    private final ObjectProperty<Supplier<T>> supplier;

    private final BooleanProperty hasProperties;

    private final DoubleProperty minIconWidth;

    private final BooleanProperty prefIconOnly;

    public ControlButtonController(@NamedArg("hasProperties") final boolean initHasProperties,
                                   @NamedArg("prefIconOnly") boolean initPrefIcon) {
        this.hasProperties = new SimpleBooleanProperty(this, "hasProperties", initHasProperties);
        this.prefIconOnly = new SimpleBooleanProperty(this, "prefIconOnly", initPrefIcon);
        this.items = new SimpleListProperty<>(this, "items", null);
        this.selectionModel = new SimpleObjectProperty<>(this, "selectionModel", null);
        this.supplier = new SimpleObjectProperty<>(this, "supplier", null);

        this.minIconWidth = new SimpleDoubleProperty(this, "minIconWidth");

        IntegerBinding sizeBinding = Bindings.size(getChildren());
        minWidthProperty().bind(EasyBind.combine(sizeBinding, spacingProperty(), (size, spacing) ->
                    size.intValue() * 30 - (size.intValue() - 1) * spacing.intValue()));

        this.minIconWidth.bind(sizeBinding.multiply(90));

        prefWidthProperty().bind(EasyBind.combine(prefIconOnly, sizeBinding, spacingProperty(),
                (iconOnly, size, spacing) -> {

                    int cWidth = (iconOnly ? 24 : 75);
                    return size.intValue() * cWidth - (size.intValue() - 1) * spacing.intValue();
                }));
    }

    /**
     * Default constructor so it can be used within the Scene Builder
     */
    public ControlButtonController() {
        this(true, false);
    }

    public ControlButtonController(@NamedArg("prefIconOnly") boolean initPrefIcon) {
        this(true, initPrefIcon);
    }

    public void setInteractionHandler(InteractionHandler<T> handler) {
        checkNotNull(handler);

        newButton.disableProperty().bind(handler.newButtonEnabledBinding().not());
        duplicateButton.disableProperty().bind(handler.duplicateButtonEnabledBinding().not());
        deleteButton.disableProperty().bind(handler.deleteButtonEnabledBinding().not());
        propertiesButton.disableProperty().bind(handler.propertiesButtonEnabledBinding().not());

        items.bind(handler.itemsBinding());
        selectionModel.bind(handler.selectionModelBinding());
    }

    /**
     * Loads the buttons FXML file, see {@link #FXML_PATH} for the location.
     */
    protected void loadFXML() {
        try {
            FXMLLoaderFactory.fromRootController(this, ControlButtonController.class, FXML_PATH).load();
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    @FXML @SuppressWarnings("unused")
    protected void initialize() {
        allButtons = Arrays.asList(newButton, duplicateButton, deleteButton, propertiesButton);

        allButtons.forEach(this::bindHidingContentDisplay);

        EasyBind.subscribe(hasProperties, hasProperties -> {
            if (!hasProperties) {
                getChildren().remove(propertiesButton);
            } else {
                getChildren().set(3, propertiesButton);
            }
        });

        EasyBind.subscribe(prefIconOnly, iconOnly -> {
            if (iconOnly) {
                allButtons.forEach(btn -> btn.getStyleClass().add("icon-only"));
            } else {
                allButtons.forEach(btn -> btn.getStyleClass().remove("icon-only"));
            }
        });
    }

    private void bindHidingContentDisplay(Labeled control) {
        final ContentDisplay initialDisplay = control.getContentDisplay();
        ObjectBinding<ContentDisplay> binding = Bindings.createObjectBinding(() -> {
                if (getWidth() < minIconWidth.get()) {
                    return ContentDisplay.GRAPHIC_ONLY;
                } else {
                    return initialDisplay;
                }
            }, widthProperty(), minIconWidth, hasProperties);
        control.contentDisplayProperty().bind(binding);
    }

    public BooleanProperty hasPropertiesProperty() {
        return hasProperties;
    }

    public BooleanProperty prefIconOnlyProperty() {
        return prefIconOnly;
    }

    /**
     * creates a new instruction when clicking on New button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML @SuppressWarnings("unused")
    protected void onNewButtonClick(ActionEvent e) {
        //add a new item at the end of the list.
        ObservableList<T> items = this.items.get();
        checkState(items != null,
                "No interaction information set or null items, " +
                        "call #setInteractionHandler(InteractionHandler)");

        String uniqueName = NamedObject.createUniqueName(items, '?');
        final T newValue = supplier.getValue().get();
        newValue.setName(uniqueName);

        items.add(newValue);
        selectionModel.get().select(newValue);
    }

    /**
     * Return {@code false} if the value to delete is not acceptable. This is an extension point
     * for subclasses, by default, this method checks that the module is not in use by any
     * {@link Microinstruction} instances, and if so, it will confirm with the user before returning
     * {@code true}.
     *
     * @param toDelete Value that is requested to be deleted.
     * @return {@code true} if the deletion is allowed.
     */
    protected boolean checkDelete(T toDelete) {
        return true;
    }

    /**
     * deletes an existing instruction when clicking on Delete button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML @SuppressWarnings("unused")
    protected void onDeleteButtonClick(ActionEvent e) {
        ObservableList<T> items = this.items.get();
        checkState(items != null,
                "No interaction information set or null items, " +
                        "call #setInteractionHandler(InteractionHandler)");

        SelectionModel<T> selectionModel = this.selectionModel.get();
        final T selectedValue = selectionModel.getSelectedItem();

        if (!checkDelete(selectedValue)) {
            return;
        }

        items.remove(selectionModel.getSelectedIndex());
    }

    /**
     * duplicates the selected instruction when clicking on Duplicate button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML @SuppressWarnings("unused")
    protected void onDuplicateButtonClick(ActionEvent e) {
        //add a new item at the end of the list.
        ObservableList<T> items = this.items.get();
        checkState(items != null,
                "No interaction information set or null items, " +
                        "call #setInteractionHandler(InteractionHandler)");

        SelectionModel<T> selectionModel = this.selectionModel.get();

        final T newObject = selectionModel.getSelectedItem().cloneOf();
        final int selectedIndex = selectionModel.getSelectedIndex();
        newObject.setName(newObject.getName() + "_copy");
        items.add(selectedIndex + 1, newObject);
        selectionModel.select(selectedIndex + 1);
    }

    /**
     * edits the selected register array
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML @SuppressWarnings("unused")
    protected void onPropertiesButtonClick(ActionEvent e) {
        if (!hasProperties.get()) {
            throw new IllegalStateException("Called onPropertiesButtonClick on something with no extended properties");
        } else {
            throw new Error("The method, onPropertiesButtonClick(ActionEvent) must be implemented if advanced " +
                    "properties are present.");
        }
    }

    public static <T> BooleanBinding selectedItemIsNotNullBinding(ObjectProperty<? extends SelectionModel<T>> property) {
        return Bindings.createBooleanBinding(() -> {
            if (property.get() == null) {
                return false;
            } else {
                return !property.get().isEmpty();
            }
        }, property);
    }

    /**
     * Interface to define what status checks are important for the status.
     */
    public interface InteractionHandler<T> {

        /**
         * Denotes if the "New" button is enabled
         * @return {@code true} if the button is enabled.
         */
        BooleanBinding newButtonEnabledBinding();

        /**
         * Denotes if the delete button is enabled.
         * @return {@code true} if the button is enabled.
         */
        BooleanBinding deleteButtonEnabledBinding();

        /**
         * Denotes if the delete button is enabled.
         * @return {@code true} if the button is enabled.
         */
        BooleanBinding duplicateButtonEnabledBinding();

        /**
         * Denotes if the properties button is enabled.
         * @return {@code true} if the button is enabled.
         */
        default BooleanBinding propertiesButtonEnabledBinding() {
            return Bindings.createBooleanBinding(() -> false);
        }

        /**
         * Gets a binding to a {@link ListBinding} for modifications.
         */
        ObjectBinding<ObservableList<T>> itemsBinding();

        /**
         * Get the currently selected Item from the {@link #itemsBinding()} {@code List}.
         * @return Binding to the currently selected item.
         */
        ObjectBinding<SelectionModel<T>> selectionModelBinding();

        /**
         * Creates a new instance
         * @return a new, non-{@code null} instance
         */
        Supplier<T> supplierBinding();
    }
}
