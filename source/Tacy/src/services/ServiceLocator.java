package services;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class ServiceLocator implements ILocator {

    private final Map<Class<?>, Object> instanceCache = new HashMap<Class<?>, Object>();
    private final Map<Class<?>, Class<?>> serviceMapping = new HashMap<Class<?>, Class<?>>();
    private final Map<Class<?>, IGenericCreator<?>> creatorMapping = new HashMap<Class<?>, IGenericCreator<?>>();


    /**
     * Registers the given Instance whithhin the given matching interface as singleton
     * 
     * @param inter The Interface of this Service
     * @param impl The given Implementation.
     */
    public <I, T extends I> void registerInstance(Class<I> inter, T impl) {
	instanceCache.put(inter, impl);
    }
    
    /**
     * Registers a lazy loaded singleton. The singleton will be created when it is requested the first time.
     * @param inter The Interface of this Service
     * @param impl The Implementation of the Interface which will be created
     */
    public <I, T extends I> void registerSingleton(Class<I> inter, Class<T> implType) {
	serviceMapping.put(inter, implType);
    }
    
    /**
     * Registers an external action to be executed when the given Interface is requested. On every Request, the external creator Method will be called.
     * @param inter The Interface of this Service
     * @param creator A generig creator which must return an instance of the requested Interface
     */
    public <I, T extends I> void registerExternal(Class<I> inter, IGenericCreator<T>  creator) {
	creatorMapping.put(inter, creator);
    }

    
    /* (non-Javadoc)
     * @see archimedesJ.services.ILocator#resolve(java.lang.Class)
     */
    @Override
    public <T> T resolve(Class<T> iclazz) {
	T instance = findInstance(iclazz);

	if(instance == null){
	    // we couldnt find an instance
	    // we may create one
	    try {
		instance = createInstance(iclazz);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

	return instance;
    }

    
    @SuppressWarnings("unchecked")
    private <T> T findInstance(Class<T> iclazz){
	T instance = null;

	if(instanceCache.containsKey(iclazz))
	    return (T) instanceCache.get(iclazz);

	return instance;
    }
    
    @SuppressWarnings("unchecked")
    private <T> T createInstance(Class<T> iclazz) throws Exception{
	T instance = null;

	if(serviceMapping.containsKey(iclazz))
	{
	    Class<T> implementationType = (Class<T>) serviceMapping.get(iclazz);
	    Constructor<T> constructor = implementationType.getConstructor();
	    instance = constructor.newInstance();
	    if(instance != null)
		    instanceCache.put(iclazz, instance); // cache the created service
	}else if(creatorMapping.containsKey(iclazz)){
	    IGenericCreator<T> typeCreator = (IGenericCreator<T>) creatorMapping.get(iclazz);
	    instance = typeCreator.getInstance(); 
	}else
	    throw new Exception("Can not create service implementation because no implementation type was registered for " + iclazz.getName());

	return instance;
    }

}
