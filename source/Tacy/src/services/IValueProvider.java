package services;

/**
 * Provider for a single value
 *
 * @param <T>
 */
public interface IValueProvider<T> {
    /**
     * Gets the value
     * @return
     */
    public abstract T getValue();
}
