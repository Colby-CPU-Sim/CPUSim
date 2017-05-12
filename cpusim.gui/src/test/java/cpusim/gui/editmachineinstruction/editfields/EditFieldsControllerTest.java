package cpusim.gui.editmachineinstruction.editfields;

import cpusim.gui.editmachineinstruction.FieldListControl;
import cpusim.gui.harness.FXHarness;
import cpusim.gui.harness.FXMatchers;
import cpusim.gui.harness.FXRunner;
import cpusim.gui.util.ControlButtonController;
import cpusim.model.Field;
import cpusim.model.FieldValue;
import cpusim.model.Machine;
import cpusim.model.harness.BindMachine;
import cpusim.model.harness.FXMachineInjectionRule;
import cpusim.model.harness.matchers.CPUSimMatchers;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testfx.matcher.control.TableViewMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;
import org.textfx.matcher.control.CheckBoxMatchers;
import org.textfx.matcher.control.MoreTableViewMatchers;
import org.textfx.matcher.control.SpinnerMatchers;

import java.util.UUID;

import static cpusim.gui.harness.FXMatchers.allItems;
import static cpusim.gui.harness.FXMatchers.hasValue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.*;
import static org.textfx.matcher.control.ComboBoxMatchers.hasSelectedItem;

/**
 *
 * @since 2016-12-12
 */
public class EditFieldsControllerTest extends FXHarness {

    private Field f1;
    private Field f2;

    
    @BindMachine
    private EditFieldsController underTest;
    
    @Rule
    public FXMachineInjectionRule machineProperty = new FXMachineInjectionRule(this);

    @Before
    public void setupFields() {
        Machine machine = machineProperty.get();

        f1 = new Field("f1", UUID.randomUUID(), machine, 8,
                Field.Relativity.absolute, null, 0x00,
                Field.SignedType.Unsigned, Field.Type.required);
        f1.getValues().addAll(new FieldValue("a", UUID.randomUUID(), machine, 1));
        f2 = new Field("f2", UUID.randomUUID(), machine, 4,
                Field.Relativity.absolute, null, 0x00,
                Field.SignedType.Signed, Field.Type.required);
        f2.getValues().addAll(new FieldValue("b", UUID.randomUUID(), machine, 2),
                new FieldValue("c", UUID.randomUUID(), machine, 3));
    }

    @FXRunner.StageSetup
    public void start(Stage stage) throws Exception {
        underTest = new EditFieldsController();
    
        BorderPane pane = new BorderPane(underTest);

        Scene dialogScene = new Scene(pane);
        stage.setScene(dialogScene);
        stage.show();
    }

    @Test
    public void verifyBaseState() {
        FieldListControl fieldList = lookup("#fieldsList").<FieldListControl>tryQuery().get();
        verifyThat(from(fieldList).lookup("#newButton"), isEnabled());
        verifyThat(from(fieldList).lookup("#duplicateButton"), isDisabled());
        verifyThat(from(fieldList).lookup("#deleteButton"), isDisabled());
        
        verifyThat(fieldList, hasChildren(3, ".button"));
        
        verifyThat(from(fieldList).lookup(".table-view"), MoreTableViewMatchers.isEmpty());
        
        BorderPane rootPane = lookup("#rootPane").<BorderPane>tryQuery().get();
        verifyThat(rootPane.getCenter(), isDisabled());
        
        verifyThat("#nameText", TextInputControlMatchers.hasText(isEmptyString()));
    
        ComboBox<Field.Type> typeChoice = lookup("#typeChoice").query();
        verifyThat(typeChoice,
                allItems(ComboBox.class,
                         ComboBox<Field.Type>::getItems,
                         Matchers.contains(Field.Type.values())));
    
        ComboBox<Field.Relativity> relativityComboBox = lookup("#relativityChoice").query();
        verifyThat(relativityComboBox,
                allItems(ComboBox.class,
                         ComboBox<Field.Relativity>::getItems,
                         Matchers.contains(Field.Relativity.values())));
    
        Spinner<Integer> bitsSpinner = lookup("#bitsSpinner").query();
        verifyThat(bitsSpinner, hasValue(Spinner.class, Spinner<Integer>::getValue, Matchers.is(4)));
    
        Spinner<Long> defaultValueSpinner = lookup("#defaultValueSpinner").query();
        verifyThat(defaultValueSpinner, hasValue(Spinner.class, Spinner<Long>::getValue, Matchers.is(0L)));
    
        verifyThat("#unsignedCheckbox", hasValue(CheckBox.class, CheckBox::isSelected, is(Boolean.FALSE)));
    }

    

