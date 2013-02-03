package it.fb.structs.asm;

import it.fb.structs.StructArray;
import it.fb.structs.StructData;
import it.fb.structs.StructPointer;
import it.fb.structs.internal.IStructArrayFactory;
import it.fb.structs.internal.SField;
import it.fb.structs.internal.SField.SFieldVisitor;
import it.fb.structs.internal.SStructDesc;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.Type;

/**
 *
 * @author Flavio
 */
public class AsmStructArrayFactory<T, D extends StructData> implements IStructArrayFactory<T, D> {
    
    private final StructData.Factory<D> dataFactory;
    private final Class<T> structInterface;
    private final Class<? extends T> structImplementation;
    private final Constructor<?> constructor;
    private final int structSize;

    public AsmStructArrayFactory(StructData.Factory<D> dataFactory, Class<T> structInterface, Class<? extends T> structImplementation, Constructor<?> constructor, int structSize) {
        this.dataFactory = dataFactory;
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
        @SuppressWarnings("UseSpecificCatch")
        public T get(int index) {
            try {
                return structInterface.cast(constructor.newInstance(data, this, index));
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        @SuppressWarnings("UseSpecificCatch")
        public StructPointer<T> at(int index) {
            try {
                return StructPointer.class.cast(constructor.newInstance(data, this, index));
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        
        @Override
        public void release() {
            throw new UnsupportedOperationException("TODO");
        }        
    }
    
    public static class Builder<T, D extends StructData> implements IStructArrayFactory.Builder<T, D> {

        private final StructData.Factory<D> dataFactory;        
        private final Class<T> structInterface;
        private final ClassWriter cw;
        private final String internalName;
        
        private final String bcDescriptor;
        private final String bcInternalName;
        private final int invokeOpcode;

        public Builder(StructData.Factory<D> dataFactory, Class<T> structInterface, ClassWriter cw, String internalName) {
            this.dataFactory = dataFactory;
            this.structInterface = structInterface;
            this.cw = cw;
            this.internalName = internalName;
            
            this.bcDescriptor = Type.getDescriptor(dataFactory.getBufferClass());
            this.bcInternalName = Type.getInternalName(dataFactory.getBufferClass());
            this.invokeOpcode = dataFactory.getBufferClass().isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL;
        }

        @Override
        public void addGetter(Method getter, SField field, final int offset) {
            final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, getter.getName(), Type.getMethodDescriptor(getter), null, null);
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
                    return primitiveGetter(IRETURN, "getByte", "B");
                }

                @Override
                public Void visitChar(SField field) {
                    return primitiveGetter(IRETURN, "getChar", "C");
                }

                @Override
                public Void visitShort(SField field) {
                    return primitiveGetter(IRETURN, "getShort", "S");
                }

                @Override
                public Void visitInt(SField field) {
                    return primitiveGetter(IRETURN, "getInt", "I");
                }

                @Override
                public Void visitLong(SField field) {
                    return primitiveGetter(LRETURN, "getLong", "J");
                }

                @Override
                public Void visitFloat(SField field) {
                    return primitiveGetter(FRETURN, "getFloat", "F");
                }

                @Override
                public Void visitDouble(SField field) {
                    return primitiveGetter(DRETURN, "getDouble", "D");
                }

                private Void primitiveGetter(int returnOpcode, String method, String typeDescriptor) {
                    mv.visitMethodInsn(invokeOpcode, bcInternalName, method, "(I)" + typeDescriptor);
                    mv.visitInsn(returnOpcode);
                    return null;
                }

                @Override
                public Void visitStruct(SField field, SStructDesc structDesc) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            });
            
            mv.visitMaxs(4, 2);
            mv.visitEnd();
        }

        @Override
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
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            });
            
            mv.visitInsn(RETURN);
            mv.visitMaxs(4, 3);
            mv.visitEnd();
        }

        @Override
        public IStructArrayFactory<T, D> build(int structSize) {
            {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", 
                        "(" + bcDescriptor + Type.getDescriptor(StructArray.class) + "I)V",
                        "(" + bcDescriptor + getGenericDescriptor(StructArray.class, structInterface) + "I)V", null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitFieldInsn(PUTFIELD, internalName, "data", bcDescriptor);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 2);
                mv.visitFieldInsn(PUTFIELD, internalName, "owner", Type.getDescriptor(StructArray.class));
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ILOAD, 3);
                visitInsnConst(mv, structSize);
                mv.visitInsn(IMUL);
                mv.visitFieldInsn(PUTFIELD, internalName, "position", "I");
                mv.visitInsn(RETURN);
                mv.visitMaxs(3, 4);
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
                mv.visitVarInsn(ILOAD, 1);
                visitInsnConst(mv, structSize);
                mv.visitInsn(IMUL);
                mv.visitFieldInsn(PUTFIELD, internalName, "position", "I");
                mv.visitVarInsn(ALOAD, 0);
                mv.visitInsn(ARETURN);
                mv.visitMaxs(3, 2);
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
            cw.visitField(ACC_PRIVATE, "position", "I", null, null).visitEnd();
            cw.visitEnd();
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            Class<? extends T> implementation = (Class<? extends T>) loadInto(ccl, cw.toByteArray());
            Constructor<?> constructor;
            try {
                constructor = implementation.getDeclaredConstructor(dataFactory.getBufferClass(), StructArray.class, Integer.TYPE);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
            return new AsmStructArrayFactory<>(dataFactory, structInterface, implementation, constructor, structSize);
        }
    }
    
    public static IStructArrayFactory.Builder.Factory Factory = new IStructArrayFactory.Builder.Factory() {
        @Override
        public <T, D extends StructData> Builder<T, D> newBuilder(StructData.Factory<D> dataFactory, Class<T> structInterface) {
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
            String name = Type.getInternalName(structInterface) + "$$Impl$$" + System.identityHashCode(cw);
            cw.visit(V1_5, 
                    ACC_PUBLIC + ACC_SUPER,
                    name,
                    Type.getDescriptor(Object.class) + Type.getDescriptor(structInterface) + getGenericDescriptor(StructPointer.class, structInterface),
                    Type.getInternalName(Object.class),
                    new String[] { Type.getInternalName(structInterface), Type.getInternalName(StructPointer.class) });
            return new Builder<>(dataFactory, structInterface, cw, name);
        }
    };
    
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

    @SuppressWarnings("UseSpecificCatch")
    private static Class<?> loadInto(ClassLoader loader, byte[] classData) {
        try {
            Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, Integer.TYPE, Integer.TYPE);
            defineClass.setAccessible(true);
            return Class.class.cast(defineClass.invoke(loader, null, classData, 0, classData.length));
        } catch (Exception ex) {
            throw new IllegalStateException("Error forcing load of class to " + loader, ex);
        }
    }
}
