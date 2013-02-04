package it.fb.structs.asm;

import it.fb.structs.ArrayStruct;
import it.fb.structs.BasicStruct;
import it.fb.structs.ByteBufferStructData;
import it.fb.structs.ComplexStruct;
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
    public void testMediumSizes() {
        StructArray<MediumStruct> structArray = factory.newStructArray(MediumStruct.class, 32);
        assertEquals(32, structArray.getLength());
        assertEquals(84, structArray.getStructSize());
        assertEquals(32, structArray.at(0).getLength());
        assertEquals(84, structArray.at(0).getStructSize());
        assertEquals(1,  structArray.at(0).get().getSimple().getLength());
        assertEquals(44, structArray.at(0).get().getSimple().getStructSize());
    }

    @Test
    public void testComplexSizes() {
        StructArray<ComplexStruct> structArray = factory.newStructArray(ComplexStruct.class, 32);
        assertEquals(32, structArray.getLength());
        assertEquals(804, structArray.getStructSize());
        assertEquals(32, structArray.at(0).getLength());
        assertEquals(804, structArray.at(0).getStructSize());
        assertEquals(8, structArray.at(0).get().getMedium(0).getLength());
        assertEquals(84, structArray.at(0).get().getMedium(0).getStructSize());
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
        
        StructPointer<MediumStruct> ptr = structArray.at(0);
        for (int i = 0; i < structArray.getLength(); i++) {
            try {
                structArray.get(i).setI(i + 19);
                ptr.at(i).get().setF(i + 1.5f);
                MediumStruct cur = ptr.get();
                for (int j = 0; j < 32; j++) {
                    cur.setB(j, (byte)(i + j));
                }
                cur.getSimple().get().setI(i + 31);
                cur.getSimple().get().setL(i + 58);
                for (int j = 0; j < 32; j++) {
                    cur.getSimple().get().setB(j, (byte)(i + j + 22));
                }
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException("Error on " + i, ex);
            }
        }
        
        for (int i = 0; i < structArray.getLength(); i++) {
            try {
                assertEquals(i + 19, structArray.get(i).getI());
                assertEquals(i + 1.5f, ptr.at(i).get().getF(), 0.0f);
                MediumStruct cur = ptr.get();
                for (int j = 0; j < 32; j++) {
                    assertEquals((byte)(i + j), cur.getB(j));
                }
                assertEquals(i + 31, cur.getSimple().get().getI());
                assertEquals(i + 58, cur.getSimple().get().getL());
                for (int j = 0; j < 32; j++) {
                    assertEquals((byte)(i + j + 22), cur.getSimple().get().getB(j));
                }
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException("Error on " + i, ex);
            }
        }
    }
    
    @Test
    public void testComplexStruct() {
        StructArray<ComplexStruct> structArray = factory.newStructArray(ComplexStruct.class, 8);
        assertEquals(8, structArray.getLength());
        assertEquals(804, structArray.getStructSize());
        
        for (int i = 0; i < structArray.getLength(); i++) {
            StructPointer<ComplexStruct> ptr = structArray.at(i);
            StructPointer<MediumStruct> medPtr = ptr.get().getMedium(0);
            ptr.get().setI(i);
            for (int j = 0; j < 8; j++) {
                MediumStruct medStr = medPtr.at(j).get();
                medStr.setF(i + j * 63.0f);
                medStr.setI(i + j * 6);
                medStr.getSimple().get().setI(i + j * 8); 
                medStr.getSimple().get().setL(i + j * 49L); 
                for (int k = 0; k < 32; k++) {
                    medStr.setB(k, (byte) (i + j + k));
                    medStr.getSimple().get().setB(k, (byte) (i * 2 + j * 3 + k * 4));
                }
            }
            for (int j = 0; j < 16; j++) {
                ptr.get().setD(j, i + j * 12.3);
            }
        }

        for (int i = 0; i < structArray.getLength(); i++) {
            StructPointer<ComplexStruct> ptr = structArray.at(i);
            StructPointer<MediumStruct> medPtr = ptr.get().getMedium(0);
            assertEquals(i, ptr.get().getI());
            for (int j = 0; j < 8; j++) {
                MediumStruct medStr = medPtr.at(j).get();
                assertEquals(i + j * 63.0f, medPtr.at(j).get().getF(), 0.0f);
                assertEquals(i + j * 6, medStr.getI());
                assertEquals(i + j * 8, medStr.getSimple().get().getI()); 
                assertEquals(i + j * 49L, medStr.getSimple().get().getL()); 
                for (int k = 0; k < 32; k++) {
                    assertEquals((byte) (i + j + k), medStr.getB(k));
                    assertEquals((byte) (i * 2 + j * 3 + k * 4), medStr.getSimple().get().getB(k));
                }
            }
            for (int j = 0; j < 16; j++) {
                assertEquals(i + j * 12.3, ptr.get().getD(j), 0.0);
            }
        }
    }
}
