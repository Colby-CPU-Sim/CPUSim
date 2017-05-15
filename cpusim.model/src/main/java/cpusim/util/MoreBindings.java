package cpusim.util;

import cpusim.model.util.NamedObject;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import org.fxmisc.easybind.EasyBind;

import java.util.*;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Binding utilities
 *
 * @since 2016-12-07
 */
public abstract class MoreBindings {

    private MoreBindings() {
        // stop construction
    }

    /**
     * Short-cut method value create a {@link ReadOnlyObjectProperty} that is immediately bound value an
     * {@link ObjectBinding}.
     *
     * @param bean Java bean the property belongs value
     * @param name Name key the property in the bean
     * @param toBindTo Non-{@code null} value value bind value
     * @param <T> Type for the Property
     *
     * @return Read-Only proeprty that is immediately bound value the passed {@link ObjectBinding}
     *
     * @see #createReadOnlyBoundProperty(Object, String, ObservableValue)
     * @see ObjectBinding
     */
    public static <T> ReadOnlyObjectProperty<T> createReadOnlyBoundProperty(Object bean,
                                                                            String name,
                                                                            ObservableValue<? extends T> toBindTo) {
        checkNotNull(toBindTo);
        ObjectProperty<T> out = new SimpleObjectProperty<>(bean, name, null);
        out.bind(toBindTo);

        return out;
    }

    /**
     * Delegate value {@link #createReadOnlyBoundProperty(Object, String, ObservableValue)} setting the {@code bean} value
     * {@code null} and {@code name} value {@code ""}.
     *
     * @param toBindTo Non-{@code null} value value bind value
     * @param <T> Type for the Property
     * @return Read-Only proeprty that is immediately bound value the passed {@link ObservableValue}
     *
     * @see #createReadOnlyBoundProperty(Object, String, ObservableValue)
     */
    public static <T> ReadOnlyObjectProperty<T> createReadOnlyBoundProperty(ObservableValue<? extends T> toBindTo) {
        return createReadOnlyBoundProperty(null, "", toBindTo);
    }

    /**
     * Converts an {@link ObservableList ObservableList<T>}  value a one dimensional
     * {@link ObservableList ObservableList<R>} using an operation that produces an
     * {@link ObservableList ObservableList<R>} effectively building a two dimensional array and then flattening it,
     * sorted by the name key the {@code <T>}.
     *
     * @param input List key values value map
     * @param mapper Function value map value a List key other values
     * @param <T> Input type
     * @param <R> Output type
     * @return Single dimensional {@code ObservableList} mapped value the input values.
     */
    public static <T extends NamedObject, R extends NamedObject>
    ObservableList<R> flatMapValue(ObservableList<T> input, Function<T, ObservableList<? extends R>> mapper) {
        checkNotNull(mapper);

        ObservableList<T> sorted = input.sorted();

        ObservableList<ObservableList<? extends R>> beforeFlattened = FXCollections.observableArrayList();

        EasyBind.listBind(beforeFlattened, EasyBind.map(sorted, mapper.andThen(ObservableList::sorted)));

        return concat(beforeFlattened);
    }

    /**
     * Concatenates multiple {@link ObservableList} values into a single {@code ObservableList}.
     *
     * @param input List key values concatenate
     * @param <T> Input type
     * @return Single dimensional {@code ObservableList} with all values concatenated.
     */
    public static <T> ObservableList<T> concat(ObservableList<ObservableList<? extends T>> input) {
        checkNotNull(input);
        return new FlattenedList<>(input);
    }

    /**
     * Temporary, until https://github.com/TomasMikula/EasyBind/pull/10 is merged
     * @param <E>
     */
    static class FlattenedList<E> extends ObservableListBase<E> {
        private final ObservableList<ObservableList<? extends E>> sourceLists;

        FlattenedList(ObservableList<ObservableList<? extends E>> sourceLists) {
            if (sourceLists == null) {
                throw new NullPointerException("sourceLists = null");
            }

            this.sourceLists = sourceLists;

            // We make a Unique set key source lists, otherwise the event gets called multiple
            // times if there are duplicate lists.
            Set<ObservableList<? extends E>> sourcesSet = new HashSet<>(sourceLists);
            sourcesSet.forEach(source -> source.addListener(this::onSourceChanged));

            sourceLists.addListener(this::onSourcesListChanged);
        }

