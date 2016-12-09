package cpusim.model.util;

import cpusim.model.Machine;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.junit.Test;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 *
 * @since 2016-12-09
 */
public class MoreBindingsTest {
    @Test
    public void flatMapValue() throws Exception {

        Machine machine = mock(Machine.class);
//
//        Register A = new Register("A", UUID.randomUUID(), null,
//                4, 0, Register.Access.readWrite());
//
//        Register B = new Register("B", UUID.randomUUID(), null,
//                4, 0, Register.Access.readWrite());

        RegisterArray arr1 = new RegisterArray("arr1", UUID.randomUUID(), machine,
                4, 4, 0, Register.Access.readWrite());

        assertEquals(4, arr1.getLength());

        ObservableList<RegisterArray> arrays = FXCollections.observableArrayList(arr1);

        ObservableList<? extends Register> fromArrays = MoreBindings.flatMapValue(arrays, RegisterArray::registersProperty);
        fromArrays.addListener(new ListChangeListener<Register>() {
            @Override
            public void onChanged(Change<? extends Register> c) {
                while (c.next()) {

                }
            }
        });

        arr1.setLength(8);
        assertEquals(8, arr1.getLength());
        assertEquals(8, fromArrays.size());
    }

}