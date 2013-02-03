package it.fb.structs.asm;

import it.fb.structs.ArrayStruct;
import it.fb.structs.BasicStruct;
import it.fb.structs.ByteBufferStructData;
import it.fb.structs.IStructArrayFactory;
import it.fb.structs.MediumStruct;
import it.fb.structs.StructArray;
import it.fb.structs.StructPointer;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Flavio
 */
public class ByteBufferAsmSAFTest {
    
    private IStructArrayFactory<?> factory;
    
    @Before
    public void setUp() {
        factory = AsmStructArrayFactory.newInstance(ByteBufferStructData.Plain.Native);
    }
    
    @Test
    public void testBasicStruct() {
        StructArray<BasicStruct> structArray = factory.newStructArray(BasicStruct.class, 32);
        assertEquals(32, structArray.getLength());
        assertEquals(36, structArray.getStructSize());
        
        StructPointer<BasicStruct> ptr = structArray.at(0);
        for (int i = 0; i < 32; i++) {
            try {
                structArray.get(i).setI(i + 12);
                ptr.at(i).get().setL(i + 123L);
                ptr.get().setB((byte) (i + 11));
                ptr.get().setS((short) (i + 412));
                ptr.get().setF(3.0f * i);
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException("Error on " + i, ex);
            }
        }
        
        for (int i = 0; i < structArray.getLength(); i++) {
            assertEquals(i + 12, structArray.get(i).getI());
            assertEquals(i + 123L, ptr.at(i).get().getL());
            assertEquals((byte) (i + 11), ptr.get().getB());
            assertEquals((short) (i + 412), ptr.get().getS());
            assertEquals(3.0f * i, ptr.get().getF(), 0.0f);
        }
    }

    @Test
    public void testArrayStruct() {
        StructArray<ArrayStruct> structArray = factory.newStructArray(ArrayStruct.class, 16);
        assertEquals(16, structArray.getLength());
        assertEquals(928, structArray.getStructSize());
        
        StructPointer<ArrayStruct> ptr = structArray.at(0);
        for (int i = 0; i < structArray.getLength(); i++) {
            for (int j = 0; j < 32; j++) {
                try {
                    structArray.get(i).setI(j, i + j + 12);
                    ptr.at(i).get().setL(j, i + j + 123L);
                    ptr.get().setB(j, (byte) (i + j + 11));
                    ptr.get().setS(j, (short) (i + j + 412));
                    ptr.get().setF(j, 3.0f * i + j);
                } catch (RuntimeException ex) {
                    throw new IllegalArgumentException("Error on (" + i + "," + j + ")", ex);
                }
            }
        }
        
        for (int i = 0; i < structArray.getLength(); i++) {
            for (int j = 0; j < 32; j++) {
                try {
                    assertEquals(i + j + 12, structArray.get(i).getI(j));
                    assertEquals(i + j + 123L, ptr.at(i).get().getL(j));
                    assertEquals((byte) (i + j + 11), ptr.get().getB(j));
                    assertEquals((short) (i + j + 412), ptr.get().getS(j));
                    assertEquals(3.0f * i + j, ptr.get().getF(j), 0.0f);
                } catch (RuntimeException ex) {
                    throw new IllegalArgumentException("Error on (" + i + "," + j + ")", ex);
                }
            }
        }
    }
    
    @Test
    public void testMediumStruct() {
        StructArray<MediumStruct> structArray = factory.newStructArray(MediumStruct.class, 16);
        assertEquals(16, structArray.getLength());
        assertEquals(84, structArray.getStructSize());
    }
}
