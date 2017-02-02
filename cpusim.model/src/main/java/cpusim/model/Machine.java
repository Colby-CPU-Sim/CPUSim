package cpusim.model;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import cpusim.model.assembler.EQU;
import cpusim.model.assembler.PunctChar;
import cpusim.model.assembler.PunctChar.Use;
import cpusim.model.iochannel.FileChannel;
import cpusim.model.iochannel.IOChannel;
import cpusim.model.iochannel.StreamChannel;
import cpusim.model.microinstruction.*;
import cpusim.model.module.*;
import cpusim.model.util.*;
import cpusim.util.Gullectors;
import cpusim.util.MoreBindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This file contains the class for Machines created using CPUSim
 *
 * @since 2013-10-01
 */
public class Machine extends Module<Machine> {

    /**
     * constants for different running modes
     */
    public enum RunModes {
        RUN,
        STEP_BY_MICRO,
        STEP_BY_INSTR,
        RUN_AND_FIRE_CYCLES,
        STOP,
        ABORT,
        COMMAND_LINE
    }

    /**
     * constants for the current state of the Machine.
     */
    public enum State {
        NEVER_RUN, //initial state when machine is first loaded
        START_OF_EXECUTE_THREAD, //called once at the start of execution
        EXCEPTION_THROWN,
        EXECUTION_ABORTED,
        EXECUTION_HALTED,
        START_OF_MACHINE_CYCLE,
        START_OF_MICROINSTRUCTION,
        BREAK,
        HALTED_STEP_BY_MICRO
    }

    /**
     * StateWrapper class wraps a State enum and an Object value so that listeners will
     * register a change when the state is set, even if the enum is the same so
     * that changed method is always called by listeners
     */
    public class StateWrapper {

        /**
         * state is the enum State of the Machine
         */
        State state;

        /**
         * value is any additional information needed by listeners who are
         * listening to state changes.
         */
        Object value;

        /**
         * constructor initializes field state and value to parameter newState and
         * newValue
         *
         * @param newState new enum State of Machine
         * @param newValue any additional information needed by listeners who are
         *                 listening to state changes.
         */
        StateWrapper(State newState, Object newValue) {
            this.state = newState;
            this.value = newValue;
        }

        /**
         * returns the Machine of this State
         *
         * @return the Machine of this State
         */
        public Machine getMachine() {
            return Machine.this;
        }

        /**
         * getter for the state field
         *
         * @return the State of the Machine
         */
        public State getState() {
            return this.state;
        }

        /**
         * getter for the value field
         *
         * @return the value of the state
         */
        public Object getValue() {
            return this.value;
        }

        /**
         * setter for the state field
         *
         * @param newState the new State of the Machine
         */
        public void setState(State newState) {
            this.state = newState;
        }

        /**
         * setter for the value field
         *
         * @param newValue the new value of the state
         */
        public void setValue(Object newValue) {
            this.value = newValue;
        }

        /**
         * @return a string displaying the state and the value
         */
        public String toString() {
            return this.state + ":" + this.value;
        }
    }


    /**
     * Get all of the supported implementing {@link Class} values for the {@link Microinstruction}.
     * @return
     */
    public static ImmutableSet<Class<? extends Microinstruction<?>>> getMicroClasses() {
        ImmutableSet.Builder<Class<? extends Microinstruction<?>>> bld = ImmutableSet.builder();
        bld.add(Arithmetic.class);
        bld.add(Branch.class);
        bld.add(Decode.class);
        bld.add(End.class);
        bld.add(Comment.class);
        bld.add(Increment.class);
        bld.add(IO.class);
        bld.add(Logical.class);
        bld.add(MemoryAccess.class);
        bld.add(SetBits.class);
        bld.add(SetCondBit.class);
        bld.add(Shift.class);
        bld.add(Test.class);
        bld.add(TransferRtoR.class);
        bld.add(TransferRtoA.class);
        bld.add(TransferAtoR.class);
        return bld.build();
    }
    
    /**
     * Get all of the supported implementing {@link Class} values for {@link Module} instances.
     * @return
     */
    public static ImmutableSet<Class<? extends Module<?>>> getModuleClasses() {
        ImmutableSet.Builder<Class<? extends Module<?>>> bld = ImmutableSet.builder();
        bld.add(Register.class);
        bld.add(RegisterArray.class);
        bld.add(RAM.class);
        bld.add(ConditionBit.class);
        return bld.build();
    }

    private Logger logger = LogManager.getLogger(Machine.class);

    private final ReadOnlyMapProperty<UUID, MachineComponent> components;

    private final ReadOnlySetProperty<MachineComponent> children;

    /**
     * the hardware modules making up the machine
     */
    @ChildComponent
    private final ListProperty<RAM> rams;

    @ChildComponent
    private final ListProperty<Register> registers;

    @ChildComponent
    private final ListProperty<RegisterArray> registerArrays;

    @ChildComponent
    private final ListProperty<ConditionBit> conditionBits;

    /**
     * Property of all of the registers internal to the {@link Machine}.
     */
    private final ReadOnlyListProperty<Register> allRegisters;

    // Machine instructions
    @ChildComponent
    private final ListProperty<MachineInstruction> instructions;
    // EQUs
    private ListProperty<EQU> EQUs;

    // key = micro name, value = list of microinstructions
    @ChildComponent
    private final Map<Class<? extends Microinstruction<?>>, ListProperty<? extends Microinstruction<?>>> microMap;

