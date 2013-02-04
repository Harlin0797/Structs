package it.fb.structs.asm;

import it.fb.structs.MediumStruct;
import it.fb.structs.SimpleStruct;
import it.fb.structs.StructArray;
import it.fb.structs.StructData;
import it.fb.structs.StructPointer;
import it.fb.structs.impl.SimpleStructImpl;

/**
 *
 * @author Flavio
 */
public final class MediumStructImpl implements MediumStruct, StructPointer<MediumStruct> {
    
    private static final int SIZE = 928;
    private final StructData data;
    private final StructArray<SimpleStruct> owner;
    private final SimpleStructImpl _Simple;
    private final SimpleStructImpl _Simple2;
    private final SimpleStructImpl _Simple3;
    private final SimpleStructImpl _Simple4;
    private int baseOffset;
    private int position;

    public MediumStructImpl(StructData data, StructArray<SimpleStruct> owner, int baseOffset, int index) {
        this.data = data;
        this.owner = owner;
        this.baseOffset = baseOffset;
        this._Simple = new SimpleStructImpl(data, null, 36, 0);
        this._Simple2 = new SimpleStructImpl(data, null, 86, 0);
        this._Simple3 = new SimpleStructImpl(data, null, 66, 0);
        this._Simple4 = new SimpleStructImpl(data, null, 16, 0);
        at(index);
    }
    
    @Override
    public int getI() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setI(int value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float getF() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setF(float value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte getB(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setB(int index, byte value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StructPointer<SimpleStruct> getSimple() {
        return _Simple;
    }

    public StructPointer<SimpleStruct> getSimple(int index) {
        return _Simple.at(index);
    }

    @Override
    public MediumStruct get() {
        return this;
    }

    @Override
    public StructPointer<MediumStruct> at(int index) {
        int p = position = baseOffset + index * SIZE;
        _Simple.setBaseOffset(p + 44);
        _Simple2.setBaseOffset(p + 52);
        _Simple3.setBaseOffset(p + 68);
        _Simple4.setBaseOffset(p + 32);
        return this;
    }

    public void setBaseOffset(int baseOffset) {
        this.baseOffset = baseOffset;
        at(0);
    }

    @Override
    public StructArray<MediumStruct> getOwner() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
