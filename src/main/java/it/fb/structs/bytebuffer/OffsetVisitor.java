package it.fb.structs.bytebuffer;

import it.fb.structs.internal.ParsedField;
import it.fb.structs.internal.ParsedField.ParsedFieldVisitor;

/**
 *
 * @author Flavio
 */
public abstract class OffsetVisitor implements ParsedFieldVisitor<Integer> {

    protected final int alignment;
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

    public Integer visitBoolean(ParsedField field) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Integer visitByte(ParsedField field) {
        return addSize(1, field.getArrayLength());
    }

    @Override
    public Integer visitChar(ParsedField field) {
        return addSize(2, field.getArrayLength());
    }

    @Override
    public Integer visitShort(ParsedField field) {
        return addSize(2, field.getArrayLength());
    }

    @Override
    public Integer visitInt(ParsedField field) {
        return addSize(4, field.getArrayLength());
    }

    @Override
    public Integer visitLong(ParsedField field) {
        return addSize(8, field.getArrayLength());
    }

    @Override
    public Integer visitFloat(ParsedField field) {
        return addSize(4, field.getArrayLength());
    }

    @Override
    public Integer visitDouble(ParsedField field) {
        return addSize(8, field.getArrayLength());
    }

    @Override
    public Integer visitStruct(ParsedField field) {
        return addSize(getStructSize(field.getType().getClassName()), field.getArrayLength());
    }

    public int getSize() {
        return size;
    }

    public int getAlignment() {
        return alignment;
    }
    
    protected abstract int getStructSize(String className);
}