    // key = module type, value = list of modules
    @ChildComponent
    private final Map<Class<? extends Module<?>>, ListProperty<? extends Module<?>>> moduleMap;

    // Control unit for keeping track of index of micro within machine instruction
    @ChildComponent
    private final ObjectProperty<ControlUnit> controlUnit;

    // The machine's fetch sequence
    @ChildComponent
    private final ObjectProperty<MachineInstruction> fetchSequence;

    // Fields of the machine instructions
    @ChildComponent
    private final ListProperty<Field> fields;

    // RAM where the code store resides
    @ChildComponent
    private final ObjectProperty<RAM> codeStore;

    // Register used for stopping at break points--initially null
    @ChildComponent
    private final ObjectProperty<Register> programCounter;

    // True if bit indexing starts of the right side, false if on the left
    private BooleanProperty indexFromRight;

    // Address to start when loading RAM with a program
    private IntegerProperty startingAddressForLoading;

    // Array of PunctChars.
    private final ListProperty<PunctChar> punctChars;

    // Can be: RUN, STEP_BY_MICRO, STEP_BY_INSTR, STOP, ABORT, COMMAND_LINE
    // It is volatile to ensure that all threads can read it at any time
    private volatile RunModes runMode = RunModes.ABORT;

    /**
     * the current state of the machine, defined by enum state
     * and associated Object value
     */
    private SimpleObjectProperty<StateWrapper> wrappedState;

    // true if the machine just halted due to a breakpoint.  It is used to turn off the
    // break point temporarily to allow continuing past the breakpoint.
    private boolean justBroke;

    /**
     * Creates a new machine.
     *
     * @param name - The name of the machine.
     */
    public Machine(String name) {
        super(name, IdentifiedObject.generateRandomID(), null);

        rams = new SimpleListProperty<>(this, "rams",
                FXCollections.observableArrayList());
        registers = new SimpleListProperty<>(this, "registers",
                FXCollections.observableArrayList());
        registerArrays = new SimpleListProperty<>(this, "registerArray",
                FXCollections.observableArrayList());
        conditionBits = new SimpleListProperty<>(this, "conditionBits",
                FXCollections.observableArrayList());
        
        // We bind the "allRegisters" array to listen to the changes to the registers and registerArray lists,
        // this way when they change, the allRegisters array is always up to date!


        wrappedState = new SimpleObjectProperty<>(this, "machine state", new
                StateWrapper(State.NEVER_RUN, ""));

        moduleMap = new HashMap<>();
        microMap = new HashMap<>();

        instructions = new SimpleListProperty<>(this, "instructions", FXCollections.observableArrayList());
        EQUs = new SimpleListProperty<>(this, "equs", FXCollections.observableArrayList());
        fields = new SimpleListProperty<>(this, "fields", FXCollections.observableArrayList());
        punctChars = new SimpleListProperty<>(this, "punctChars", FXCollections.observableArrayList());
        punctChars.addAll(getDefaultPunctChars());

        fetchSequence = new SimpleObjectProperty<>(this, "fetchSequence",
                new MachineInstruction("Fetch sequence", UUID.randomUUID(), this,
                        0, null, null));
        controlUnit = new SimpleObjectProperty<>(this, "controlUnit",
                new ControlUnit("ControlUnit", UUID.randomUUID(), this));

        startingAddressForLoading = new SimpleIntegerProperty(0);
        programCounter = new SimpleObjectProperty<>(null);
        codeStore = new SimpleObjectProperty<>(null);
        indexFromRight = new SimpleBooleanProperty(true); //conventional indexing order

        justBroke = false;
        initializeModuleMap();
        initializeMicroMap();

        ObservableCollectionBuilder<MachineComponent> childrenBuilder = MachineComponent.collectChildren(this);

        this.components = childrenBuilder.buildMap(this, "components");
        this.children = childrenBuilder.buildSet(this, "children");

        ObservableList<Register> allRegisters =
                MoreBindings.concat(
                        FXCollections.observableArrayList(registers,
                                MoreBindings.flatMapValue(registerArrays, RegisterArray::getRegisters)));
        this.allRegisters = new SimpleListProperty<>(this, "allRegisters", allRegisters);
    }

    /**
     * This constructor exists for backwards compatibility. The default
     * on old existing machines was to index from the left, where the default on new
     * machines
     * is to index from the right
     *
     * @param name          machines name
     * @param indexFromLeft if true then index from
     *                      the left rather than the right side of the registers
     */
    public Machine(String name, boolean indexFromLeft) {
        this(name);
        indexFromRight.set(!indexFromLeft);
    }

    @Override
    public ReadOnlySetProperty<MachineComponent> getChildrenComponents() {
        return this.children;
    }

    @Override
    public ReadOnlySetProperty<MachineComponent> getDependantComponents() {
        // FIXME
        throw new UnsupportedOperationException("unimplemented");
    }

    /**
     * Returns the wrapped state of the machine.
     *
     * @return - the wrapped state of the machine.
     */
    private StateWrapper getStateWrapper() {
        return wrappedState.get();
    }

    /**
     * Sets the state of the machine, resulting in a new StateWrapper object
     * that wraps the newState parameter and associates the newValue parameter.
     *
     * @param newState - The new state's name.
     * @param newValue - The new state's value.
     */
    public void setState(State newState, Object newValue) {
        StateWrapper stateWrapper = new StateWrapper(newState, newValue);
        this.wrappedState.set(stateWrapper);
    }

