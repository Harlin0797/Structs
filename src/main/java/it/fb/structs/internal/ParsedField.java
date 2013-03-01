package it.fb.structs.internal;

import java.lang.reflect.Method;

/**
 *
 * @author Flavio
 */
public class ParsedField {
    
    private final ParsedFieldType type;
    private final int arrayLength;
    private final String name;
    private final int position;
    private final Method getter;
    private final Method setter;

    public ParsedField(ParsedFieldType type, int arrayLength, String name, int position, Method getter, Method setter) {
        this.type = type;
        this.arrayLength = arrayLength;
        this.name = name;
        this.position = position;
        this.getter = getter;
        this.setter = setter;
    }

    public boolean isArray() {
        return arrayLength > 1;
    }
    
    public ParsedFieldType getType() {
        return type;
    }

    public int getArrayLength() {
        return arrayLength;
    }

    public String getName() {
        return name;
    }

    public Method getGetter() {
        return getter;
    }

    public Method getSetter() {
        return setter;
    }

    public int getPosition() {
        return position;
    }

    public <R> R accept(final ParsedFieldVisitor<R> visitor) {
        return type.accept(new ParsedFieldType.SFieldTypeVisitor<R, Void>() {
            @Override
            public R visitBoolean(Void p) {
                return visitor.visitBoolean(ParsedField.this);
            }
            @Override
            public R visitByte(Void p) {
                return visitor.visitByte(ParsedField.this);
            }

            @Override
            public R visitChar(Void p) {
                return visitor.visitChar(ParsedField.this);
            }

            @Override
            public R visitShort(Void p) {
                return visitor.visitShort(ParsedField.this);
            }

            @Override
            public R visitInt(Void p) {
                return visitor.visitInt(ParsedField.this);
            }

            @Override
            public R visitLong(Void p) {
                return visitor.visitLong(ParsedField.this);
            }

            @Override
            public R visitFloat(Void p) {
                return visitor.visitFloat(ParsedField.this);
            }

            @Override
            public R visitDouble(Void p) {
                return visitor.visitDouble(ParsedField.this);
            }

            @Override
            public R visitStruct(Void p) {
                return visitor.visitStruct(ParsedField.this);
            }
        }, null);
    }

    @Override
    public String toString() {
        return type + " " + name;
    }

    public interface ParsedFieldVisitor<T> {
        T visitBoolean(ParsedField field);
        T visitByte(ParsedField field);
        T visitChar(ParsedField field);
        T visitShort(ParsedField field);
        T visitInt(ParsedField field);
        T visitLong(ParsedField field);
        T visitFloat(ParsedField field);
        T visitDouble(ParsedField field);
        T visitStruct(ParsedField field);
    }
}