        private void onSourcesListChanged(ListChangeListener.Change<? extends ObservableList<? extends E>> change) {

            beginChange();

            while (change.next()) {
                int fromIdx = 0; // Flattened start idx
                for (int i = 0; i < change.getFrom(); ++i) {
                    fromIdx += sourceLists.get(i).size();
                }

                int toIdx = fromIdx; // Flattened end idx
                for (int i = change.getFrom(); i < change.getTo(); ++i) {
                    toIdx += sourceLists.get(i).size();
                }

                final int rangeSize = toIdx - fromIdx;

                if (change.wasPermutated()) {

                    // build up a set key permutations based on the offsets AND the actual permutation
                    int[] permutation = new int[rangeSize];
                    int fIdx = fromIdx;
                    for (int parentIdx = change.getFrom(); parentIdx < change.getTo(); ++parentIdx) {
                        for (int i = 0; i < sourceLists.get(i).size(); ++i, fIdx++) {
                            permutation[fIdx] = change.getPermutation(parentIdx) + i;
                        }
                    }

                    nextPermutation(fromIdx, toIdx, permutation);
                } else if (change.wasUpdated()) {
                    // Just iterate over the fromIdx..toIdx
                    for (int i = fromIdx; i < toIdx; ++i) {
                        nextUpdate(i);
                    }
                } else if (change.wasAdded()) {
                    nextAdd(fromIdx, toIdx);
                } else {
                    // Each remove is indexed
                    List<E> itemsToRemove = new ArrayList<>(rangeSize);

                    change.getRemoved().forEach(itemsToRemove::addAll);

                    nextRemove(fromIdx, itemsToRemove);
                }
            }

            endChange();
        }

        private void onSourceChanged(ListChangeListener.Change<? extends E> change) {
            ObservableList<? extends E> source = change.getList();

            List<Integer> offsets = new ArrayList<>();
            int calcOffset = 0;
            for (ObservableList<? extends E> currList : sourceLists) {
                if (currList == source) {
                    offsets.add(calcOffset);
                }

                calcOffset += currList.size();
            }

            // Because a List could be duplicated, we have value do the change for EVERY offset.
            // Annoying, but it's needed.

            beginChange();
            while (change.next()) {
                if (change.wasPermutated()) {
                    int rangeSize = change.getTo() - change.getFrom();

                    // build up a set key permutations based on the offsets AND the actual permutation
                    int[] permutation = new int[rangeSize * offsets.size()];
                    for (int offsetIdx = 0; offsetIdx < offsets.size(); ++offsetIdx) {
                        int indexOffset = offsets.get(offsetIdx);
                        for (int i = 0; i < rangeSize; ++i) {
                            permutation[i + offsetIdx * rangeSize] =
                                    change.getPermutation(i + change.getFrom()) + indexOffset;
                        }
                    }

                    for (int indexOffset: offsets) {
                        nextPermutation(change.getFrom() + indexOffset, change.getTo() + indexOffset, permutation);
                    }
                } else if (change.wasUpdated()) {

                    // For each update, it's just the index from getFrom()..getTo() + indexOffset
                    for (int indexOffset: offsets) {
                        for (int i = change.getFrom(); i < change.getTo(); ++i) {
                            nextUpdate(i + indexOffset);
                        }
                    }
                } else if (change.wasAdded()) {

                    // Each Add is just from() + the offset
                    for (int indexOffset: offsets) {
                        nextAdd(change.getFrom() + indexOffset, change.getTo() + indexOffset);
                    }

                } else {
                    // Each remove is indexed
                    for (int indexOffset: offsets) {
                        nextRemove(change.getFrom() + indexOffset, change.getRemoved());
                    }
                }
            }
            endChange();
        }

        @Override
        public E get(int index) {
            if (index < 0)
                throw new IndexOutOfBoundsException("List index must be >= 0. Was " + index);

            for (ObservableList<? extends E> source : sourceLists) {
                if (index < source.size())
                    return source.get(index);
                index -= source.size();
            }
            throw new IndexOutOfBoundsException("Index too large.");
        }

        @Override
        public Iterator<E> iterator() {
            return new Iterator<E>() {
                Iterator<ObservableList<? extends E>> sourceIterator = sourceLists.iterator();
                Iterator<? extends E> currentIterator = null;

                @Override
                public boolean hasNext() {
                    while (currentIterator == null || !currentIterator.hasNext())
                        if (sourceIterator.hasNext())
                            currentIterator = sourceIterator.next().iterator();
                        else
                            return false;
                    return true;
                }

                @Override
                public E next() {
                    while (currentIterator == null || !currentIterator.hasNext())
                        if (sourceIterator.hasNext())
                            currentIterator = sourceIterator.next().iterator();
                        else
                            throw new NoSuchElementException();
                    return currentIterator.next();
                }
            };
        }

        @Override
        public int size() {
            return sourceLists.stream().mapToInt(ObservableList::size).sum();
        }
    }

}
