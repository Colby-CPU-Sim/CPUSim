package cpusim.gui.editmachineinstruction.editfields;

import cpusim.gui.editmachineinstruction.FieldListControl;
import cpusim.gui.harness.FXHarness;
import cpusim.gui.harness.FXRunner;
import cpusim.model.Field;
import cpusim.model.Machine;
import cpusim.model.harness.MachineInjectionRule;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testfx.matcher.control.ListViewMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;

import java.util.UUID;

import static cpusim.gui.harness.FXMatchers.allItems;
import static cpusim.gui.harness.FXMatchers.hasValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.*;

/**
 *
 * @since 2016-12-12
 */
public class EditFieldsControllerTest extends FXHarness {

    private Field f1;
    private Field f2;

    
    @MachineInjectionRule.BindMachine
    private EditFieldsController underTest;
    
    @Rule
    public MachineInjectionRule machineProperty = new MachineInjectionRule(this);

    @Before
    public void setupFields() {
        Machine machine = machineProperty.get();

        f1 = new Field("f1", UUID.randomUUID(), machine, 8,
                Field.Relativity.absolute, null, 0x00,
                Field.SignedType.Unsigned, Field.Type.required);
        f2 = new Field("f2", UUID.randomUUID(), machine, 4,
                Field.Relativity.absolute, null, 0x00,
                Field.SignedType.Signed, Field.Type.required);
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
        
        verifyThat(from(fieldList).lookup(".list-view"), ListViewMatchers.isEmpty());
        
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
        verifyThat(defaultValueSpinner, hasValue(Spinner.class, Spinner<Long>::getValue, Matchers.is(0l)));
    
        verifyThat("#unsignedCheckbox", hasValue(CheckBox.class, CheckBox::isSelected, is(Boolean.FALSE)));
    }
    
    

    @Test
    public void test() {
    }
    
}