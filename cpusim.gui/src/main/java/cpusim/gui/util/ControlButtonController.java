package cpusim.gui.util;

import cpusim.model.microinstruction.Microinstruction;
import cpusim.model.util.Copyable;
import cpusim.model.util.NamedObject;
import javafx.beans.NamedArg;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.ListBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Labeled;
import javafx.scene.control.SelectionModel;
import javafx.scene.layout.HBox;
import org.fxmisc.easybind.EasyBind;

import javax.annotation.Nonnull;
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

    private InteractionHandler<T> handler;

    private final BooleanProperty isExtendedProperties;

    private final DoubleProperty minIconWidth;

    private final BooleanProperty prefIconOnly;

    public ControlButtonController(@NamedArg("isExtendedProperties") final boolean initHasProperties,
                                   @NamedArg("prefIconOnly") boolean initPrefIcon) {
        this.isExtendedProperties = new SimpleBooleanProperty(this, "isExtendedProperties", initHasProperties);
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
        this.handler = checkNotNull(handler);

        handler.bindNewButtonDisabled(newButton.disableProperty());
        handler.bindDuplicateButtonDisabled(duplicateButton.disableProperty());
        handler.bindDeleteButtonDisabled(deleteButton.disableProperty());
        handler.bindPropertiesButtonDisabled(propertiesButton.disableProperty());

        handler.bindItems(items);
        handler.selectionModelBinding(selectionModel);
        supplier.set(handler.getSupplier());
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

        EasyBind.subscribe(isExtendedProperties, hasProperties -> {
            ObservableList<Node> children = this.getChildren();

            if (hasProperties ^ children.contains(propertiesButton)) {
                // The children must be changed
                if (!hasProperties) {
                    children.remove(propertiesButton);
                } else {
                    children.add(3, propertiesButton);
                }
            }
        });
    }

    private void bindHidingContentDisplay(Labeled control) {
        final ContentDisplay initialDisplay = control.getContentDisplay();
        ObjectBinding<ContentDisplay> binding = Bindings.createObjectBinding(() -> {
                if (prefIconOnly.get() || getWidth() < minIconWidth.get()) {
                    return ContentDisplay.GRAPHIC_ONLY;
                } else {
                    return initialDisplay;
                }
            }, widthProperty(), minIconWidth, isExtendedProperties, prefIconOnly);
        control.contentDisplayProperty().bind(binding);
    }

    public void setExtendedProperties(boolean isExtendedProperties) {
        this.isExtendedProperties.set(isExtendedProperties);
    }

    public boolean isExtendedProperties() {
        return isExtendedProperties.get();
    }

    public BooleanProperty isExtendedPropertiesProperty() {
        return isExtendedProperties;
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

        handler.onNewValueCreated(newValue);

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

        if (!checkDelete(selectedValue) && !handler.checkDelete(selectedValue)) {
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

        handler.onNewValueCreated(newObject);

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
        if (!isExtendedProperties.get()) {
            throw new IllegalStateException("Called onPropertiesButtonClick on something with no extended properties");
        } else {
            throw new Error("The method, onPropertiesButtonClick(ActionEvent) must be implemented if advanced " +
                    "properties are present.");
        }
    }

    public static <T> void bindSelectedItemIsNull(BooleanProperty toBind,
                                                  ObjectProperty<? extends SelectionModel<T>> property) {
        toBind.bind(Bindings.createBooleanBinding(() ->
                property.get() == null || property.get().isEmpty(), property));
    }

    /**
     * Interface to define what status checks are important for the status.
     */
    public interface InteractionHandler<T> {

        /**
         * Denotes if the "New" button is enabled
         * @return {@code true} if the button is enabled.
         * @param toBind
         */
        void bindNewButtonDisabled(@Nonnull BooleanProperty toBind);

        /**
         * Denotes if the delete button is enabled.
         * @return {@code true} if the button is enabled.
         * @param toBind
         */
        void bindDeleteButtonDisabled(@Nonnull BooleanProperty toBind);

        /**
         * Denotes if the delete button is enabled.
         * @return {@code true} if the button is enabled.
         * @param toBind
         */
        void bindDuplicateButtonDisabled(@Nonnull BooleanProperty toBind);

        /**
         * Denotes if the properties button is enabled.
         * @return {@code true} if the button is enabled.
         * @param toBind
         */
        default void bindPropertiesButtonDisabled(@Nonnull BooleanProperty toBind) {
            toBind.bind(new ReadOnlyBooleanWrapper(true));
        }

        /**
         * Gets a binding to a {@link ListBinding} for modifications.
         * @param toBind
         */
        void bindItems(@Nonnull Property<ObservableList<T>> toBind);

        /**
         * Get the currently selected Item from the {@link #bindItems(Property)} {@code List}.
         * @return Binding to the currently selected item.
         * @param toBind
         */
        void selectionModelBinding(@Nonnull ObjectProperty<SelectionModel<T>> toBind);

        /**
         * Creates a new instance
         * @return a new, non-{@code null} instance
         */
        Supplier<T> getSupplier();

        /**
         * Called when a new value gets created by either a duplication or from the {@link #getSupplier()}.
         *
         * @param newValue Non-{@code null} value
         */
        default void onNewValueCreated(@Nonnull T newValue) {
            checkNotNull(newValue);
        }

        default boolean checkDelete(@Nonnull T value) {
            checkNotNull(value);
            return true;
        }
    }
}
