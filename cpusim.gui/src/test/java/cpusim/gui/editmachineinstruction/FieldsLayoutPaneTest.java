package cpusim.gui.editmachineinstruction;

import cpusim.gui.harness.FXHarness;
import cpusim.gui.harness.FXRunner;
import cpusim.model.Field;
import cpusim.model.MachineInstruction;
import cpusim.model.harness.BindMachine;
import cpusim.model.harness.MachineInjectionRule;
import cpusim.model.harness.SamplesFixture;
import cpusim.util.MoreIteratables;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableRow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.fxmisc.easybind.EasyBind;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.npathai.hamcrestopt.OptionalMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        FieldsLayoutPaneTest.AssemblyFields.class,
        FieldsLayoutPaneTest.InstructionFields.class
})
public class FieldsLayoutPaneTest extends FXHarness {

    private static final double WIDTH = 100;

    // TODO Tooltip tests?

    public static abstract class BaseSuite extends FXHarness {


        @BindMachine
        protected FieldsLayoutPane underTest;

        @BindMachine
        protected FieldListControl fieldList;

        protected ObjectProperty<MachineInstruction> instruction;

        protected ObservableList<Field> instructionFields;

        @Rule
        public MachineInjectionRule machineProperty = new MachineInjectionRule(this,
                SamplesFixture.MAXWELL.factory(0));


        private final MachineInstruction.FieldType fieldType;

        BaseSuite(MachineInstruction.FieldType fieldType) {
            this.fieldType = fieldType;
            this.instruction = new SimpleObjectProperty<>();

            EasyBind.subscribe(instruction, currentInstruction -> {
                if (currentInstruction != null) {
                    instructionFields = currentInstruction.fieldsProperty(fieldType);
                }
            });
        }

        @FXRunner.StageSetup
        public void start(Stage stage) throws Exception {

            fieldList = new FieldListControl(true, false);
            underTest = new FieldsLayoutPane(fieldType);
            HBox hbox = new HBox(fieldList, underTest);

            HBox.setHgrow(underTest, Priority.ALWAYS);

            Scene scene = new Scene(hbox, 400, 100);
            stage.setScene(scene);
            stage.show();
        }

        protected List<FieldsLayoutPane.FieldLabel> getFieldLabels() {
            return lookup("#fieldsBox")
                    .<HBox>query().getChildren()
                    .stream().map(n -> (FieldsLayoutPane.FieldLabel)n)
                    .collect(Collectors.toList());
        }

        /**
         * Get a {@link MachineInstruction} by name from the current {@link cpusim.model.Machine}
         * @param name
         * @return
         */
        protected MachineInstruction byName(String name) {
            return getMachine().getInstructions()
                    .stream()
                    .filter(m -> m.getName().equals(name))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Failed to find the \"" + name + "\" instruction."));

        }

        @Before
        public void setup() {
            instruction.set(byName("exit"));

            interact(() -> {
                underTest.currentInstructionProperty()
                        .bind(instruction);
            });
        }

        protected final double calcWidthPerBit() {
            double bitWidth = 0;
            for (Field f : instructionFields) {
                bitWidth += f.getNumBits();
            }

            return underTest.getWidth() / bitWidth;
        }

        protected void verifyFieldsShowing() {

            List<FieldsLayoutPane.FieldLabel> fieldLabels = getFieldLabels();
            List<Field> currentFields = fieldLabels.stream()
                    .map(FieldsLayoutPane.FieldLabel::getField)
                    .collect(Collectors.toList());

            assertEquals(instructionFields, currentFields);

            final double widthPerBit = calcWidthPerBit();
            MoreIteratables.zip(instructionFields, fieldLabels, (field, node) -> {
                assertEquals(field, node.getField());
                assertEquals("Width does not match",
                        Math.round(widthPerBit * field.getNumBits()), node.getWidth(), 1.0);

                Label nameLabel = node.getNameLabel();
                assertNotNull(nameLabel);

                // check the values now:
                assertThat(nameLabel.getText(), is(field.getName()));

                additionalFieldChecks(field, node);
            });
        }

        protected abstract void additionalFieldChecks(Field field, FieldsLayoutPane.FieldLabel node);

        @Test
        public void base() {
            verifyFieldsShowing();
        }

        @Test
        public void changeInstruction() {
            verifyFieldsShowing();

            interact(() -> instruction.set(byName("ret")));

            verifyFieldsShowing();
        }

        @Test
        public void permutateFields() {
            Field toAdd = getMachine().getFields().stream()
                    .filter(f -> !instructionFields.contains(f))
                    .findFirst().orElseThrow(NullPointerException::new);

            interact(() -> instructionFields.add(toAdd));

            verifyFieldsShowing();

            Field toAdd2 = getMachine().getFields().stream()
                    .filter(f -> !instructionFields.contains(f))
                    .findFirst().orElseThrow(NullPointerException::new);

            interact(() -> instructionFields.add(instructionFields.size() - 2, toAdd2));

            verifyFieldsShowing();

            interact(() -> instructionFields.remove(toAdd));

            verifyFieldsShowing();

        }

        /**
         * Adding duplicate fields into an assembly used to cause this to blow-up because the control
         * would create duplicate labels, this should NOT be a problem.
         */
        @Test
        public void duplicateFields() {
            Field toAdd = instructionFields.get(0);

            interact(() -> instructionFields.add(toAdd));

            verifyFieldsShowing();
        }

        /**
         * Add field to the assembly
         */
        @Test
        public void dragAddField() {
            List<TableRow<Field>> list = new ArrayList<>(lookup(".table-row-cell")
                    .<TableRow<Field>>match(c -> c != null && !c.isEmpty())
                    .queryAll());

            int size = instructionFields.size();

            assertEquals("Base case before dragging fields", size, getFieldLabels().size());

            interact(() -> {
                drag(list.get(0), MouseButton.PRIMARY)
                        .moveTo(underTest.localToScreen(10, 20))
                        .sleep(100)
                        .drop()
                        .sleep(100);
            });

            assertEquals("Failed to add field to control after dragging", size+1, getFieldLabels().size());

            verifyFieldsShowing();

            interact(() -> {
                drag(list.get(1), MouseButton.PRIMARY)
                        .moveTo(getFieldLabels().get(1).localToScreen(10, 10))
                        .sleep(100)
                        .drop()
                        .sleep(100);
            });

            assertEquals("Failed to add field to control", size+2, getFieldLabels().size());

            verifyFieldsShowing();
        }
    }

    public static class AssemblyFields extends BaseSuite {

        public AssemblyFields() {
            super(MachineInstruction.FieldType.Assembly);
        }

        @Override
        protected void additionalFieldChecks(Field field, FieldsLayoutPane.FieldLabel node) {
            Optional<Label> widthLabel = node.getWidthLabel();
            assertThat(widthLabel, isEmpty());
        }
    }

    public static class InstructionFields extends BaseSuite {

        public InstructionFields() {
            super(MachineInstruction.FieldType.Instruction);
        }

        @Override
        protected void additionalFieldChecks(Field field, FieldsLayoutPane.FieldLabel node) {
            Optional<Label> widthLabel = node.getWidthLabel();
            assertThat(widthLabel, isPresent());

            verifyThat(widthLabel, hasValue(hasText(Integer.toString(field.getNumBits()))));
        }
    }



}