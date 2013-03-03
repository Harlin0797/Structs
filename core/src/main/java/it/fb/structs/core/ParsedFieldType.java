package it.fb.structs.core;

import java.lang.reflect.Type;

/**
 *
 * @author Flavio
 */
public abstract class ParsedFieldType {

    public abstract String getTypeName();

    public abstract <R, P> R accept(PFieldTypeVisitor<R, P> visitor, P parameter);

    public abstract int getSize();

    public abstract boolean isPrimitive();

    public abstract Class<?> getPrimitiveClass();

    public static ParsedFieldType typeOf(Class<?> typeClass) {
        return typeOf(typeClass.getName());
    }

    public static ParsedFieldType typeOf(Type type) {
        return typeOf((Class<?>) type);
    }

    public static ParsedFieldType typeOf(String typeName) {
        if (Byte.TYPE.getName().equals(typeName)) {
            return PFTByte;
        } if (Short.TYPE.getName().equals(typeName)) {
            return PFTShort;
        } if (Character.TYPE.getName().equals(typeName)) {
            return PFTChar;
        } if (Integer.TYPE.getName().equals(typeName)) {
            return PFTInt;
        } if (Long.TYPE.getName().equals(typeName)) {
            return PFTLong;
        } if (Float.TYPE.getName().equals(typeName)) {
            return PFTFloat;
        } if (Double.TYPE.getName().equals(typeName)) {
            return PFTDouble;
        } else {
            return new PTypeStruct(typeName);
        }
    }

    private static abstract class PBaseType extends ParsedFieldType {
        protected final String typeName;
        protected final int size;

        public PBaseType(String typeName, int size) {
            this.typeName = typeName;
            this.size = size;
        }
        
        @Override
        public String getTypeName() {
            return typeName;
        }

        @Override
        public int getSize() {
            if (size <= 0) {
                throw new IllegalStateException("No default size for this type");
            }
            return size;
        }

        @Override
        public boolean isPrimitive() {
            return false;
        }

        @Override
        public Class<?> getPrimitiveClass() {
            throw new UnsupportedOperationException("Type is not primitive: " + typeName);
        }

        @Override
        public String toString() {
            return typeName;
        }
    }
    
    private static abstract class PPrimitiveType extends PBaseType {
        private final Class<?> javaType;

        public PPrimitiveType(Class<?> javaType, int size) {
            super(javaType.getName(), size);
            this.javaType = javaType;
        }

        @Override
        public boolean isPrimitive() {
            return true;
        }

        @Override
        public Class<?> getPrimitiveClass() {
            return javaType;
        }
    }
    
    public static final ParsedFieldType PFTBoolean = new PPrimitiveType(Boolean.TYPE, 1) {
        @Override
        public <R, P> R accept(PFieldTypeVisitor<R, P> visitor, P parameter) {
            return visitor.visitBoolean(parameter);
        }
    };
    
    public static final ParsedFieldType PFTByte = new PPrimitiveType(Byte.TYPE, 1) {
        @Override
        public <R, P> R accept(PFieldTypeVisitor<R, P> visitor, P parameter) {
            return visitor.visitByte(parameter);
        }
    };
    
    public static final ParsedFieldType PFTShort = new PPrimitiveType(Short.TYPE, 2) {
        @Override
        public <R, P> R accept(PFieldTypeVisitor<R, P> visitor, P parameter) {
            return visitor.visitShort(parameter);
        }
    };
    
    public static final ParsedFieldType PFTChar = new PPrimitiveType(Character.TYPE, 2) {
        @Override
        public <R, P> R accept(PFieldTypeVisitor<R, P> visitor, P parameter) {
            return visitor.visitChar(parameter);
        }
    };
    
    public static final ParsedFieldType PFTInt = new PPrimitiveType(Integer.TYPE, 4) {
        @Override
        public <R, P> R accept(PFieldTypeVisitor<R, P> visitor, P parameter) {
            return visitor.visitInt(parameter);
        }
    };
    
    public static final ParsedFieldType PFTLong = new PPrimitiveType(Long.TYPE, 8) {
        @Override
        public <R, P> R accept(PFieldTypeVisitor<R, P> visitor, P parameter) {
            return visitor.visitLong(parameter);
        }
    };
    
    public static final ParsedFieldType PFTFloat = new PPrimitiveType(Float.TYPE, 4) {
        @Override
        public <R, P> R accept(PFieldTypeVisitor<R, P> visitor, P parameter) {
            return visitor.visitFloat(parameter);
        }
    };
    
    public static final ParsedFieldType PFTDouble = new PPrimitiveType(Double.TYPE, 8) {
        @Override
        public <R, P> R accept(PFieldTypeVisitor<R, P> visitor, P parameter) {
            return visitor.visitDouble(parameter);
        }
    };

    private static class PTypeStruct extends PBaseType {

        public PTypeStruct(String typeName) {
            super(typeName, -1);
        }

        @Override
        public <R, P> R accept(PFieldTypeVisitor<R, P> visitor, P parameter) {
            return visitor.visitStruct(typeName, parameter);
        }
    }

}
