package it.fb.structs.asm;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author Flavio
 */
public final class ByteBufferStructData implements StructData {
    
    private final ByteBuffer buffer;

    private ByteBufferStructData(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int getSize() {
        return buffer.capacity();
    }

    @Override
    public byte getByte(int index) {
        return buffer.get(index);
    }

    @Override
    public char getChar(int index) {
        return buffer.getChar(index);
    }

    @Override
    public short getShort(int index) {
        return buffer.getShort(index);
    }

    @Override
    public int getInt(int index) {
        return buffer.getInt(index);
    }

    @Override
    public long getLong(int index) {
        return buffer.getLong(index);
    }

    @Override
    public float getFloat(int index) {
        return buffer.getFloat(index);
    }

    @Override
    public double getDouble(int index) {
        return buffer.getDouble(index);
    }

    @Override
    public void putByte(int index, byte b) {
        buffer.put(index, b);
    }

    @Override
    public void putChar(int index, char value) {
        buffer.putChar(index, value);
    }

    @Override
    public void putShort(int index, short value) {
        buffer.putShort(index, value);
    }

    @Override
    public void putInt(int index, int value) {
        buffer.putInt(index, value);
    }

    @Override
    public void putLong(int index, long value) {
        buffer.putLong(index, value);
    }

    @Override
    public void putFloat(int index, float value) {
        buffer.putFloat(index, value);
    }

    @Override
    public void putDouble(int index, double value) {
        buffer.putDouble(index, value);
    }

    @Override
    public void release() {
    }

    public static class Plain implements StructData.Factory<ByteBufferStructData> {
        public static final StructData.Factory<ByteBufferStructData> Native 
                = new Plain(ByteOrder.nativeOrder());
        public static final StructData.Factory<ByteBufferStructData> BigEndian 
                = new Plain(ByteOrder.BIG_ENDIAN);
        public static final StructData.Factory<ByteBufferStructData> LittleEndian
                = new Plain(ByteOrder.LITTLE_ENDIAN);

        private final ByteOrder order;

        private Plain(ByteOrder order) {
            this.order = order;
        }

        @Override
        public Class<ByteBufferStructData> getBufferClass() {
            return ByteBufferStructData.class;
        }

        @Override
        public final ByteBufferStructData newBuffer(int size) {
            return new ByteBufferStructData(ByteBuffer.allocate(size).order(order));
        }
    }

    public static final class Direct implements StructData.Factory<ByteBufferStructData> {
        public static final StructData.Factory<ByteBufferStructData> Native 
                = new Direct(ByteOrder.nativeOrder());
        public static final StructData.Factory<ByteBufferStructData> BigEndian 
                = new Direct(ByteOrder.BIG_ENDIAN);
        public static final StructData.Factory<ByteBufferStructData> LittleEndian
                = new Direct(ByteOrder.LITTLE_ENDIAN);

        private final ByteOrder order;

        private Direct(ByteOrder order) {
            this.order = order;
        }

        @Override
        public Class<ByteBufferStructData> getBufferClass() {
            return ByteBufferStructData.class;
        }

        @Override
        public final ByteBufferStructData newBuffer(int size) {
            return new ByteBufferStructData(ByteBuffer.allocateDirect(size).order(order));
        }
    }
    
}
