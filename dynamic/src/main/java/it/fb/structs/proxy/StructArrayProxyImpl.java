package it.fb.structs.proxy;

import it.fb.structs.StructArray;
import it.fb.structs.StructPointer;
import it.fb.structs.asm.StructData;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;

/**
 *
 * @author Flavio
 */
class StructArrayProxyImpl<T, D extends StructData> implements StructArray<T> {
    private final Class<T> structInterface;
    private final InvocationHandlerFactory<T, D> invocationHandlerFactory;
    private final Constructor pointerConstructor;
    private final D data;

    public StructArrayProxyImpl(Class<T> structInterface, InvocationHandlerFactory<T, D> invocationHandlerFactory, Constructor pointerConstructor, D data) {
        this.structInterface = structInterface;
        this.invocationHandlerFactory = invocationHandlerFactory;
        this.pointerConstructor = pointerConstructor;
        this.data = data;
    }

    public int getLength() {
        return data.getSize() / getStructSize();
    }

    public int getStructSize() {
        return invocationHandlerFactory.getStructSize();
    }

    public T get(int index) {
        return structInterface.cast(at(index));
    }

    public StructPointer<T> at(int index) {
        InvocationHandler invocationHandler = invocationHandlerFactory.newInvocationHandler(
                data, getLength(), index);
        try {
            return ((StructPointer<T>) pointerConstructor.newInstance(invocationHandler));
        } catch (Exception ex) {
            throw new IllegalStateException("Error creating new pointer for " + structInterface, ex);
        }
    }

    public void release() {
        data.release();
    }

}
