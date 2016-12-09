package cpusim.model.microinstruction;

import cpusim.model.Machine;
import cpusim.model.module.Module;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.Register;
import cpusim.model.util.MachineComponent;
import cpusim.model.util.units.ArchValue;
import javafx.beans.property.*;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The increment microinstrucion adds an integer constant to the contents of a {@link Register}.
 *
 * @since 2013-06-06
 */
public class Increment extends Microinstruction<Increment> {

    @DependantComponent
    private final ObjectProperty<Register> register;

    @DependantComponent
    private final ObjectProperty<ConditionBit> carryBit;

    @DependantComponent
    private final ObjectProperty<ConditionBit> overflowBit;

    @DependantComponent
    private final ObjectProperty<ConditionBit> zeroBit;
    private final LongProperty delta;

    private final ReadOnlySetProperty<MachineComponent> dependencies;
    /**
     * Constructor
     * creates a new Increment object with input values.
     *  @param name name of the microinstruction.
     * @param id Unique ID for the machine
     * @param machine the machine that the microinstruction belongs to.
     * @param register the register whose value is to be incremented.
     * @param delta the integer value what will be added to the register contents.
     * @param overflowBit a condition bit.
     * @param zeroBit
     */
    public Increment(String name,
                     UUID id,
                     Machine machine,
                     Register register,
                     long delta,
                     ConditionBit carryBit, ConditionBit overflowBit, ConditionBit zeroBit) {
        super(name, id, machine);
        this.register = new SimpleObjectProperty<>(this, "register", register);
        this.carryBit = new SimpleObjectProperty<>(this, "carryBit", carryBit);
        this.overflowBit = new SimpleObjectProperty<>(this, "overflowBit", overflowBit);
        this.zeroBit = new SimpleObjectProperty<>(this, "zeroBit", zeroBit);
        this.delta = new SimpleLongProperty(this, "delta", delta);

        dependencies = MachineComponent.collectDependancies(this)
                .buildSet(this, "dependantComponents");
    }

    /**
     * Copy constructor
     * @param other instance to copy from
     */
    public Increment(Increment other) {
        this(other.getName(),
                UUID.randomUUID(),
                other.getMachine(),
                other.getRegister(),
                other.getDelta(),
                other.getCarryBit().orElse(null),
                other.getOverflowBit().orElse(null),
                null);
    }

    @Override
    public ReadOnlySetProperty<MachineComponent> getDependantComponents() {
        return dependencies;
    }

    /**
     * returns the register to be incremented.
     * @return the name of the register.
     */
    public Register getRegister(){
        return register.get();
    }

    /**
     * updates the register used by the microinstruction.
     * @param newRegister the new selected register for the set microinstruction.
     */
    public void setRegister(Register newRegister){
        register.set(newRegister);
    }

    public ObjectProperty<Register> registerProperty() {
        return register;
    }

    /**
     * returns the status of recording overflowBit.
     *
     * @return the status of recording overflowBit as a string.
     */
    public Optional<ConditionBit> getOverflowBit(){
        return Optional.ofNullable(overflowBit.get());
    }

    /**
     * updates the status of whether recording the overflow.
     *
     * @param newOverflowBit the new string for the status.
     */
    public void setOverflowBit(ConditionBit newOverflowBit){
        overflowBit.set(newOverflowBit);
    }

    public ObjectProperty<ConditionBit> overflowBitProperty() {
        return overflowBit;
    }

    /**
     * returns the status of recording carryBit.
     *
     * @return the status of recording carryBit as a string.
     */
    public Optional<ConditionBit> getCarryBit() { return Optional.ofNullable(carryBit.get()); }

    /**
     * updates the status of the carryBit.
     *
     * @param newCarryBit the new string for the status.
     */
    public void setCarryBit(ConditionBit newCarryBit) { carryBit.set(newCarryBit);}

    public ObjectProperty<ConditionBit> carryBitProperty() {
        return carryBit;
    }

    public Optional<ConditionBit> getZeroBit() {
        return Optional.ofNullable(zeroBit.get());
    }

    public ObjectProperty<ConditionBit> zeroBitProperty() {
        return zeroBit;
    }

    public void setZeroBit(ConditionBit zeroBit) {
        this.zeroBit.set(zeroBit);
    }

    /**
     * returns the fixed value stored in the set microinstruction.
     * @return the integer value of the field.
     */
    public long getDelta(){
        return delta.get();
    }

    /**
     * updates the fixed value stored in the set microinstruction.
     * @param newDelta the new value for the field.
     */
    public void setDelta(long newDelta){
        delta.set(newDelta);
    }

    public LongProperty deltaProperty() {
        return delta;
    }

