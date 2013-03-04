package it.fb.structs.proxy;

import it.fb.structs.asm.StructData;
import it.fb.structs.core.PFieldTypeVisitor;

/**
 *
 * @author Flavio
 */
public class GetterVisitor<D extends StructData> implements PFieldTypeVisitor<Object, Integer> {
    
    private final D data;

    public GetterVisitor(D data) {
        this.data = data;
    }

    public Object visitBoolean(Integer position) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object visitByte(Integer position) {
        return data.getByte(position);
    }

    public Object visitChar(Integer position) {
        return data.getChar(position);
    }

    public Object visitShort(Integer position) {
        return data.getShort(position);
    }

    public Object visitInt(Integer position) {
        return data.getInt(position);
    }

    public Object visitLong(Integer position) {
        return data.getLong(position);
    }

    public Object visitFloat(Integer position) {
        return data.getFloat(position);
    }

    public Object visitDouble(Integer position) {
        return data.getDouble(position);
    }

    public Object visitStruct(String typeName, Integer position) {
        throw new UnsupportedOperationException();
    }
    
}
