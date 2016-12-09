package cpusim.model;

import cpusim.model.microinstruction.Increment;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.Register;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class IncrementTest {

    @Test
    public void test() {
        Machine machine = new Machine("test",true);
        Register reg = new Register("reg", UUID.randomUUID(), machine,
                4, 0, Register.Access.readWrite());
        Register statusReg = new Register("Status", UUID.randomUUID(), machine,
                3, 0, Register.Access.readWrite());

        ConditionBit ofBit = new ConditionBit("ofBit", UUID.randomUUID(), machine,
                statusReg, 0, false);
        ConditionBit cBit = new ConditionBit("cBit", UUID.randomUUID(), machine,
                statusReg, 1, false);
        ConditionBit zBit = new ConditionBit("zBit", UUID.randomUUID(), machine,
                statusReg, 2, false);

        Increment incInstr = new Increment("Test", UUID.randomUUID(), machine,
                reg, 7L, cBit, ofBit, zBit);

        incInstr.execute();
        Assert.assertEquals(reg.getValue(),7);
        Assert.assertEquals(cBit.isSet(), false);
        Assert.assertEquals(false, ofBit.isSet());
        Assert.assertEquals(false, zBit.isSet());

        incInstr.execute();
        Assert.assertEquals(-2, reg.getValue());
        Assert.assertEquals(false, cBit.isSet());
        Assert.assertEquals(true, ofBit.isSet());
        Assert.assertEquals(false, zBit.isSet());

        incInstr.execute();
        Assert.assertEquals(reg.getValue(),5);
        Assert.assertEquals(true, cBit.isSet());
        Assert.assertEquals(false, ofBit.isSet());
        Assert.assertEquals(false, zBit.isSet());

        reg.setWidth(6);
        reg.setValue(-1);
        incInstr.setDelta(1);

        incInstr.execute();
        Assert.assertEquals(0, reg.getValue());
        Assert.assertEquals(true, cBit.isSet());
        Assert.assertEquals(false, ofBit.isSet());
        Assert.assertEquals(true, zBit.isSet());

        incInstr.setDelta(31L);
        reg.setValue(0);

        incInstr.execute();
        Assert.assertEquals(31, reg.getValue());
        Assert.assertEquals(false, cBit.isSet());
        Assert.assertEquals(false, ofBit.isSet());
        Assert.assertEquals(false, zBit.isSet());

        incInstr.execute();
        Assert.assertEquals(-2, reg.getValue());
        Assert.assertEquals(false, cBit.isSet());
        Assert.assertEquals(true, ofBit.isSet());
        Assert.assertEquals(false, zBit.isSet());

        incInstr.execute();
        Assert.assertEquals(29, reg.getValue());
        Assert.assertEquals(true, cBit.isSet());
        Assert.assertEquals(false, ofBit.isSet());
        Assert.assertEquals(false, zBit.isSet());
    }
}
