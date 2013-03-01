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

    public <T> T accept(final ParsedFieldVisitor<T> visitor) {
        return type.accept(new ParsedFieldType.SFieldTypeVisitor<T>() {
            @Override
            public T visitBoolean() {
                return visitor.visitBoolean(ParsedField.this);
            }
            @Override
            public T visitByte() {
                return visitor.visitByte(ParsedField.this);
            }

            @Override
            public T visitChar() {
                return visitor.visitChar(ParsedField.this);
            }

            @Override
            public T visitShort() {
                return visitor.visitShort(ParsedField.this);
            }

            @Override
            public T visitInt() {
                return visitor.visitInt(ParsedField.this);
            }

            @Override
            public T visitLong() {
                return visitor.visitLong(ParsedField.this);
            }

            @Override
            public T visitFloat() {
                return visitor.visitFloat(ParsedField.this);
            }

            @Override
            public T visitDouble() {
                return visitor.visitDouble(ParsedField.this);
            }

            @Override
            public T visitStruct() {
                return visitor.visitStruct(ParsedField.this);
            }
        });
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
