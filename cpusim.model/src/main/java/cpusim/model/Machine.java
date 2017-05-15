package cpusim.model;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import cpusim.model.assembler.EQU;
import cpusim.model.assembler.PunctChar;
import cpusim.model.assembler.PunctChar.Use;
import cpusim.model.iochannel.FileChannel;
import cpusim.model.iochannel.IOChannel;
import cpusim.model.iochannel.StreamChannel;
import cpusim.model.microinstruction.*;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.ControlUnit;
import cpusim.model.module.Module;
import cpusim.model.module.Modules;
import cpusim.model.module.RAM;
import cpusim.model.module.RAMLocation;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import cpusim.model.util.IdentifiedObject;
import cpusim.model.util.MachineBound;
import cpusim.model.util.MachineComponent;
import cpusim.model.util.NamedObject;
import cpusim.model.util.ObservableCollectionBuilder;
import cpusim.model.util.Validatable;
import cpusim.model.util.ValidationException;
import cpusim.model.util.structure.MachineVisitor;
import cpusim.model.util.structure.MicroinstructionVisitor;
import cpusim.model.util.structure.ModuleVisitor;
import cpusim.model.util.structure.VisitResult;
import cpusim.util.Gullectors;
import cpusim.util.MoreBindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.*;

/**
 * This file contains the class for Machines created using CPUSim
 *
 * @since 2013-10-01
 */
public class Machine extends Module<Machine> {


    public static final String PROPERTY_NAME_RAMS = "rams";
    public static final String PROPERTY_NAME_REGISTERS = "registers";
    public static final String PROPERTY_NAME_REGISTER_ARRAYS = "registerArray";
    public static final String PROPERTY_NAME_CONDITION_BITS = "conditionBits";

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

    private static final Logger logger = LogManager.getLogger(Machine.class);

    private final ReadOnlyMapProperty<UUID, MachineComponent> components;

    private final ReadOnlySetProperty<MachineComponent> children;

    // the hardware modules making up the machine
    
    @ChildComponent
    private final ListProperty<RAM> rams;

    @ChildComponent
    private final ListProperty<Register> registers;

    @ChildComponent
    private final ListProperty<RegisterArray> registerArrays;

    @ChildComponent
    private final ListProperty<ConditionBit> conditionBits;


    // Control unit for keeping track of index of micro within machine instruction
    @ChildComponent
    private final ObjectProperty<ControlUnit> controlUnit;

    // RAM where the code store resides
    @ChildComponent
    private final ObjectProperty<RAM> codeStore;

    // Register used for stopping at break points--initially null
    @ChildComponent
    private final ObjectProperty<Register> programCounter;

    /**
     * Property of all of the registers internal to the {@link Machine}.
     */
    private final ReadOnlyListProperty<Register> allRegisters;

    // Machine instructions
    @ChildComponent
    private final ListProperty<MachineInstruction> instructions;

    // EQUs
    private final ListProperty<EQU> EQUs;

    // key = micro name, value = list of microinstructions
    @ChildComponent
    private final MapProperty<Class<? extends Microinstruction<?>>, ListProperty<? extends Microinstruction<?>>> microMap;

    // key = module type, value = list of modules
    @ChildComponent
    private final MapProperty<Class<? extends Module<?>>, ListProperty<? extends Module<?>>> moduleMap;


    // The machine's fetch sequence
    @ChildComponent
    private final ObjectProperty<MachineInstruction> fetchSequence;

    // Fields of the machine instructions
    @ChildComponent
    private final ListProperty<Field> fields;


    // True if bit indexing starts of the right side, false if on the left
    private final BooleanProperty indexFromRight;

    // Address to start when loading RAM with a program
    private final IntegerProperty startingAddressForLoading;

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

        rams = new SimpleListProperty<>(this, PROPERTY_NAME_RAMS,
                FXCollections.observableArrayList());
        registers = new SimpleListProperty<>(this, PROPERTY_NAME_REGISTERS,
                FXCollections.observableArrayList());
        registerArrays = new SimpleListProperty<>(this, PROPERTY_NAME_REGISTER_ARRAYS,
                FXCollections.observableArrayList());
        conditionBits = new SimpleListProperty<>(this, PROPERTY_NAME_CONDITION_BITS,
                FXCollections.observableArrayList());

