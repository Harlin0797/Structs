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
    private final boolean hasGetter;
    private final boolean hasSetter;

    public ParsedField(ParsedFieldType type, int arrayLength, String name, int position, boolean hasGetter, boolean hasSetter) {
        this.type = type;
        this.arrayLength = arrayLength;
        this.name = name;
        this.position = position;
        this.hasGetter = hasGetter;
        this.hasSetter = hasSetter;
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

    public int getPosition() {
        return position;
    }

    public boolean hasGetter() {
        return hasGetter;
    }

    public boolean hasSetter() {
        return hasSetter;
    }

    public <R, P> R accept(final ParsedFieldVisitor<R, P> visitor, P parameter) {
        return type.accept(new ParsedFieldType.SFieldTypeVisitor<R, P>() {
            @Override
            public R visitBoolean(P p) {
                return visitor.visitBoolean(ParsedField.this, p);
            }
            @Override
            public R visitByte(P p) {
                return visitor.visitByte(ParsedField.this, p);
            }

            @Override
            public R visitChar(P p) {
                return visitor.visitChar(ParsedField.this, p);
            }

            @Override
            public R visitShort(P p) {
                return visitor.visitShort(ParsedField.this, p);
            }

            @Override
            public R visitInt(P p) {
                return visitor.visitInt(ParsedField.this, p);
            }

            @Override
            public R visitLong(P p) {
                return visitor.visitLong(ParsedField.this, p);
            }

            @Override
            public R visitFloat(P p) {
                return visitor.visitFloat(ParsedField.this, p);
            }

            @Override
            public R visitDouble(P p) {
                return visitor.visitDouble(ParsedField.this, p);
            }

            @Override
            public R visitStruct(P p) {
                return visitor.visitStruct(ParsedField.this, p);
            }
        }, parameter);
    }

    @Override
    public String toString() {
        return type + " " + name;
    }

    public Method findGetterOn(Class<?> claxx) {
        try {
            if (isArray()) {
                return claxx.getMethod("get" + name, Integer.TYPE);
            } else {
                return claxx.getMethod("get" + name);
            }
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException("No getter for " + name + " on class " + claxx, ex);
        }
    }

    public Method findSetterOn(Class<?> claxx) {
        try {
            if (isArray()) {
                return claxx.getMethod("set" + name, Integer.TYPE, type.getPrimitiveClass());
            } else {
                return claxx.getMethod("set" + name, type.getPrimitiveClass());
            }
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException("No getter for " + name + " on class " + claxx, ex);
        }
    }

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
}
