package cpusim.gui.editmicroinstruction;

import com.google.common.collect.ImmutableMap;
import cpusim.Mediator;
import cpusim.model.microinstruction.Microinstruction;
import cpusim.model.microinstruction.*;
import cpusim.model.util.Copyable;

import javax.annotation.concurrent.Immutable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mapping from a {@link Microinstruction} sub-type to a {@link MicroController} instance.
 */
@Immutable
final class ImmutableMicroControllerMap implements Map<Class<? extends Microinstruction>, MicroController<? extends Microinstruction>> {

    /**
     * An {@link ImmutableMap} that holds types as the keys and sub {@link MicroController} as types.
     */
    private ImmutableMap<Class<? extends Microinstruction>, MicroController<?>> typesMap;
    
    /**
     * Creates a new instance of a {@link ImmutableMicroControllerMap}.
     * @param mediator
     */
    ImmutableMicroControllerMap(final Mediator mediator) {
        checkNotNull(mediator);
        
        typesMap = ImmutableMap.<Class<? extends Microinstruction>, MicroController<?>>builder()
                .put(CpusimSet.class, new SetTableController(mediator))
                .put(Test.class, new TestTableController(mediator))
                .put(Increment.class, new IncrementTableController(mediator))
                .put(Shift.class, new ShiftTableController(mediator))
                .put(Logical.class, new LogicalTableController(mediator))
                .put(Arithmetic.class, new ArithmeticTableController(mediator))
                .put(Branch.class, new BranchTableController(mediator))
                .put(TransferRtoR.class, new TransferRtoRTableController(mediator))
                .put(TransferRtoA.class, new TransferRtoATableController(mediator))
                .put(TransferAtoR.class, new TransferAtoRTableController(mediator))
                .put(Decode.class, new DecodeTableController(mediator))
                .put(SetCondBit.class, new SetCondBitTableController(mediator))
                .put(IO.class, new IOTableController(mediator))
                .put(MemoryAccess.class, new MemoryAccessTableController(mediator))
                .build();
    }
    
    @Override
    public int size() {
        return typesMap.size();
    }
    
    @Override
    public boolean isEmpty() {
        return typesMap.isEmpty();
    }
    
    @Override
    public boolean containsKey(final Object key) {
        return typesMap.containsKey(key);
    }
    
    @Override
    public boolean containsValue(final Object value) {
        return typesMap.containsValue(value);
    }
    
    @Override
    public MicroController<?> get(final Object key) {
        if (CharSequence.class.isAssignableFrom(key.getClass())) {
            String strKey = key.toString();
            
            try {
                return typesMap.get(Class.forName(strKey));
            } catch (ClassNotFoundException cnfe) {
                // The Map#get(Object) format says to return null, not throw an exception. :(
                return null;
            }
        } else if (key instanceof Class) {
            return typesMap.get(key);
        } else {
            throw new IllegalArgumentException("Unknown type passed: " + key.getClass().getName());
        }
    }
    
    @Override
    public Set<Class<? extends Microinstruction>> keySet() {
        return typesMap.keySet();
    }
    
    @Override
    public Collection<MicroController<?>> values() {
        return typesMap.values();
    }
    
    @Override
    public Set<Entry<Class<? extends Microinstruction>, MicroController<?>>> entrySet() {
        return typesMap.entrySet();
    }
    
    public <Ins extends Microinstruction & Copyable<Ins>, Ctrl extends MicroController<Ins>>
    Ctrl get(Class<Ins> keyClazz, Class<Ctrl> valueClazz) {
        
        @SuppressWarnings("unchecked") final Ctrl out = (Ctrl)typesMap.get(keyClazz);
        checkState(keyClazz.isAssignableFrom(out.getMicroClass()),
                "MicroController does not store a valid MicroInstruction class.");
        
        return out;
    }
    
    /**
     * Get a {@link MicroController} instance based on the `keyClazz`.
     * @param keyClazz
     * @param <Ins>
     * @return
     */
    public <Ins extends Microinstruction> MicroController<? extends Microinstruction> getController(Class<Ins> keyClazz) {
        
        for (Class<? extends Microinstruction> clazz : typesMap.keySet()) {
            if (clazz.isAssignableFrom(keyClazz)) {
                MicroController<?> out = typesMap.get(clazz);
    
                return out;
            }
        }
        
        throw new IllegalArgumentException(keyClazz.getName() + " is not supported.");
        
    }
    
    // Unused methods:
    
    @Override
    public MicroController<?> put(final Class<? extends Microinstruction> key, final MicroController<?> value) {
        throw new UnsupportedOperationException("#put(...) is unsupported, this class is Immutable.");
    }
    
    @Override
    public MicroController<?> remove(final Object key) {
        throw new UnsupportedOperationException("#remove(...) is unsupported, this class is Immutable.");
    }
    
    @Override
    public void putAll(final Map<? extends Class<? extends Microinstruction>, ? extends MicroController<?>> m) {
        throw new UnsupportedOperationException("#putAll(...) is unsupported, this class is Immutable.");
        
    }
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException("#clear() is unsupported, this class is Immutable.");
    }
    
}
