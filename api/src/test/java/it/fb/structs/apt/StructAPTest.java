package it.fb.structs.apt;

import it.fb.structs.ArrayStruct;
import it.fb.structs.BasicStruct;
import it.fb.structs.ComplexStruct;
import it.fb.structs.MediumStruct;
import it.fb.structs.SimpleStruct;
import it.fb.structs.StructPointer;
import java.io.IOException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Flavio
 */
public class StructAPTest {

    private static StructLoader loader;

    @BeforeClass
    public static void beforeClass() throws IOException {
        loader = StructLoader.create(SimpleStruct.class, MediumStruct.class, ComplexStruct.class, ArrayStruct.class, BasicStruct.class);
    }

    @Test
    public void testMediumSizes() {
        StructPointer<MediumStruct> ptr = loader.newStructArray(MediumStruct.class, 32);
        assertEquals(32, ptr.length());
        assertEquals(84, ptr.structSize());
        assertEquals(32, ptr.at(0).length());
        assertEquals(84, ptr.at(0).structSize());
        assertEquals(1,  ptr.at(0).get().getSimple().length());
        assertEquals(44, ptr.at(0).get().getSimple().structSize());
    }

    @Test
    public void testComplexSizes() {
        StructPointer<ComplexStruct> ptr = loader.newStructArray(ComplexStruct.class, 32);
        assertEquals(32, ptr.length());
        assertEquals(804, ptr.structSize());
        assertEquals(32, ptr.at(0).length());
        assertEquals(804, ptr.at(0).structSize());
        assertEquals(8, ptr.at(0).get().getMedium(0).length());
        assertEquals(84, ptr.at(0).get().getMedium(0).structSize());
    }

    @Test
    public void testBasicStruct() {
        StructPointer<BasicStruct> ptr = loader.newStructArray(BasicStruct.class, 32);
        assertEquals(32, ptr.length());
        assertEquals(36, ptr.structSize());
        
        for (int i = 0; i < 32; i++) {
            try {
                ptr.duplicate().at(i).get().setI(i + 12);
                ptr.at(i).get().setL(i + 123L);
                ptr.get().setB((byte) (i + 11));
                ptr.get().setS((short) (i + 412));
                ptr.get().setF(3.0f * i);
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException("Error on " + i, ex);
            }
        }
        
        for (int i = 0; i < ptr.length(); i++) {
            assertEquals(i + 12, ptr.at(i).get().getI());
            assertEquals(i + 123L, ptr.at(i).get().getL());
            assertEquals((byte) (i + 11), ptr.get().getB());
            assertEquals((short) (i + 412), ptr.get().getS());
            assertEquals(3.0f * i, ptr.get().getF(), 0.0f);
        }
    }

    @Test
    public void testArrayStruct() {
        StructPointer<ArrayStruct> ptr = loader.newStructArray(ArrayStruct.class, 16);
        assertEquals(16, ptr.length());
        assertEquals(928, ptr.structSize());
        
        for (int i = 0; i < ptr.length(); i++) {
            for (int j = 0; j < 32; j++) {
                try {
                    ptr.at(i).get().setI(j, i + j + 12);
                    ptr.at(i).get().setL(j, i + j + 123L);
                    ptr.get().setB(j, (byte) (i + j + 11));
                    ptr.get().setS(j, (short) (i + j + 412));
                    ptr.get().setF(j, 3.0f * i + j);
                } catch (RuntimeException ex) {
                    throw new IllegalArgumentException("Error on (" + i + "," + j + ")", ex);
                }
            }
        }
        
        for (int i = 0; i < ptr.length(); i++) {
            for (int j = 0; j < 32; j++) {
                try {
                    assertEquals(i + j + 12, ptr.at(i).get().getI(j));
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
        StructPointer<MediumStruct> ptr = loader.newStructArray(MediumStruct.class, 16);
        assertEquals(16, ptr.length());
        assertEquals(84, ptr.structSize());
        
        for (int i = 0; i < ptr.length(); i++) {
            try {
                ptr.at(i).get().setI(i + 19);
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
        
        for (int i = 0; i < ptr.length(); i++) {
            try {
                assertEquals(i + 19, ptr.at(i).get().getI());
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
        StructPointer<ComplexStruct> structArray = loader.newStructArray(ComplexStruct.class, 8);
        assertEquals(8, structArray.length());
        assertEquals(804, structArray.structSize());
        
        for (int i = 0; i < structArray.length(); i++) {
            StructPointer<ComplexStruct> ptr = structArray.duplicate().at(i);
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

        for (int i = 0; i < structArray.length(); i++) {
            StructPointer<ComplexStruct> ptr = structArray.duplicate().at(i);
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
    
    @Test
    public void testDuplicate() {
        StructPointer<SimpleStruct> structArray = loader.newStructArray(SimpleStruct.class, 8);
        StructPointer<SimpleStruct> ptr0 = structArray.duplicate().at(0);
        StructPointer<SimpleStruct> ptr1 = ptr0.duplicate().at(1);
        
        ptr0.get().setI(12);
        ptr1.get().setI(24);
        
        assertEquals(12, structArray.at(0).get().getI());
        assertEquals(24, structArray.at(1).get().getI());
    }
    
}