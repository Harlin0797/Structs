package it.fb.structs.asm;

import it.fb.structs.MasterStructPointer;
import it.fb.structs.StructPointer;
import it.fb.structs.core.AbstractOffsetVisitor;
import it.fb.structs.core.AbstractAllocator;
import it.fb.structs.core.PStructDesc;
import it.fb.structs.core.ParsedField;
import it.fb.structs.core.ParsedFieldVisitor;
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
public class AsmAllocator<D extends StructData> extends AbstractAllocator<D> {

    private final IClassDump dump;

    public AsmAllocator(DataStorage<D> dataFactory, IClassDump dump) {
        super(dataFactory);
        this.dump = dump;
    }

    @Override
    protected <T> AbstractStructArrayClassFactory<T> newStructArrayClassFactory(Class<T> structInterface, PStructDesc desc) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
        String internalName = Type.getInternalName(structInterface) + "__Impl__" + System.identityHashCode(this);
        cw.visit(V1_5, 
                ACC_PUBLIC + ACC_FINAL + ACC_SUPER + ACC_SYNTHETIC,
                internalName,
                Type.getDescriptor(Object.class) + Type.getDescriptor(structInterface) + getGenericDescriptor(StructPointer.class, structInterface),
                Type.getInternalName(Object.class),
                new String[] { Type.getInternalName(structInterface), Type.getInternalName(MasterStructPointer.class) });
        Builder<T> builder = new Builder<T>(dataFactory, structInterface, cw, internalName);
        AbstractOffsetVisitor ov = new LocalOffsetVisitor(4);

        for (ParsedField field : desc.getFields()) {
            int fieldOffset = field.accept(ov, null);
            builder.addGetter(field, fieldOffset);
            if (field.hasSetter()) {
                builder.addSetter(field, fieldOffset);
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
        public MasterStructPointer<T> newStructArray(int length) {
            D dataBuffer = dataFactory.newBuffer(length * structSize);
            try {
                return MasterStructPointer.class.cast(constructor.newInstance(dataBuffer, length, 0, 0));
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public MasterStructPointer<T> wrap(D data) {
            throw new UnsupportedOperationException("TODO");
        }

        @Override
        public Class<? extends T> getStructImplementation() {
            return structImplementation;
        }
    }
    
    private class Builder<T> {

        private final DataStorage<D> dataFactory;
        private final Class<T> structInterface;
        private final ClassWriter cw;
        private final String internalName;
        
        private final String bcDescriptor;
        private final String bcInternalName;
        private final int invokeOpcode;
        
        private final List<ChildFieldData> childFields = new ArrayList<ChildFieldData>(); 

        public Builder(DataStorage<D> dataFactory, Class<T> structInterface, ClassWriter cw, String internalName) {
            this.dataFactory = dataFactory;
            this.structInterface = structInterface;
            this.cw = cw;
            this.internalName = internalName;
            
            this.bcDescriptor = Type.getDescriptor(dataFactory.getBufferClass());
            this.bcInternalName = Type.getInternalName(dataFactory.getBufferClass());
            this.invokeOpcode = dataFactory.getBufferClass().isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL;
        }

        public void addGetter(ParsedField field, final int offset) {
            field.accept(new ParsedFieldVisitor<Void, Void>() {
                @Override
                public Void visitBoolean(ParsedField field, Void p) {
                    throw new UnsupportedOperationException("TODO");
                }
                @Override
                public Void visitByte(ParsedField field, Void p) {
                    return primitiveGetter(field, IRETURN, "getByte", "B");
                }

                @Override
                public Void visitChar(ParsedField field, Void p) {
                    return primitiveGetter(field, IRETURN, "getChar", "C");
                }

                @Override
                public Void visitShort(ParsedField field, Void p) {
                    return primitiveGetter(field, IRETURN, "getShort", "S");
                }

                @Override
                public Void visitInt(ParsedField field, Void p) {
                    return primitiveGetter(field, IRETURN, "getInt", "I");
                }

                @Override
                public Void visitLong(ParsedField field, Void p) {
                    return primitiveGetter(field, LRETURN, "getLong", "J");
                }

                @Override
                public Void visitFloat(ParsedField field, Void p) {
                    return primitiveGetter(field, FRETURN, "getFloat", "F");
                }

                @Override
                public Void visitDouble(ParsedField field, Void p) {
                    return primitiveGetter(field, DRETURN, "getDouble", "D");
                }

                private Void primitiveGetter(ParsedField field, int returnOpcode, String method, String typeDescriptor) {
                    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, 
                            "get" + field.getName(),
                            (field.isArray() ? "(I)" : "()") + typeDescriptor,
                            (field.isArray() ? "(I)" : "()") + getGenericDescriptor(StructPointer.class, structInterface),
                            null);
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
                    
                    mv.visitMethodInsn(invokeOpcode, bcInternalName, method, "(I)" + typeDescriptor);
                    mv.visitInsn(returnOpcode);
                    mv.visitMaxs(4, 2);
                    mv.visitEnd();
                    return null;
                }

                @Override
                public Void visitStruct(ParsedField field, Void p) {
                    Class<?> childClass;
                    try {
                        childClass = Class.forName(field.getType().getTypeName());
                    } catch (ClassNotFoundException ex) {
                        throw new IllegalStateException(ex);
                    }
                    AbstractStructArrayClassFactory<?> childFactory = AsmAllocator.this.getClassFactory(
                            childClass);
                    childFields.add(new ChildFieldData(field, offset, childFactory));
                    
                    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, 
                            "get" + field.getName(),
                            (field.isArray() ? "(I)" : "()") + Type.getDescriptor(StructPointer.class),
                            null, null);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, internalName, "_" + field.getName(), Type.getDescriptor(childFactory.getStructImplementation()));
                    if (field.isArray()) {
                        mv.visitVarInsn(ILOAD, 1);
                        mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(childFactory.getStructImplementation()), 
                                "at", "(I)" + Type.getDescriptor(StructPointer.class));                        
                    }
                    mv.visitInsn(ARETURN);
                    mv.visitMaxs(2, 2);
                    mv.visitEnd();
                    return null;
                }
            }, null);
        }