    /**
     * Gives the state Simple Object Property.
     *
     * @return - the state Simple Object Property.
     */
    public ObjectProperty<StateWrapper> stateProperty() {
        return this.wrappedState;
    }

    /**
     * for the punctuation characters:  !@$%^&*()_={}[]|\:;,.?/~#-+`
     * creates the default uses.
     * The other four punctuation characters: <>"'
     *    already have special meanings and cannot be redefined.
     *    <>"' are all quoting characters.
     * The punctuation characters + and - can initiate numbers but
     *    otherwise they can be symbol chars or
     *    individual tokens or illegal.
     */
    private static final ImmutableList<PunctChar> DEFAULT_PUNCT_CHARS = 
    		ImmutableList.of(new PunctChar('!', PunctChar.Use.symbol),
                    new PunctChar('#', PunctChar.Use.symbol),
                    new PunctChar('$', PunctChar.Use.symbol),
                    new PunctChar('%', PunctChar.Use.symbol),
                    new PunctChar('&', PunctChar.Use.symbol),
                    new PunctChar('^', PunctChar.Use.symbol),
                    new PunctChar('_', PunctChar.Use.symbol),
                    new PunctChar('`', PunctChar.Use.symbol),
                    new PunctChar('*', PunctChar.Use.symbol),
                    new PunctChar('?', PunctChar.Use.symbol),
                    new PunctChar('@', PunctChar.Use.symbol),
                    new PunctChar('~', PunctChar.Use.symbol),
                    new PunctChar('+', PunctChar.Use.symbol),
                    new PunctChar('-', PunctChar.Use.symbol),
                    new PunctChar('(', PunctChar.Use.token),
                    new PunctChar(')', PunctChar.Use.token),
                    new PunctChar(',', PunctChar.Use.token),
                    new PunctChar('/', PunctChar.Use.token),
                    new PunctChar('=', PunctChar.Use.token),
                    new PunctChar('[', PunctChar.Use.token),
                    new PunctChar('\\', PunctChar.Use.token),
                    new PunctChar(']', PunctChar.Use.token),
                    new PunctChar('{', PunctChar.Use.token),
                    new PunctChar('|', PunctChar.Use.token),
                    new PunctChar('}', PunctChar.Use.token),
                    new PunctChar('.', PunctChar.Use.pseudo),
                    new PunctChar(':', PunctChar.Use.label),
                    new PunctChar(';', PunctChar.Use.comment));
    
    /**
     * Gives an {@link ImmutableList} of the default {@link PunctChar}s and their {@link Use}.
     *
     * @return - an {@link ImmutableList} of the default {@link PunctChar} and their {@link Use}
     * 
     * @see #DEFAULT_PUNCT_CHARS
     */
    public static ImmutableList<PunctChar> getDefaultPunctChars() {
	    return DEFAULT_PUNCT_CHARS;
    }

    ////////////////// Setters and setters //////////////////

    ReadOnlyMapProperty<UUID, MachineComponent> componentsProperty() {
        return new ReadOnlyMapWrapper<>(components);
    }

    Map<UUID, MachineComponent> getComponents() {
        return Collections.unmodifiableMap(components);
    }

    public List<PunctChar> getPunctChars() {
        return punctChars;
    }

    public void setPunctChars(List<PunctChar> punctChars) {
        checkNotNull(punctChars);

        this.punctChars.clear();
        this.punctChars.addAll(punctChars);
    }

    public char getLabelChar() {
        for (PunctChar c : punctChars) {
            if (c.getUse() == PunctChar.Use.label) {
                return c.getChar();
            }
        }
        //should never get this far
        assert false : "No label char found in the machine's punctChars";
        return ':';  //to satisfy the compiler
    }

    public char getCommentChar() {
        for (PunctChar c : punctChars) {
            if (c.getUse() == PunctChar.Use.comment) {
                return c.getChar();
            }
        }
        //should never get this far
        throw new IllegalStateException("No comment char found in the machine's punctChars");
    }

    public ObservableList<Field> getFields() {
        return fields;
    }

    public ListProperty<Field> fieldsProperty() {
        return fields;
    }
    
    /**
     * A getter method for all module objects
     *
     * @param moduleClazz a String that describes the type of module
     * @return a Vector object
     */
    @SuppressWarnings("unchecked")
    public <T extends Module<T>> ListProperty<T> getModules(final Class<T> moduleClazz) {
        return (ListProperty<T>)moduleMap.get(moduleClazz);
    }
    
    /**
     * A getter method for all module objects
     *
     * @param moduleClazz a String that describes the type of module
     * @return a Vector object
     */
    @SuppressWarnings("unchecked")
    public ListProperty<Module<?>> getModuleUnchecked(final Class<? extends Module<?>> moduleClazz) {
        return (ListProperty<Module<?>>)moduleMap.get(moduleClazz);
    }
    
    /**
     * Gets all of the modules into a single {@link ImmutableList}.
     * @return
     */
    public ImmutableList<List<? extends Module<?>>> getAllModules() {
        ImmutableList.Builder<List<? extends Module<?>>> moduleBuilder = ImmutableList.builder();
        Machine.getModuleClasses().stream().map(this::getModuleUnchecked).forEach(moduleBuilder::add);
        return moduleBuilder.build();
    }

    public ImmutableMap<Class<? extends Module<?>>, List<? extends Module<?>>> getModuleMap() {
        return Machine.getModuleClasses().stream()
                .collect(Gullectors.toImmutableMap(Function.identity(), this::getModuleUnchecked));
    }
    