    @Override
    public Increment cloneFor(IdentifierMap oldToNew) {
        return new Increment(getName(), UUID.randomUUID(), oldToNew.getNewMachine(),
                oldToNew.get(getRegister()), getDelta(),
                oldToNew.copyOrNull(getCarryBit()),
                oldToNew.copyOrNull(getOverflowBit()),
                oldToNew.copyOrNull(getZeroBit()));
    }

    @Override
    public <U extends Increment> void copyTo(U newIncr) {
        checkNotNull(newIncr);
        newIncr.setName(getName());
        newIncr.setRegister(getRegister());
        newIncr.setOverflowBit(getOverflowBit().orElse(null));
        newIncr.setCarryBit(getCarryBit().orElse(null));
        newIncr.setZeroBit(getZeroBit().orElse(null));
        newIncr.setDelta(getDelta());
    }

    /**
     * execute the micro instruction from machine
     */
    @Override
    public void execute()
    {
        BigInteger bigValue = BigInteger.valueOf(register.get().getValue());
        BigInteger bigDelta = BigInteger.valueOf(delta.get());
        BigInteger bigResult = bigValue.add(bigDelta);

        final int width = register.get().getWidth();

        getOverflowBit().ifPresent(overflowBit -> {
            //handle overflow
            BigInteger twoToWidthMinusOne = BigInteger.valueOf(2).pow(width - 1);
            boolean overflowSet = (bigResult.compareTo(twoToWidthMinusOne) >= 0 ||
                    bigResult.compareTo(twoToWidthMinusOne.negate()) < 0);
            overflowBit.set(overflowSet);
        });

        getCarryBit().ifPresent(carryBit -> {
            boolean carrySet = ((bigValue.intValue() < 0 && bigDelta.intValue() < 0) ||
                    (bigValue.intValue() < 0 && bigDelta.intValue() >= -bigValue.intValue()) ||
                    (bigDelta.intValue() < 0 && bigValue.intValue() >= -bigDelta.intValue()));
            carryBit.set(carrySet);
        });

        //set destination's value to the result
        long result = bigResult.longValue() & ArchValue.bits(width).mask();

        getZeroBit().ifPresent(zeroBit -> zeroBit.set(result == 0));

        register.get().setValue((result << (64 - width)) >> (64 - width));
    }

    /**
     * returns the XML description
     * @return the XML description
     */
    @Override
    public String getXMLDescription(String indent){
        StringBuilder xml = new StringBuilder();

        xml.append(indent);
        xml.append("<Increment name=\"");
        xml.append(getHTMLName());
        xml.append("\" register=\"");
        xml.append(getRegister().getID());
        xml.append("\" delta=\"");
        xml.append(getDelta());
        xml.append("\" id=\"");
        xml.append(getID());
        xml.append("\" ");

        getCarryBit().ifPresent(carryBit -> {
            xml.append("carryBit=\"");
            xml.append(carryBit.getID());
            xml.append("\" ");
        });

        getOverflowBit().ifPresent(overflowBit -> {
            xml.append("overflowBit=\"");
            xml.append(overflowBit.getID());
            xml.append("\" ");
        });

        getZeroBit().ifPresent(zeroBit -> {
            xml.append("zeroBit=\"");
            xml.append(zeroBit.getID());
            xml.append("\" ");
        });

        xml.append(" />");
        return xml.toString();
    }

    /**
     * returns the HTML description
     * @return the HTML description
     */
    @Override
    public String getHTMLDescription(String indent) {
        StringBuilder html = new StringBuilder();
        html.append(indent);
        html.append("<TR><TD>");
        html.append(getHTMLName());
        html.append("</TD><TD>");
        html.append(getRegister().getHTMLName());
        html.append("</TD>");

        getCarryBit().ifPresent(carryBit -> {
            html.append("<TD>");
            html.append(carryBit.getHTMLName());
            html.append("</TD>");
        });

        getOverflowBit().ifPresent(overflowBit -> {
            html.append("<TD>");
            html.append(overflowBit.getHTMLName());
            html.append("</TD>");
        });

        getZeroBit().ifPresent(zeroBit -> {
            html.append("<TD>");
            html.append(zeroBit.getHTMLName());
            html.append("</TD>");
        });

        html.append("</TR>");

        return html.toString();
    }

    /**
     * returns true if this microinstruction uses m
     * (so if m is modified, this micro may need to be modified.
     * @param m the module that holds the microinstruction
     * @return boolean value true if this micro used the module
     */
    @Override
    public boolean uses(Module<?> m){
        return (m == register.get() ||
                m == overflowBit.get() ||
                m == carryBit.get() ||
                m == zeroBit.get());
    }
}
