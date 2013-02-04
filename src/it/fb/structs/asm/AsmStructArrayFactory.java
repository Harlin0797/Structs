package it.fb.structs.asm;

import it.fb.structs.IStructArrayFactory;
import it.fb.structs.StructArray;
import it.fb.structs.StructData;
import it.fb.structs.StructPointer;
import it.fb.structs.bytebuffer.OffsetVisitor;
import it.fb.structs.impl.AbstractStructArrayFactory;
import it.fb.structs.internal.Parser;
import it.fb.structs.internal.SField;
import it.fb.structs.internal.SField.SFieldVisitor;
import it.fb.structs.internal.SStructDesc;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.Type;

/**
 *
 * @author Flavio
 */
public class AsmStructArrayFactory<D extends StructData> extends AbstractStructArrayFactory<D> {
    
    public AsmStructArrayFactory(StructData.Factory<D> dataFactory) {
        super(dataFactory);
    }

    @Override
    protected <T> AbstractStructArrayClassFactory<T> newStructArrayClassFactory(Class<T> structInterface) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
        String internalName = Type.getInternalName(structInterface) + "$$Impl$$" + System.identityHashCode(this);
        cw.visit(V1_5, 
                ACC_PUBLIC + ACC_FINAL + ACC_SUPER + ACC_SYNTHETIC,
                internalName,
                Type.getDescriptor(Object.class) + Type.getDescriptor(structInterface) + getGenericDescriptor(StructPointer.class, structInterface),
                Type.getInternalName(Object.class),
                new String[] { Type.getInternalName(structInterface), Type.getInternalName(StructPointer.class) });
        Builder<T> builder = new Builder<T>(dataFactory, structInterface, cw, internalName);
        SStructDesc desc = Parser.parse(structInterface);
        OffsetVisitor ov = new OffsetVisitor(4);
        
        for (SField field : desc.getFields()) {
            int fieldOffset = field.accept(ov);
            builder.addGetter(field.getGetter(), field, fieldOffset);
            if (field.getSetter() != null) {
                builder.addSetter(field.getSetter(), field, fieldOffset);
            }
        }
        
