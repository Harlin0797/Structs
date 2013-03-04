package it.fb.structs.proxy;

import it.fb.structs.StructArray;
import it.fb.structs.StructPointer;
import it.fb.structs.asm.IStructArrayFactory;
import it.fb.structs.asm.StructData;
import it.fb.structs.asm.StructData.Factory;
import it.fb.structs.core.OffsetVisitor;
import it.fb.structs.core.AbstractStructArrayFactory;
import it.fb.structs.core.PStructDesc;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 *
 * @author Flavio
 */
public class ProxyStructArrayFactory<D extends StructData> extends AbstractStructArrayFactory<D> {

    public ProxyStructArrayFactory(Factory<D> dataFactory) {
        super(dataFactory);
    }

    @Override
    protected <T> AbstractStructArrayClassFactory<T> newStructArrayClassFactory(
            final Class<T> structInterface, PStructDesc structDesc) {
        final Class<?> proxyClass = Proxy.getProxyClass(structInterface.getClassLoader(), 
                structInterface, StructPointer.class);
        final Constructor pointerConstructor;
        try {
            pointerConstructor = proxyClass.getConstructor(InvocationHandler.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Error accessing proxy constructor", ex);
        }
        final InvocationHandlerFactory<T, D> invocationHandlerFactory = 
                InvocationHandlerFactory.<T, D>create(structInterface, structDesc, new LocalOffsetVisitor(4)); // TODO: Alignment
        return new ProxyStructArrayClassFactory<T>(proxyClass, invocationHandlerFactory, structInterface, pointerConstructor);
    }
    
    public static <D extends StructData> IStructArrayFactory<D> newInstance(StructData.Factory<D> factory) {
        return new ProxyStructArrayFactory<D>(factory);
    }

    private class ProxyStructArrayClassFactory<T> extends AbstractStructArrayClassFactory<T> {

        private final Class<?> proxyClass;
        private final InvocationHandlerFactory<T, D> invocationHandlerFactory;
        private final Class<T> structInterface;
        private final Constructor pointerConstructor;

        public ProxyStructArrayClassFactory(Class<?> proxyClass, InvocationHandlerFactory<T, D> invocationHandlerFactory, Class<T> structInterface, Constructor pointerConstructor) {
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
        public StructArray<T> newStructArray(int length) {
            D data = dataFactory.newBuffer(invocationHandlerFactory.getStructSize() * length);
            return wrap(data);
        }

        @Override
        public StructArray<T> wrap(D data) {
            return new StructArrayProxyImpl<T, D>(structInterface, invocationHandlerFactory, pointerConstructor, data);
        }
    }
    
    private class LocalOffsetVisitor extends OffsetVisitor {

        public LocalOffsetVisitor(int alignment) {
            super(alignment);
        }

        @Override
        protected int getStructSize(String className) {
            try {
                return ((ProxyStructArrayClassFactory<?>)getClassFactory(Class.forName(className))).invocationHandlerFactory.getStructSize();
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
        }
        
    }
}
