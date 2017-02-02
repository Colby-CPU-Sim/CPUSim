package cpusim.util;

import com.google.common.collect.ForwardingMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Similar to a {@link LinkedHashSet} this {@link Multimap} maintains the order of element
 * insertion when iterating across it's entries.
 */
public class EntryOrderedForwardingMultimap<K, V> extends ForwardingMultimap<K, V> {

    private final ListMultimap<K, V> delegate;

    private final LinkedHashSet<Map.Entry<K, V>> entriesOrder;

    public EntryOrderedForwardingMultimap(@Nonnull ListMultimap<K, V> delegate) {
        this.delegate = checkNotNull(delegate);

        entriesOrder = new LinkedHashSet<>(this.delegate.size());
    }

    @Override
    public Collection<Map.Entry<K, V>> entries() {
        return entriesOrder;
    }

    @Override
    public Multiset<K> keys() {
        return entriesOrder.stream()
                .map(Map.Entry::getKey)
                .collect(Gullectors.toMultiset());
    }

    @Override
    public Set<K> keySet() {
        return entriesOrder.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<V> values() {
        return entriesOrder.stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public boolean put(K key, V value) {
        entriesOrder.add(new InternalEntry(key, value));

        return super.put(key, value);
    }

    @Override
    public void clear() {
        entriesOrder.clear();

        super.clear();
    }

    @Override
    public boolean putAll(K key, Iterable<? extends V> values) {
        boolean result = super.putAll(key, values);
        if (result) {
            List<Map.Entry<K, V>> valueList = new ArrayList<>();
            for (V v: values) {
                valueList.add(new InternalEntry(key, v));
            }
            entriesOrder.addAll(valueList);
        }

        return result;
    }

    /**
     * Puts the values in multimap into the delegate map, maintaining the order of {@code multimap.entries()} as
     * the insertion order.
     * @param multimap
     * @return
     */
    @Override
    public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
        boolean result = super.putAll(multimap);
        if (result) {
            entriesOrder.addAll(multimap.entries().stream()
                    .map(e -> new InternalEntry(e.getKey(), e.getValue()))
                    .collect(Collectors.toList()));
        }

        return result;
    }

    @Override
    public boolean remove(@Nullable Object key, @Nullable Object value) {
        boolean result = super.remove(key, value);
        if (result) {
            entriesOrder.remove(new InternalEntry((K)key, (V)value));
        }

        return result;
    }

    @Override
    public Collection<V> removeAll(@Nullable Object key) {

        entriesOrder.removeAll(
                entriesOrder.stream()
                        .filter(e -> e.getKey().equals(key))
                        .collect(Collectors.toList()));

        return super.removeAll(key);
    }

    @Override
    public Collection<V> replaceValues(K key, Iterable<? extends V> values) {
        entriesOrder.removeAll(
                entriesOrder.stream()
                        .filter(e -> e.getKey().equals(key))
                        .collect(Collectors.toList()));

        List<InternalEntry> newEntries = new ArrayList<>();
        for (V value: values) {
            newEntries.add(new InternalEntry(key, value));
        }
        entriesOrder.addAll(newEntries);

        return super.replaceValues(key, values);
    }

    @Override
    protected ListMultimap<K, V> delegate() {
        return this.delegate;
    }

    /**
     * Used for storing Key-value pairs
     */
    private class InternalEntry implements Map.Entry<K, V> {

        private final K key;
        private V value;

        InternalEntry(K key, @Nullable V value) {
            this.key = checkNotNull(key);
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V old = this.value;
            this.value = value;

            return old;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || !Map.Entry.class.isAssignableFrom(o.getClass())) return false;

            Map.Entry<?, ?> that = (Map.Entry<?, ?>) o;
            return Objects.equals(getKey(), that.getKey()) &&
                    Objects.equals(getValue(), that.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getKey(), getValue());
        }

        @Override
        public String toString() {
            return getKey() + " -> " + getValue();
        }
    }
}