        // We bind the "allRegisters" array to listen to the changes to the registers and registerArray lists,
        // this way when they change, the allRegisters array is always up to date!


        wrappedState = new SimpleObjectProperty<>(this, "machine state", new
                StateWrapper(State.NEVER_RUN, ""));

        moduleMap = new SimpleMapProperty<>(this, "modules", FXCollections.observableHashMap());
        microMap = new SimpleMapProperty<>(this, "microinstructions", FXCollections.observableHashMap());

        instructions = new SimpleListProperty<>(this, "instructions", FXCollections.observableArrayList());
        fixMachineBinding(instructions);

        EQUs = new SimpleListProperty<>(this, "equs", FXCollections.observableArrayList());

        fields = new SimpleListProperty<>(this, "fields", FXCollections.observableArrayList());
        fixMachineBinding(fields);

        punctChars = new SimpleListProperty<>(this, "punctChars", FXCollections.observableArrayList());
        punctChars.addAll(getDefaultPunctChars());

        fetchSequence = new SimpleObjectProperty<>(this, "fetchSequence",
                new MachineInstruction("Fetch sequence", UUID.randomUUID(), this,
                        0, null, null));
        fixMachineBinding(fetchSequence);

        controlUnit = new SimpleObjectProperty<>(this, "controlUnit",
                new ControlUnit("ControlUnit", UUID.randomUUID(), this));
        fixMachineBinding(controlUnit);

        startingAddressForLoading = new SimpleIntegerProperty(this, "startingAddressForLoading",0);

        programCounter = new SimpleObjectProperty<>(this, "programCounter", null);
        fixMachineBinding(programCounter);

        codeStore = new SimpleObjectProperty<>(this, "codeStore",null);
        fixMachineBinding(codeStore);

        indexFromRight = new SimpleBooleanProperty(this, "indexedFromRight", true); //conventional indexing order

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

