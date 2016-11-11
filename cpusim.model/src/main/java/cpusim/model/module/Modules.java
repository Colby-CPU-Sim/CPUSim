package cpusim.model.module;

import cpusim.model.Module;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Modules utility.
 */
public abstract class Modules {
    
    private Modules() {
        // no-op, constructor
    }
    
    /**
     * Copies data from an input List, checking the `assocList` parameter for clones if they exist.
     * @param list Values
     * @param assocList Mapping between modules and clones
     * @param <T>
     * @return
     */
    public static <T extends Module<T>> List<T> createNewModulesListWithAssociation(List<T> list, Map<? extends T, ? extends T> assocList) {
        checkNotNull(assocList);
        
        final List<T> out = new ArrayList<>();
        
        for (final T module : checkNotNull(list)) {
            
            @SuppressWarnings("unchecked")
            T old = (T)assocList.get(module);
            
            if (old != null) {
                //if the new value is just an edited clone of an old module,
                //then just copy the new data to the old module
                module.copyTo(old);
                out.add(old);
            }
        }
        
        return out;
    }
    
    /**
     * Copies data from an input List, checking the `assocList` parameter for clones if they exist.
     * @param list Values
     * @param assocList Mapping between modules and clones
     * @param <T>
     * @return
     *
     * @see #createNewModulesListWithAssociation(List, Map)
     */
    public static <T extends Module<T>> List<T> createNewModulesListWithAssociationUnsafe(List<T> list, Map<Module<?>, Module<?>> assocList) {
        checkNotNull(assocList);
        
        @SuppressWarnings("unchecked")
        final Map<T, T> m = (Map<T, T>)assocList;
        
        return createNewModulesListWithAssociation(list, m);
    }
}
