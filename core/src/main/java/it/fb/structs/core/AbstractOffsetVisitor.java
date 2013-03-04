package it.fb.structs.core;

/**
 *
 * @author Flavio
 */
public abstract class AbstractOffsetVisitor implements ParsedFieldVisitor<Integer, Void> {

    protected final int alignment;
    private int size = 0;

    public AbstractOffsetVisitor(int alignment) {
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

    public Integer visitBoolean(ParsedField field, Void p) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Integer visitByte(ParsedField field, Void p) {
        return addSize(1, field.getArrayLength());
    }

    @Override
    public Integer visitChar(ParsedField field, Void p) {
        return addSize(2, field.getArrayLength());
    }

    @Override
    public Integer visitShort(ParsedField field, Void p) {
        return addSize(2, field.getArrayLength());
    }

    @Override
    public Integer visitInt(ParsedField field, Void p) {
        return addSize(4, field.getArrayLength());
    }

    @Override
    public Integer visitLong(ParsedField field, Void p) {
        return addSize(8, field.getArrayLength());
    }

    @Override
    public Integer visitFloat(ParsedField field, Void p) {
        return addSize(4, field.getArrayLength());
    }

    @Override
    public Integer visitDouble(ParsedField field, Void p) {
        return addSize(8, field.getArrayLength());
    }

    @Override
    public Integer visitStruct(ParsedField field, Void p) {
        return addSize(getStructSize(field.getType().getTypeName()), field.getArrayLength());
    }

    public int getSize() {
        return size;
    }

    public int getAlignment() {
        return alignment;
    }
    
    protected abstract int getStructSize(String className);
}
