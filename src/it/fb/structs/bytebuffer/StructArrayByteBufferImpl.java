package it.fb.structs.bytebuffer;

import it.fb.structs.StructArray;
import it.fb.structs.StructPointer;
import it.fb.structs.Structs;
import static it.fb.structs.Structs.*;
import it.fb.structs.internal.Parser;
import it.fb.structs.internal.SField;
import it.fb.structs.internal.SStructDesc;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Flavio
 */
public final class StructArrayByteBufferImpl<T> implements StructArray<T> {
    
    private final Class<T> structInterface;
    private final int length;
    private final int structSize;
    private final Map<Method, IProxyMethodImplementor> implementors;
    private final ByteBuffer data;

    public StructArrayByteBufferImpl(Class<T> structInterface, int length, int structSize, Map<Method, IProxyMethodImplementor> implementors, ByteBuffer data) {
        this.structInterface = structInterface;
        this.length = length;
        this.structSize = structSize;
        this.implementors = implementors;
        this.data = data;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public T get(int index) {
        return (T) Proxy.newProxyInstance(structInterface.getClassLoader(), 
                new Class[]{structInterface}, 
                new SABBDataInvocationHandler(implementors, data, structSize, null, index));
    }

    @Override
    public StructPointer<T> at(int index) {
        return (StructPointer<T>) Proxy.newProxyInstance(structInterface.getClassLoader(), 
                new Class[]{structInterface, StructPointer.class}, 
                new SABBPointerInvocationHandler(this, implementors, data, structSize, null, index));
    }

    public static <T> StructArrayByteBufferImpl<T> create(Class<T> structInterface, int size) {
        SStructDesc desc = Parser.parse(structInterface);
        OffsetVisitor ov = new OffsetVisitor(4);
        Map<Method, IProxyMethodImplementor> implementors = new HashMap<>();
        for (SField field : desc.getFields()) {
            int fieldOffset = field.accept(ov);
            implementors.put(field.getGetter(), field.accept(new ProxyGetterMethodVisitor(fieldOffset)));
            if (field.getSetter() != null) {
                implementors.put(field.getSetter(), field.accept(new ProxySetterMethodVisitor(fieldOffset)));
            }
        }
        ByteBuffer data = ByteBuffer.allocate(ov.getSize() * size);
        return new StructArrayByteBufferImpl<>(structInterface, size, ov.getSize(), implementors, data);
    }

    private static class SABBDataInvocationHandler implements InvocationHandler {
        protected final Map<Method, IProxyMethodImplementor> implementors;
        protected final ByteBuffer data;
        protected final int structSize;
        protected final SABBDataInvocationHandler parentHandler;
        protected int index;

        public SABBDataInvocationHandler(Map<Method, IProxyMethodImplementor> implementors, ByteBuffer data, int structSize, SABBDataInvocationHandler parentHandler, int index) {
            this.implementors = implementors;
            this.data = data;
            this.structSize = structSize;
            this.parentHandler = parentHandler;
            this.index = index;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            IProxyMethodImplementor implementor = implementors.get(method);
            if (implementor != null) {
                return implementor.run(data, getBaseOffset(), args);
            } else {
                throw new UnsupportedOperationException(method.toString());
            }
        }
        
        public int getBaseOffset() {
            return (parentHandler == null ? index * structSize : parentHandler.getBaseOffset() + index * structSize);
        }
    }
    
    private static final class SABBPointerInvocationHandler extends SABBDataInvocationHandler {

        private final StructArrayByteBufferImpl owner;

        public SABBPointerInvocationHandler(StructArrayByteBufferImpl owner, Map<Method, IProxyMethodImplementor> implementors, ByteBuffer data, int structSize, SABBDataInvocationHandler parentHandler, int index) {
            super(implementors, data, structSize, parentHandler, index);
            this.owner = owner;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.equals(POINTER_AT_METHOD)) {
                index = (Integer) args[0];
                return proxy;
            } else if (method.equals(POINTER_GET_METHOD)) {
                return proxy;
            } else if (method.equals(POINTER_OWNER_METHOD)) {
                return owner;
            } else {
                return super.invoke(proxy, method, args);
            }
        }

    }
}
