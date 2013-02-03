package it.fb.structs.impl;

import it.fb.structs.SimpleStruct;
import it.fb.structs.StructArray;
import it.fb.structs.StructData;
import it.fb.structs.StructPointer;

/**
 *
 * @author Flavio
 */
public final class SimpleStructImpl implements SimpleStruct, StructPointer<SimpleStruct> {
    
    private static final int SIZE = 928;
    private final StructData data;
    private final StructArray<SimpleStruct> owner;
    private int baseOffset;
    private int position;

    public SimpleStructImpl(StructData data, StructArray<SimpleStruct> owner, int baseOffset, int index) {
        this.data = data;
        this.owner = owner;
        this.baseOffset = baseOffset;
        at(index);
    }

    @Override
    public int getI() {
        return data.getInt(position);
    }

    @Override
    public void setI(int value) {
        data.putInt(position, value);
    }

    @Override
    public long getL() {
        return data.getLong(position + 4);
    }

    @Override
    public void setL(long value) {
        data.putLong(position + 4, value);
    }
    
    public float getF() {
        return data.getFloat(position + 32);
    }
    
    public void setF(float value) {
        data.putFloat(position + 32, value);
    }

    @Override
    public byte getB(int index) {
        return data.getByte(position + 12 + index);
    }

    @Override
    public void setB(int index, byte value) {
        data.putByte(position + 12 + index, value);
    }

    public long getL(int index) {
        return data.getLong(position + 14 + index * 8);
    }

    public void setL(int index, long value) {
        data.putLong(position + 14 + index * 8, value);
    }

    public int getI(int index) {
        return data.getInt(position + 320 + index * 4);
    }

    public void setI(int index, int value) {
        data.putInt(position + 320 + index * 4, value);
    }

    @Override
    public SimpleStruct get() {
        return this;
    }

    @Override
    public StructPointer<SimpleStruct> at(int index) {
        position = baseOffset + index * SIZE;
        return this;
    }

    public void setBaseOffset(int baseOffset) {
        this.baseOffset = baseOffset;
        this.position = baseOffset;
    }

    @Override
    public StructArray<SimpleStruct> getOwner() {
        return owner;
    }
    
}
