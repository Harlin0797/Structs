package it.fb.structs.asm;

import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import it.fb.structs.BasicStruct;
import it.fb.structs.ByteBufferStructData;
import it.fb.structs.IStructArrayFactory;
import it.fb.structs.StructArray;
import it.fb.structs.StructData;
import it.fb.structs.StructPointer;
import it.fb.structs.UnsafeStructData;

/**
 *
 * @author Flavio
 */
public class ByteBufferAsmSAFBenchmark extends SimpleBenchmark {
    
    public enum FactoryEnum {
        
        PlainNative(ByteBufferStructData.Plain.Native),
        DirectNative(ByteBufferStructData.Direct.Native),
        Unsafe(UnsafeStructData.Factory);

        private final StructData.Factory<?> factory;
        
        FactoryEnum(StructData.Factory<?> factory) {
            this.factory = factory;
        }

        public StructData.Factory<?> getFactory() {
            return factory;
        }
    }

    @Param
    private FactoryEnum factoryEnum;
    private IStructArrayFactory<?> factory;
    private StructArray<BasicStruct> array;
    private StructPointer<BasicStruct> ptr;

    @Override
    protected void setUp() throws Exception {
        factory = AsmStructArrayFactory.newInstance(factoryEnum.getFactory());
        array = factory.newStructArray(BasicStruct.class, 32);
        ptr = array.at(16);
        ptr.get().setI(33);
    }

    public int timeGetSimpleInt(int reps) {
        int dummy = 0;
        for (int i = 0; i < reps; i++) {
            dummy += ptr.get().getI();
        }
        return dummy;
    }

    public static void main(String[] args) {
        Runner.main(ByteBufferAsmSAFBenchmark.class, args);
    }
}
