package it.fb.structs.internal;

/**
 *
 * @author Flavio
 */
public abstract class SFieldType {
    
    public abstract Class<?> getJavaType();
    
    public abstract <T> T accept(SFieldTypeVisitor<T> visitor);
    
    public static interface SFieldTypeVisitor<T> {
        T visitByte();
        T visitChar();
        T visitShort();
        T visitInt();
        T visitLong();
        T visitStruct();
    }
    
    public static SFieldType typeOf(Class<?> javaType) {
        if (javaType == Integer.TYPE) {
            return STypeInt;
        } else if (javaType == Long.TYPE) {
            return STypeLong;
        } else if (javaType == Byte.TYPE) {
            return STypeByte;
        } else {
            return new STypeStruct(Parser.parse(javaType));
        }
    }
    
    static abstract class SBaseType extends SFieldType {
        protected final Class<?> javaType;

        public SBaseType(Class<?> javaType) {
            this.javaType = javaType;
        }

        @Override
        public Class<?> getJavaType() {
            return javaType;
        }
    }
    
    public static final SFieldType STypeByte = new SBaseType(Byte.TYPE) {
        @Override
        public <T> T accept(SFieldTypeVisitor<T> visitor) {
            return visitor.visitByte();
        }
    };
    
    public static final SFieldType STypeChar = new SBaseType(Character.TYPE) {
        @Override
        public <T> T accept(SFieldTypeVisitor<T> visitor) {
            return visitor.visitChar();
        }
    };
    
    public static final SFieldType STypeInt = new SBaseType(Integer.TYPE) {
        @Override
        public <T> T accept(SFieldTypeVisitor<T> visitor) {
            return visitor.visitInt();
        }
    };
    
    public static final SFieldType STypeLong = new SBaseType(Long.TYPE) {
        @Override
        public <T> T accept(SFieldTypeVisitor<T> visitor) {
            return visitor.visitLong();
        }
    };
    
    
    
    public static class STypeStruct extends SBaseType {
        
        private final SStructDesc desc;
        
        public STypeStruct(SStructDesc desc) {
            super(desc.getJavaInterface());
            this.desc = desc;
        }
        
        @Override
        public <T> T accept(SFieldTypeVisitor<T> visitor) {
            return visitor.visitStruct();
        }
    }
    

   
}
