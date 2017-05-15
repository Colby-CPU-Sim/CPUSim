package cpusim.model.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import cpusim.model.Field;
import cpusim.model.Machine;
import cpusim.model.MachineCloner;
import cpusim.model.MachineInstruction;
import cpusim.model.assembler.EQU;
import cpusim.model.microinstruction.Microinstruction;
import cpusim.model.module.*;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates the necessary contents to build out a machine.
 */
public abstract class MachineBuilder<T, S extends MachineBuilder<T, S>> {

    // Other properties
    protected boolean isIndexedFromRight = true;
    protected int startingAddressForLoading = 0;

    // Modules
    protected final Multimap<Class<? extends Module<?>>, Module<?>> modules;
    protected ControlUnit controlUnit = null;
    protected RAM codeStore = null;
    protected Register programCounter = null;
    
    // Instructions:
    protected final List<MachineInstruction> instructions;
    protected final List<EQU> equs;
    
    protected MachineInstruction fetchSequence = null;
    
    // Micros
    protected final Multimap<Class<? extends Microinstruction<?>>, Microinstruction<?>> micros;
    protected final List<Field> fields;
    
    protected final Machine machine;
    
    public MachineBuilder(String machineName) {
        modules = HashMultimap.create();
        micros = HashMultimap.create();
        instructions = new ArrayList<>();
        equs = new ArrayList<>();
        fields = new ArrayList<>();
        
        this.machine = new Machine(machineName);
    }

    @ParametersAreNonnullByDefault
    private static <T extends Copyable<? extends T> & MachineBound>
    Collection<T> cloneCollection(Machine machine, Collection<T> toClone) {
        List<T> out = new ArrayList<>(toClone.size());
        
        for (T t : toClone) {
            @SuppressWarnings("unchecked")
            T c = (T)t.cloneOf();
            c.machineProperty().setValue(machine);
            out.add(c);
        }
        
        return out;
    }
    
    protected MachineBuilder(@Nonnull S other) {
        checkNotNull(other, "null base passed");

        this.machine = MachineCloner.cloneMachine(other.machine);

        this.isIndexedFromRight = other.isIndexedFromRight;
    
        // Modules
        this.modules = HashMultimap.create();
        for (Class<? extends Module<?>> clazz: other.modules.keySet()) {
        
            Collection<? extends Module<?>> toClone = other.modules.get(clazz);
        
            Collection<Module<?>> cloned = cloneCollection(this.machine, (Collection<Module<?>>)toClone);
            this.modules.putAll(clazz, cloned);
        }
        
        if (other.controlUnit != null) {
            this.controlUnit = new ControlUnit(other.controlUnit.getName(), UUID.randomUUID(), this.machine);
            other.controlUnit.copyTo(this.controlUnit);
        }
    
        if (other.codeStore != null) {
            this.codeStore = new RAM(other.codeStore.getName(), UUID.randomUUID(), this.machine,
                    other.codeStore.getLength(), other.codeStore.getCellSize());
            other.codeStore.copyTo(this.codeStore);
        }
        this.startingAddressForLoading = other.startingAddressForLoading;
        
        if (other.programCounter != null) {
    
            this.programCounter = new Register(other.programCounter.getName(), UUID.randomUUID(), this.machine,
                    other.programCounter.getWidth(),
                    other.programCounter.getInitialValue(),
                    other.programCounter.getAccess());
            other.programCounter.copyTo(this.programCounter);
        }
        
        // Instructions
        this.instructions = new ArrayList<>(other.instructions.size());
        for (MachineInstruction ins: other.instructions) {
            MachineInstruction c = ins.cloneOf();
            c.machineProperty().set(this.machine);
            this.instructions.add(c);
        }
        
        if (other.fetchSequence != null) {
            this.fetchSequence = other.fetchSequence.cloneOf();
            this.fetchSequence.machineProperty().set(this.machine);
        }
        
        this.equs = new ArrayList<>(other.equs);
        
        // Micros
        this.micros = HashMultimap.create();
        for (Class<? extends Microinstruction<?>> clazz: other.micros.keySet()) {
            
            Collection<? extends Microinstruction<?>> toClone = other.micros.get(clazz);
            
            Collection<Microinstruction<?>> cloned = cloneCollection(this.machine, (Collection<Microinstruction<?>>)toClone);
            this.micros.putAll(clazz, cloned);
        }
        
        this.fields = new ArrayList<>(cloneCollection(this.machine, other.fields));
    }

    @CheckReturnValue
    protected abstract S copyOf();
    
    /**
     * Gets the machine for all components.
     *
     * @return
     */
    public Machine getMachine() {
        return machine;
    }


    /**
     * Set the {@link Machine#indexFromRightProperty()}
     * @param isIndexedFromRight {@code true} if indexing from the right
     * @return New builder with the property adjusted
     *
     * @see Machine#indexFromRightProperty()
     */
    public S withIndexedFromRight(boolean isIndexedFromRight) {
        S out = copyOf();
        out.isIndexedFromRight = isIndexedFromRight;
        return out;
    }

    public S withControlUnit(ControlUnit unit) {
        S out = copyOf();
        out.controlUnit = unit;
        return out;
    }
    
    public S withCodeStore(RAM codeStore) {
        S out = copyOf();
        out.codeStore = codeStore;
        return out;
    }

    /**
     * Set the {@link Machine#startingAddressForLoadingProperty()}
     * @param startingAddressForLoading value to start addressing at
     * @return New builder with the property adjusted
     *
     * @see Machine#startingAddressForLoadingProperty()
     */
    public S withStartingAddressForLoading(int startingAddressForLoading) {
        S out = copyOf();
        out.startingAddressForLoading = startingAddressForLoading;
        return out;
    }
    
