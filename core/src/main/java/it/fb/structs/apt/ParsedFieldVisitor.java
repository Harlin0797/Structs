package it.fb.structs.apt;

/**
 *
 * @author Flavio
 */
public interface ParsedFieldVisitor<R, P> {
    R visitBoolean(ParsedField field, P parameter);

    R visitByte(ParsedField field, P parameter);

    R visitChar(ParsedField field, P parameter);

    R visitShort(ParsedField field, P parameter);

    R visitInt(ParsedField field, P parameter);

    R visitLong(ParsedField field, P parameter);

    R visitFloat(ParsedField field, P parameter);

    R visitDouble(ParsedField field, P parameter);

    R visitStruct(ParsedField field, P parameter);
}
