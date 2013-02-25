package it.fb.structs.apt;

/**
 *
 * @author Flavio
 */
public abstract class ParsedFieldType {
    
    public abstract String getTypeName();
    
    public abstract <R, P> R accept(PFieldTypeVisitor<R, P> visitor, P parameter);
    
    public abstract int getSize();
    
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
        
        protected PBaseType(Class<?> javaType, int size) {
            this.typeName = javaType.getName();
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
        public String toString() {
            return typeName;
        }
    }
    
    public static final ParsedFieldType PFTBoolean = new PBaseType(Boolean.TYPE, 1) {
        @Override
        public <R, P> R accept(PFieldTypeVisitor<R, P> visitor, P parameter) {
            return visitor.visitBoolean(parameter);
        }
    };
    
    public static final ParsedFieldType PFTByte = new PBaseType(Byte.TYPE, 1) {
        @Override
        public <R, P> R accept(PFieldTypeVisitor<R, P> visitor, P parameter) {
            return visitor.visitByte(parameter);
        }
    };
    
    public static final ParsedFieldType PFTShort = new PBaseType(Short.TYPE, 2) {
        @Override
        public <R, P> R accept(PFieldTypeVisitor<R, P> visitor, P parameter) {
            return visitor.visitShort(parameter);
        }
    };
    
    public static final ParsedFieldType PFTChar = new PBaseType(Character.TYPE, 2) {
        @Override
        public <R, P> R accept(PFieldTypeVisitor<R, P> visitor, P parameter) {
            return visitor.visitChar(parameter);
        }
    };
    
    public static final ParsedFieldType PFTInt = new PBaseType(Integer.TYPE, 4) {
        @Override
        public <R, P> R accept(PFieldTypeVisitor<R, P> visitor, P parameter) {
            return visitor.visitInt(parameter);
        }
    };
    
    public static final ParsedFieldType PFTLong = new PBaseType(Long.TYPE, 8) {
        @Override
        public <R, P> R accept(PFieldTypeVisitor<R, P> visitor, P parameter) {
            return visitor.visitLong(parameter);
        }
    };
    
    public static final ParsedFieldType PFTFloat = new PBaseType(Float.TYPE, 4) {
        @Override
        public <R, P> R accept(PFieldTypeVisitor<R, P> visitor, P parameter) {
            return visitor.visitFloat(parameter);
        }
    };
    
    public static final ParsedFieldType PFTDouble = new PBaseType(Double.TYPE, 8) {
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
