package it.fb.structs.bytebuffer;

import it.fb.structs.internal.ParsedField;
import it.fb.structs.internal.ParsedField.ParsedFieldVisitor;
import java.nio.ByteBuffer;

/**
 *
 * @author Flavio
 */
class ProxySetterMethodVisitor implements ParsedFieldVisitor<IProxyMethodImplementor> {
    
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

    public IProxyMethodImplementor visitBoolean(ParsedField field) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public BaseProxyMethodImplementor visitByte(ParsedField field) {
        return new BaseProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.put(baseOffset + offset + getIndex(args), (Byte) getValue(args));
            }
        };
    }

    @Override
    public BaseProxyMethodImplementor visitChar(ParsedField field) {
        return new BaseProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.putChar(baseOffset + offset + getIndex(args) * 2, (Character) getValue(args));
            }
        };
    }

    @Override
    public BaseProxyMethodImplementor visitShort(ParsedField field) {
        return new BaseProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.putShort(baseOffset + offset + getIndex(args) * 2, (Short) getValue(args));
            }
        };
    }

    @Override
    public BaseProxyMethodImplementor visitInt(ParsedField field) {
        return new BaseProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.putInt(baseOffset + offset + getIndex(args) * 4, (Integer) getValue(args));
            }
        };
    }

    @Override
    public BaseProxyMethodImplementor visitLong(ParsedField field) {
        return new BaseProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.putLong(baseOffset + offset + getIndex(args) * 8, (Long) getValue(args));
            }
        };
    }

    @Override
    public BaseProxyMethodImplementor visitFloat(ParsedField field) {
        return new BaseProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.putFloat(baseOffset + offset + getIndex(args) * 4, (Float) getValue(args));
            }
        };
    }

    @Override
    public BaseProxyMethodImplementor visitDouble(ParsedField field) {
        return new BaseProxyMethodImplementor() {
            @Override
            public Object run(ByteBuffer data, int baseOffset, Object[] args) {
                return data.putDouble(baseOffset + offset + getIndex(args) * 8, (Double) getValue(args));
            }
        };
    }

    @Override
    public BaseProxyMethodImplementor visitStruct(ParsedField field) {
        throw new UnsupportedOperationException("Struct setter must not be specified");
    }
    
}