        return builder.build(ov.getSize());
    }

    private class AsmStructArrayClassFactory<T> extends AbstractStructArrayClassFactory<T> {

        private final Class<T> structInterface;
        private final Class<? extends T> structImplementation;
        private final Constructor<?> constructor;
        private final int structSize;

        public AsmStructArrayClassFactory(Class<T> structInterface, Class<? extends T> structImplementation, Constructor<?> constructor, int structSize) {
            this.structInterface = structInterface;
            this.structImplementation = structImplementation;
            this.constructor = constructor;
            this.structSize = structSize;
        }

        @Override
        public StructArray<T> newStructArray(int length) {
            D dataBuffer = dataFactory.newBuffer(length * structSize);
            return new StructArrayImpl(dataBuffer, length);
        }

        @Override
        public StructArray<T> wrap(D data) {
            throw new UnsupportedOperationException("TODO");
        }

        @Override
        public Class<? extends T> getStructImplementation() {
            return structImplementation;
        }

        private class StructArrayImpl implements StructArray<T> {

            private final D data;
            private final int length;

            public StructArrayImpl(D data, int length) {
                this.data = data;
                this.length = length;
            }

            @Override
            public int getLength() {
                return length;
            }

            @Override
            public int getStructSize() {
                return structSize;
            }

            @Override
            public T get(int index) {
                try {
                    return structInterface.cast(constructor.newInstance(data, this, 0, index));
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
            }

            @Override
            public StructPointer<T> at(int index) {
                try {
                    return StructPointer.class.cast(constructor.newInstance(data, this, 0, index));
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
            }

            @Override
            public void release() {
                throw new UnsupportedOperationException("TODO");
            }        
        }
    }
    
    private class Builder<T> {

        private final StructData.Factory<D> dataFactory;        
        private final Class<T> structInterface;
        private final ClassWriter cw;
        private final String internalName;
        
        private final String bcDescriptor;
        private final String bcInternalName;
        private final int invokeOpcode;
        
        private final List<ChildFieldData> childFields = new ArrayList<ChildFieldData>(); 

        public Builder(StructData.Factory<D> dataFactory, Class<T> structInterface, ClassWriter cw, String internalName) {
            this.dataFactory = dataFactory;
            this.structInterface = structInterface;
            this.cw = cw;
            this.internalName = internalName;
            
            this.bcDescriptor = Type.getDescriptor(dataFactory.getBufferClass());
            this.bcInternalName = Type.getInternalName(dataFactory.getBufferClass());
            this.invokeOpcode = dataFactory.getBufferClass().isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL;
        }

        public void addGetter(Method getter, SField field, final int offset) {
            final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, getter.getName(), Type.getMethodDescriptor(getter), null, null);
            mv.visitCode();

            field.accept(new SFieldVisitor<Void>() {
                @Override
                public Void visitByte(SField field) {
                    return primitiveGetter(field, IRETURN, "getByte", "B");
                }

                @Override
                public Void visitChar(SField field) {
                    return primitiveGetter(field, IRETURN, "getChar", "C");
                }

                @Override
                public Void visitShort(SField field) {
                    return primitiveGetter(field, IRETURN, "getShort", "S");
                }

                @Override
                public Void visitInt(SField field) {
                    return primitiveGetter(field, IRETURN, "getInt", "I");
                }

                @Override
                public Void visitLong(SField field) {
                    return primitiveGetter(field, LRETURN, "getLong", "J");
                }

                @Override
                public Void visitFloat(SField field) {
                    return primitiveGetter(field, FRETURN, "getFloat", "F");
                }

                @Override
                public Void visitDouble(SField field) {
                    return primitiveGetter(field, DRETURN, "getDouble", "D");
                }

                private Void primitiveGetter(SField field, int returnOpcode, String method, String typeDescriptor) {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, internalName, "data", bcDescriptor);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, internalName, "position", "I");
                    if (offset != 0) {
                        visitInsnConst(mv, offset);
                        mv.visitInsn(IADD);
                    }

                    if (field.isArray()) {
                        mv.visitVarInsn(ILOAD, 1);
                        if (field.getType().getSize() != 1) {
                            visitInsnConst(mv, field.getType().getSize());
                            mv.visitInsn(IMUL);
                        }
                        mv.visitInsn(IADD);
                    }
                    
                    mv.visitMethodInsn(invokeOpcode, bcInternalName, method, "(I)" + typeDescriptor);
                    mv.visitInsn(returnOpcode);
                    mv.visitMaxs(4, 2);
                    return null;
                }

                @Override
                public Void visitStruct(SField field, SStructDesc structDesc) {
                    AbstractStructArrayClassFactory<?> childFactory = AsmStructArrayFactory.this.getClassFactory(structDesc.getJavaInterface());
                    childFields.add(new ChildFieldData(field, offset, childFactory));
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, internalName, "_" + field.getName(), Type.getDescriptor(childFactory.getStructImplementation()));
                    if (field.isArray()) {
                        mv.visitVarInsn(ILOAD, 1);
                        mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(childFactory.getStructImplementation()), 
                                "at", "(I)" + Type.getDescriptor(StructPointer.class));                        
                    }
                    mv.visitInsn(ARETURN);
                    mv.visitMaxs(2, 2);
                    return null;
                }
            });
            
            mv.visitEnd();
        }

        public void addSetter(Method setter, SField field, final int offset) {
            final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, setter.getName(), Type.getMethodDescriptor(setter), null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, internalName, "data", bcDescriptor);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, internalName, "position", "I");
            if (offset != 0) {
                visitInsnConst(mv, offset);
                mv.visitInsn(IADD);
            }
            
            if (field.isArray()) {
                mv.visitVarInsn(ILOAD, 1);
                if (field.getType().getSize() != 1) {
                    visitInsnConst(mv, field.getType().getSize());
                    mv.visitInsn(IMUL);
                }
                mv.visitInsn(IADD);
            }

            field.accept(new SFieldVisitor<Void>() {
                @Override
                public Void visitByte(SField field) {
                    return primitiveSetter(field, ILOAD, "putByte", "B");
                }

                @Override
                public Void visitChar(SField field) {
                    return primitiveSetter(field, ILOAD, "putChar", "C");
                }

                @Override
                public Void visitShort(SField field) {
                    return primitiveSetter(field, ILOAD, "putShort", "S");
                }

                @Override
                public Void visitInt(SField field) {
                    return primitiveSetter(field, ILOAD, "putInt", "I");
                }

                @Override
                public Void visitLong(SField field) {
                    return primitiveSetter(field, LLOAD, "putLong", "J");
                }

                @Override
                public Void visitFloat(SField field) {
                    return primitiveSetter(field, FLOAD, "putFloat", "F");
                }

                @Override
                public Void visitDouble(SField field) {
                    return primitiveSetter(field, DLOAD, "putDouble", "D");
                }

                private Void primitiveSetter(SField field, int loadOpcode, String method, String typeDescriptor) {
                    mv.visitVarInsn(loadOpcode, field.isArray() ? 2 : 1);
                    mv.visitMethodInsn(invokeOpcode, bcInternalName, method, "(I" + typeDescriptor + ")V");
                    return null;
                }

                @Override
                public Void visitStruct(SField field, SStructDesc structDesc) {
                    throw new UnsupportedOperationException("Struct setters are not supported (" + field.getName() + ")");
                }
            });
            
            mv.visitInsn(RETURN);
            mv.visitMaxs(4, 3);
            mv.visitEnd();
        }

        public AsmStructArrayClassFactory<T> build(int structSize) {
            {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", 
                        "(" + bcDescriptor + Type.getDescriptor(StructArray.class) + "II)V",
                        "(" + bcDescriptor + getGenericDescriptor(StructArray.class, structInterface) + "II)V", null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
                for (ChildFieldData childField : childFields) {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitTypeInsn(NEW, Type.getInternalName(childField.childClassFactory.getStructImplementation()));
                    mv.visitInsn(DUP);
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitInsn(ACONST_NULL);
                    visitInsnConst(mv, childField.offset);
                    mv.visitInsn(ICONST_0);
                    mv.visitMethodInsn(INVOKESPECIAL, 
                            Type.getInternalName(childField.childClassFactory.getStructImplementation()), 
                            "<init>",
                            "(" + bcDescriptor + Type.getDescriptor(StructArray.class) + "II)V");
                    mv.visitFieldInsn(PUTFIELD, internalName, "_" + childField.field.getName(), Type.getDescriptor(childField.childClassFactory.getStructImplementation()));
                }
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitFieldInsn(PUTFIELD, internalName, "data", bcDescriptor);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 2);
                mv.visitFieldInsn(PUTFIELD, internalName, "owner", Type.getDescriptor(StructArray.class));
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ILOAD, 3);
                mv.visitFieldInsn(PUTFIELD, internalName, "baseOffset", "I");
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ILOAD, 4);
                mv.visitMethodInsn(INVOKEVIRTUAL, internalName, "at", "(I)" + Type.getDescriptor(StructPointer.class));
                mv.visitInsn(POP);
                mv.visitInsn(RETURN);
                mv.visitMaxs(2, 5);
                mv.visitEnd();
            }
            {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "get", "()" + Type.getDescriptor(structInterface), null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitInsn(ARETURN);
                mv.visitMaxs(1, 1);
                mv.visitEnd();
            }
            {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "at",
                        "(I)" + Type.getDescriptor(StructPointer.class),
                        "(I)" + getGenericDescriptor(StructPointer.class, structInterface), null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, internalName, "baseOffset", "I");
                mv.visitVarInsn(ILOAD, 1);
                visitInsnConst(mv, structSize);
                mv.visitInsn(IMUL);
                mv.visitInsn(IADD);
                if (childFields.isEmpty()) {
                    mv.visitFieldInsn(PUTFIELD, internalName, "position", "I");
                } else {
                    mv.visitInsn(DUP_X1);
                    mv.visitFieldInsn(PUTFIELD, internalName, "position", "I");
                    mv.visitVarInsn(ISTORE, 2);
                    for (ChildFieldData childField : childFields) {
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, internalName, "_" + childField.field.getName(),
                                Type.getDescriptor(childField.childClassFactory.getStructImplementation()));
                        mv.visitVarInsn(ILOAD, 2);
                        visitInsnConst(mv, childField.offset);
                        mv.visitInsn(IADD);
                        mv.visitMethodInsn(INVOKEVIRTUAL, 
                                Type.getInternalName(childField.childClassFactory.getStructImplementation()), 
                                "setBaseOffset", "(I)V");
                    }
                }
                mv.visitVarInsn(ALOAD, 0);
                mv.visitInsn(ARETURN);
                mv.visitMaxs(4, 2);
                mv.visitEnd();
            }
            {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "setBaseOffset", "(I)V", null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ILOAD, 1);
                mv.visitFieldInsn(PUTFIELD, internalName, "baseOffset", "I");
                mv.visitVarInsn(ALOAD, 0);
                mv.visitInsn(ICONST_0);
                mv.visitMethodInsn(INVOKEVIRTUAL, internalName, "at", "(I)" + Type.getDescriptor(StructPointer.class));
                mv.visitInsn(POP);
                mv.visitInsn(RETURN);
                mv.visitMaxs(2, 2);
                mv.visitEnd();
            }
            {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getOwner",
                        "(I)" + Type.getDescriptor(StructArray.class),
                        "(I)" + getGenericDescriptor(StructArray.class, structInterface), null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, internalName, "owner", Type.getDescriptor(StructArray.class));
                mv.visitInsn(ARETURN);
                mv.visitMaxs(1, 1);
                mv.visitEnd();
            }
            {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "get", "()Ljava/lang/Object;", null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKEVIRTUAL, internalName, "get", "()" + Type.getDescriptor(structInterface));
                mv.visitInsn(ARETURN);
                mv.visitMaxs(1, 1);
                mv.visitEnd();
            }

            cw.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "SIZE", "I", null, Integer.valueOf(structSize)).visitEnd();
            cw.visitField(ACC_PRIVATE + ACC_FINAL, "data", bcDescriptor, null, null).visitEnd();
            cw.visitField(ACC_PRIVATE + ACC_FINAL, "owner", Type.getDescriptor(StructArray.class), getGenericDescriptor(StructArray.class, structInterface), null).visitEnd();
            cw.visitField(ACC_PRIVATE, "baseOffset", "I", null, null).visitEnd();
            cw.visitField(ACC_PRIVATE, "position", "I", null, null).visitEnd();
            
            for (ChildFieldData childField : childFields) {
                cw.visitField(ACC_PRIVATE + ACC_FINAL, 
                        "_" + childField.field.getName(),
                        Type.getDescriptor(childField.childClassFactory.getStructImplementation()),
                        null, null).visitEnd();
            }
            cw.visitEnd();
            
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            Class<? extends T> implementation = (Class<? extends T>) loadInto(ccl, 
                    Type.getObjectType(internalName).getClassName(),
                    cw.toByteArray());
            Constructor<?> constructor;
            try {
                constructor = implementation.getDeclaredConstructor(
                        dataFactory.getBufferClass(), StructArray.class, Integer.TYPE, Integer.TYPE);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
            return new AsmStructArrayClassFactory<T>(structInterface, implementation, constructor, structSize);
        }
    }

    public static <D extends StructData> IStructArrayFactory<D> newInstance(StructData.Factory<D> factory) {
        return new AsmStructArrayFactory<D>(factory);
    }

    private static String getGenericDescriptor(Class<?> outerClass, Class<?> innerClass) {
        String outerDesc = Type.getDescriptor(outerClass);
        String innerDesc = Type.getDescriptor(innerClass);
        
        return outerDesc.substring(0, outerDesc.length() - 1) + "<" + innerDesc + ">;";
    }
    
    private static final int[] CONSTS = new int[] { ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5 };
    private static void visitInsnConst(MethodVisitor mv, int offset) {
        switch (offset) {
            case 0:
                throw new IllegalArgumentException("Invalid const: " + offset);
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                mv.visitInsn(CONSTS[offset]);
                break;
            default:
                if ((byte)offset == offset) {
                    mv.visitIntInsn(BIPUSH, offset);
                } else if ((short)offset == offset) {
                    mv.visitIntInsn(SIPUSH, offset);
                } else {
                    throw new IllegalArgumentException("Invalid const: " + offset);
                }
                break;
        }
    }

    private static Class<?> loadInto(ClassLoader loader, String binaryName, byte[] classData) {
        try {
            Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, Integer.TYPE, Integer.TYPE);
            defineClass.setAccessible(true);
            return Class.class.cast(defineClass.invoke(loader, binaryName, classData, 0, classData.length));
        } catch (Exception ex) {
            throw new IllegalStateException("Error forcing load of class " + binaryName + " to " + loader, ex);
        }
    }
    
    private final class ChildFieldData {

        private final SField field;
        private final int offset;
        private final AbstractStructArrayClassFactory<?> childClassFactory;

        public ChildFieldData(SField field, int offset, AbstractStructArrayClassFactory<?> childClassFactory) {
            this.field = field;
            this.offset = offset;
            this.childClassFactory = childClassFactory;
        }
    }
}
