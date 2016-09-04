import cpusim.model.Machine;
import cpusim.model.microinstruction.Increment;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.Register;
import org.junit.Assert;
import org.junit.Test;

public class IncrementTest {

    @Test
    public void test() {
        Machine machine = new Machine("test",true);
        Register reg = new Register("reg",4);
        Register statusReg = new Register("Status", 2);
        ConditionBit ofBit = new ConditionBit("ofBit", machine, statusReg, 0, false);
        ConditionBit cBit = new ConditionBit("cBit", machine, statusReg, 1, false);
        Increment incInstr = new Increment("Test", machine, reg, ofBit, cBit, 7L);

        incInstr.execute();
        Assert.assertEquals(reg.getValue(),7);
        Assert.assertEquals(cBit.isSet(), false);
        Assert.assertEquals(ofBit.isSet(), false);

        incInstr.execute();
        Assert.assertEquals(reg.getValue(),-2);
        Assert.assertEquals(cBit.isSet(), false);
        Assert.assertEquals(ofBit.isSet(), true);

        incInstr.execute();
        Assert.assertEquals(reg.getValue(),5);
        Assert.assertEquals(cBit.isSet(), true);
        Assert.assertEquals(ofBit.isSet(), false);

        reg.setWidth(6);
        incInstr.setDelta(31L);
        reg.setValue(0);

        incInstr.execute();
        Assert.assertEquals(reg.getValue(),31);
        Assert.assertEquals(cBit.isSet(), false);
        Assert.assertEquals(ofBit.isSet(), false);

        incInstr.execute();
        Assert.assertEquals(reg.getValue(),-2);
        Assert.assertEquals(cBit.isSet(), false);
        Assert.assertEquals(ofBit.isSet(), true);

        incInstr.execute();
        Assert.assertEquals(reg.getValue(),29);
        Assert.assertEquals(cBit.isSet(), true);
        Assert.assertEquals(ofBit.isSet(), false);

        reg.setWidth(6);
        incInstr.setDelta(31L);

    }
}