    public S withProgramCounter(Register pc) {
        S out = copyOf();
        out.programCounter = pc;
        return out;
    }

    /**
     * Add the EQUs to the new machine
     * @param equs
     * @return
     */
    public S withEQU(EQU... equs) {
        return withEQU(Arrays.asList(equs));
    }

    public S withEQU(Collection<? extends EQU> equs) {
        S out = copyOf();
        out.equs.addAll(equs);
        return out;
    }

    @ParametersAreNonnullByDefault
    public S withMachineInstruction(Collection<? extends MachineInstruction> instructions) {
        S out = copyOf();
        out.instructions.addAll(instructions);
        return out;
    }

    public S withMachineInstruction(MachineInstruction... instructions) {
        return withMachineInstruction(Arrays.asList(instructions));
    }

    /**
     * Set the {@link Machine#fetchSequenceProperty()} of the {@link Machine}.
     *
     * @param fetchSequence Value for the fetch sequence
     * @return Modified builder
     */
    public S withFetchSequence(MachineInstruction fetchSequence) {
        S out = copyOf();
        out.fetchSequence = fetchSequence;
        return out;
    }

    @ParametersAreNonnullByDefault
    public S withField(Collection<? extends Field> fields) {
        S out = copyOf();
        out.fields.addAll(fields);
        return out;
    }

    @ParametersAreNonnullByDefault
    public S withField(Field... fields) {
        return withField(Arrays.asList(fields));
    }

    @SuppressWarnings({"varargs", "unchecked"})
    @ParametersAreNonnullByDefault
    public <U extends Module<U>> S withModule(Class<U> clazz, U... modules) {
        return this.withModule(clazz, Arrays.asList(modules));
    }
    
    @SuppressWarnings("unchecked")
    @ParametersAreNonnullByDefault
    public <U extends Module<U>> S withModule(Class<U> clazz, Collection<? extends U> modules) {
        S out = copyOf();
        Collection<U> stored = (Collection<U>) out.modules.get(clazz);
        stored.addAll(modules);
        return out;
    }
    
    @SuppressWarnings("unchecked")
    @ParametersAreNonnullByDefault
    protected <U extends Module<U>> Collection<U> getModules(Class<U> clazz) {
        return (Collection<U>)modules.get(clazz);
    }

    
    @SuppressWarnings({"varargs", "unchecked"})
    @ParametersAreNonnullByDefault
    public <U extends Microinstruction<U>> S withMicros(Class<U> clazz, U... micros) {
        return this.withMicros(clazz, Arrays.asList(micros));
    }
    
    @SuppressWarnings("unchecked")
    @ParametersAreNonnullByDefault
    public <U extends Microinstruction<U>> S withMicros(Class<U> clazz, Collection<? extends U> micros) {
        S out = copyOf();
        Collection<U> stored = (Collection<U>) out.micros.get(clazz);
        stored.addAll(micros);
        return out;
    }
    
    @SuppressWarnings("unchecked")
    @ParametersAreNonnullByDefault
    protected <U extends Microinstruction<U>> Collection<U> getMicros(Class<U> clazz) {
        return (Collection<U>)micros.get(clazz);
    }
    
    /**
     * Uses the internal arguments and creates a new machine.
     * @return
     */
    @SuppressWarnings("unchecked") @CheckReturnValue
    public abstract T build();

    /**
     * Build a machine from a {@link MachineBuilder} instance. This method is provided as a
     * utility.
     * @param bld
     *
     * @return New machine instance
     */
    public static Machine buildMachine(@Nonnull MachineBuilder<Machine, ?> bld) {
        checkNotNull(bld, "builder == null");
        Machine out = MachineCloner.cloneMachine(bld.machine);

        // Hardware
        out.setProgramCounter(bld.programCounter);
        out.setCodeStore(bld.codeStore);
        out.setStartingAddressForLoading(bld.startingAddressForLoading);
        out.setControlUnit(bld.controlUnit);

        out.setRAMs(bld.getModules(RAM.class));
        out.setRegisterArrays(bld.getModules(RegisterArray.class));
        out.setRegisters(bld.getModules(Register.class));
        out.setConditionBits(bld.getModules(ConditionBit.class));

        out.setIndexFromRight(bld.isIndexedFromRight);

        // Software

        out.setInstructions(bld.instructions);
        out.setFetchSequence(bld.fetchSequence);

        out.setEQUs(bld.equs);

        for (Class<? extends Microinstruction<?>> mclazz : bld.micros.keySet()) {
            List<Microinstruction<?>> toSet = out.getMicrosUnchecked(mclazz);
            toSet.clear();
            toSet.addAll(bld.micros.get(mclazz));
        }

        out.setFields(bld.fields);

        return out;
    }

    /**
     * Used to create a {@link Machine} from components
     */
    public static final class Default extends MachineBuilder<Machine, Default> {
        
        public Default(final String machineName) {
            super(machineName);
        }
        
        protected Default(Default other) {
            super(other);
        }
    
        @Override
        protected Default copyOf() {
            return new Default(this);
        }
    
        /**
         * Uses the internal arguments and creates a new machine.
         * @return new Machine
         *
         * @see MachineBuilder#buildMachine(MachineBuilder)
         */
        @Override
        public Machine build() {
            return MachineBuilder.buildMachine(this);
        }
    }
    
}
