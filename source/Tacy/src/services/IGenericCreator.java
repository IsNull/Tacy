package services;

/**
 * Geneirc instance resolver
 * @author pascal.buettiker
 *
 * @param <T>
 */
public interface IGenericCreator<T> {
    
    /***
     * 
     * @return Returns a Instance of Type <code>T</code>
     */
    public abstract T getInstance();
}
