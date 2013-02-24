package it.fb.structs.asm;

import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import it.fb.structs.ByteBufferStructData;
import it.fb.structs.IStructArrayFactory;
import it.fb.structs.SimpleStruct;
import it.fb.structs.StructArray;
import it.fb.structs.StructData;
import it.fb.structs.StructPointer;
import it.fb.structs.UnsafeStructData;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import sun.misc.Unsafe;

/**
 *
 * @author Flavio
 */
public class ByteBufferAsmSAFBenchmark extends SimpleBenchmark {
    
    public enum FactoryEnum {
        
        PlainNative(ByteBufferStructData.Plain.Native),
        DirectNative(ByteBufferStructData.Direct.Native),
        Unsafe(UnsafeStructData.Factory),
        DummyUnsafe(null) {
            @Override
            public StructPointer<SimpleStruct> getPtr() {
                return new UnsafePtr();
            }
        },
        DummyField(null) {
            @Override
            public StructPointer<SimpleStruct> getPtr() {
                return new FieldPtr();
            }
        },
        WrapUnsafe(null) {
            @Override
            public StructPointer<SimpleStruct> getPtr() {
                return new DummyWrapper(UnsafeStructData.Factory.newBuffer(128));
            }
        },
        WrapDirectByteBuffer(null) {
            @Override
            public StructPointer<SimpleStruct> getPtr() {
                return new DummyWrapper(ByteBufferStructData.Direct.Native.newBuffer(128));
            }
        };

        private final StructData.Factory<?> dataFactory;
        
        FactoryEnum(StructData.Factory<?> dataFactory) {
            this.dataFactory = dataFactory;
        }

        public StructPointer<SimpleStruct> getPtr() {
            IStructArrayFactory<?> factory = AsmStructArrayFactory.newInstance(dataFactory,
                    new ClassDumpImpl(name()));
            StructArray<SimpleStruct> array = factory.newStructArray(SimpleStruct.class, 32);
            StructPointer<SimpleStruct> ptr = array.at(16);
            ptr.get().setI(42);
            return ptr;
        }
    }

    @Param
    private FactoryEnum factoryEnum;

    public int timeGetSimpleInt(int reps) {
        StructPointer<SimpleStruct> ptr = factoryEnum.getPtr();
        int dummy = 0;
        for (int i = 0; i < reps; i++) {
            dummy += ptr.get().getI();
            ptr.get().setI(dummy);
        }
        return dummy;
    }

    public static void main(String[] args) {
        Runner.main(ByteBufferAsmSAFBenchmark.class, args);
    }
    
    private static class ClassDumpImpl implements IClassDump {

        private final String prefix;

        public ClassDumpImpl(String prefix) {
            this.prefix = prefix;
        }

        public void dump(Class<?> structInterface, Class<?> structImplementation, byte[] data) {
            OutputStream out = null;
            try {
                File dir = new File(prefix, structImplementation.getPackage().getName().replaceAll("\\.", "/"));
                dir.mkdirs();
                if (dir.list().length > 0) {
                    return;
                }
                File f = new File(dir, structImplementation.getSimpleName() + ".class");
                System.out.println("Dumping to " + f.getAbsolutePath());
                out = new FileOutputStream(f);
                out.write(data);
                out.close();
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
    }
    
    private static final class UnsafePtr implements SimpleStruct, StructPointer<SimpleStruct> {

        private final static Unsafe TheUnsafe;
        
        static {
            try {
                java.lang.reflect.Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                TheUnsafe = (sun.misc.Unsafe) field.get(null);
            } catch (Exception e) {
                throw new ExceptionInInitializerError(e);
            }
        }
        
        private final long addr;

        public UnsafePtr() {
            addr = TheUnsafe.allocateMemory(128);
            setI(42);
        }
        
        public SimpleStruct get() {
            return this;
        }

        public int getI() {
            return TheUnsafe.getInt(addr + 32);
        }

        public void setI(int value) {
            TheUnsafe.putInt(addr + 32, value);
        }
        
        public byte getB() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setB(byte value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public char getC() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setC(char value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public short getS() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setS(short value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public long getL() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setL(long value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public float getF() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setF(float value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public double getD() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setD(double value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public StructPointer<SimpleStruct> at(int index) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public StructArray<SimpleStruct> getOwner() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int length() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int structSize() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public SimpleStruct pin() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public StructPointer<SimpleStruct> duplicate() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int index() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public byte getB(int index) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setB(int index, byte value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
    private static final class FieldPtr implements SimpleStruct, StructPointer<SimpleStruct> {
        
        private int val = 42;
        
        public SimpleStruct get() {
            return this;
        }

        public int getI() {
            return val;
        }

        public void setI(int value) {
            this.val = value;
        }

        public byte getB() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setB(byte value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public char getC() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setC(char value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public short getS() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setS(short value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public long getL() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setL(long value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public float getF() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setF(float value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public double getD() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setD(double value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public StructPointer<SimpleStruct> at(int index) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public StructArray<SimpleStruct> getOwner() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int length() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int structSize() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public SimpleStruct pin() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public StructPointer<SimpleStruct> duplicate() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int index() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public byte getB(int index) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setB(int index, byte value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }

    private static final class DummyWrapper implements SimpleStruct, StructPointer<SimpleStruct> {
        
        private final StructData structData;

        public DummyWrapper(StructData structData) {
            this.structData = structData;
        }

        public SimpleStruct get() {
            return this;
        }

        public int getI() {
            return structData.getInt(32);
        }

        public void setI(int value) {
            structData.putInt(32, value);
        }

        public byte getB() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setB(byte value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public char getC() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setC(char value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public short getS() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setS(short value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public long getL() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setL(long value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public float getF() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setF(float value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public double getD() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setD(double value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public StructPointer<SimpleStruct> at(int index) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public StructArray<SimpleStruct> getOwner() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int length() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int structSize() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public SimpleStruct pin() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public StructPointer<SimpleStruct> duplicate() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int index() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public byte getB(int index) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setB(int index, byte value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
}
