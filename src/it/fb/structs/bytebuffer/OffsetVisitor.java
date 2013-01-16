package it.fb.structs.bytebuffer;

import it.fb.structs.internal.SField;
import it.fb.structs.internal.SField.SFieldVisitor;
import it.fb.structs.internal.SStructDesc;

/**
 *
 * @author Flavio
 */
public class OffsetVisitor implements SFieldVisitor<Integer> {

    private final int alignment;
    private int size = 0;

    public OffsetVisitor(int alignment) {
        this.alignment = alignment;
    }
    
    private int addSize(int baseSize, int arrayLength) {
        int ret = size;
        size += (arrayLength <= 0 ? baseSize : baseSize * arrayLength);
        if (size % alignment != 0) {
            size += alignment - (size % alignment);
        }
        return ret;
    }

    @Override
    public Integer visitByte(SField field) {
        return addSize(1, field.getArrayLength());
    }

    @Override
    public Integer visitChar(SField field) {
        return addSize(2, field.getArrayLength());
    }

    @Override
    public Integer visitShort(SField field) {
        return addSize(2, field.getArrayLength());
    }

    @Override
    public Integer visitInt(SField field) {
        return addSize(4, field.getArrayLength());
    }

    @Override
    public Integer visitLong(SField field) {
        return addSize(8, field.getArrayLength());
    }

    @Override
    public Integer visitFloat(SField field) {
        return addSize(4, field.getArrayLength());
    }

    @Override
    public Integer visitDouble(SField field) {
        return addSize(8, field.getArrayLength());
    }

    @Override
    public Integer visitStruct(SField field, SStructDesc structDesc) {
        OffsetVisitor ov = new OffsetVisitor(alignment);
        for (SField innerField : structDesc.getFields()) {
            innerField.accept(ov);
        }
        return addSize(ov.getSize(), field.getArrayLength());
    }

    public int getSize() {
        return size;
    }

    public int getAlignment() {
        return alignment;
    }
    
}
