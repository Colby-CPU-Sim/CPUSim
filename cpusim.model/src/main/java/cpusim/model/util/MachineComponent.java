package cpusim.model.util;

import cpusim.model.Machine;
import cpusim.util.ClassCleaner;
import cpusim.util.MoreFXCollections;
import javafx.beans.property.*;

import javax.annotation.Nullable;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Denotes a type that can be cloned via the {@link #cloneFor(IdentifierMap)} method.
 *
 * @since 2016-12-07
 */
public interface MachineComponent extends ReadOnlyMachineBound, IdentifiedObject {

    /**
     * Clones the current value into a new instance, pulling any required instances from the {@link Machine} passed.
     *
     * @param oldToNew Non-{@code null} instance to the currently cloning {@link Machine} and a mapping of original
     *          {@link UUID} to new values.
     * @return New, non-{@code null} instance.
     *
     * @throws NullPointerException if {@code newMachine} is {@code null}
     */
    MachineComponent cloneFor(IdentifierMap oldToNew);

    /**
     * Used to reflectively buildSet up a {@link Set} of child {@link MachineComponent} values.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface ChildComponent {

    }

    /**
     * Gets any children components that are bound within the {@link MachineComponent}.
     * @return any children properties.
     */
    default ReadOnlySetProperty<MachineComponent> getChildrenComponents() {
        return new ReadOnlySetWrapper<>(this, "childrenComponents", MoreFXCollections.emptyObservableSet());
    }

    /**
     * Recursively searches a type hierarchy for {@link DependantComponent} annotated fields and creates a
     * {@link ReadOnlySetProperty} of the components that is bound to the annoted fields.
     *
     * @param base Object to read fields from.
     * @return Set of {@link MachineComponent} backed by the annotated properties.
     *
     * @throws IllegalStateException if a field is incorrectly annotated
     * @throws NullPointerException if {@code base} is null.
     */
    static ObservableCollectionBuilder<MachineComponent> collectChildren(MachineComponent base) {
        return collectMarkedComponents(base, ChildComponent.class);
    }

    /**
     * Used to reflectively buildSet up a {@link Set} of dependant {@link MachineComponent} values.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DependantComponent {

    }


    /**
     * Gets any dependant components that are bound within a {@link MachineComponent}.
     * @return any dependant properties
     */
    default ReadOnlySetProperty<MachineComponent> getDependantComponents() {
        return new ReadOnlySetWrapper<>(this, "dependantComponents", MoreFXCollections.emptyObservableSet());
    }

    /**
     * Recursively searches a type hierarchy for {@link DependantComponent} annotated fields and creates a
     * {@link ReadOnlySetProperty} of the components that is bound to the annoted fields.
     *
     * @param base Object to read fields from.
     * @return Set of {@link MachineComponent} backed by the annotated properties.
     *
     * @throws IllegalStateException if a field is incorrectly annotated
     * @throws NullPointerException if {@code base} is null.
     */
    static ObservableCollectionBuilder<MachineComponent> collectDependancies(MachineComponent base) {
        return collectMarkedComponents(base, DependantComponent.class);
    }

    static ObservableCollectionBuilder<MachineComponent> collectMarkedComponents(MachineComponent base, Class<? extends Annotation> annotationClazz) {
        checkNotNull(base);

        ObservableCollectionBuilder<MachineComponent> builder = new ObservableCollectionBuilder<>();

        Class<?> clazz = ClassCleaner.cleanClass(base.getClass());

        while (clazz != Object.class) {

            for (Field f : clazz.getDeclaredFields()) {
                if (f.getAnnotation(annotationClazz) != null) {
                    if (Modifier.isStatic(f.getModifiers())) {
                        throw new IllegalArgumentException(
                                String.format("Type, %s, has static field, %s, annotated with %s this is not allowed.",
                                        clazz.getName(), f.getName(), annotationClazz.getName()));
                    }

                    if (!Modifier.isFinal(f.getModifiers())) {
                        throw new IllegalArgumentException(
                                String.format("Type, %s, has non-final field, %s, annotated with %s this is not allowed.",
                                        clazz.getName(), f.getName(), annotationClazz.getName()));
                    }

                    // now we know the field was marked as a dependant, so lets get the content:
                    try {
                        f.setAccessible(true); // force it to be accessible

                        final Object fValue = f.get(base);

                        if (ReadOnlySetProperty.class.isAssignableFrom(f.getType())) {
                            // its a set of values, bind that:
                            // This should be safe, but we don't know, it'll throw if its bad
                            @SuppressWarnings("unchecked")
                            ReadOnlySetProperty<? extends MachineComponent> comps =
                                    (ReadOnlySetProperty<? extends MachineComponent>) fValue;
                            builder.addAll(comps);
                        } else if (ReadOnlyListProperty.class.isAssignableFrom(f.getType())) {
                            // its a set of values, bind that:
                            // This should be safe, but we don't know, it'll throw if its bad
                            @SuppressWarnings("unchecked")
                            ReadOnlyListProperty<? extends MachineComponent> comps =
                                    (ReadOnlyListProperty<? extends MachineComponent>) fValue;
                            builder.addAll(comps);
                        } else if (ReadOnlyObjectProperty.class.isAssignableFrom(f.getType())) {
                            // it's a readonly object
                            // This should be safe, but we don't know, it'll throw if its bad
                            @SuppressWarnings("unchecked")
                            ReadOnlyObjectProperty<? extends MachineComponent> comp =
                                    (ReadOnlyObjectProperty<? extends MachineComponent>) fValue;
                            builder.add(comp);
                        } else if (Map.class.isAssignableFrom(f.getType())) {

                            // This is an odd one, it's for Machine..
                            @SuppressWarnings("unchecked")
                            Map<?, ListProperty<? extends MachineComponent>> map =
                                    (Map<?, ListProperty<? extends MachineComponent>>)fValue;
                            map.values().forEach(builder::addAll);
                        } else if (Collection.class.isAssignableFrom(f.getType())) {
                            // it's collection of values
                            // This should be safe, but we don't know, it'll throw if its bad
                            @SuppressWarnings("unchecked")
                            Collection<ReadOnlyProperty<? extends MachineComponent>> comps =
                                    (Collection<ReadOnlyProperty<? extends MachineComponent>>) fValue;
                            builder.addAll(comps);
                        } else {
                            throw new IllegalStateException(
                                    String.format("Type, %s, has field, %s, with invalid type, %s, annotated with %s only " +
                                                    "ReadOnlyObjectProperty<? extends MachineComponent>, " +
                                                    "ReadOnlySetProperty<? extends MachineComponent>, " +
                                                    "and Collection<? extends ReadOnlyProperty<? extends MachineComponent> " +
                                                    "are allowed.",
                                            clazz.getName(),
                                            f.getName(), f.getType().getName(),
                                            annotationClazz.getName()));
                        }
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("Failed trying to get value from Field, " + f.getName()
                                + " from type: " + clazz.getName());
                    }
                }
            }

            clazz = clazz.getSuperclass();
        }

        return builder;
    }


    @SuppressWarnings("unchecked")
    static <T extends MachineComponent> T copyOrNull(MachineComponent.IdentifierMap oldToNew,
                                                        Optional<T> option) {
        checkNotNull(oldToNew);
        checkNotNull(option);

        return option.map(cb -> (T)cb.cloneFor(oldToNew)) // clone if present, null otherwise
                .orElse(null);
    }

    static <Base, T extends MachineComponent> void copyProperty(MachineComponent.IdentifierMap oldToNew,
                                                                   Base original, Base newValue,
                                                                   Function<? super Base, ? extends Property<T>> accessor) {
        ReadOnlyProperty<T> from = accessor.apply(original);
        Property<T> to = accessor.apply(newValue);

        copyProperty(oldToNew, to, from);
    }

    static <T extends MachineComponent> void copyProperty(MachineComponent.IdentifierMap oldToNew,
                                                             Property<T> to,
                                                             ReadOnlyProperty<? extends T> from) {
        to.setValue(oldToNew.get(from.getValue()));
    }

    static <Base, T extends MachineComponent>
    void copyListProperty(MachineComponent.IdentifierMap oldToNew,
                          Base original, Base newValue,
                          Function<? super Base, ListProperty<T>> accessor) {
        checkNotNull(oldToNew, "IdentifierMap == null");
        checkNotNull(accessor, "accessor function is null");

        ReadOnlyListProperty<T> from = accessor.apply(original);
        ListProperty<T> to = accessor.apply(newValue);

        copyListProperty(oldToNew, to, from);
    }

    @SuppressWarnings("unchecked")
    static <T extends MachineComponent>
    void copyListProperty(MachineComponent.IdentifierMap oldToNew,
                          ListProperty<T> to,
                          ReadOnlyListProperty<? extends T> from) {
        checkNotNull(oldToNew);
        checkNotNull(to);
        checkNotNull(from);

        from.clear();
        to.addAll(from.stream().map(c -> (T)c.cloneFor(oldToNew)).collect(Collectors.toList()));
    }

    /**
     * Provides a "cache" for original {@link UUID} values to the "new" cloned version.
     */
    class IdentifierMap {

        private Machine newMachine;
        private Map<UUID, MachineComponent> oldToNew;

        public IdentifierMap() {
            oldToNew = new HashMap<>();
        }

        /**
         * Performs the actual work to do the clone. Polymorphism makes this "safe."
         * @param oldValue Value to clone.
         * @return Newly cloned value or value from cache.
         */
        private MachineComponent unsafeClone(@Nullable MachineComponent oldValue) {
            if (oldValue == null) {
                return oldValue;
            }

            final UUID oldId = oldValue.getID();

            if (oldToNew.containsKey(oldId)) {
                return oldToNew.get(oldId);
            } else {
                // not cloned yet, so clone it and cache the result.
                final MachineComponent newValue = oldValue.cloneFor(this);
                oldToNew.put(oldId, newValue);

                return newValue;
            }
        }

        public MachineComponent clone(@Nullable MachineComponent instance) {
            return unsafeClone(instance);
        }

        public <T extends MachineComponent> T get(@Nullable final T oldValue) {
            @SuppressWarnings("unchecked") // UUIDs are unique, this cast will be fine
            final T toReturn = (T)clone(oldValue);

            return toReturn;
        }

        /**
         * Checks if an identifier has been cloned yet.
         * @param id Non-{@code null} identifier
         * @return {@code true} if the value has already been cloned.
         */
        public boolean isCloned(UUID id) {
            return oldToNew.keySet().contains(checkNotNull(id));
        }

        public Machine getNewMachine() {
            return newMachine;
        }

        public void setNewMachine(final Machine newMachine) {
            this.newMachine = checkNotNull(newMachine);
        }

        public <Base, T extends MachineComponent> void copyProperty(
                Base original, Base newValue,
                Function<? super Base, ObjectProperty<T>> accessor) {
            MachineComponent.copyProperty(this, original, newValue, accessor);
        }

        public <T extends MachineComponent> void copyProperty(ObjectProperty<T> to,
                                                                 ReadOnlyObjectProperty<? extends T> from) {
            MachineComponent.copyProperty(this, to, from);
        }

        public <T extends MachineComponent> void copyListProperty(ListProperty<T> to,
                                                                     ListProperty<? extends T> from) {
            MachineComponent.copyListProperty(this, to, from);
        }

        public <Base, T extends MachineComponent> void copyListProperty(
                Base original, Base newValue,
                Function<? super Base, ListProperty<T>> accessor) {
            MachineComponent.copyListProperty(this, original, newValue, accessor);
        }

        public <T extends MachineComponent> T copyOrNull(Optional<T> option) {
            checkNotNull(option);
            return MachineComponent.copyOrNull(this, option);
        }
    }
}
