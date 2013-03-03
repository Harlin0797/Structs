package it.fb.structs.asm;

/**
 *
 * @author Flavio
 */
public interface StructData {
    
    public int getSize();
    
    public byte getByte(int position);
    public void putByte(int position, byte value);
    
    public short getShort(int position);
    public void putShort(int position, short value);
    
    public char getChar(int position);
    public void putChar(int position, char value);
    
    public int getInt(int position);
    public void putInt(int position, int value);
    
    public long getLong(int position);
    public void putLong(int position, long value);
    
    public float getFloat(int position);
    public void putFloat(int position, float value);
    
    public double getDouble(int position);
    public void putDouble(int position, double value);
    
    public void release();

    public interface Factory<D extends StructData> {
        public Class<D> getBufferClass();
        public D newBuffer(int size);
    }
}