    @Test
    public void clickFields() {
        ObservableList<Field> fields = getMachine().getFields();

        interact(() -> fields.addAll(f1, f2));

        FieldListControl parent = lookup("#fieldsList").query();
        for (Field f : fields) {
            clickOn(from(parent).lookup(".table-row-cell")
                    .match(FXMatchers.forItem(is(f)))
                    .<TableRow<Field>>query());
            verifyFieldShown(f);
        }
    }

    private void clickField(Field f) {
        ObservableList<Field> fields = getMachine().getFields();

        interact(() -> fields.add(f));

        FieldListControl parent = lookup("#fieldsList").query();
        TableRow<Field> cell = from(parent).lookup(".table-row-cell")
                .match(FXMatchers.forItem(is(f)))
                .query();
        interact(() -> clickOn(cell));
        verifyFieldShown(f);

        verifyThat("#valuesTable", isVisible());

        ControlButtonController<FieldValue> buttons = lookup("#valuesButtons").query();
        verifyThat(from(buttons).lookup("#newButton"), isEnabled());
        verifyThat(from(buttons).lookup("#duplicateButton"), isDisabled());
        verifyThat(from(buttons).lookup("#deleteButton"), isDisabled());
    }

    public void clickFieldValue(Field f, FieldValue fv) {
        clickField(f);

        TableView<FieldValue> valuesTable = lookup("#valuesTable").query();
        TableRow<FieldValue> row = from(valuesTable).lookup(".table-row-cell")
                .match(MoreTableViewMatchers.rowItem(is(fv)))
                .query();
        clickOn(row);

        ControlButtonController<FieldValue> buttons = lookup("#valuesButtons").query();
        verifyThat(from(buttons).lookup("#newButton"), isEnabled());
        verifyThat(from(buttons).lookup("#duplicateButton"), isEnabled());
        verifyThat(from(buttons).lookup("#deleteButton"), isEnabled());
    }

    @Test
    public void duplicateFieldValue() {
        clickFieldValue(f1, f1.getValues().get(0));

        ControlButtonController<FieldValue> buttons = lookup("#valuesButtons").query();
        Button duplicate = from(buttons).lookup("#duplicateButton").query();
        Button delete = from(buttons).lookup("#deleteButton").query();

        verifyThat(duplicate, isEnabled());
        verifyThat(delete, isEnabled());

        clickOn(duplicate);

        verifyThat(duplicate, isEnabled());
        verifyThat(delete, isEnabled());

        verifyThat(lookup("#valuesTable"), TableViewMatchers.hasItems(2));

        assertThat(f1.getValues(), hasSize(2));
        f1.getValues().forEach(fv -> verifyThat("#valuesTable", MoreTableViewMatchers.hasRowWith(fv)));
    }

    @Test
    public void deleteFieldValue() {
        clickFieldValue(f2, f2.getValues().get(0));

        ControlButtonController<FieldValue> buttons = lookup("#valuesButtons").query();
        Button duplicate = from(buttons).lookup("#duplicateButton").query();
        Button delete = from(buttons).lookup("#deleteButton").query();

        verifyThat(duplicate, isEnabled());
        verifyThat(delete, isEnabled());

        clickOn(delete);

        verifyThat(duplicate, isEnabled());
        verifyThat(delete, isEnabled());

        assertThat(f2.getValues(), hasSize(1));
        verifyThat("#valuesTable", TableViewMatchers.hasItems(f2.getValues().size()));
        f2.getValues().forEach(fv -> verifyThat("#valuesTable", MoreTableViewMatchers.hasRowWith(fv)));

        clickOn(delete);

        verifyThat(duplicate, isDisabled());
        verifyThat(delete, isDisabled());

        assertThat(f2.getValues(), hasSize(0));
        verifyThat("#valuesTable", TableViewMatchers.hasItems(0));
    }

