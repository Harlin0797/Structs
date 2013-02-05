package it.fb.structs.internal;

import java.lang.reflect.Method;

/**
 *
 * @author Flavio
 */
public interface SFieldFactory<F extends SField> {
    public F newField(SFieldType type, int arrayLength, String name, Method getter, Method setter);
}
