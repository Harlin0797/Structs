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
    
    public abstract <R, P> R accept(SFieldTypeVisitor<R, P> visitor, P parameter);
    
    public abstract int getSize();
    
    public static interface SFieldTypeVisitor<R, P> {
        R visitBoolean(P parameter);
        R visitByte(P parameter);
        R visitChar(P parameter);
        R visitShort(P parameter);
        R visitInt(P parameter);
        R visitLong(P parameter);
        R visitFloat(P parameter);
        R visitDouble(P parameter);
        R visitStruct(P parameter);
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
        public <R, P> R accept(SFieldTypeVisitor<R, P> visitor, P parameter) {
            return visitor.visitByte(parameter);
        }
    };
    
    public static final ParsedFieldType STypeShort = new SBaseType(Short.TYPE, 2) {
        @Override
        public <R, P> R accept(SFieldTypeVisitor<R, P> visitor, P parameter) {
            return visitor.visitShort(parameter);
        }
    };
    
    public static final ParsedFieldType STypeChar = new SBaseType(Character.TYPE, 2) {
        @Override
        public <R, P> R accept(SFieldTypeVisitor<R, P> visitor, P parameter) {
            return visitor.visitChar(parameter);
        }
    };
    
    public static final ParsedFieldType STypeInt = new SBaseType(Integer.TYPE, 4) {
        @Override
        public <R, P> R accept(SFieldTypeVisitor<R, P> visitor, P parameter) {
            return visitor.visitInt(parameter);
        }
    };
    
    public static final ParsedFieldType STypeLong = new SBaseType(Long.TYPE, 8) {
        @Override
        public <R, P> R accept(SFieldTypeVisitor<R, P> visitor, P parameter) {
            return visitor.visitLong(parameter);
        }
    };
    
    public static final ParsedFieldType STypeFloat = new SBaseType(Float.TYPE, 4) {
        @Override
        public <R, P> R accept(SFieldTypeVisitor<R, P> visitor, P parameter) {
            return visitor.visitFloat(parameter);
        }
    };
    
    public static final ParsedFieldType STypeDouble = new SBaseType(Double.TYPE, 8) {
        @Override
        public <R, P> R accept(SFieldTypeVisitor<R, P> visitor, P parameter) {
            return visitor.visitDouble(parameter);
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
        public <R, P> R accept(SFieldTypeVisitor<R, P> visitor, P parameter) {
            return visitor.visitStruct(parameter);
        }
    }

}