    private <T extends MachineBound> void fixMachineBinding(Property<T> property) {
        property.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                newValue.machineProperty().setValue(Machine.this);
            }
        });
    }

    /**
     * Adds a change listener to an {@link ObservableList} that will automatically rebind
     * the {@link MachineBound#machineProperty()} to the current {@link Machine}.
     * 
     * @param property
     * @param <T>
     */
    private <T extends MachineBound> void fixMachineBinding(ObservableList<T> property) {
        property.addListener((ListChangeListener<T>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(m ->
                            m.machineProperty().setValue(Machine.this));
                }
            }
        });
    }

    private <T extends MachineBound> void fixMachineBinding(ObservableMap<?, ListProperty<? extends T>> map) {
        map.addListener((MapChangeListener<Object, ListProperty<? extends T>>) change -> {
            if (change.wasAdded()) {
                Machine.this.fixMachineBinding(change.getValueAdded());
            }
        });
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
        Modules.getModuleClasses().stream().map(this::getModuleUnchecked).forEach(moduleBuilder::add);
        return moduleBuilder.build();
    }

    public ImmutableMap<Class<? extends Module<?>>, List<? extends Module<?>>> getModuleMap() {
        return Modules.getModuleClasses().stream()
                .collect(Gullectors.toImmutableMap(Function.identity(), this::getModuleUnchecked));
    }
    
    public MapProperty<Class<? extends Module<?>>, ListProperty<? extends Module<?>>> modulesProperty() {
        return moduleMap;
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
        Microinstructions.getMicroClasses().stream().map(this::getMicrosUnchecked).forEach(microBuilder::add);
    
        return microBuilder.build();
    }
    
    /**
     * Gets an {@link ImmutableMap} of all the Microinstruction classes to their values.
     *
     * @return Non-{@code null}, possibly empty Map
     */
    public ImmutableMap<Class<? extends Microinstruction<?>>, List<? extends Microinstruction<?>>> getMicrosMap() {
        return Microinstructions.getMicroClasses().stream()
                .collect(Gullectors.toImmutableMap(Function.identity(),
                        k -> ImmutableList.copyOf(this.getMicrosUnchecked(k))));
    }
    
    public MapProperty<Class<? extends Microinstruction<?>>, ListProperty<? extends Microinstruction<?>>> microsProperty() {
        return microMap;
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
    
    public ListProperty<EQU> equsProperty() {
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

    public boolean isIndexFromRight() {
        return indexFromRight.get();
    }

    public void setIndexFromRight(boolean b) {
        indexFromRight.set(b);
    }

    public BooleanProperty indexFromRightProperty() {
        return indexFromRight;
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
        fixMachineBinding(moduleMap);

        moduleMap.put(Register.class, registers);
        moduleMap.put(RegisterArray.class, registerArrays);
        moduleMap.put(ConditionBit.class, conditionBits);
        moduleMap.put(RAM.class, rams);

    }

    private void initializeMicroMap() {
        fixMachineBinding(microMap);

        for (Class<? extends Microinstruction<?>> microClazz : Microinstructions.getMicroClasses()) {
            microMap.put(microClazz, new SimpleListProperty<>(FXCollections.observableArrayList()));
        }
        getMicros(End.class).add(new End(this));
        getMicros(Comment.class).add(new Comment("Comment", IdentifiedObject.generateRandomID(),this));


    }

    public void setFields(Collection<? extends Field> newFields) {
        logger.traceEntry("setFields: newFields = {}", newFields);
        
        checkNotNull(newFields, "newFields == null");
        
        if (newFields == fields) {
            logger.traceExit("newFields is the same collection as fields, short-circuiting");
            return;
        }
        
        fields.clear();
        fields.addAll(newFields);
        
        logger.traceExit();
    }

    /**
     * Updates the machine instructions.
     * @param newInstructions
     */
    public void setInstructions(Collection<? extends MachineInstruction> newInstructions) {
        logger.traceEntry("setInstructions: newInstructions = {}", newInstructions);
        
        checkNotNull(newInstructions, "newInstructions == null");

        if (newInstructions != instructions) {
            instructions.clear();
            instructions.addAll(newInstructions);
            
            logger.traceExit("newInstructions is the same collection as instructions, short-circuiting");
        } else {
            logger.traceExit();
        }
    }

    //-------------------------------
    // updates global EQUs
    public void setEQUs(Collection<? extends EQU> newEQUs) {
        checkNotNull(newEQUs);
        
        if (newEQUs != this.EQUs) {
            this.EQUs.clear();
            this.EQUs.addAll(newEQUs);
        }
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
    
    public void setRegisters(Collection<? extends Register> newRegisters) {
        logger.traceEntry("setRegisters: newRegisters = {}", newRegisters);
        
        if (registers == newRegisters) {
            logger.traceExit("Attempted to setRegisters() to same collection, skipping.");
            return;
        }
        
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
        if(!newRegisters.contains(programCounter.getValue())) {
            logger.trace("New registers do not contain the programCounter, setting to null");
            setProgramCounter(null);
        }
        
        logger.traceExit();
    }

    //-------------------------------
    // updates the register arrays
    public void setRegisterArrays(final Collection<? extends RegisterArray> newRegisterArrays) {
        logger.traceEntry("setRegisterArrays: newRegisterArrays = {}", newRegisterArrays);
        
        if (registerArrays == newRegisterArrays) {
            // shortcut
            logger.traceExit("Attempted to setRegisterArrays() to same collection, skipping.");
            return;
        }
        
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
        
        getProgramCounter().ifPresent(pc -> {
            if (!registers.contains(pc)) {
                boolean found = false;
                for (RegisterArray array : newRegisterArrays) {
                    if (array.getRegisters().contains(pc)) {
                        found = true;
                        break;
                    }
                }
        
                if (!found) {
                    logger.trace("programCounter was in removed register array, setting to null");
                    setProgramCounter(null);
                }
            }
        });
    
        logger.traceExit();
    }

    //--------------------------------
    // returns a HashMap whose keys consist of all microinstructions that
    // use m and such that the value associated with each key is the List
    // of microinstructions that contains the key.
    public Map<Microinstruction<?>, ObservableList<Microinstruction<?>>> getMicrosThatUse(Module<?> m) {
        final Map<Microinstruction<?>, ObservableList<Microinstruction<?>>> result
                = new HashMap<>(Microinstructions.getMicroClasses().size());

        for (Class<? extends Microinstruction<?>> mc : Microinstructions.getMicroClasses()) {
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
        logger.traceEntry("setEnd: end = {}", end);
        
        ObservableList<End> ends = getMicros(End.class);
        ends.clear();
        ends.add(end);
        
        logger.traceExit();
    }

//    //-------------------------------
//    // updates the condition bits
    public void setConditionBits(Collection<? extends ConditionBit> newConditionBits) {
        logger.traceEntry("setConditionBits: newConditionBits == {}", newConditionBits);
        if (conditionBits == newConditionBits) {
            logger.traceExit("Same collection passed, returning early.");
            return;
        }
        
        //first delete arithmetics, setCondBits, and increments that use
        //any deleted ConditionBits, including removing
        //these micros from all machine instructions that use them.

        final List<SetCondBit> setCondBits = getMicros(SetCondBit.class);
        setCondBits.stream().filter(micro -> !newConditionBits.contains(micro.getBit()))
                .forEach(micro -> {
                    removeAllOccurencesOf(micro);
                    setCondBits.remove(micro);
                });

        final List<Increment> increments = getMicros(Increment.class);
        increments.stream().filter(micro ->
                (micro.getOverflowBit().isPresent()
                        && !newConditionBits.contains(micro.getOverflowBit().get())))
                .forEach(micro -> {
                    removeAllOccurencesOf(micro);
                    increments.remove(micro);
                });

        conditionBits.clear();
        conditionBits.addAll(newConditionBits);
        
        logger.traceExit();
    }

    //-------------------------------
    // updates the RAM arrays
    public void setRAMs(final Collection<? extends RAM> newRams) {
        //first delete all MemoryAccess micros that reference any deleted rams
        //    and remove the micros from all machine instructions that use them.
        
        logger.traceEntry("setRAMs: newRams = {}", newRams);
        if (rams == newRams) {
            logger.traceExit("Same collection passed, short-circuiting");
            return;
        }
        
        getMicros(MemoryAccess.class).stream()
                .filter(access -> !newRams.contains(access.getMemory()))
                .forEach(access -> {
                    removeAllOccurencesOf(access);
                    getMicros(MemoryAccess.class).remove(access);
                });

        rams.clear();
        rams.addAll(newRams);
        
        logger.traceExit();
    }

    public ObservableList<Register> getAllRegisters() {
        return allRegisters.get();
    }
    
    /**
     * Gets a read-only property that includes <strong>all</strong> {@link Register} values including those
     * stored in {@link RegisterArray}s.
     *
     * @return Read-only property of all registers
     */
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
    public <U extends Microinstruction<U>> void setMicros(Class<U> microClazz, Collection<? extends U> newMicros) {
        //first delete all references in machine instructions
        // to any old micros not in the new list
        logger.traceEntry("setMicros: clazz = {}, newMicros = {}", microClazz, newMicros);
        
        List<U> original = getMicros(microClazz);
        if (original == newMicros) {
            logger.traceExit("New collection is the same as original, short-circuiting");
            return;
        }
        
        getMicros(microClazz).stream()
                .filter(oldMicro -> !newMicros.contains(oldMicro))
                .forEach(this::removeAllOccurencesOf);

        ListProperty<U> micros = getMicros(microClazz);
        micros.clear();
        micros.addAll(newMicros);
        
        logger.traceExit();
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
        logger.traceEntry("resetAllChannels");
        getMicros(IO.class).stream()
                .map(IO::getConnection)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(IOChannel::reset);
        logger.traceExit();
    }

    /**
     * Resets all of the channels except for the {@link StreamChannel#console()}.
     */
    public void resetAllChannelsButConsole() {
        logger.traceEntry("resetAllChannelsButConsole");
        
        getMicros(IO.class).stream()
                .map(IO::getConnection)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(c -> c != StreamChannel.console())
                .forEach(IOChannel::reset);
    
        logger.traceExit();
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
    
    
    public void setControlUnit(final ControlUnit controlUnit) {
        this.controlUnit.set(controlUnit);
    }
    
    public Optional<ControlUnit> getControlUnit() {
        return Optional.ofNullable(controlUnit.get());
    }

    public ObjectProperty<ControlUnit> controlUnitProperty() {
        return controlUnit;
    }

    //--------------------------------
    // get & set the fetch sequence

    public Optional<MachineInstruction> getFetchSequence() {
        return Optional.ofNullable(fetchSequence.get());
    }

    public void setFetchSequence(MachineInstruction f) {
        fetchSequence.set(f);
    }

    public ObjectProperty<MachineInstruction> fetchSequenceProperty() {
        return fetchSequence;
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


        ControlUnit controlUnit = getControlUnit()
                .orElseThrow(() -> new ExecutionException("No control unit set"));

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
                    final MachineInstruction fetchSequence = Machine.this.fetchSequence.get();
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
                                currentInstruction == fetchSequence) {
                            // it's the start of a machine cycle
                            if (codeStore.breakAtAddress((int)programCounter.getValue())
                                    && ! justBroke) {
                                RAMLocation breakLocation = codeStore.data()
                                        .get((int) programCounter.getValue());
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
                            io.getConnection().ifPresent(channel -> {
                                // FIXME #95
                                if (channel instanceof FileChannel &&
                                        io.getDirection().equals("output")) {
                                    ((FileChannel) channel).writeToFile();
                                }
                            });
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
        // FIXME Replace this with a binding to the Machine#indexFromRightProperty()

        for (ConditionBit cb : getModules(ConditionBit.class)) {
            cb.setBit(cb.getRegister().getWidth() - cb.getBit() - 1);
        }

        for (TransferRtoR tRtoR : getMicros(TransferRtoR.class)) {
            Register source = tRtoR.getSource()
                    .orElseThrow(() -> new IllegalStateException("TransferRtoR " + tRtoR.getName() + " does not have source register"));
            Register dest = tRtoR.getDest()
                    .orElseThrow(() -> new IllegalStateException("TransferRtoR " + tRtoR.getName() + " does not have dest register"));

            tRtoR.setDestStartBit(dest.getWidth() - tRtoR.getNumBits() - tRtoR.getDestStartBit());
            tRtoR.setSrcStartBit(source.getWidth() - tRtoR.getNumBits() - tRtoR.getSrcStartBit());
        }

        for (TransferRtoA tRtoA : getMicros(TransferRtoA.class)) {
            Register source = tRtoA.getSource()
                    .orElseThrow(() -> new IllegalStateException("TransferRtoA " + tRtoA.getName() + " does not have source register"));
            RegisterArray dest = tRtoA.getDest()
                    .orElseThrow(() -> new IllegalStateException("TransferRtoA " + tRtoA.getName() + " does not have dest register array"));
            Register index = tRtoA.getIndex()
                    .orElseThrow(() -> new IllegalStateException("TransferRtoA " + tRtoA.getName() + " does not have source register"));

            tRtoA.setDestStartBit(dest.getWidth() - tRtoA.getNumBits() - tRtoA.getDestStartBit());
            tRtoA.setSrcStartBit(source.getWidth() - tRtoA.getNumBits() - tRtoA.getSrcStartBit());
            tRtoA.setIndexStart(index.getWidth() - tRtoA.getIndexNumBits() - tRtoA.getIndexStart());
        }

        for (TransferAtoR tAtoR : this.getMicros(TransferAtoR.class)) {
            RegisterArray source = tAtoR.getSource()
                    .orElseThrow(() -> new IllegalStateException("TransferAtoR " + tAtoR.getName() + " does not have source register array"));
            Register dest = tAtoR.getDest()
                    .orElseThrow(() -> new IllegalStateException("TransferAtoR " + tAtoR.getName() + " does not have dest register"));
            Register index = tAtoR.getIndex()
                    .orElseThrow(() -> new IllegalStateException("TransferAtoR " + tAtoR.getName() + " does not have source register"));


            tAtoR.setDestStartBit(dest.getWidth() - tAtoR.getNumBits() - tAtoR.getDestStartBit());
            tAtoR.setSrcStartBit(source.getWidth() - tAtoR.getNumBits() - tAtoR.getSrcStartBit());
            tAtoR.setIndexStart(index.getWidth() - tAtoR.getIndexNumBits() - tAtoR.getIndexStart());
        }

        for (Test test : getMicros(Test.class)) {
            Register register = test.getRegister()
                    .orElseThrow(() -> new IllegalStateException("Test " + test.getName() + " does not have register"));

            test.setStart(register.getWidth() - test.getNumBits() - test.getStart());
        }

        for (SetBits set : getMicros(SetBits.class)) {
            Register register = set.getRegister()
                    .orElseThrow(() -> new IllegalStateException("SetBits " + set.getName() + " does not have register"));

            set.setStart(register.getWidth() - set.getNumBits() - set.getStart());
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
        checkNotNull(oldToNew, "Identifier Map is null");

        // Don't clone execution state.. this should be separated :|
        Machine newInst = new Machine(getName(), !isIndexFromRight());
        oldToNew.setNewMachine(newInst);

        Modules.getModuleClasses().forEach(mc -> copyModules(oldToNew, mc));
        Microinstructions.getMicroClasses().forEach(mc -> copyMicros(oldToNew, mc));

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
        
        if (programCounter.get() == null) {
            throw new ValidationException("Program counter register is not set.");
        }
    }


    /**
     * Used as a quick way to follow common behaviour from the {@link VisitResult} values when
     * visiting modules -- the code was very repetitive.
     *
     * @param check Lambda that does the visiting and returns a result
     * @return True if top-level traversal should continue
     */
    private static boolean visitModuleHelper(Supplier<VisitResult> check) {
        switch (check.get()) {
            case SkipSiblings:
            case Stop:
                // If skipping now, we just return.
                return false;

            case SkipChildren:
            case Continue:
                return true;

            default:
                throw new IllegalStateException("VisitResult was not properly handled.");
        }
    }

    /**
     * Used to reduce repeated code via functional calls to the {@code Machine} {@link Module} properties.
     *
     * @param property List of values that must be checked
     * @param start The method that "starts" visiting modules
     * @param visit Actual visit method
     * @param end The method that "ends" visiting modules
     * @param <T> Type of the property
     * @return The result of the visit calls, returns the last call returned
     */
    private static
    <T extends Module<T>> VisitResult visitModulesHelperI(
            ObservableList<T> property,
            final Function<ObservableList<T>, VisitResult> start,
            final Function<T, VisitResult> visit,
            final Function<ObservableList<T>, VisitResult> end) {

        VisitResult startResult = start.apply(property);
        switch (startResult) {

            case SkipSiblings:
            case SkipChildren:
            case Stop:
                return startResult;

            case Continue: {
                // visit the children now
                ChildrenLoop: for (T c: property) {
                    VisitResult childVR = visit.apply(c);
                    switch (childVR) {

                        case Stop:
                            return childVR;

                        case SkipSiblings:
                            break ChildrenLoop;

                        case SkipChildren:
                        case Continue:
                            break;
                    }
                }

                return end.apply(property);
            }
        }

        throw new IllegalStateException("Got unknown result from start: " + startResult);
    }

    /**
     * Utilizes {@link Property#getName()} to trigger the correct visit calls of
     * {@link #visitModulesHelperI(ObservableList, Function, Function, Function)}.
     *
     * This is the simplest, cleanest way to do this known. If not done this way, type-safety is thrown out
     * because everything inherits from {@link Module} and there's no discerning the different values.
     *
     * @param property Property to visit
     * @param visitor The visitor in use
     * @return The last result of the visit
     *
     * @throws IllegalStateException if the property's name is not known.
     */
    @SuppressWarnings("unchecked")
    private static
    VisitResult visitModulesHelper(ListProperty<? extends Module<?>> property, ModuleVisitor visitor) {
        checkNotNull(property, "property == null");

        switch (property.getName()) {
            case PROPERTY_NAME_RAMS: {
                return visitModulesHelperI((ListProperty<RAM>)property,
                        visitor::startRams,
                        visitor::visitRam,
                        visitor::endRams);
            }

            case PROPERTY_NAME_CONDITION_BITS: {
                return visitModulesHelperI((ListProperty<ConditionBit>)property,
                        visitor::startConditionBits,
                        visitor::visitConditionBit,
                        visitor::endConditionBits);
            }

            case PROPERTY_NAME_REGISTERS: {
                return visitModulesHelperI((ListProperty<Register>)property,
                        visitor::startRegisters,
                        visitor::visitRegister,
                        visitor::endRegisters);
            }

            case PROPERTY_NAME_REGISTER_ARRAYS: {
                return visitModulesHelperI((ListProperty<RegisterArray>)property,
                        visitor::startRegisterArrays,
                        visitor::visitRegisterArray,
                        visitor::endRegisterArray);
            }

            default: {
                throw new IllegalStateException("Unknown property passed: " + property.getName());
            }
        }
    }

    /**
     * Visits all of the {@link Module Modules} in the {@link Machine} in a non-guaranteed order using
     * a {@link ModuleVisitor}.
     * @param visitor
     *
     * @see <a href="https://en.wikipedia.org/wiki/Visitor_pattern">Visitor Pattern</a>
     */
    public void acceptModulesVisitor(@Nonnull ModuleVisitor visitor) {
        checkNotNull(visitor, "visitor == null");

        if (visitModuleHelper(() -> visitor.visitCodeStore(codeStore.get()))) {
            return;
        }

        if (visitModuleHelper(() -> visitor.visitProgramCounter(programCounter.get()))) {
            return;
        }

        if (visitModuleHelper(() -> visitor.visitControlUnit(controlUnit.get()))) {
            return;
        }

        for (ListProperty<? extends Module<?>> property: moduleMap.values()) {
            switch (visitModulesHelper(property, visitor)) {
                case SkipSiblings:
                case Stop:
                    return;
                case SkipChildren:
                case Continue:
                    break;
            }
        }
    }

    /**
     * Visits all of the {@link Microinstruction Microinstructions} in the {@code Machine} using the
     * {@link MicroinstructionVisitor} passed. The order of visiting is <strong>not</strong> guaranteed.
     *
     * @param visitor Used to visit each value
     *
     * @see <a href="https://en.wikipedia.org/wiki/Visitor_pattern">Visitor Pattern</a>
     */
    public void acceptMicrosVisitor(@Nonnull MicroinstructionVisitor visitor) {
        checkNotNull(visitor, "visitor == null");

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

                case Continue:
                    break;
            }

            List<Microinstruction<?>> sortedMicros = microMap.get(mc).stream()
                    .sorted(Comparator.comparing(NamedObject::getName))
                    .map(v -> (Microinstruction<?>)v)
                    .collect(Collectors.toList());

            MicroLoop: for (Microinstruction<?> micro: sortedMicros) {
                switch (visitor.visitMicro(micro)) {
                    case SkipChildren:
                    case Continue:
                        continue MicroLoop;

                    case SkipSiblings:
                        break MicroLoop;

                    case Stop:
                        break CategoryLoop;
                }
            }
        }
    }

    /**
     * Accepts a {@link MachineVisitor} to allow for simple traversal of a {@code Machine}.
     *
     * There are <strong>no ordering guarantees</strong> of this traversal except that all components will be
     * visited eventually.
     *
     * @param visitor Visitor that will be used with all parts.
     */
    public void acceptVisitor(@Nonnull MachineVisitor visitor) {
        checkNotNull(visitor, "visitor == null");

        // used to pass a variable between lambdas .. the variable "appears" final
        // to the compiler, it's a hack, but it works. :)
        class WrappedBool {
            private boolean value = false;
        }

        visitor.visitName(getName());
        visitor.visitStartingAddressForLoading(getStartingAddressForLoading());
        visitor.visitIndexFromRight(isIndexFromRight());

        WrappedBool shouldExit = new WrappedBool();

        visitor.getModuleVisitor().ifPresent(mv -> {
            switch (visitor.startModules()) {
                case SkipSiblings:
                case Stop:
                    shouldExit.value = true;
                    return;
                case SkipChildren:
                    break;
                case Continue: {
                    acceptModulesVisitor(mv);
                    switch (visitor.endModules()) {
                        case SkipSiblings:
                        case Stop:
                            shouldExit.value = true;
                            return;

                        case SkipChildren:
                        case Continue:
                            break;
                    }
                } break;
            }
        });

        if (!shouldExit.value) {
            visitor.getMicrosVisitor().ifPresent(mv -> {
                switch (visitor.startMicros()) {
                    case SkipSiblings:
                    case Stop:
                        return;
                    case SkipChildren:
                        break;
                    case Continue: {
                        acceptMicrosVisitor(mv);
                        switch (visitor.endMicros()) {
                            case SkipSiblings:
                            case Stop:
                                return;

                            case SkipChildren:
                            case Continue:
                                break;
                        }
                    } break;
                }
            });
        }

        switch (visitor.startFields(getFields())) {
            case SkipSiblings:
            case Stop:
                return;

            case SkipChildren:
                break;

            case Continue: {
                fieldLoop: for (Field f: fields) {
                    switch (visitor.visitField(f)) {
                        case SkipSiblings:
                        case Stop:
                            return;

                        case SkipChildren:
                            break fieldLoop; // break the loop

                        case Continue:
                            break;
                    }
                }

                visitor.endFields(fields);
            } break;
        }
    }


}