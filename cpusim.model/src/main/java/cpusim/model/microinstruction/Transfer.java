package cpusim.model.microinstruction;

import cpusim.model.Machine;
import cpusim.model.module.Module;
import cpusim.model.module.Register;
import cpusim.model.module.Sized;
import cpusim.model.util.MachineComponent;
import cpusim.model.util.ValidationException;
import javafx.beans.property.*;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Denotes common behavior for a {@link cpusim.model.module.Register} transfer
 *
 * @since 2016-11-14
 */
public abstract class Transfer<From extends Module<From> & Sized<From>, To extends Module<To> & Sized<To>,
        Sub extends Transfer<From, To, Sub>>
        extends Microinstruction<Sub> {

    @DependantComponent
    protected final ObjectProperty<From> source;
    protected final IntegerProperty srcStartBit;

    @DependantComponent
    protected final ObjectProperty<To> dest;
    protected final IntegerProperty destStartBit;
    protected final IntegerProperty numBits;

    protected ReadOnlySetProperty<MachineComponent> dependencies;

    /**
     * Constructor
     * creates a new Transfer object with input values.
     *
     * @param name name of the microinstruction.
     * @param machine the machine that the microinstruction belongs to.
     * @param source the register whose value is to be tested.
     * @param srcStartBit an integer indicting the leftmost or rightmost bit to be transfered.
     * @param dest the destination register.
     * @param destStartBit an integer indicting the leftmost or rightmost bit to be changed.
     * @param numBits a non-negative integer indicating the number of bits to be tested.
     */
    public Transfer(String name,
                    UUID id,
                    Machine machine,
                    From source,
                    int srcStartBit,
                    To dest,
                    int destStartBit,
                    int numBits){
        super(name, id, machine);
        this.source = new SimpleObjectProperty<>(this, "source", source);
        this.srcStartBit = new SimpleIntegerProperty(this, "srcStartBit", srcStartBit);
        this.dest = new SimpleObjectProperty<>(this, "dest", dest);
        this.destStartBit = new SimpleIntegerProperty(this, "destStartBit", destStartBit);
        this.numBits = new SimpleIntegerProperty(this, "numBits", numBits);
    }
    
    /**
     * Copy constructor
     * @param other Copied instance
     */
    public Transfer(final Transfer<From, To, Sub> other) {
        this(checkNotNull(other).getName(),
                UUID.randomUUID(),
                other.getMachine(),
                other.getSource().orElse(null),
                other.getSrcStartBit(),
                other.getDest().orElse(null),
                other.getDestStartBit(),
                other.getNumBits());
    }

    @Override
    public ReadOnlySetProperty<MachineComponent> getDependantComponents() {
        return this.dependencies;
    }

    /**
     * returns the name of the set microinstruction as a string.
     *
     * @return the name of the set microinstruction.
     */
    public final Optional<From> getSource(){
        return Optional.ofNullable(source.get());
    }
    
    /**
     * updates the register used by the microinstruction.
     *
     * @param newSource the new selected register for the set microinstruction.
     */
    public final void setSource(@Nullable From newSource){
        source.set(newSource);
    }

    public ObjectProperty<From> sourceProperty() {
        return source;
    }

    /**
     * returns the index of the start bit of the microinstruction.
     *
     * @return the integer value of the index.
     */
    public final int getSrcStartBit(){
        return srcStartBit.get();
    }
    
    /**
     * updates the index of the start bit of the microinstruction.
     *
     * @param newSrcStartBit the new index of the start bit for the set microinstruction.
     */
    public final void setSrcStartBit(int newSrcStartBit){
        srcStartBit.set(newSrcStartBit);
    }

    public IntegerProperty srcStartBitProperty() {
        return srcStartBit;
    }

    /**
     * returns destination memory
     *
     * @return Destination memory, {@link Optional#empty()} if not set
     */
    public final Optional<To> getDest(){
        return Optional.ofNullable(dest.get());
    }
    
    /**
     * updates the register used by the microinstruction.
     *
     * @param newDest the new selected register for the set microinstruction.
     */
    public final void setDest(To newDest){
        dest.set(newDest);
    }

    public ObjectProperty<To> destProperty() {
        return dest;
    }

    /**
     * returns the index of the start bit of the microinstruction.
     *
     * @return the integer value of the index.
     */
    public final int getDestStartBit(){
        return destStartBit.get();
    }
    
    /**
     * updates the index of the start bit of the microinstruction.
     *
     * @param newDestStartBit the new index of the start bit for the set microinstruction.
     */
    public final void setDestStartBit(int newDestStartBit){
        destStartBit.set(newDestStartBit);
    }

    public IntegerProperty destStartBitProperty() {
        return destStartBit;
    }

    /**
     * returns the number of bits of the value.
     *
     * @return the integer value of the number of bits.
     */
    public final int getNumBits(){
        return numBits.get();
    }
    
    /**
     * updates the number of bits of the value.
     *
     * @param newNumbits the new value of the number of bits.
     */
    public final void setNumBits(int newNumbits){
        numBits.set(newNumbits);
    }

    public IntegerProperty numBitsProperty() {
        return numBits;
    }

    /**
     * returns true if this microinstruction uses m
     * (so if m is modified, this micro may need to be modified.
     * @param m the module that holds the microinstruction
     * @return boolean value true if this micro used the module
     */
    @Override
    public boolean uses(Module<?> m){
        return (m == source.get() || m == dest.get());
    }

    @Override
    public <U extends Sub> void copyTo(final U other) {
        checkNotNull(other);

        other.setName(getName());
        other.setSource(getSource().orElse(null));
        other.setSrcStartBit(getSrcStartBit());
        other.setDest(getDest().orElse(null));
        other.setDestStartBit(getDestStartBit());
        other.setNumBits(getNumBits());
    }
    
    @Override
    public void validate() {
        super.validate();

        String boundPhrase = "";
    
        int srcStartBit = getSrcStartBit();
        int destStartBit = getDestStartBit();
        int numBits = getNumBits();
    
        if (srcStartBit < 0 || destStartBit < 0 || numBits < 0) {
            throw new ValidationException("You have a negative value for one of the " +
                    "start bits or the number of bits\nin the " +
                    "microinstruction \"" + getName() + "\".");
        }

        if (!getSource().isPresent()) {
            throw new ValidationException("No source module set");
        }

        if (!getDest().isPresent()) {
            throw new ValidationException("No destination module set");
        }

        From source = getSource().get();
        To dest = getDest().get();

        if (srcStartBit > source.getWidth()) {
            boundPhrase = "srcStartBit";
        } else if (destStartBit > dest.getWidth()) {
            boundPhrase = "destStartBit";
        }
    
        if (!boundPhrase.isEmpty()) {
            throw new ValidationException(boundPhrase + " has an invalid value for the " +
                    "specified register in instruction " + getName() +
                    ".\nIt must be non-negative, and less than the " +
                    "register's length.");
        }
    
        if (srcStartBit + numBits > source.getWidth() ||
                destStartBit + numBits > dest.getWidth()) {
            throw new ValidationException("The number of bits being transferred is " +
                    "too large to fit in the source register or the " +
                    "destination array.\n" +
                    "Please specify a new start bit or a smaller number " +
                    "of bits to copy in the microinstruction \"" +
                    getName() + ".\"");
        }
    }

    protected StringBuilder getXMLDescriptionBase(StringBuilder bld) {
        checkNotNull(bld, "builder == null");

        bld.append(" name=\"")
                .append(getHTMLName())
                .append("\"");

        bld.append("\" id=\"")
                .append(getID())
                .append("\"");

        bld.append(" source=\"");
        getSource().ifPresent(source -> bld.append(source.getID()));
        bld.append("\"");

        bld.append(" srcStartBit=\"")
                .append(getSrcStartBit())
                .append("\"");

        bld.append(" dest=\"");
        getDest().ifPresent(dest -> bld.append(dest.getID()));
        bld.append("\"");

        bld.append(" destStartBit=\"")
                .append(getDestStartBit())
                .append("\"");

        bld.append(" numBits=\"")
                .append(getNumBits())
                .append("\"");

        return bld;
    }

    protected StringBuilder getHTMLDescriptionBase(StringBuilder bld,
                                                   @Nullable Consumer<StringBuilder> moreProps) {
        bld.append("<TR>");

        bld.append("<TD>")
            .append(getHTMLName())
            .append("</TD>");

        bld.append("<TD>");
        getSource().ifPresent(source -> bld.append(source.getHTMLName()));
        bld.append("</TD>");

        bld.append("<TD>")
                .append(getSrcStartBit())
                .append("</TD>");

        bld.append("<TD>");
        getDest().ifPresent(dest -> bld.append(dest.getHTMLName()));
        bld.append("</TD>");

        bld.append("<TD>")
                .append(getDestStartBit())
                .append("</TD>");

        bld.append("<TD>")
                .append(getNumBits())
                .append("</TD>");

        if (moreProps != null) {
            moreProps.accept(bld);
        }

        bld.append("</TR>");

        return bld;
    }

    /**
     * Helper function for use with index {@link Register} when using a
     * {@link cpusim.model.module.RegisterArray}
     *
     * @param register Get the {@link Optional} index register
     * @param startBit Get the starting bit
     * @param numBits get the width of the value
     * @return Consumer that will put the HTML output into the {@link StringBuilder}
     */
    Consumer<StringBuilder> getHTMLIndexRegister(Supplier<Optional<Register>> register,
                                                 Supplier<Integer> startBit,
                                                 Supplier<Integer> numBits) {
        return bld -> {
            bld.append("<TD>");
            register.get().ifPresent(index -> bld.append(index.getHTMLName()));
            bld.append("</TD>");

            bld.append("<TD>")
                    .append(startBit.get())
                    .append("</TD>");

            bld.append("<TD>")
                    .append(numBits.get())
                    .append("</TD>");
        };
    }
}
