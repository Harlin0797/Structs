package it.fb.structs.bytebuffer;

import it.fb.structs.StructPointer;
import static it.fb.structs.Structs.*;
import it.fb.structs.internal.Parser;
import it.fb.structs.internal.ParsedField;
import it.fb.structs.internal.PStructDesc;
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
class ByteBufferProxyHandlerFactory<T> {
    
    private final Class<T> structInterface;
    private final int structSize;
    private final Map<Method, IProxyMethodImplementor> implementors;

    public ByteBufferProxyHandlerFactory(Class<T> structInterface, int structSize, Map<Method, IProxyMethodImplementor> implementors) {
        this.structInterface = structInterface;
        this.structSize = structSize;
        this.implementors = implementors;
    }

    public Class<T> getStructInterface() {
        return structInterface;
    }

    public int getStructSize() {
        return structSize;
    }
    
    
    public SABBPointerInvocationHandler newPointerHandler(StructArrayByteBufferImpl<T> owner, ByteBuffer data, int offset, int index) {
        SABBPointerInvocationHandler handler = new SABBPointerInvocationHandler(owner, implementors, data, structSize, offset, index);
        handler.init();
        return handler;
    }

    public SABBDataInvocationHandler newDataHandler(ByteBuffer data, int offset, int index) {
        SABBDataInvocationHandler handler = new SABBDataInvocationHandler(implementors, data, structSize, offset, index);
        handler.init();
        return handler;
    }
    
    public T newDataInstance(ByteBuffer data, int offset, int index) {
        return (T) Proxy.newProxyInstance(structInterface.getClassLoader(), 
                new Class<?>[] { structInterface }, 
                newDataHandler(data, offset, index));
    }
    
    public StructPointer<T> newPointerInstance(StructArrayByteBufferImpl<T> owner, ByteBuffer data, int offset, int index) {
        return (StructPointer<T>) Proxy.newProxyInstance(structInterface.getClassLoader(), 
                new Class<?>[] { structInterface , StructPointer.class }, 
                newPointerHandler(owner, data, offset, index));
    }
    
    public static <T> ByteBufferProxyHandlerFactory<T> newHandlerFactory(Class<T> structInterface) {
        PStructDesc desc = Parser.parse(structInterface);
        OffsetVisitor ov = new RecursiveOffsetVisitor(4);
        Map<Method, IProxyMethodImplementor> implementors = new HashMap<Method, IProxyMethodImplementor>();
        for (ParsedField field : desc.getFields()) {
            int fieldOffset = field.accept(ov);
            implementors.put(field.getGetter(), field.accept(new ProxyGetterMethodVisitor(fieldOffset)));
            if (field.getSetter() != null) {
                implementors.put(field.getSetter(), field.accept(new ProxySetterMethodVisitor(fieldOffset)));
            }
        }
        return new ByteBufferProxyHandlerFactory<T>(structInterface, ov.getSize(), implementors);
    }
    
    static class SABBDataInvocationHandler implements InvocationHandler {
        protected final Map<Method, IProxyMethodImplementor> implementors;
        protected final ByteBuffer data;
        protected final int structSize;
        protected int offset;
        protected int index;

        public SABBDataInvocationHandler(Map<Method, IProxyMethodImplementor> implementors, ByteBuffer data, int structSize, int offset, int index) {
            this.implementors = implementors;
            this.data = data;
            this.structSize = structSize;
            this.offset = offset;
            this.index = index;
        }
        
        public void init() {
            for (IProxyMethodImplementor implementor : implementors.values()) {
                implementor.init(this);
            }
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            IProxyMethodImplementor implementor = implementors.get(method);
            if (implementor != null) {
                return implementor.run(data, offset + index * structSize, args);
            } else {
                throw new UnsupportedOperationException(method.toString());
            }
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public ByteBuffer getData() {
            return data;
        }
    }
    
    static final class SABBPointerInvocationHandler extends SABBDataInvocationHandler {

        private final StructArrayByteBufferImpl owner;

        public SABBPointerInvocationHandler(StructArrayByteBufferImpl owner, Map<Method, IProxyMethodImplementor> implementors, ByteBuffer data, int structSize, int offset, int index) {
            super(implementors, data, structSize, offset, index);
            this.owner = owner;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.equals(POINTER_AT_METHOD)) {
                setIndex((Integer) args[0]);
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

    private static class RecursiveOffsetVisitor extends OffsetVisitor {

        public RecursiveOffsetVisitor(int alignment) {
            super(alignment);
        }

        @Override
        protected int getStructSize(String className) {
            PStructDesc desc;
            try {
                desc = Parser.parse(Class.forName(className));
            } catch (ClassNotFoundException ex) {
                throw new IllegalArgumentException(ex);
            }
            RecursiveOffsetVisitor ov = new RecursiveOffsetVisitor(alignment);
            for (ParsedField field : desc.getFields()) {
                field.accept(ov);
            }
            return ov.getSize();
        }

    }
}
