package me.jfenn.colorpickerdialog.utils;

import androidx.annotation.Nullable;

import java.lang.reflect.Constructor;

public class DelayedInstantiation<T> {

    private final Class<T> tClass;
    private Instantiator<T> instantiator;

    private DelayedInstantiation(Class<T> tClass, Instantiator<T> instantiator) {
        this.tClass = tClass;
        this.instantiator = instantiator;
    }

    /**
     * Create a new delayed instantiation to instantiate a class using reflection.
     *
     * @param tClass The class to instantiate.
     * @param args   Class types of the arguments in the class's constructor.
     * @param <X>    The class to be instantiated.
     * @return The created `DelayedInstantiation` object.
     */
    @Nullable
    public static <X> DelayedInstantiation<X> from(Class<X> tClass, Class... args) {
        try {
            return new DelayedInstantiation<>(tClass, new ConstructionInstantiator<>(tClass.getConstructor(args)));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Specify a custom `Instantiator` instance to create the class.
     *
     * @param instantiator The interface to use to instantiate the class.
     * @return "This" instantiation instance, for method chaining.
     */
    public DelayedInstantiation<T> withInstantiator(Instantiator<T> instantiator) {
        this.instantiator = instantiator;
        return this;
    }

    /**
     * Instantiate the class, using reflection.
     *
     * @param args Arguments to pass to the constructor.
     * @return The instantiated class, or null if the
     * instantiation failed.
     */
    @Nullable
    public T instantiate(Object... args) {
        return instantiator.instantiate(args);
    }

    /**
     * Get the type class of the delayed instantiation.
     *
     * @return The class to be instantiated.
     */
    public Class<T> gettClass() {
        return tClass;
    }

    /**
     * Get the name of the type class for the delayed instantiation.
     *
     * @return The (string) name of the class to be
     * instantiated.
     */
    public String gettClassName() {
        return gettClass().getName();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof DelayedInstantiation && equalsClass(((DelayedInstantiation) obj).tClass);
    }

    public boolean equalsClass(@Nullable Class<T> tClass) {
        return this.tClass.equals(tClass);
    }

    public interface Instantiator<T> {
        T instantiate(Object... args);
    }

    public static class ConstructionInstantiator<T> implements Instantiator<T> {

        private final Constructor<T> constructor;

        public ConstructionInstantiator(Constructor<T> constructor) {
            this.constructor = constructor;
        }

        @Override
        @Nullable
        public T instantiate(Object... args) {
            try {
                return constructor.newInstance(args);
            } catch (Exception e) {
                return null;
            }
        }

    }
}
