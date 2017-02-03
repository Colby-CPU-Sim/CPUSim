package cpusim.gui.editmachineinstruction;

import cpusim.gui.harness.FXHarness;
import cpusim.gui.harness.FXRunner;
import cpusim.model.Field;
import cpusim.model.MachineInstruction;
import cpusim.model.harness.MachineInjectionRule;
import cpusim.model.harness.SamplesFixture;
import cpusim.util.MoreIteratables;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.fxmisc.easybind.EasyBind;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.List;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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

    public static class BaseSuite extends FXHarness {


        protected FieldLayoutPane underTest;

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
            underTest = new FieldLayoutPane(fieldType);

            Scene scene = new Scene(underTest, WIDTH, 40);
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

        protected void verifyFieldsShowing() {
            List<Node> children = underTest.getChildren();
            assertEquals(instructionFields.size(), children.size());

            double bitWidth = 0;
            for (Field f : instructionFields) {
                bitWidth += f.getNumBits();
            }

            final double widthPerBit = WIDTH / bitWidth;
            MoreIteratables.zip(instructionFields, children, (field, node) -> {
                assertThat(node, instanceOf(FieldLayoutPane.FieldLabel.class));

                FieldLayoutPane.FieldLabel label = (FieldLayoutPane.FieldLabel)node;
                assertEquals(field, label.getField());

                assertEquals(Math.round(widthPerBit * field.getNumBits()), label.getWidth(), 1.0);

                // check the values now:
                assertThat(label.getText(), is(field.getName()));
            });
        }

        @Test
        public void base() {
            verifyFieldsShowing();
        }

        @Test
        public void changeInstruction() {
            Field toAdd = getMachine().getFields().stream()
                    .filter(f -> !instructionFields.contains(f))
                    .findFirst().orElseThrow(NullPointerException::new);

            interact(() -> instructionFields.add(toAdd));

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

    }

    public static class AssemblyFields extends BaseSuite {

        public AssemblyFields() {
            super(MachineInstruction.FieldType.Assembly);
        }
    }

    public static class InstructionFields extends BaseSuite {

        public InstructionFields() {
            super(MachineInstruction.FieldType.Instruction);
        }
    }



}