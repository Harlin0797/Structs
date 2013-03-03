package it.fb.structs.apt;

/**
 *
 * @author Flavio
 */
public interface PFieldTypeVisitor<R, P> {
    R visitBoolean(P parameter);

    R visitByte(P parameter);

    R visitChar(P parameter);

    R visitShort(P parameter);

    R visitInt(P parameter);

    R visitLong(P parameter);

    R visitFloat(P parameter);

    R visitDouble(P parameter);

    R visitStruct(String typeName, P parameter);
}
