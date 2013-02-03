package it.fb.structs.impl;

import it.fb.structs.IStructArrayFactory;
import it.fb.structs.StructArray;
import it.fb.structs.StructData;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Flavio
 */
public abstract class AbstractStructArrayFactory<D extends StructData> implements IStructArrayFactory<D> {

    protected final StructData.Factory<D> dataFactory;
    private final Map<Class<?>, AbstractStructArrayClassFactory<?>> classCache = 
            new HashMap<Class<?>, AbstractStructArrayClassFactory<?>>();

    public AbstractStructArrayFactory(StructData.Factory<D> dataFactory) {
        this.dataFactory = dataFactory;
    }
    
    @Override
    public <T> StructArray<T> newStructArray(Class<T> structInterface, int length) {
        return getClassFactory(structInterface).newStructArray(length);
    }

    @Override
    public <T> StructArray<T> wrap(Class<T> structInterface, D data) {
        return getClassFactory(structInterface).wrap(data);
    }
    
    protected <T> AbstractStructArrayClassFactory<T> getClassFactory(Class<T> structInterface) {
        AbstractStructArrayClassFactory<?> classFactory = classCache.get(structInterface);
        if (classFactory == null) {
            classFactory = newStructArrayClassFactory(structInterface);
            classCache.put(structInterface, classFactory);
        }
        return (AbstractStructArrayClassFactory<T>) classFactory;
    }
 
    protected abstract <T> AbstractStructArrayClassFactory<T> newStructArrayClassFactory(Class<T> structInterface);
    
    protected abstract class AbstractStructArrayClassFactory<T> {
        public abstract Class<?> getStructImplementation();
        public abstract StructArray<T> newStructArray(int length);
        public abstract StructArray<T> wrap(D data) ;
    }
    
}
