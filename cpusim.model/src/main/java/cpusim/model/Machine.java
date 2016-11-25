/**
 * File: Machine
 * Last update: October 2013
 */

/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 12/5/13
 * with the following changes:
 *
 * 1.) Changed execute so it write to the output file when the state reaches the end of
  * a program
 */
package cpusim.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import cpusim.model.assembler.EQU;
import cpusim.model.assembler.PunctChar;
import cpusim.model.assembler.PunctChar.Use;
import cpusim.model.iochannel.FileChannel;
import cpusim.model.iochannel.IOChannel;
import cpusim.model.iochannel.StreamChannel;
import cpusim.model.microinstruction.*;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.ControlUnit;
import cpusim.model.module.RAM;
import cpusim.model.module.RAMLocation;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import cpusim.model.util.Validatable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.*;

/**
 * This file contains the class for Machines created using CPU Sim
 * <p>
 * Class Modifications:
 * Generics added to setRegisterArrays, getMicrosThatUse, setEnd, setRAMs,
 * haltBitsThatAreSet and getInstructionsThatUse methods.
 * <p>
 * In the execute method we added "fire property change" to indicate the start
 * of a microinstruction. This property change is used to indicate when to put
 * a new microInstructionStack of HashMaps as an element in the
 * machineInstructionStack.
 */
