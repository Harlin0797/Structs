package it.fb.structs;

import sun.misc.Unsafe;

/**
 *
 * @author Flavio
 */
public final class UnsafeStructData implements StructData {

    private final Unsafe unsafe;
    private final long baseAddress;
    private final int size;

    private UnsafeStructData(Unsafe unsafe, long baseAddress, int size) {
        this.unsafe = unsafe;
        this.baseAddress = baseAddress;
        this.size = size;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public byte getByte(int position) {
        return unsafe.getByte(baseAddress + position);
    }

    @Override
    public void putByte(int position, byte value) {
        unsafe.putByte(baseAddress + position, value);
    }

    @Override
    public short getShort(int position) {
        return unsafe.getShort(baseAddress + position);
    }

    @Override
    public void putShort(int position, short value) {
        unsafe.putShort(baseAddress + position, value);
    }

    @Override
    public char getChar(int position) {
        return unsafe.getChar(baseAddress + position);
    }

    @Override
    public void putChar(int position, char value) {
        unsafe.putChar(baseAddress + position, value);
    }

    @Override
    public int getInt(int position) {
        return unsafe.getInt(baseAddress + position);
    }

    @Override
    public void putInt(int position, int value) {
        unsafe.putInt(baseAddress + position, value);
    }

    @Override
    public long getLong(int position) {
        return unsafe.getLong(baseAddress + position);
    }

    @Override
    public void putLong(int position, long value) {
        unsafe.putLong(baseAddress + position, value);
    }

    @Override
    public float getFloat(int position) {
        return unsafe.getFloat(baseAddress + position);
    }

    @Override
    public void putFloat(int position, float value) {
        unsafe.putFloat(baseAddress + position, value);
    }

    @Override
    public double getDouble(int position) {
        return unsafe.getDouble(baseAddress + position);
    }

    @Override
    public void putDouble(int position, double value) {
        unsafe.putDouble(baseAddress + position, value);
    }

    @Override
    public void release() {
        unsafe.freeMemory(baseAddress);
    }

    public static StructData.Factory<UnsafeStructData> Factory = new StructData.Factory<UnsafeStructData>() {
        
        private final Unsafe TheUnsafe;
        
        {
            try {
                java.lang.reflect.Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                TheUnsafe = (sun.misc.Unsafe) field.get(null);
            } catch (Exception e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        @Override
        public Class<UnsafeStructData> getBufferClass() {
            return UnsafeStructData.class;
        }

        @Override
        public UnsafeStructData newBuffer(int size) {
            long baseAddress = TheUnsafe.allocateMemory(size);
            return new UnsafeStructData(TheUnsafe, baseAddress, size);
        }
    };
}
