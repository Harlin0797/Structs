package it.fb.structs.core;

import it.fb.structs.core.PFieldTypeVisitor;

/**
 *
 * @author Flavio
 */
public class SimpleFieldTypeVisitor<R, P> implements PFieldTypeVisitor<R, P> {
    
    public R visitBoolean(P parameter) {
        return null;
    }

    public R visitByte(P parameter) {
        return null;
    }

    public R visitChar(P parameter) {
        return null;
    }

    public R visitShort(P parameter) {
        return null;
    }

    public R visitInt(P parameter) {
        return null;
    }

    public R visitLong(P parameter) {
        return null;
    }

    public R visitFloat(P parameter) {
        return null;
    }

    public R visitDouble(P parameter) {
        return null;
    }

    public R visitStruct(String typeName, P parameter) {
        return null;
    }
    
}
