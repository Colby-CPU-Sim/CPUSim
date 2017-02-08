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
import javafx.scene.Node;
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

/**
 * Created by kevin on 31/01/2017.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        FieldLayoutPaneTest.AssemblyFields.class,
        FieldLayoutPaneTest.InstructionFields.class
})
public class FieldLayoutPaneTest extends FXHarness {

    private static final double WIDTH = 100;

    // TODO Tooltip tests?

    public static abstract class BaseSuite extends FXHarness {


        @BindMachine
        protected FieldLayoutPane underTest;

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
            underTest = new FieldLayoutPane(fieldType);
            HBox hbox = new HBox(fieldList, underTest);

            HBox.setHgrow(underTest, Priority.ALWAYS);

            Scene scene = new Scene(hbox, 400, 100);
            stage.setScene(scene);
            stage.show();
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
            List<Node> children = underTest.getChildren();
            assertEquals(instructionFields.size(), children.size());

            List<FieldLayoutPane.FieldLabel> asFL = children.stream()
                    .map(node -> (FieldLayoutPane.FieldLabel)node)
                    .collect(Collectors.toList());

            final double widthPerBit = calcWidthPerBit();
            MoreIteratables.zip(instructionFields, asFL, (field, node) -> {
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

        protected abstract void additionalFieldChecks(Field field, FieldLayoutPane.FieldLabel node);

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

            assertEquals(size, underTest.getChildren().size());

            interact(() -> {
                drag(list.get(0), MouseButton.PRIMARY)
                        .moveTo(underTest.localToScreen(10, 20))
                        .drop();
            });

            assertEquals(size + 1, underTest.getChildren().size());

            verifyFieldsShowing();

            interact(() -> {
                drag(list.get(1), MouseButton.PRIMARY)
                        .moveTo(underTest.getChildren().get(1).localToScreen(10, 10))
                        .drop();
            });

            assertEquals(size + 2, underTest.getChildren().size());

            verifyFieldsShowing();
        }
    }

    public static class AssemblyFields extends BaseSuite {

        public AssemblyFields() {
            super(MachineInstruction.FieldType.Assembly);
        }

        @Override
        protected void additionalFieldChecks(Field field, FieldLayoutPane.FieldLabel node) {
            Optional<Label> widthLabel = node.getWidthLabel();
            assertThat(widthLabel, isEmpty());
        }
    }

    public static class InstructionFields extends BaseSuite {

        public InstructionFields() {
            super(MachineInstruction.FieldType.Instruction);
        }

        @Override
        protected void additionalFieldChecks(Field field, FieldLayoutPane.FieldLabel node) {
            Optional<Label> widthLabel = node.getWidthLabel();
            assertThat(widthLabel, isPresent());

            verifyThat(widthLabel, hasValue(hasText(Integer.toString(field.getNumBits()))));
        }
    }



}