    @Test
    public void editFieldValues() throws Exception {
        clickField(f1);

        FieldValue fv = f1.getValues().get(0);

        TableRow<FieldValue> row = lookup("#valuesTable")
                .lookup(".table-row-cell")
                .match(MoreTableViewMatchers.rowItem(CPUSimMatchers.isId(fv.getID())))
                .query();

        TableCell<FieldValue, String> nameCell = from(row)
                .lookup(".table-cell")
                .match(MoreTableViewMatchers.cellItem(is(fv.getName())))
                .query();

        TableCell<FieldValue, Long> valueCell = from(row)
                .lookup(".table-cell")
                .match(MoreTableViewMatchers.cellItem(is(fv.getValue())))
                .query();

        // Set the value
        long newValue = 0xDEAD;

        doubleClickOn(valueCell)
                .write("0x" + Long.toHexString(newValue))
                .type(KeyCode.ENTER);

        assertThat(fv.getValue(), is(newValue));
        verifyThat(valueCell, MoreTableViewMatchers.cellItem(is(newValue)));

        // Set the name
        String newName = "new_name";

        doubleClickOn(nameCell)
                .write(newName)
                .type(KeyCode.ENTER);

        verifyThat(nameCell, MoreTableViewMatchers.cellItem(is(newName)));
        assertThat(fv.getName(), is(newName));
    }

    @Test
    public void editField() throws Exception {
        clickField(f1);

        Field changeField = new Field("check", UUID.randomUUID(), getMachine(), 16,
                Field.Relativity.pcRelativePostIncr, FXCollections.observableArrayList(f1.getValues()),
                0xffff, Field.SignedType.Signed, Field.Type.ignored);

        interact("#nameText", this::doubleClickOn);
        // Write the field name
        interact(() -> write(changeField.getName()));

        clickComboBoxOption(lookup("#typeChoice"), changeField.getType());

        interact("#bitsSpinner", this::doubleClickOn);
        interact(() -> write(Long.toString(changeField.getNumBits())));
        // need to make sure to click on something else now
        // change unfortunately commits when focus is lost... :(

        // Set the default value after, since it is bound to the #bitsSpinner
        interact("#defaultValueSpinner", this::doubleClickOn);
        interact(() -> write("0x" + Long.toHexString(changeField.getDefaultValue())));

        interact("#unsignedCheckbox", this::clickOn);

        clickComboBoxOption(lookup("#relativityChoice"), changeField.getRelativity());

        verifyFieldShown(changeField);
    }

    private void verifyFieldShown(Field f) {
        verifyThat("#nameText", TextInputControlMatchers.hasText(f.getName()));
        verifyThat("#typeChoice", hasSelectedItem(f.getType()));
        verifyThat("#relativityChoice", hasSelectedItem(f.getRelativity()));
        verifyThat("#bitsSpinner", SpinnerMatchers.hasValue(f.getNumBits()));
        verifyThat("#defaultValueSpinner", SpinnerMatchers.hasValue(f.getDefaultValue()));
        verifyThat("#unsignedCheckbox",
                CheckBoxMatchers.isSelected(f.getSigned() == Field.SignedType.Unsigned));

        verifyThat("#valuesTable", TableViewMatchers.hasItems(f.getValues().size()));
        for (FieldValue fv : f.getValues()) {
            verifyThat("#valuesTable", MoreTableViewMatchers.hasRowWith(fv));
        }
    }

}