package it.fb.structs.apt;

import it.fb.structs.StructPointer;
import sun.misc.Unsafe;

/**
 *
 * @author Flavio
 */
public abstract class AbstractPointer<T> implements StructPointer<T> {

    protected final static Unsafe TheUnsafe;

    static {
        try {
            java.lang.reflect.Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            TheUnsafe = (sun.misc.Unsafe) field.get(null);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