    /**
     * A getter method for all microinstructions
     *
     * @param clazz Denotes the type stored
     * @return a Vector object
     *
     * @since 2016-11-20
     */
    @SuppressWarnings("unchecked")
    public <U extends Microinstruction<U>> ListProperty<U> getMicros(Class<U> clazz) {
        return (ListProperty<U>)microMap.get(clazz);
    }
    
    /**
     * A getter method for {@link Microinstruction} values
     *
     * @param clazz Denotes the type stored
     * @return a Vector object
     *
     * @since 2016-12-01
     */
    @SuppressWarnings("unchecked")
    public ListProperty<Microinstruction<?>> getMicrosUnchecked(Class<? extends Microinstruction<?>> clazz) {
        return (ListProperty<Microinstruction<?>>)microMap.get(clazz);
    }
    
    /**
     * Get all of the loaded {@link Microinstruction} in the {@link Machine}.
     * @return List of all the {@link Microinstruction}s present
     */
    public List<List<Microinstruction<?>>> getAllMicros() {
        ImmutableList.Builder<List<Microinstruction<?>>> microBuilder = ImmutableList.builder();
        getMicroClasses().stream().map(this::getMicrosUnchecked).forEach(microBuilder::add);
    
        return microBuilder.build();
    }

    public ImmutableMap<Class<? extends Microinstruction<?>>, List<Microinstruction<?>>> getMicrosMap() {
        return Machine.getMicroClasses().stream()
                .collect(Gullectors.toImmutableMap(Function.identity(), this::getMicrosUnchecked));
    }

    /**
     * Adds a {@link ListChangeListener} to all of the {@link Microinstruction} {@link ObservableList} stored internally.
     * @param listener Non-{@code null} listener
     */
    public void addChangeListenerForMicros(ListChangeListener<Microinstruction<?>> listener) {
        checkNotNull(listener);

        for (ObservableList<? extends Microinstruction<?>> list: microMap.values()) {
            list.addListener(listener);
        }
    }

    public ListProperty<MachineInstruction> instructionsProperty() { return instructions; }
    public ObservableList<MachineInstruction> getInstructions() {
        return instructions;
    }

    public ObservableList<EQU> getEQUs() {
        return EQUs;
    }


    public End getEnd() {
        return (End) (microMap.get(End.class)).get(0);
    }

    public int getStartingAddressForLoading() {
        return startingAddressForLoading.get();
    }

    public IntegerProperty startingAddressForLoadingProperty() {
        return startingAddressForLoading;
    }

    public boolean getIndexFromRight() {
        return indexFromRight.get();
    }

    public void setIndexFromRight(boolean b) {
        indexFromRight.set(b);
    }
    
    public ObjectProperty<RAM> codeStoreProperty() {
        return codeStore;
    }
    
    public Optional<RAM> getCodeStore() {
        return Optional.ofNullable(codeStore.get());
    }

    public void setCodeStore(RAM r) {
        codeStore.set(r);
    }

    public void setStartingAddressForLoading(int add) {
        startingAddressForLoading.set(add);
    }

    public ObjectProperty<Register> programCounterProperty() {
        return programCounter;
    }
    
    public Optional<Register> getProgramCounter() {
        return Optional.ofNullable(programCounter.get());
    }

    public void setProgramCounter(Register programCounter) {
        this.programCounter.setValue(programCounter);
    }

    private void initializeModuleMap() {
        moduleMap.put(Register.class, registers);
        moduleMap.put(RegisterArray.class, registerArrays);
        moduleMap.put(ConditionBit.class, conditionBits);
        moduleMap.put(RAM.class, rams);
    }

    private void initializeMicroMap() {
        for (Class<? extends Microinstruction<?>> microClazz : getMicroClasses()) {
            microMap.put(microClazz, new SimpleListProperty<>(FXCollections.observableArrayList()));
        }
        getMicros(End.class).add(new End(this));
        getMicros(Comment.class).add(new Comment("Comment", IdentifiedObject.generateRandomID(),this));
    }

    public void setFields(List<? extends Field> newFields) {
        checkNotNull(newFields);

        fields.clear();
        fields.addAll(newFields);
    }

    /**
     * Updates the machine instructions.
     * @param newInstructions
     */
    public void setInstructions(List<? extends MachineInstruction> newInstructions) {
        checkNotNull(newInstructions);

        instructions.clear();
        instructions.addAll(newInstructions);
    }

    //-------------------------------
    // updates global EQUs
    public void setEQUs(List<? extends EQU> newEQUs) {
        checkNotNull(newEQUs);

        this.EQUs.clear();
        this.EQUs.addAll(newEQUs);
    }

    //-------------------------------
    // updates the registers
    
    /**
     * Removes a {@link Module} from the machine
     * @param module
     */
    private void removeModule(final Module<?> module) {
        Map<Microinstruction<?>, ObservableList<Microinstruction<?>>> microsThatUseIt
                = getMicrosThatUse(module);
        Set<Microinstruction<?>> e = microsThatUseIt.keySet();
        for (Microinstruction<?> micro : e) {
            //remove it from all machine instructions
            removeAllOccurencesOf(micro);
            //also remove the microinstruction from its list.
            microsThatUseIt.get(micro).remove(micro);
        }
    }
    
