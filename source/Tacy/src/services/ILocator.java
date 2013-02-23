package services;

/**
 * ServiceLocator
 * provides basic locator ability
 *
 */
public interface ILocator {

    /**
     * Resolves the given Type (Interface) to a instance of this type
     * @param iclazz
     * @return Returns an Instance which implements <code>T</code>
     */
    public abstract <T> T resolve(Class<T> iclazz);
}