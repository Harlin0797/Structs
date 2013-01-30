package it.fb.structs.internal;

import it.fb.structs.StructArray;
import java.lang.reflect.Method;

/**
 *
 * @author Flavio
 */
public interface IStructArrayFactory<T> {
    
    StructArray<T> newStructArray(int length);

    public interface Builder<T> {
        void addGetter(Method getter, SField field, int offset); 
        void addSetter(Method setter, SField field, int offset);
        IStructArrayFactory<T> build(int structSize);
        
        public interface Factory {
            public <T> Builder<T> newBuilder(Class<T> structInterface);
        }
    }
    
}
