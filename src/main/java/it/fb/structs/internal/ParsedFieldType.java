package it.fb.structs.internal;

import it.fb.structs.StructPointer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 *
 * @author Flavio
 */
public abstract class ParsedFieldType {
    
    public abstract String getClassName();
    
    public abstract <T> T accept(SFieldTypeVisitor<T> visitor);
    
    public abstract int getSize();
    
    public static interface SFieldTypeVisitor<T> {
        T visitByte();
        T visitChar();
        T visitShort();
        T visitInt();
        T visitLong();
        T visitStruct(String className);
        T visitFloat();
        T visitDouble();
    }
    
    public static ParsedFieldType typeOf(Type javaType) {
        if (javaType == Byte.TYPE) {
            return STypeByte;
        } else if (javaType == Short.TYPE) {
            return STypeShort;
        } else if (javaType == Character.TYPE) {
            return STypeChar;
        } else if (javaType == Integer.TYPE) {
            return STypeInt;
        } else if (javaType == Long.TYPE) {
            return STypeLong;
        } else if (javaType == Float.TYPE) {
            return STypeFloat;
        } else if (javaType == Double.TYPE) {
            return STypeDouble;
        } else if (javaType instanceof ParameterizedType 
                && (StructPointer.class.equals(((ParameterizedType)javaType).getRawType()))) {
            return new STypeStruct((Class<?>)(((ParameterizedType)javaType).getActualTypeArguments()[0]));
        } else {
            return new STypeStruct(((Class<?>)javaType).getName());
        }
    }
    
    static abstract class SBaseType extends ParsedFieldType {
        protected final String className;
        protected final int size;

        public SBaseType(Class<?> javaType, int size) {
            this(javaType.getName(), size);
        }

        public SBaseType(String className, int size) {
            this.className = className;
            this.size = size;
        }

        public String getClassName() {
            return className;
        }

        @Override
        public int getSize() {
            if (size <= 0) {
                throw new IllegalStateException("No default size for this type");
            }
            return size;
        }

        @Override
        public String toString() {
            return className;
        }
    }
    
    public static final ParsedFieldType STypeByte = new SBaseType(Byte.TYPE, 1) {
        @Override
        public <T> T accept(SFieldTypeVisitor<T> visitor) {
            return visitor.visitByte();
        }
    };
    
    public static final ParsedFieldType STypeShort = new SBaseType(Short.TYPE, 2) {
        @Override
        public <T> T accept(SFieldTypeVisitor<T> visitor) {
            return visitor.visitShort();
        }
    };
    
    public static final ParsedFieldType STypeChar = new SBaseType(Character.TYPE, 2) {
        @Override
        public <T> T accept(SFieldTypeVisitor<T> visitor) {
            return visitor.visitChar();
        }
    };
    
    public static final ParsedFieldType STypeInt = new SBaseType(Integer.TYPE, 4) {
        @Override
        public <T> T accept(SFieldTypeVisitor<T> visitor) {
            return visitor.visitInt();
        }
    };
    
    public static final ParsedFieldType STypeLong = new SBaseType(Long.TYPE, 8) {
        @Override
        public <T> T accept(SFieldTypeVisitor<T> visitor) {
            return visitor.visitLong();
        }
    };
    
    public static final ParsedFieldType STypeFloat = new SBaseType(Float.TYPE, 4) {
        @Override
        public <T> T accept(SFieldTypeVisitor<T> visitor) {
            return visitor.visitFloat();
        }
    };
    
    public static final ParsedFieldType STypeDouble = new SBaseType(Double.TYPE, 8) {
        @Override
        public <T> T accept(SFieldTypeVisitor<T> visitor) {
            return visitor.visitDouble();
        }
    };
    
    public static class STypeStruct extends SBaseType {

        public STypeStruct(Class<?> structClass) {
            super(structClass, -1);
        }

        public STypeStruct(String className) {
            super(className, -1);
        }

        @Override
        public <T> T accept(SFieldTypeVisitor<T> visitor) {
            return visitor.visitStruct(className);
        }
    }

}
