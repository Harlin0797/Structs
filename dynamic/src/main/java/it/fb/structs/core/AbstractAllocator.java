package it.fb.structs.core;

import it.fb.structs.MasterStructPointer;
import it.fb.structs.asm.DataStorage;
import it.fb.structs.asm.Allocator;
import it.fb.structs.asm.StructData;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Flavio
 */
public abstract class AbstractAllocator<D extends StructData> implements Allocator<D> {

    protected final DataStorage<D> dataFactory;
    private final Map<Class<?>, PStructDesc> structDescriptors =
            new HashMap<Class<?>, PStructDesc>();
    private final Map<Class<?>, AbstractStructArrayClassFactory<?>> classCache = 
            new HashMap<Class<?>, AbstractStructArrayClassFactory<?>>();

    public AbstractAllocator(DataStorage<D> dataFactory) {
        this.dataFactory = dataFactory;
    }
    
    @Override
    public <T> MasterStructPointer<T> newStructArray(Class<T> structInterface, int length) {
        return getClassFactory(structInterface).newStructArray(length);
    }

    @Override
    public <T> MasterStructPointer<T> wrap(Class<T> structInterface, D data) {
        return getClassFactory(structInterface).wrap(data);
    }
    
    protected <T> AbstractStructArrayClassFactory<T> getClassFactory(Class<T> structInterface) {
        AbstractStructArrayClassFactory<?> classFactory = classCache.get(structInterface);
        if (classFactory == null) {
            classFactory = newStructArrayClassFactory(structInterface, getStructDescriptor(structInterface));
            classCache.put(structInterface, classFactory);
        }
        return (AbstractStructArrayClassFactory<T>) classFactory;
    }

    protected PStructDesc getStructDescriptor(Class<?> structInterface) {
        PStructDesc ret = structDescriptors.get(structInterface);
        if (ret == null) {
            ret = Parser.parse(structInterface);
            structDescriptors.put(structInterface, ret);
        }
        return ret;
    }

    protected abstract <T> AbstractStructArrayClassFactory<T> newStructArrayClassFactory(Class<T> structInterface, PStructDesc structDesc);
    
    protected abstract class AbstractStructArrayClassFactory<T> {
        public abstract Class<?> getStructImplementation();
        public abstract MasterStructPointer<T> newStructArray(int length);
        public abstract MasterStructPointer<T> wrap(D data) ;
    }

    protected class LocalOffsetVisitor extends AbstractOffsetVisitor {

        public LocalOffsetVisitor(int alignment) {
            super(alignment);
        }

        @Override
        protected int getStructSize(String className) {
            try {
                LocalOffsetVisitor ov = new LocalOffsetVisitor(alignment);
                for (ParsedField field : getStructDescriptor(Class.forName(className)).getFields()) {
                    field.accept(ov, null);
                }
                return ov.getSize();
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
}