public class Machine extends Module<Machine> implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final Register PLACE_HOLDER_REGISTER =
                               new Register("(none)",64,Long.MAX_VALUE,true);


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
     * Get all of the supported implementing {@link Class} values for the {@link Microinstruction}.
     * @return
     */
    public static ImmutableList<Class<? extends Microinstruction>> getMicroClasses() {
        ImmutableList.Builder<Class<? extends Microinstruction>> bld = ImmutableList.builder();
        Arrays.stream(MicroClassMapping.values()).map(v -> v.instructionType).forEach(bld::add);
        return bld.build();
    }

    public static Optional<Class<? extends Microinstruction>> getMicroClassByName(String name) {
        return getMicroClasses().stream().filter(c -> c.getSimpleName().equals(name)).findFirst();
    }
    
    /**
     * Get all of the supported implementing {@link Class} values for {@link Module} instances.
     * @return
     */
    public static ImmutableList<Class<? extends Module<?>>> getModuleClasses() {
        ImmutableList.Builder<Class<? extends Module<?>>> bld = ImmutableList.builder();
        Arrays.stream(ModuleClassMapping.values()).map(v -> v.moduleType).forEach(bld::add);
        return bld.build();
    }


    /**
     * the hardware modules making up the machine
     */
    private ObservableList<Register> registers;
    private ObservableList<RegisterArray> registerArrays;
    private ObservableList<ConditionBit> conditionBits;
    private ObservableList<RAM> rams;

    /**
     * the current state of the machine, defined by enum state
     * and associated Object value
     */
    private SimpleObjectProperty<StateWrapper> wrappedState;

    // Machine instructions
    private List<MachineInstruction> instructions;
    // EQUs
    private ObservableList<EQU> EQUs;
    // key = micro name, value = list of microinstructions
    private Map<Class<? extends Microinstruction>, ObservableList<? extends Microinstruction>> microMap;
    // key = module type, value = list of modules
    private Map<Class<? extends Module<?>>, ObservableList<? extends Module<?>>> moduleMap;
    // Control unit for keeping track of index of micro within machine instruction
    private ControlUnit controlUnit;
    // The machine's fetch sequence
    private MachineInstruction fetchSequence;
    // Can be: RUN, STEP_BY_MICRO, STEP_BY_INSTR, STOP, ABORT, COMMAND_LINE
    // It is volatile to ensure that all threads can read it at any time
    private volatile RunModes runMode = RunModes.ABORT;
    // Fields of the machine instructions
    private List<Field> fields;
    // Array of PunctChars.
    private List<PunctChar> punctChars;
    // Address to start when loading RAM with a program
    private int startingAddressForLoading;
    // RAM where the code store resides
    private SimpleObjectProperty<RAM> codeStore;
    // True if bit indexing starts of the right side, false if on the left
    private SimpleBooleanProperty indexFromRight;
    // Register used for stopping at break points--initially null
    private Register programCounter;
    // true if the machine just halted due to a breakpoint.  It is used to turn off the
    // break point temporarily to allow continuing past the breakpoint.
    private boolean justBroke;

    /**
     * Creates a new machine.
     *
     * @param name - The name of the machine.
     */
    public Machine(String name) {
        super(name);
        registers = FXCollections.observableArrayList();
        registerArrays = FXCollections.observableArrayList();
        conditionBits = FXCollections.observableArrayList();
        rams = FXCollections.observableArrayList();
        wrappedState = new SimpleObjectProperty<>(this, "machine state", new
                StateWrapper(State.NEVER_RUN, ""));

        moduleMap = new HashMap<>();
        microMap = new HashMap<>();

        instructions = new ArrayList<>();
        EQUs = FXCollections.observableArrayList(new ArrayList<EQU>());
        fields = new ArrayList<>();
        punctChars = Lists.newArrayList(getDefaultPunctChars());

        fetchSequence = new MachineInstruction("Fetch sequence", 0, "", this);
        controlUnit = new ControlUnit("ControlUnit", this);

        startingAddressForLoading = 0;
        programCounter = PLACE_HOLDER_REGISTER;
        codeStore = new SimpleObjectProperty<>(null);
        indexFromRight = new SimpleBooleanProperty(true); //conventional indexing order
        justBroke = false;
        initializeModuleMap();
        initializeMicroMap();

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
    public SimpleObjectProperty<StateWrapper> stateProperty() {
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

    public List<PunctChar> getPunctChars() {
        return punctChars;
    }

    public void setPunctChars(List<PunctChar> punctChars) {
        this.punctChars = checkNotNull(punctChars);
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
        assert false : "No comment char found in the machine's punctChars";
        return ';';  //to satisfy the compiler
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> f) {
        fields = f;
    }

    /**
     * A getter method for all module objects
     *
     * @param moduleType a String that describes the type of module
     * @return a Vector object
     *
     * @deprecated Use {@link #getModule(Class)}
     */
    public ObservableList<? extends Module<?>> getModule(String moduleType) {
        return moduleMap.get(ModuleClassMapping.fromName(moduleType));
    }
    
    /**
     * A getter method for all module objects
     *
     * @param moduleClazz a String that describes the type of module
     * @return a Vector object
     */
    @SuppressWarnings("unchecked")
    public <T extends Module<T>> ObservableList<T> getModule(final Class<T> moduleClazz) {
        return (ObservableList<T>)moduleMap.get(moduleClazz);
    }
    
    /**
     * A getter method for all module objects
     *
     * @param moduleClazz a String that describes the type of module
     * @return a Vector object
     */
    @SuppressWarnings("unchecked")
    public ObservableList<? extends Module<?>> getModuleUnchecked(final Class<? extends Module<?>> moduleClazz) {
        return moduleMap.get(moduleClazz);
    }
    
    /**
     * Gets all of the modules into a single {@link ImmutableList}.
     * @return
     */
    public ImmutableList<List<? extends Module<?>>> getAllModules() {
        ImmutableList.Builder<List<? extends Module<?>>> moduleBuilder = ImmutableList.builder();
        Machine.getModuleClasses().stream().map(c -> machine.getModuleUnchecked(c)).forEach(moduleBuilder::add);
        return moduleBuilder.build();
    }

    /**
     * A getter method for all microinstructions
     *
     * @param micro a String that describes the microinstruction type
     * @return a Vector object
     *
     * @deprecated Use {@link #getMicros(Class)}
     */
    public ObservableList<? extends Microinstruction> getMicros(String micro) {
        return microMap.get(MicroClassMapping.fromName(micro));
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
    public <U extends Microinstruction> ObservableList<U> getMicros(Class<U> clazz) {
        return (ObservableList<U>)microMap.get(clazz);
    }
    
    /**
     * Get all of the loaded {@link Microinstruction} in the {@link Machine}.
     * @return
     */
    public List<List<? extends Microinstruction>> getAllMicros() {
        ImmutableList.Builder<List<? extends Microinstruction>> microBuilder = ImmutableList.builder();
        getMicroClasses().stream().map(c -> machine.getMicros(c)).forEach(microBuilder::add);
    
        return microBuilder.build();
    }

    public List<MachineInstruction> getInstructions() {
        return instructions;
    }

    public ObservableList<EQU> getEQUs() {
        return EQUs;
    }


    public End getEnd() {
        return (End) (microMap.get("end")).get(0);
    }

    public int getStartingAddressForLoading() {
        return startingAddressForLoading;
    }

    public SimpleObjectProperty<RAM> getCodeStoreProperty() {
        return codeStore;
    }

    public boolean getIndexFromRight() {
        return indexFromRight.get();
    }

    public void setIndexFromRight(boolean b) {
        indexFromRight.set(b);
    }

    public RAM getCodeStore() {
        return codeStore.get();
    }

    public void setCodeStore(RAM r) {
        codeStore.set(r);
    }

    public void setStartingAddressForLoading(int add) {
        startingAddressForLoading = add;
    }

    public Register getProgramCounter() {
        return programCounter;
    }

    public void setProgramCounter(Register programCounter) {
        this.programCounter = programCounter;
    }

    private void initializeModuleMap() {
        moduleMap.put(Register.class, registers);
        moduleMap.put(RegisterArray.class, registerArrays);
        moduleMap.put(ConditionBit.class, conditionBits);
        moduleMap.put(RAM.class, rams);
    }


    private void initializeMicroMap() {
        for (Class<? extends Microinstruction> microClazz : getMicroClasses())
            microMap.put(microClazz, FXCollections.observableArrayList(new ArrayList<>()));
        getMicros(End.class).add(new End(this));
        getMicros(Comment.class).add(new Comment());
    }
    
    
    /**
     *
     * @return a new {@link ObservableList} containing all the registers of the machine
     */
    
    // with the registers coming from register arrays at the end of the vector.
    public ObservableList<Register> getAllRegisters() {
        ObservableList<Register> allRegisters = FXCollections.observableArrayList();
        
        for (Register register1 : registers) {
            allRegisters.add(register1);
        }

        for (RegisterArray registerArray : registerArrays) {
            allRegisters.addAll(registerArray.registers());
        }
        
        return allRegisters;
    }
    
    /**
     * @returns a new observable list containing all the RAMs of the machine.
     */
    public ObservableList<RAM> getAllRAMs() {
        return getModule(RAM.class);
    }
    
    /**
     * Updates the machine instructions.
     * @param newInstructions
     */
    public void setInstructions(List<MachineInstruction> newInstructions) {
        instructions = newInstructions;
    }

    //-------------------------------
    // updates global EQUs
    public void setEQUs(ObservableList<EQU> newEQUs) {
        EQUs = newEQUs;
    }

    //-------------------------------
    // updates the registers
    
    /**
     * Removes a {@link Module} from the machine
     * @param module
     */
    private void removeMicro(final Module<?> module) {
        Map<Microinstruction, ObservableList<Microinstruction>> microsThatUseIt = getMicrosThatUse(module);
        Set<Microinstruction> e = microsThatUseIt.keySet();
        for (Microinstruction micro : e) {
            //remove it from all machine instructions
            removeAllOccurencesOf(micro);
            //also remove the microinstruction from its list.
            microsThatUseIt.get(micro).remove(micro);
        }
    }
    
    public void setRegisters(List<Register> newRegisters) {
        for (Register oldRegister : registers) {
            if (!newRegisters.contains(oldRegister)) {
                removeMicro(oldRegister);
            }
        }

        //reuse the old Vector since the desktop uses it as the key
        //in the moduleWindows hashtable.
        registers.clear();
        registers.addAll(newRegisters);


        // test whether the program counter was deleted and, if so,
        // set the program counter to the place holder register
        if(! newRegisters.contains(programCounter))
            setProgramCounter(Machine.PLACE_HOLDER_REGISTER);
    }

    //-------------------------------
    // updates the register arrays
    public void setRegisterArrays(final List<RegisterArray> newRegisterArrays) {
        for (RegisterArray oldArray : registerArrays) {
            if (!newRegisterArrays.contains(oldArray)) {
                removeMicro(oldArray);
            }
        }

        //reuse the old Vector in case in the future some other objects use it.
        registerArrays.clear();
        registerArrays.addAll(newRegisterArrays);

        // test whether the program counter was deleted and, if so,
        // set the program counter to the place holder register
        for(RegisterArray array : newRegisterArrays) {
            if (array.registers().contains(programCounter))
                return;
        }
        setProgramCounter(Machine.PLACE_HOLDER_REGISTER);
    }

    //--------------------------------
    // returns a HashMap whose keys consist of all microinstructions that
    // use m and such that the value associated with each key is the List
    // of microinstructions that contains the key.
    public Map<Microinstruction, ObservableList<Microinstruction>> getMicrosThatUse(Module<?> m) {
        final Map<Microinstruction, ObservableList<Microinstruction>> result = new HashMap<>(getMicroClasses().size());

        for (Class<? extends Microinstruction> mc : getMicroClasses()) {
            ObservableList<? extends Microinstruction> v = getMicros(mc);
            v.stream().filter(micro -> micro.uses(m))
                    .collect(Collectors.toMap(Function.identity(), _ignore -> v));
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

    //-------------------------------
    // updates the condition bits
    public void setConditionBits(List<ConditionBit> newConditionBits) {
        //first delete arithmetics, setCondBits, and increments that use
        //any deleted ConditionBits, including removing
        //these micros from all machine instructions that use them.

        final List<Arithmetic> arithmetics = getMicros(Arithmetic.class);
        arithmetics.stream().filter(micro ->
                (((!newConditionBits.contains(micro.getOverflowBit())) &&
                    (micro.getOverflowBit() != ConditionBit.none())) ||
                    ((!newConditionBits.contains(micro.getCarryBit())) &&
                            (micro.getCarryBit() != ConditionBit.none())))
        ).forEach(m -> {
            removeAllOccurencesOf(m);
            arithmetics.remove(m);
        });

        final List<SetCondBit> setCondBits = getMicros(SetCondBit.class);
        setCondBits.stream().filter(micro -> !newConditionBits.contains(micro.getBit()))
                .forEach(micro -> {
                    removeAllOccurencesOf(micro);
                    setCondBits.remove(micro);
                });

        final List<Increment> increments = getMicros(Increment.class);
        increments.stream().filter(micro ->
                ((!newConditionBits.contains(micro.getOverflowBit())) &&
                    (micro.getOverflowBit() != ConditionBit.none())))
                .forEach(micro -> {
                    removeAllOccurencesOf(micro);
                    increments.remove(micro);
                });

        conditionBits.clear();
        conditionBits.addAll(newConditionBits);
    }

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

    //-------------------------------
    // updates the given vector of micros of a given class, but first deletes
    // all references to a deleted micro
    
    /**
     *
     * @param microClazz
     * @param newMicros
     */
    public <U extends Microinstruction> void setMicros(Class<U> microClazz, ObservableList<U> newMicros) {
        //first delete all references in machine instructions
        // to any old micros not in the new list
        getMicros(microClazz).stream()
                .filter(oldMicro -> !newMicros.contains(oldMicro))
                .forEach(this::removeAllOccurencesOf);
        
        microMap.put(microClazz, newMicros);
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
        getModule(Register.class).forEach(Register::clear);
    }

    /**
     * Calls {@link Register#clear()} on all {@link Register} values.
     */
    public void clearAllRegisterArrays() {
        getModule(RegisterArray.class).forEach(RegisterArray::clear);
    }

    /**
     * clearAllRAMs
     * @since 2000-11-03
     */
    public void clearAllRAMs() {
        getModule(RAM.class).forEach(RAM::clear);
    }

    /**
     * @return {@link List} of all {@link Microinstruction}s that use m
     */
    public List<MachineInstruction> getInstructionsThatUse(final Microinstruction m) {
        List<MachineInstruction> result = instructions.stream()
                .filter(instr -> instr.usesMicro(m))
                .collect(Collectors.toList());
        
        //don't forget the fetchSequence too.
        if (fetchSequence.usesMicro(m)) {
            result.add(fetchSequence);
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
        fetchSequence.removeMicro(m);
    }

    //--------------------------------
    // get the control unit

    public ControlUnit getControlUnit() {
        return controlUnit;
    }


    //--------------------------------
    // get & set the fetch sequence

    public MachineInstruction getFetchSequence() {
        return fetchSequence;
    }

    public void setFetchSequence(MachineInstruction f) {
        fetchSequence = f;
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
        setRunMode(mode);


        if (mode == RunModes.COMMAND_LINE) {
            // do not use the GUI at all--purely command line execution
            // There is no stepping or backing up.  It executes in the
            // main (and only) thread until it finishes or the user
            // quits it from the command line (like with Ctrl-C).
            while (runMode != RunModes.STOP &&
                    runMode != RunModes.ABORT &&
                    haltBitsThatAreSet().size() == 0) {

                MachineInstruction currentInstruction = controlUnit.getCurrentInstruction();
                List<Microinstruction> microInstructions =
                        currentInstruction.getMicros();
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
                    while (runMode != RunModes.STOP &&
                            runMode != RunModes.ABORT &&
                            !isCancelled() &&
                            haltBitsThatAreSet().size() == 0) {

                        MachineInstruction currentInstruction =
                                controlUnit.getCurrentInstruction();
                        List<Microinstruction> microInstructions =
                                currentInstruction.getMicros();
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
                            if (getCodeStore().breakAtAddress((int)programCounter.getValue())
                                    && ! justBroke) {
                                RAMLocation breakLocation = codeStore.get().data().get((int)
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
                        Microinstruction currentMicro = microInstructions.get
                                (currentIndex);
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
                        if (runMode == RunModes.STEP_BY_INSTR && currentMicro == getEnd
                                ()) {
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
     * CpusimSet (Set) micros
     * Test micros
     */
    public void changeStartBits() {
        for (ConditionBit cb : getModule(ConditionBit.class)) {
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

        for (CpusimSet set : getMicros(CpusimSet.class)) {
            set.setStart(set.getRegister().getWidth() - set.getNumBits() - set.getStart());
        }
    }

    //--------------------------------
    // haltBitsThatAreSet: returns a Vector of all condition bits
    // specified by all Halt micros have value 1

    public List<ConditionBit> haltBitsThatAreSet() {
        return getModule(ConditionBit.class).stream()
                .filter(c -> c.getHalt() && c.isSet())
                .collect(Collectors.toList());
    }

    public ObservableList<Register> getRegisters() {
        return registers;
    }


    public List<Comment> getCommentMicros() {
        //go through all the instrs and get their Comment micros in a list.
        List<Comment> result = new ArrayList<Comment>();
        for (MachineInstruction instr : instructions) {
            List<Microinstruction> micros = instr.getMicros();
            for (Microinstruction micro : micros)
                if (micro instanceof Comment) {
                    result.add((Comment) micro);
                }
        }
        //now do the same for the fetch sequence
        for (Microinstruction micro : fetchSequence.getMicros()) {
            if (micro instanceof Comment) {
                result.add((Comment) micro);
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

	@Override
	public <U extends Machine> void copyTo(U other) {
		throw new UnsupportedOperationException("Unimplemented.");
	}
    
    @Override
    protected void validateState() {
        // Validate all of the internal state
        moduleMap.values().stream().flatMap(List::stream).forEach(Validatable::validate);
        microMap.values().stream().flatMap(List::stream).forEach(Validatable::validate);
    }
    
    /**
     * Type to map between older {@link String} names to {@link Class} instances. This is only for transitioning. Later,
     * perhaps injection.
     *
     * @deprecated Do not use, transition to using {@link Class} values directly.
     */
    private enum MicroClassMapping {
        ARITHMETIC("arithmetic", Arithmetic.class)
        , BRANCH("branch", Branch.class)
        , DECODE("decode", Decode.class)
        , END("end", End.class)
        , COMMENT("comment", Comment.class)
        , INCREMENT("increment", Increment.class)
        , IO("io", IO.class)
        , LOGICAL("logical", Logical.class)
        , MEMORY_ACCESS("memoryAccess", MemoryAccess.class)
        , SET("set", CpusimSet.class)
        , SET_COND_BIT("setCondBit", SetCondBit.class)
        , SHIFT("shift", Shift.class)
        , TEST("test", Test.class)
        , TRANSFER_R2R("transferRtoR", TransferRtoR.class)
        , TRANSFER_R2A("transferRtoA", TransferRtoA.class)
        , TRANSFER_A2R("transferAtoR", TransferAtoR.class);

        final String name;
        final Class<? extends Microinstruction> instructionType;

        private static final ImmutableMap<String, MicroClassMapping> FROM_NAME
                = ImmutableMap.copyOf(Arrays.stream(MicroClassMapping.values())
                .collect(Collectors.toMap(m -> m.name, Function.identity())));

        MicroClassMapping(final String name, final Class<? extends Microinstruction> clazz) {
            this.name = name;
            this.instructionType = clazz;
        }

        /**
         * Get the {@link MicroClassMapping} from the value stored from {@link #getName()}.
         * @param name Name to find
         *
         * @throws NoSuchElementException if the {@code name} does not exist.
         */
        public static Class<? extends Microinstruction> fromName(final String name) {
            final MicroClassMapping m = FROM_NAME.get(name);
            if (m == null) {
                throw new NoSuchElementException("Name does not exist: " + name);
            }

            return m.instructionType;
        }
    }


    /**
     *
     * @deprecated This is a transition type between {@link String} values and {@link Class}-based {@link Map}.
     */
    private enum ModuleClassMapping {

        REGISTER("arithmetic", Register.class)
        , REGISTER_ARRAY("registerArrays", RegisterArray.class)
        , RAM("rams", RAM.class)
        , CONDITION_BIT("conditionBits", ConditionBit.class);

        private final String name;
        private final Class<? extends Module<?>> moduleType;


        private static final ImmutableMap<String, ModuleClassMapping> FROM_NAME
                = ImmutableMap.copyOf(Arrays.stream(ModuleClassMapping.values())
                .collect(Collectors.toMap(m -> m.name, Function.identity())));

        ModuleClassMapping(final String name, final Class<? extends Module<?>> clazz) {
            this.name = name.toLowerCase();
            this.moduleType = clazz;
        }

        /**
         * Get the {@link ModuleClassMapping} from the value stored from {@link #getName()}.
         * @param name Name to find
         * @return
         *
         * @throws NoSuchElementException if the {@code name} does not exist.
         */
        public static Class<? extends Module<?>> fromName(final String name) {
            final ModuleClassMapping m = FROM_NAME.get(name.toLowerCase());
            if (m == null) {
                throw new NoSuchElementException("Name does not exist: " + name);
            }

            return m.moduleType;
        }
    }

}