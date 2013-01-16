package it.fb.structs.internal;

import java.lang.reflect.Method;

/**
 *
 * @author Flavio
 */
public class SField {
    
    private final SFieldType type;
    private final int arrayLength;
    private final String name;
    private final Method getter;
    private final Method setter;

    public SField(SFieldType type, int arrayLength, String name, Method getter, Method setter) {
        this.type = type;
        this.arrayLength = arrayLength;
        this.name = name;
        this.getter = getter;
        this.setter = setter;
    }

    public boolean isArray() {
        return arrayLength > 1;
    }
    
    public SFieldType getType() {
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

    public <T> T accept(final SFieldVisitor<T> visitor) {
        return type.accept(new SFieldType.SFieldTypeVisitor<T>() {
            @Override
            public T visitByte() {
                return visitor.visitByte(SField.this);
            }

            @Override
            public T visitChar() {
                return visitor.visitChar(SField.this);
            }

            @Override
            public T visitShort() {
                return visitor.visitShort(SField.this);
            }

            @Override
            public T visitInt() {
                return visitor.visitInt(SField.this);
            }

            @Override
            public T visitLong() {
                return visitor.visitLong(SField.this);
            }

            @Override
            public T visitFloat() {
                return visitor.visitFloat(SField.this);
            }

            @Override
            public T visitDouble() {
                return visitor.visitDouble(SField.this);
            }

            @Override
            public T visitStruct(SStructDesc structDesc) {
                return visitor.visitStruct(SField.this, structDesc);
            }
        });
    }
    
    public interface SFieldVisitor<T> {
        T visitByte(SField field);
        T visitChar(SField field);
        T visitShort(SField field);
        T visitInt(SField field);
        T visitLong(SField field);
        T visitFloat(SField field);
        T visitDouble(SField field);
        T visitStruct(SField field, SStructDesc structDesc);
    }
}
