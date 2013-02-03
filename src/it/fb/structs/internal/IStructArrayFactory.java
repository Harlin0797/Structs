package it.fb.structs.internal;

import it.fb.structs.StructArray;
import it.fb.structs.StructData;
import java.lang.reflect.Method;

/**
 *
 * @author Flavio
 */
public interface IStructArrayFactory<T, D extends StructData> {
    
    StructArray<T> newStructArray(int length);
    StructArray<T> wrap(D data);

    public interface Builder<T, D extends StructData> {
        void addGetter(Method getter, SField field, int offset); 
        void addSetter(Method setter, SField field, int offset);
        IStructArrayFactory<T, D> build(int structSize);
    }

    public interface Factory {
        public <T, D extends StructData> Builder<T, D> newBuilder(StructData.Factory<D> factory, Class<T> structInterface);
    }

}
