package it.fb.structs.bytebuffer;

import it.fb.structs.internal.SField;
import it.fb.structs.internal.SField.SFieldVisitor;
import it.fb.structs.internal.SStructDesc;
import java.nio.ByteBuffer;

/**
 *
 * @author Flavio
 */
public class ProxySetterMethodVisitor implements SFieldVisitor<IProxyMethodImplementor> {
    
    private final int offset;

    public ProxySetterMethodVisitor(int offset) {
        this.offset = offset;
    }
    
    private static int getIndex(Object[] args) {
        return (args.length == 1 ? 0 : (Integer) args[0]);
    }
    
    private static Object getValue(Object[] args) {
        return (args.length == 1 ? args[0] : args[1]);
    }

    @Override
    public IProxyMethodImplementor visitByte(SField field) {
        return new IProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.put(baseOffset + offset + getIndex(args), (Byte) getValue(args));
            }
        };
    }

    @Override
    public IProxyMethodImplementor visitChar(SField field) {
        return new IProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.putChar(baseOffset + offset + getIndex(args) * 2, (Character) getValue(args));
            }
        };
    }

    @Override
    public IProxyMethodImplementor visitShort(SField field) {
        return new IProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.putShort(baseOffset + offset + getIndex(args) * 2, (Short) getValue(args));
            }
        };
    }

    @Override
    public IProxyMethodImplementor visitInt(SField field) {
        return new IProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.putInt(baseOffset + offset + getIndex(args) * 4, (Integer) getValue(args));
            }
        };
    }

    @Override
    public IProxyMethodImplementor visitLong(SField field) {
        return new IProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.putLong(baseOffset + offset + getIndex(args) * 8, (Long) getValue(args));
            }
        };
    }

    @Override
    public IProxyMethodImplementor visitFloat(SField field) {
        return new IProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.putFloat(baseOffset + offset + getIndex(args) * 4, (Float) getValue(args));
            }
        };
    }

    @Override
    public IProxyMethodImplementor visitDouble(SField field) {
        return new IProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.putDouble(baseOffset + offset + getIndex(args) * 8, (Double) getValue(args));
            }
        };
    }

    @Override
    public IProxyMethodImplementor visitStruct(SField field, SStructDesc structDesc) {
        throw new UnsupportedOperationException("Struct setter must not be specified");
    }
    
}