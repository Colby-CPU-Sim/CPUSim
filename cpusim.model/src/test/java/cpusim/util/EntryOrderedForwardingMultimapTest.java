package cpusim.util;

import com.google.common.collect.ArrayListMultimap;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static cpusim.util.MoreIteratables.zip;

/**
 * Created by kevin on 01/02/2017.
 */
public class EntryOrderedForwardingMultimapTest {

    private EntryOrderedForwardingMultimap<String, Integer> underTest;

    public EntryOrderedForwardingMultimapTest() {
        underTest = new EntryOrderedForwardingMultimap<>(ArrayListMultimap.create());
    }

    @Test
    public void putOrdering() {
        List<Entry<String, Integer>> expected = Arrays.asList(
                new Entry<>("a", 1),
                new Entry<>("b", 2),
                new Entry<>("a", 3),
                new Entry<>("a", 1));
        Set<Entry<String, Integer>> expectedSet = new LinkedHashSet<>(expected);

        expected.forEach(e -> underTest.put(e.getKey(), e.getValue()));

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(underTest.entries());

        zip(expectedSet, entries, Assert::assertEquals);
    }

    @Test
    public void removeOrdering() {
        Entry<String, Integer> toRemove = new Entry<>("a", 1);
        List<Entry<String, Integer>> expected = Arrays.asList(
                toRemove,
                new Entry<>("b", 2),
                new Entry<>("a", 3));
        Set<Entry<String, Integer>> expectedSet = new LinkedHashSet<>(expected);
        expectedSet.remove(toRemove);

        expected.forEach(e -> underTest.put(e.getKey(), e.getValue()));

        underTest.remove(toRemove.getKey(), toRemove.getValue());

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(underTest.entries());

        zip(expectedSet, entries, Assert::assertEquals);
    }

    /**
     * Used for storing Key-value pairs
     */
    private class Entry<K, V> implements Map.Entry<K, V> {

        private final K key;
        private V value;

        Entry(K key, @Nullable V value) {
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