        public void addSetter(ParsedField field, final int offset) {
            field.accept(new ParsedFieldVisitor<Void, Void>() {
                @Override
                public Void visitBoolean(ParsedField field, Void p) {
                    throw new UnsupportedOperationException("TODO");
                }
                @Override
                public Void visitByte(ParsedField field, Void p) {
                    return primitiveSetter(field, ILOAD, "putByte", "B");
                }

                @Override
                public Void visitChar(ParsedField field, Void p) {
                    return primitiveSetter(field, ILOAD, "putChar", "C");
                }

                @Override
                public Void visitShort(ParsedField field, Void p) {
                    return primitiveSetter(field, ILOAD, "putShort", "S");
                }

                @Override
                public Void visitInt(ParsedField field, Void p) {
                    return primitiveSetter(field, ILOAD, "putInt", "I");
                }

                @Override
                public Void visitLong(ParsedField field, Void p) {
                    return primitiveSetter(field, LLOAD, "putLong", "J");
                }

                @Override
                public Void visitFloat(ParsedField field, Void p) {
                    return primitiveSetter(field, FLOAD, "putFloat", "F");
                }

                @Override
                public Void visitDouble(ParsedField field, Void p) {
                    return primitiveSetter(field, DLOAD, "putDouble", "D");
                }

                private Void primitiveSetter(ParsedField field, int loadOpcode, String method, String typeDescriptor) {
                    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, 
                            "set" + field.getName(),
                            (field.isArray() ? "(I" : "(") + typeDescriptor + ")V",
                            null, null);
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
                    mv.visitVarInsn(loadOpcode, field.isArray() ? 2 : 1);
                    mv.visitMethodInsn(invokeOpcode, bcInternalName, method, "(I" + typeDescriptor + ")V");
                    mv.visitInsn(RETURN);
                    mv.visitMaxs(4, 3);
                    mv.visitEnd();
                    return null;
                }

                @Override
                public Void visitStruct(ParsedField field, Void p) {
                    throw new UnsupportedOperationException("Struct setters are not supported (" + field.getName() + ")");
                }
            }, null);
        }

        public AsmStructArrayClassFactory<T> build(int structSize) {
            {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", 
                        "(" + bcDescriptor + "III)V",
                        "(" + bcDescriptor + "III)V", null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
                for (ChildFieldData childField : childFields) {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitTypeInsn(NEW, Type.getInternalName(childField.childClassFactory.getStructImplementation()));
                    mv.visitInsn(DUP);
                    mv.visitVarInsn(ALOAD, 1);
                    visitInsnConst(mv, childField.field.getArrayLength() <= 0 ? 1 : childField.field.getArrayLength());
                    visitInsnConst(mv, childField.offset);
                    mv.visitInsn(ICONST_0);
                    mv.visitMethodInsn(INVOKESPECIAL, 
                            Type.getInternalName(childField.childClassFactory.getStructImplementation()), 
                            "<init>",
                            "(" + bcDescriptor + "III)V");
                    mv.visitFieldInsn(PUTFIELD, internalName, "_" + childField.field.getName(), Type.getDescriptor(childField.childClassFactory.getStructImplementation()));
                }
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitFieldInsn(PUTFIELD, internalName, "data", bcDescriptor);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ILOAD, 2);
                mv.visitFieldInsn(PUTFIELD, internalName, "length", "I");
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
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "length", "()I", null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, internalName, "length", "I");
                mv.visitInsn(IRETURN);
                mv.visitMaxs(1, 1);
                mv.visitEnd();
            }
            {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "structSize", "()I", null, null);
                mv.visitCode();
                visitInsnConst(mv, structSize);
                mv.visitInsn(IRETURN);
                mv.visitMaxs(1, 1);
                mv.visitEnd();
            }
            {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "index", "()I", null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, internalName, "position", "I");
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, internalName, "baseOffset", "I");
                visitInsnConst(mv, structSize);
                mv.visitInsn(IDIV);
                mv.visitInsn(ISUB);
                mv.visitInsn(IRETURN);
                mv.visitMaxs(3, 1);
                mv.visitEnd();
            }
            {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "duplicate", 
                        "()" + Type.getObjectType(internalName).getDescriptor(),
                        null, null);
                mv.visitCode();
                mv.visitTypeInsn(NEW, internalName);
                mv.visitInsn(DUP);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, internalName, "data", bcDescriptor);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, internalName, "length", "I");
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, internalName, "baseOffset", "I");
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKEVIRTUAL, internalName, "index", "()I");
                mv.visitMethodInsn(INVOKESPECIAL, internalName, "<init>", 
                        "(" + bcDescriptor + "III)V");
                mv.visitInsn(ARETURN);
                mv.visitMaxs(7, 1);
                mv.visitEnd();
            }
            {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "pin", 
                        "()" + Type.getObjectType(internalName).getDescriptor(),
                        null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKEVIRTUAL, internalName, "duplicate", 
                        "()" + Type.getObjectType(internalName).getDescriptor());
                mv.visitInsn(ARETURN);
                mv.visitMaxs(1, 1);
                mv.visitEnd();
            }
            {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "release", 
                        "()V",
                        null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, internalName, "data", bcDescriptor);
                mv.visitMethodInsn(INVOKEVIRTUAL, bcInternalName, "release", "()V");
                mv.visitInsn(RETURN);
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
            {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, 
                        "duplicate", "()" + Type.getDescriptor(StructPointer.class), null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKEVIRTUAL, internalName, "duplicate", 
                        "()" + Type.getObjectType(internalName).getDescriptor());
                mv.visitInsn(ARETURN);
                mv.visitMaxs(1, 1);
                mv.visitEnd();
            }
            {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, 
                        "pin", "()" + Type.getDescriptor(Object.class), null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKEVIRTUAL, internalName, "pin", 
                        "()" + Type.getObjectType(internalName).getDescriptor());
                mv.visitInsn(ARETURN);
                mv.visitMaxs(1, 1);
                mv.visitEnd();
            }

            cw.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "SIZE", "I", null, Integer.valueOf(structSize)).visitEnd();
            cw.visitField(ACC_PRIVATE + ACC_FINAL, "data", bcDescriptor, null, null).visitEnd();
            cw.visitField(ACC_PRIVATE + ACC_FINAL, "length", "I", null, null).visitEnd();
            cw.visitField(ACC_PRIVATE, "baseOffset", "I", null, null).visitEnd();
            cw.visitField(ACC_PRIVATE, "position", "I", null, null).visitEnd();
            
            for (ChildFieldData childField : childFields) {
                cw.visitField(ACC_PRIVATE + ACC_FINAL, 
                        "_" + childField.field.getName(),
                        Type.getDescriptor(childField.childClassFactory.getStructImplementation()),
                        null, null).visitEnd();
            }
            cw.visitEnd();
            byte[] classData = cw.toByteArray();

            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            Class<? extends T> implementation = (Class<? extends T>) loadInto(ccl, 
                    Type.getObjectType(internalName).getClassName(), classData);
            Constructor<?> constructor;
            try {
                constructor = implementation.getDeclaredConstructor(
                        dataFactory.getBufferClass(), Integer.TYPE, Integer.TYPE, Integer.TYPE);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }

            if (dump != null) {
                dump.dump(structInterface, implementation, classData);
            }

            return new AsmStructArrayClassFactory<T>(structInterface, implementation, constructor, structSize);
        }
    }

    public static <D extends StructData> Allocator<D> newInstance(DataStorage<D> factory) {
        return new AsmAllocator<D>(factory, null);
    }

    public static <D extends StructData> Allocator<D> newInstance(DataStorage<D> factory,
            IClassDump dump) {
        return new AsmAllocator<D>(factory, dump);
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
                    throw new UnsupportedOperationException("Unsupported const: " + offset);
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

        private final ParsedField field;
        private final int offset;
        private final AbstractStructArrayClassFactory<?> childClassFactory;

        public ChildFieldData(ParsedField field, int offset, AbstractStructArrayClassFactory<?> childClassFactory) {
            this.field = field;
            this.offset = offset;
            this.childClassFactory = childClassFactory;
        }
    }
}
