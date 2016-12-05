/**
 * 
 */
package cpusim.model.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

/**
 * Collection of utilities for FXCollections.
 * 
 * @author Kevin Brightwell
 * @since 2016-09-20
 */
public class MoreFXCollections {
	
	/**
	 * Creates a copy of an {@link ObservableMap} backed by a {@link HashMap}.
	 * 
	 * @param values
	 * @return non-<code>null</code> {@link ObservableMap}.
	 * 
	 * @throws NullPointerException if values is null. 
	 */
	public static <K, V> ObservableMap<K, V> copyObservableMap(ObservableMap<? extends K, ? extends V> values) {
        Map<K, V> result = Maps.<K, V>newHashMap(checkNotNull(values));
		
        return FXCollections.observableMap(result);
    }
	
	/**
	 * Creates a copy of an {@link ObservableList} backed by a {@link ArrayList}.
	 * 
	 * Delegates to {@link FXCollections#observableArrayList(java.util.Collection)}.
	 * 
	 * @param values
	 * @return non-<code>null</code> {@link ObservableList}.
	 * 
	 * @throws NullPointerException if values is null. 
	 * 
	 * @see #copyObservableMap(ObservableMap)
	 */
	public static <T> ObservableList<T> copyObservableList(ObservableList<? extends T> values) {
        return FXCollections.<T>observableArrayList(values);
    }
}