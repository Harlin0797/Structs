package it.fb.structs.asm;

import it.fb.structs.BasicStruct;
import it.fb.structs.StructArray;
import it.fb.structs.StructArrayRepository;
import it.fb.structs.StructPointer;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Flavio
 */
public class ByteBufferAsmSAFTest {
    
    @Test
    public void testNewStructArray() {
        StructArrayRepository sar = new StructArrayRepository();
        StructArray<BasicStruct> structArray = sar.newStructArray(ByteBufferAsmSAF.Factory, BasicStruct.class, 32);
        assertEquals(32, structArray.getLength());
        assertEquals(36, structArray.getStructSize());
        
        StructPointer<BasicStruct> ptr = structArray.at(0);
        for (int i = 0; i < 32; i++) {
            structArray.get(i).setI(i + 12);
            ptr.at(i).get().setL(i + 123L);
            ptr.get().setF(3.0f * i);
        }
        
        for (int i = 0; i < 32; i++) {
            assertEquals(i + 12, structArray.get(i).getI());
            assertEquals(i + 123L, ptr.at(i).get().getL());
            assertEquals(3.0f * i, ptr.get().getF(), 0.0f);
        }
    }
}
