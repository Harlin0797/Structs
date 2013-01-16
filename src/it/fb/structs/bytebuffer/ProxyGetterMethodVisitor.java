package it.fb.structs.bytebuffer;

import it.fb.structs.internal.SField;
import it.fb.structs.internal.SField.SFieldVisitor;
import it.fb.structs.internal.SStructDesc;
import java.nio.ByteBuffer;

/**
 *
 * @author Flavio
 */
public class ProxyGetterMethodVisitor implements SFieldVisitor<IProxyMethodImplementor> {
    
    private final int offset;

    public ProxyGetterMethodVisitor(int offset) {
        this.offset = offset;
    }
    
    private static int getIndex(Object[] args) {
        return (args == null ? 0 : (Integer) args[0]);
    }

    @Override
    public IProxyMethodImplementor visitByte(SField field) {
        return new IProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.get(baseOffset + offset + getIndex(args));
            }
        };
    }

    @Override
    public IProxyMethodImplementor visitChar(SField field) {
        return new IProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.getChar(baseOffset + offset + getIndex(args) * 2);
            }
        };
    }

    @Override
    public IProxyMethodImplementor visitShort(SField field) {
        return new IProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.getShort(baseOffset + offset + getIndex(args) * 2);
            }
        };
    }

    @Override
    public IProxyMethodImplementor visitInt(SField field) {
        return new IProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.getInt(baseOffset + offset + getIndex(args) * 4);
            }
        };
    }

    @Override
    public IProxyMethodImplementor visitLong(SField field) {
        return new IProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.getLong(baseOffset + offset + getIndex(args) * 8);
            }
        };
    }

    @Override
    public IProxyMethodImplementor visitFloat(SField field) {
        return new IProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.getFloat(baseOffset + offset + getIndex(args) * 4);
            }
        };
    }

    @Override
    public IProxyMethodImplementor visitDouble(SField field) {
        return new IProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.getDouble(baseOffset + offset + getIndex(args) * 8);
            }
        };
    }

    @Override
    public IProxyMethodImplementor visitStruct(SField field, SStructDesc structDesc) {
        return new IProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                throw new UnsupportedOperationException();
            }
        };
    }
    
}
