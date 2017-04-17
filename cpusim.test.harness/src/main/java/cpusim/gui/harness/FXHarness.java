package cpusim.gui.harness;

import cpusim.model.Machine;
import cpusim.model.harness.BindMachine;
import cpusim.model.util.MachineBound;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import org.hamcrest.core.Is;
import org.junit.runner.RunWith;
import org.testfx.api.FxRobot;
import org.testfx.service.query.NodeQuery;
import org.textfx.matcher.control.MoreListViewMatchers;

import java.util.Optional;
import java.util.function.Consumer;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeThat;

/**
 * @since 2016-12-12
 */
@RunWith(FXRunner.class)
public abstract class FXHarness extends FxRobot implements MachineBound {

    @BindMachine
    private ObjectProperty<Machine> machineProperty = new SimpleObjectProperty<>(this, "machine");

    @Override
    public ObjectProperty<Machine> machineProperty() {
        return machineProperty;
    }

    /**
     * Implemented based off of: https://github.com/TestFX/TestFX/issues/249
     * @param query
     * @param consumer
     * @param <T>
     * @return
     */
    protected <T extends Node> FXHarness interact(String query,
                                             Consumer<T> consumer) {
        final Optional<T> node = lookup(query).tryQuery();
        final T nodeValue = node.orElseThrow(NullPointerException::new);
        interact(() -> consumer.accept(nodeValue));

        return this;
    }

    protected <T> ComboBox<T> clickComboBoxOption(NodeQuery query, T item) {
        ComboBox<T> comboBox = query.query();

        assertNotNull("Could not get combobox from query: " + query.toString(), comboBox);

        clickOn(comboBox);
        clickOn(lookup(".list-view")
                .match(MoreListViewMatchers.hasCellValueType(item.getClass()))
                .lookup(".list-cell")
                .match(FXMatchers.forItem(is(item)))
                .<Node>query());

        return comboBox;
    }

    private static void ciSkip(boolean shouldSkip) {
        assumeThat(Boolean.parseBoolean(System.getProperty("CI", "false")), Is.is(!shouldSkip));
    }

    public static void skipOnCI() {
        ciSkip(true);
    }

    public static void skipOnDev() {
        ciSkip(false);
    }
}