    public void setRegisters(List<Register> newRegisters) {
        for (Register oldRegister : registers) {
            if (!newRegisters.contains(oldRegister)) {
                removeModule(oldRegister);
            }
        }

        //reuse the old Vector since the desktop uses it as the key
        //in the moduleWindows hashtable.
        registers.clear();
        registers.addAll(newRegisters);


        // test whether the program counter was deleted and, if so,
        // set the program counter to the place holder register
        if(!newRegisters.contains(programCounter.getValue()))
            setProgramCounter(null);
    }

    //-------------------------------
    // updates the register arrays
    public void setRegisterArrays(final List<RegisterArray> newRegisterArrays) {
        for (RegisterArray oldArray : registerArrays) {
            if (!newRegisterArrays.contains(oldArray)) {
                removeModule(oldArray);
            }
        }

        //reuse the old Vector in case in the future some other objects use it.
        registerArrays.clear();
        registerArrays.addAll(newRegisterArrays);

        // test whether the program counter was deleted and, if so,
        // set the program counter to the place holder register
        for(RegisterArray array : newRegisterArrays) {
            if (array.getRegisters().contains(programCounter.getValue()))
                return;
        }
        setProgramCounter(null);
    }

    //--------------------------------
    // returns a HashMap whose keys consist of all microinstructions that
    // use m and such that the value associated with each key is the List
    // of microinstructions that contains the key.
    public Map<Microinstruction<?>, ObservableList<Microinstruction<?>>> getMicrosThatUse(Module<?> m) {
        final Map<Microinstruction<?>, ObservableList<Microinstruction<?>>> result
                = new HashMap<>(getMicroClasses().size());

        for (Class<? extends Microinstruction<?>> mc : getMicroClasses()) {
            @SuppressWarnings("unchecked")
            ObservableList<Microinstruction<?>> v = getMicrosUnchecked(mc);

            result.putAll(v.stream()
                    .filter(micro -> micro.uses(m))
                    .collect(Collectors.toMap(Function.identity(), _ignore -> v)));
        }

        return result;
    }

    /**
     * updates the {@link End} microinstruction
     */
    public void setEnd(End end) {
        ObservableList<End> ends = getMicros(End.class);
        ends.clear();
        ends.add(end);
    }

//    //-------------------------------
//    // updates the condition bits
    // FIXME huh, this is never used.
//    public void setConditionBits(Set<ConditionBit> newConditionBits) {
//        //first delete arithmetics, setCondBits, and increments that use
//        //any deleted ConditionBits, including removing
//        //these micros from all machine instructions that use them.
//
//        final List<SetCondBit> setCondBits = getMicros(SetCondBit.class);
//        setCondBits.stream().filter(micro -> !newConditionBits.contains(micro.getBit()))
//                .forEach(micro -> {
//                    removeAllOccurencesOf(micro);
//                    setCondBits.remove(micro);
//                });
//
//        final List<Increment> increments = getMicros(Increment.class);
//        increments.stream().filter(micro ->
//                ((!newConditionBits.contains(micro.getOverflowBit())) &&
//                    (micro.getOverflowBit() != ConditionBit.none())))
//                .forEach(micro -> {
//                    removeAllOccurencesOf(micro);
//                    increments.remove(micro);
//                });
//
//        conditionBits.clear();
//        conditionBits.addAll(newConditionBits);
//    }

    //-------------------------------
    // updates the RAM arrays
    public void setRAMs(final List<? extends RAM> newRams) {
        //first delete all MemoryAccess micros that reference any deleted rams
        //    and remove the micros from all machine instructions that use them.
        getMicros(MemoryAccess.class).stream()
                .filter(access -> !newRams.contains(access.getMemory()))
                .forEach(access -> {
                    removeAllOccurencesOf(access);
                    getMicros(MemoryAccess.class).remove(access);
                });

        rams.clear();
        rams.addAll(newRams);
    }

    public ObservableList<Register> getAllRegisters() {
        return allRegisters.get();
    }

    public ReadOnlyListProperty<Register> allRegistersProperty() {
        return allRegisters;
    }

    //-------------------------------
    // updates the given vector of micros of a given class, but first deletes
    // all references to a deleted micro
    
    /**
     *
     * @param microClazz
     * @param newMicros
     */
    public <U extends Microinstruction<U>> void setMicros(Class<U> microClazz, ObservableList<U> newMicros) {
        //first delete all references in machine instructions
        // to any old micros not in the new list
        getMicros(microClazz).stream()
                .filter(oldMicro -> !newMicros.contains(oldMicro))
                .forEach(this::removeAllOccurencesOf);

        ListProperty<U> micros = getMicros(microClazz);
        micros.clear();
        micros.addAll(newMicros);
    }

    //--------------------------------
    // returns true if the given microinstruction is in the hash table
    // of micros for this machine
    public boolean contains(Microinstruction micro) {
        return microMap.values().stream().anyMatch(v -> v.contains(micro));
    }

    /**
     * Resets all {@link IO#getConnection()} channels.
     */
    public void resetAllChannels() {
        getMicros(IO.class).stream()
                .map(IO::getConnection)
                .forEach(IOChannel::reset);
    }

    /**
     * Resets all of the channels except for the {@link StreamChannel#console()}.
     */
    public void resetAllChannelsButConsole() {
        getMicros(IO.class).stream()
                .map(IO::getConnection)
                .filter(c -> c != StreamChannel.console())
                .forEach(IOChannel::reset);
    }

    /**
     * Calls {@link Register#clear()} on all {@link Register} values.
     */
    public void clearAllRegisters() {
        getModules(Register.class).forEach(Register::clear);
    }

    /**
     * Calls {@link Register#clear()} on all {@link Register} values.
     */
    public void clearAllRegisterArrays() {
        getModules(RegisterArray.class).forEach(RegisterArray::clear);
    }

