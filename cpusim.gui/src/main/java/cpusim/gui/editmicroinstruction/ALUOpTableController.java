package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.gui.util.table.MachineObjectCellFactories;
import cpusim.model.microinstruction.Arithmetic;
import cpusim.model.microinstruction.ArithmeticLogicOperation;
import cpusim.model.module.Register;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

/**
 * Base class for {@link cpusim.model.microinstruction.ArithmeticLogicOperation} types.
 *
 * @since 2016-12-09
 */
abstract class ALUOpTableController<T extends ArithmeticLogicOperation<T>> extends MicroinstructionTableController<T> {
    @FXML
    @SuppressWarnings("unused")
    protected TableColumn<Arithmetic, Register> lhs;
    @FXML @SuppressWarnings("unused")
    protected TableColumn<Arithmetic, Register> rhs;
    @FXML @SuppressWarnings("unused")
    protected TableColumn<Arithmetic, Register> destination;

    ALUOpTableController(Mediator mediator, String fxmlFile, Class<T> clazz) {
        super(mediator, fxmlFile, clazz);
    }

    @Override
    void initialize() {
        super.initialize();

        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Callback<TableColumn<Arithmetic,Register>,TableCell<Arithmetic,Register>> cellRegFactory =
                MachineObjectCellFactories.modulesProperty(machineProperty(), Register.class);

        //Add for EdiCell of each field, in String or in Integer

        lhs.setCellValueFactory(new PropertyValueFactory<>("lhs"));
        lhs.setCellFactory(cellRegFactory);
        lhs.setOnEditCommit(text -> text.getRowValue().setLhs(text.getNewValue()));

        rhs.setCellValueFactory(new PropertyValueFactory<>("rhs"));
        rhs.setCellFactory(cellRegFactory);
        rhs.setOnEditCommit(text -> text.getRowValue().setRhs(text.getNewValue()));

        destination.setCellValueFactory(new PropertyValueFactory<>("destination"));
        destination.setCellFactory(cellRegFactory);
        destination.setOnEditCommit(text -> text.getRowValue().setDestination(text.getNewValue()));

    }
}
