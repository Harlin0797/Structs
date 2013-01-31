package it.fb.structs.internal;

/**
 *
 * @author Flavio
 */
public abstract class SFieldType {
    
    public abstract Class<?> getJavaType();
    
    public abstract <T> T accept(SFieldTypeVisitor<T> visitor);
    
    public abstract int getSize();
    
    public static interface SFieldTypeVisitor<T> {
        T visitByte();
        T visitChar();
        T visitShort();
        T visitInt();
        T visitLong();
        T visitStruct(SStructDesc structDesc);
        T visitFloat();
        T visitDouble();
    }
    
    public static SFieldType typeOf(Class<?> javaType) {
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
        } else if (javaType.isInterface()) {
            return new STypeStruct(Parser.parse(javaType));
        } else {
            throw new UnsupportedOperationException("Unsupported type: " + javaType);
        }
    }
    
    static abstract class SBaseType extends SFieldType {
        protected final Class<?> javaType;
        protected final int size;

        public SBaseType(Class<?> javaType, int size) {
            this.javaType = javaType;
            this.size = size;
        }

        @Override
        public Class<?> getJavaType() {
            return javaType;
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
            return javaType.getSimpleName();
        }
    }
    
    public static final SFieldType STypeByte = new SBaseType(Byte.TYPE, 1) {
        @Override
        public <T> T accept(SFieldTypeVisitor<T> visitor) {
            return visitor.visitByte();
        }
    };
    
    public static final SFieldType STypeShort = new SBaseType(Short.TYPE, 2) {
        @Override
        public <T> T accept(SFieldTypeVisitor<T> visitor) {
            return visitor.visitShort();
        }
    };
    
    public static final SFieldType STypeChar = new SBaseType(Character.TYPE, 2) {
        @Override
        public <T> T accept(SFieldTypeVisitor<T> visitor) {
            return visitor.visitChar();
        }
    };
    
    public static final SFieldType STypeInt = new SBaseType(Integer.TYPE, 4) {
        @Override
        public <T> T accept(SFieldTypeVisitor<T> visitor) {
            return visitor.visitInt();
        }
    };
    
    public static final SFieldType STypeLong = new SBaseType(Long.TYPE, 8) {
        @Override
        public <T> T accept(SFieldTypeVisitor<T> visitor) {
            return visitor.visitLong();
        }
    };
    
    public static final SFieldType STypeFloat = new SBaseType(Float.TYPE, 4) {
        @Override
        public <T> T accept(SFieldTypeVisitor<T> visitor) {
            return visitor.visitFloat();
        }
    };
    
    public static final SFieldType STypeDouble = new SBaseType(Double.TYPE, 8) {
        @Override
        public <T> T accept(SFieldTypeVisitor<T> visitor) {
            return visitor.visitDouble();
        }
    };
    
    public static class STypeStruct extends SBaseType {
        
        private final SStructDesc desc;
        
        public STypeStruct(SStructDesc desc) {
            super(desc.getJavaInterface(), -1);
            this.desc = desc;
        }
        
        @Override
        public <T> T accept(SFieldTypeVisitor<T> visitor) {
            return visitor.visitStruct(desc);
        }
    }
   
}
