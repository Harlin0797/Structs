package it.fb.structs.proxy;

import it.fb.structs.MasterStructPointer;
import it.fb.structs.StructPointer;
import it.fb.structs.asm.DataStorage;
import it.fb.structs.asm.Allocator;
import it.fb.structs.asm.StructData;
import it.fb.structs.asm.DataStorage;
import it.fb.structs.core.AbstractOffsetVisitor;
import it.fb.structs.core.AbstractAllocator;
import it.fb.structs.core.PStructDesc;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 *
 * @author Flavio
 */
public class ProxyAllocator<D extends StructData> extends AbstractAllocator<D> {

    public ProxyAllocator(DataStorage<D> dataFactory) {
        super(dataFactory);
    }

    @Override
    protected <T> AbstractStructArrayClassFactory<T> newStructArrayClassFactory(
            final Class<T> structInterface, PStructDesc structDesc) {
        final Class<?> proxyClass = Proxy.getProxyClass(structInterface.getClassLoader(), 
                structInterface, MasterStructPointer.class);
        final Constructor<MasterStructPointer<T>> pointerConstructor;
        try {
            pointerConstructor = (Constructor<MasterStructPointer<T>>) proxyClass.getConstructor(InvocationHandler.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Error accessing proxy constructor", ex);
        }
        final InvocationHandlerFactory<T, D> invocationHandlerFactory = 
                InvocationHandlerFactory.<T, D>create(this, structInterface, structDesc, new LocalOffsetVisitor(4)); // TODO: Alignment
        return new ProxyStructArrayClassFactory<T>(proxyClass, invocationHandlerFactory, structInterface, pointerConstructor);
    }

    <T> InvocationHandlerFactory<T, D> getInvocationHandlerFactory(Class<T> structInterface) {
        return ((ProxyStructArrayClassFactory<T>)getClassFactory(structInterface)).invocationHandlerFactory;
    }

    <T> StructPointer<T> newPointer(Class<T> subInterface, D data, int length, int index) {
        return ((ProxyStructArrayClassFactory<T>) getClassFactory(subInterface)).newPointer(data, length, index);
    }

    public static <D extends StructData> Allocator<D> newInstance(DataStorage<D> factory) {
        return new ProxyAllocator<D>(factory);
    }

    private class ProxyStructArrayClassFactory<T> extends AbstractStructArrayClassFactory<T> {

        private final Class<?> proxyClass;
        private final InvocationHandlerFactory<T, D> invocationHandlerFactory;
        private final Class<T> structInterface;
        private final Constructor<MasterStructPointer<T>> pointerConstructor;

        public ProxyStructArrayClassFactory(Class<?> proxyClass, InvocationHandlerFactory<T, D> invocationHandlerFactory, 
                Class<T> structInterface, Constructor<MasterStructPointer<T>> pointerConstructor) {
            this.proxyClass = proxyClass;
            this.invocationHandlerFactory = invocationHandlerFactory;
            this.structInterface = structInterface;
            this.pointerConstructor = pointerConstructor;
        }

        @Override
        public Class<?> getStructImplementation() {
            return proxyClass;
        }

        @Override
        public MasterStructPointer<T> newStructArray(int length) {
            D data = dataFactory.newBuffer(invocationHandlerFactory.getStructSize() * length);
            return newPointer(data, length, 0);
        }

        @Override
        public MasterStructPointer<T> wrap(D data) {
            return newPointer(data, data.getSize() / invocationHandlerFactory.getStructSize(), 0);
        }

        MasterStructPointer<T> newPointer(D data, int length, int index) {
            try {
                return pointerConstructor.newInstance(invocationHandlerFactory.newInvocationHandler(data, length, index));
            } catch (Exception ex) {
                throw new IllegalStateException("Error building new StructPointer", ex);
            }
        }
    }

    private class LocalOffsetVisitor extends AbstractOffsetVisitor {

        public LocalOffsetVisitor(int alignment) {
            super(alignment);
        }

        @Override
        protected int getStructSize(String className) {
            try {
                return getInvocationHandlerFactory(Class.forName(className)).getStructSize();
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
        }

    }
}