    /**
     * clearAllRAMs
     * @since 2000-11-03
     */
    public void clearAllRAMs() {
        getModules(RAM.class).forEach(RAM::clear);
    }

    /**
     * @return {@link List} of all {@link Microinstruction}s that use m
     */
    public List<MachineInstruction> getInstructionsThatUse(final Microinstruction m) {
        List<MachineInstruction> result = instructions.stream()
                .filter(instr -> instr.usesMicro(m))
                .collect(Collectors.toList());
        
        //don't forget the fetchSequence too.
        if (fetchSequence.getValue().usesMicro(m)) {
            result.add(fetchSequence.getValue());
        }

        return result;
    }

    //--------------------------------
    // returns a List of all machine instructions that use f
    public List<MachineInstruction> getInstructionsThatUse(final Field f) {
        checkNotNull(f);
        
        return instructions.stream()
                .filter(instr -> instr.usesField(f))
                .collect(Collectors.toList());
    }

    /**
     * deletes from all machine instructions every use of m
     * @param m The microinstruction to remove
     */

    private void removeAllOccurencesOf(Microinstruction m) {
        instructions.forEach(in -> in.removeMicro(m));
        fetchSequence.getValue().removeMicro(m);
    }

    //--------------------------------
    // get the control unit

    public ControlUnit getControlUnit() {
        return controlUnit.get();
    }

    public ObjectProperty<ControlUnit> controlUnitProperty() {
        return controlUnit;
    }

    //--------------------------------
    // get & set the fetch sequence

    public MachineInstruction getFetchSequence() {
        return fetchSequence.get();
    }

    public void setFetchSequence(MachineInstruction f) {
        fetchSequence.set(f);
    }

    public ObjectProperty<MachineInstruction> fetchSequenceProperty() {
        return fetchSequence;
    }

    public void visitMicros(final MicroinstructionVisitor visitor) {
        
        final List<Class<? extends Microinstruction<?>>> classes = microMap.keySet()
                .stream()
                .sorted(Comparator.comparing(Class::getSimpleName))
                .map(c -> (Class<? extends Microinstruction<?>>)c)
                .collect(Collectors.toList());
        
        CategoryLoop: for (Class<? extends Microinstruction<?>> mc: classes) {
            switch (visitor.visitCategory(mc.getSimpleName())) {
            case SkipChildren:
                // Just go to the next category
                continue CategoryLoop;
                
            case SkipSiblings:
            case Stop:
                // If skipping siblings of a category, it's identical to stopping.
                break CategoryLoop;
                
            case Okay:
                break;
            }

            List<Microinstruction<?>> sortedMicros = microMap.get(mc).stream()
                    .sorted(Comparator.comparing(NamedObject::getName))
                    .map(v -> (Microinstruction<?>)v)
                    .collect(Collectors.toList());
            
            MicroLoop: for (Microinstruction<?> micro: sortedMicros) {
                switch (visitor.visitMicro(micro)) {
                case SkipChildren:
                case Okay:
                    continue MicroLoop;
                    
                case SkipSiblings:
                    break MicroLoop;
                    
                case Stop:
                    break CategoryLoop;
                }
            }
        }
    }

    //--------------------------------
    // called by user when they want to halt execution
    public void setRunMode(RunModes newRunMode) {
        runMode = newRunMode;
    }

    //--------------------------------
    // called by user when they want to know what the status of the
    // current execution.
    public RunModes getRunMode() {
        return runMode;
    }

