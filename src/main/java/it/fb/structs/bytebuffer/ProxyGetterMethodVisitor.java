package it.fb.structs.bytebuffer;

import it.fb.structs.apt.ParsedField;
import it.fb.structs.apt.ParsedFieldVisitor;
import it.fb.structs.bytebuffer.ByteBufferProxyHandlerFactory.SABBDataInvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;

/**
 *
 * @author Flavio
 */
class ProxyGetterMethodVisitor implements ParsedFieldVisitor<IProxyMethodImplementor, Integer> {
    
    public static final ProxyGetterMethodVisitor INSTANCE = new ProxyGetterMethodVisitor();
    
    private static int getIndex(Object[] args) {
        return (args == null ? 0 : (Integer) args[0]);
    }

    public IProxyMethodImplementor visitBoolean(ParsedField field, final Integer offset) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public IProxyMethodImplementor visitByte(ParsedField field, final Integer offset) {
        return new BaseProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.get(baseOffset + offset + getIndex(args));
            }
        };
    }

    @Override
    public IProxyMethodImplementor visitChar(ParsedField field, final Integer offset) {
        return new BaseProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.getChar(baseOffset + offset + getIndex(args) * 2);
            }
        };
    }

    @Override
    public IProxyMethodImplementor visitShort(ParsedField field, final Integer offset) {
        return new BaseProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.getShort(baseOffset + offset + getIndex(args) * 2);
            }
        };
    }

    @Override
    public IProxyMethodImplementor visitInt(ParsedField field, final Integer offset) {
        return new BaseProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.getInt(baseOffset + offset + getIndex(args) * 4);
            }
        };
    }

    @Override
    public IProxyMethodImplementor visitLong(ParsedField field, final Integer offset) {
        return new BaseProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.getLong(baseOffset + offset + getIndex(args) * 8);
            }
        };
    }

    @Override
    public IProxyMethodImplementor visitFloat(ParsedField field, final Integer offset) {
        return new BaseProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.getFloat(baseOffset + offset + getIndex(args) * 4);
            }
        };
    }

    @Override
    public IProxyMethodImplementor visitDouble(ParsedField field, final Integer offset) {
        return new BaseProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.getDouble(baseOffset + offset + getIndex(args) * 8);
            }
        };
    }

    @Override
    public IProxyMethodImplementor visitStruct(ParsedField field, Integer offset) {
        final ByteBufferProxyHandlerFactory<?> innerStructHandlerFactory;
        try {
            innerStructHandlerFactory = ByteBufferProxyHandlerFactory.newHandlerFactory(
                    Class.forName(field.getType().getTypeName()));
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
        return new BaseProxyMethodImplementor() {
            
            private Object proxy;
            
            @Override
            public void init(SABBDataInvocationHandler ownerHandler) {
                proxy = innerStructHandlerFactory.newDataInstance(ownerHandler.getData(), 0, 0);
            }

            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                ((SABBDataInvocationHandler)Proxy.getInvocationHandler(proxy)).setOffset(baseOffset);
                return proxy;
            }
        };
    }
    
}