    /**
     * executes the machine using the given mode of execution.
     * The mode can be any of the following values in CPUSimConstants:
     * RUN, STEP_BY_MICRO, STEP_BY_INSTR, STOP, ABORT, COMMAND_LINE
     * The machine uses the current contents of memory and its registers
     * and the control store as it executes.
     * If the GUI is used, the machine is executed in a new Thread so that
     * the GUI remains responsive.
     *
     * @param mode the run mode for execution.
     */
    public void execute(final RunModes mode) {
        validate();
        
        setRunMode(mode);


        ControlUnit controlUnit = getControlUnit();

        // FIXME Strategy pattern..


        if (mode == RunModes.COMMAND_LINE) {
            // do not use the GUI at all--purely command line execution
            // There is no stepping or backing up.  It executes in the
            // main (and only) thread until it finishes or the user
            // quits it from the command line (like with Ctrl-C).

            while (runMode != RunModes.STOP &&
                    runMode != RunModes.ABORT &&
                    haltBitsThatAreSet().size() == 0) {

                MachineInstruction currentInstruction = controlUnit.getCurrentInstruction();
                List<Microinstruction<?>> microInstructions = currentInstruction.getMicros();
                int currentIndex = controlUnit.getMicroIndex();

                if (currentIndex < 0 || currentIndex >= microInstructions.size()) {
                    System.out.println("Error: The step is out of range\n" +
                            "at step " + currentIndex + " of " +
                            controlUnit.getCurrentInstruction() + ".\n");
                    break;
                }
                Microinstruction currentMicro = microInstructions.get(currentIndex);
                controlUnit.incrementMicroIndex(1);

                try {
                    currentMicro.execute();
                } catch (ExecutionException e) {
                    System.out.println("Exception thrown: " + e.getMessage());
                    return;
                }
            }
            System.out.println("Execution halted.");
        }
        else {  // use the GUI
            setState(State.START_OF_EXECUTE_THREAD, runMode == RunModes.RUN);
            Task<Void> executionTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    final Register programCounter = Machine.this.programCounter.get();
                    final RAM codeStore = Machine.this.codeStore.get();
                    while (runMode != RunModes.STOP &&
                            runMode != RunModes.ABORT &&
                            !isCancelled() &&
                            haltBitsThatAreSet().size() == 0) {

                        MachineInstruction currentInstruction =
                                controlUnit.getCurrentInstruction();
                        List<Microinstruction<?>> microInstructions = currentInstruction.getMicros();
                        int currentIndex = controlUnit.getMicroIndex();

                        if (currentIndex < 0 ||
                                currentIndex >= microInstructions.size()) {
                            //change the state indicating an exception and quit
                            setState(Machine.State.EXCEPTION_THROWN, "The step is out " +
                                    "of range\n" +
                                    "at step " + currentIndex + " of " +
                                    currentInstruction + ".\n");
                            break;
                        }
                        if (runMode != RunModes.RUN &&
                                currentIndex == 0 &&
                                currentInstruction == getFetchSequence()) {
                            // it's the start of a machine cycle
                            if (codeStore.breakAtAddress((int)programCounter.getValue())
                                    && ! justBroke) {
                                RAMLocation breakLocation = codeStore.data().get((int)
                                        programCounter.getValue());
                                setState(Machine.State.BREAK, breakLocation);
                                runMode = RunModes.STOP;
                                justBroke = true;
                                break;
                            }
                            else {
                                justBroke = false;
                                setState(Machine.State.START_OF_MACHINE_CYCLE, false);
                                //false is unused
                            }
                        }
                        Microinstruction<?> currentMicro = microInstructions.get(currentIndex);
                        // Fire property change indicating the start of a
                        // microinstruction
                        if (currentIndex < microInstructions.size()) {
                            setState(Machine.State.START_OF_MICROINSTRUCTION,
                                    controlUnit.getCurrentState());
                        }
                        controlUnit.incrementMicroIndex(1);

                        try {
                            currentMicro.execute();
                        } catch (ExecutionException e) {
                            //fire property change indicating an exception and quit
                            setState(Machine.State.EXCEPTION_THROWN, e.getMessage());
                            controlUnit.setMicroIndex(currentIndex);
                            return null;
                        }

                        if (runMode == RunModes.STEP_BY_MICRO) {
                            runMode = RunModes.STOP;
                        }
                        if (runMode == RunModes.STEP_BY_INSTR && currentMicro == getEnd()) {
                            runMode = RunModes.STOP;
                        }
                    }

                    // fire a property change that execution halted or aborted
                    if(runMode == RunModes.ABORT)
                        setState(Machine.State.EXECUTION_ABORTED,haltBitsThatAreSet().size()>0);
                    else if(mode == RunModes.STEP_BY_MICRO
                            && getStateWrapper().getState() != Machine.State.BREAK)
                        setState(Machine.State.HALTED_STEP_BY_MICRO,haltBitsThatAreSet().size()>0);
                    else if(getStateWrapper().getState() != Machine.State.BREAK)
                        setState(Machine.State.EXECUTION_HALTED,haltBitsThatAreSet().size()>0);
                    // else if in BREAK state, leave it in that state to allow highlighting the
                    // line of text containing the break by the HighlightManager.

//                    setState(
//                            runMode == RunModes.ABORT ? Machine.State.EXECUTION_ABORTED :
//                                       mode == RunModes.STEP_BY_MICRO ?
//                                               Machine.State.HALTED_STEP_BY_MICRO :
//                                       /* else */ Machine.State.EXECUTION_HALTED,
//                            haltBitsThatAreSet().size() > 0);

                    // write buf to output file channel
                    if ((getStateWrapper().getState() == Machine.State.EXECUTION_HALTED &&
                            ((boolean) getStateWrapper().getValue())) ||
                            getStateWrapper().getState() == Machine.State.EXCEPTION_THROWN) {
                        ObservableList<IO> ios = getMicros(IO.class);
                        for (IO io : ios) {
                            final IOChannel channel = io.getConnection();
                            // FIXME #95
                            if (channel instanceof FileChannel &&
                                    io.getDirection().equals("output")) {
                                ((FileChannel) channel).writeToFile();
                            }
                        }
                    }
                    return null;
                }
            };
            new Thread(executionTask, "Machine execution thread").start();

        }
    }

    /**
     * changes indexing of bits in registers to the opposite direction while preserving
     * the behavior of the components.
     * The following components need to be modified:
     * ConditionBit module
     * TransferRtoR micros
     * TransferAtoR micros
     * TransferRtoA micros
     * SetBits (Set) micros
     * Test micros
     */
    public void changeStartBits() {
        for (ConditionBit cb : getModules(ConditionBit.class)) {
            cb.setBit(cb.getRegister().getWidth() - cb.getBit() - 1);
        }

        for (TransferRtoR tRtoR : getMicros(TransferRtoR.class)) {
            tRtoR.setDestStartBit(tRtoR.getDest().getWidth() - tRtoR.getNumBits() - tRtoR.getDestStartBit());
            tRtoR.setSrcStartBit(tRtoR.getSource().getWidth() - tRtoR.getNumBits() - tRtoR.getSrcStartBit());
        }

        for (TransferRtoA tRtoA : getMicros(TransferRtoA.class)) {
            tRtoA.setDestStartBit(tRtoA.getDest().getWidth() - tRtoA.getNumBits() - tRtoA.getDestStartBit());
            tRtoA.setSrcStartBit(tRtoA.getSource().getWidth() - tRtoA.getNumBits() - tRtoA.getSrcStartBit());
            tRtoA.setIndexStart(tRtoA.getIndex().getWidth() - tRtoA.getIndexNumBits() - tRtoA.getIndexStart());
        }

        for (TransferAtoR tAtoR : this.getMicros(TransferAtoR.class)) {
            tAtoR.setDestStartBit(tAtoR.getDest().getWidth() - tAtoR.getNumBits() - tAtoR.getDestStartBit());
            tAtoR.setSrcStartBit(tAtoR.getSource().getWidth() - tAtoR.getNumBits() - tAtoR.getSrcStartBit());
            tAtoR.setIndexStart(tAtoR.getIndex().getWidth() - tAtoR.getIndexNumBits() - tAtoR.getIndexStart());
        }

        for (Test test : getMicros(Test.class)) {
            test.setStart(test.getRegister().getWidth() - test.getNumBits() - test.getStart());
        }

        for (SetBits set : getMicros(SetBits.class)) {
            set.setStart(set.getRegister().getWidth() - set.getNumBits() - set.getStart());
        }
    }

    //--------------------------------
    // haltBitsThatAreSet: returns a Vector of all condition bits
    // specified by all Halt micros have value 1

    public List<ConditionBit> haltBitsThatAreSet() {
        return getModules(ConditionBit.class).stream()
                .filter(c -> c.getHalt() && c.isSet())
                .collect(Collectors.toList());
    }

    public ObservableList<Register> getRegisters() {
        return registers;
    }


    public List<Comment> getCommentMicros() {
        // FIXME This doesn't actually get the #getMicros(Comment.class) results.. Is it supposed to?
        //go through all the instrs and get their Comment micros in a list.
        List<Comment> result = new ArrayList<>();
        for (MachineInstruction instr : instructions) {
            List<Microinstruction<?>> micros = instr.getMicros();
            for (Microinstruction micro : micros)
                if (micro instanceof Comment) {
                    result.add((Comment) micro);
                }
        }
        //now do the same for the fetch sequence
        if (fetchSequence.getValue() != null) {
            for (Microinstruction micro : fetchSequence.get().getMicros()) {
                if (micro instanceof Comment) {
                    result.add((Comment) micro);
                }
            }

        }
        return result;
    }

	@Override
	public String getXMLDescription(String indent) {
		return getHTMLName();
	}

	@Override
	public String getHTMLDescription(String indent) {
		return getHTMLName();
	}

	private void copyModules(MachineComponent.IdentifierMap oldToNew, Class<? extends Module<?>> module) {
        ListProperty<Module<?>> property = oldToNew.getNewMachine().getModuleUnchecked(module);
        getModuleUnchecked(module).stream()
                .map(r -> (Module<?>)r.cloneFor(oldToNew))
                .forEach(property::add);
    }

    private void copyMicros(MachineComponent.IdentifierMap oldToNew, Class<? extends Microinstruction<?>> module) {
        ListProperty<Microinstruction<?>> property = oldToNew.getNewMachine().getMicrosUnchecked(module);
        property.clear();
        getMicrosUnchecked(module).stream()
                .map(r -> r.cloneFor(oldToNew))
                .forEach(property::add);
    }

	@Override
	public Machine cloneFor(MachineComponent.IdentifierMap oldToNew) {
        checkNotNull(oldToNew);

        // Don't clone execution state.. this should be separated :|
        Machine newInst = new Machine(getName(), !getIndexFromRight());
        oldToNew.setNewMachine(newInst);

        getModuleClasses().forEach(mc -> copyModules(oldToNew, mc));
        getMicroClasses().forEach(mc -> copyMicros(oldToNew, mc));

        oldToNew.copyProperty(this, newInst, Machine::controlUnitProperty);
        oldToNew.copyProperty(this, newInst, Machine::fetchSequenceProperty);
        oldToNew.copyProperty(this, newInst, Machine::programCounterProperty);
        oldToNew.copyProperty(this, newInst, Machine::codeStoreProperty);

        oldToNew.copyListProperty(this, newInst, Machine::fieldsProperty);

        newInst.indexFromRight.setValue(this.indexFromRight.getValue());
        newInst.punctChars.clear();
        newInst.punctChars.addAll(this.punctChars);

        return newInst;
	}

    @Override
    public <U extends Machine> void copyTo(U other) {
        // FIXME
        throw new UnsupportedOperationException("unimplemented");
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Machine.class)
                .addValue(getID())
                .add("name", getName())
                .toString();
    }
    
    @Override
    public void validate() {
        super.validate();

        // Validate all of the internal state
        moduleMap.values().stream().flatMap(List::stream).forEach(Validatable::validate);
        microMap.values().stream().flatMap(List::stream).forEach(Validatable::validate);
        
        if (programCounter == null) {
            throw new ValidationException("Program counter Register is not set.");
        }
    }

    public interface MicroinstructionVisitor {

        enum VisitResult {

            /**
             * Stop the traversal
             */
            Stop,

            /**
             * Skip children, but go to siblings
             */
            SkipChildren,

            /**
             * Skip the following siblings, this implies {@link #SkipChildren}.
             */
            SkipSiblings,

            /**
             * Continue with no changes.
             */
            Okay

        }

        VisitResult visitCategory(final String category);

        VisitResult visitSubCategory(final String subcategory);

        VisitResult visitMicro(final Microinstruction<?> micro);

    }

}