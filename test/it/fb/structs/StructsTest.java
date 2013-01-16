package it.fb.structs;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Flavio
 */
public class StructsTest {
    
    @Test
    public void arrayStructShouldBeInstantiableAndReturnItsLengthAndSize() {
        StructArray<SimpleStruct> array = Structs.newArray(SimpleStruct.class, 32);
        assertNotNull(array);
        assertEquals(32, array.getLength());
        assertEquals(44, array.getStructSize());
    }
    
    @Test
    public void intGettersAndSettersShouldWorkAndRememberTheValue() {
        StructArray<SimpleStruct> array = Structs.newArray(SimpleStruct.class, 32);
        for (int i = 0; i < array.getLength(); i++) {
            array.get(i).setI(i * 4);
        }
        for (int i = 0; i < array.getLength(); i++) {
            assertEquals(i * 4, array.get(i).getI());
        }
    }
    
    @Test
    public void longGettersAndSettersShouldWorkAndRememberTheValue() {
        StructArray<SimpleStruct> array = Structs.newArray(SimpleStruct.class, 32);
        for (int i = 0; i < array.getLength(); i++) {
            array.get(i).setL(i * 4L);
        }
        for (int i = 0; i < array.getLength(); i++) {
            assertEquals(i * 4L, array.get(i).getL());
        }
    }
    
    @Test
    public void byteArrayGettersAndSettersShouldWorkAndRememberTheValue() {
        StructArray<SimpleStruct> array = Structs.newArray(SimpleStruct.class, 64);
        for (int i = 0; i < array.getLength(); i++) {
            for (int j = 0; j < 32; j++) {
                array.get(i).setB(j, (byte) (i + j));
            }
        }
        for (int i = 0; i < array.getLength(); i++) {
            for (int j = 0; j < 32; j++) {
                assertEquals((byte) (i + j), array.get(i).getB(j));
            }
        }
    }

    @Test
    public void accessThroghPointerShouldWorkAsExpected() {
        StructArray<SimpleStruct> array = Structs.newArray(SimpleStruct.class, 32);
        StructPointer<SimpleStruct> ptr = array.at(0);
        for (int i = 0; i < array.getLength(); i++) {
            ptr.at(i).get().setI(i);
            ptr.get().setL(i * 2L);
        }
        for (int i = 0; i < array.getLength(); i++) {
            assertEquals(i, ptr.at(i).get().getI());
            assertEquals(i * 2L, ptr.get().getL());
        }
    }

    @Test
    public void accessingStructWithinStructShouldWork() {
        StructArray<MediumStruct> array = Structs.newArray(MediumStruct.class, 32);
        assertEquals(44 + 40, array.getStructSize());
        StructPointer<MediumStruct> ptr = array.at(0);
        for (int i = 0; i < array.getLength(); i++) {
            ptr.at(i).get().setI(i);
            ptr.get().getSimple().setL(i * 2L);
        }
        for (int i = 0; i < array.getLength(); i++) {
            assertEquals(i, ptr.at(i).get().getI());
            assertEquals(i * 2L, ptr.get().getSimple().getL());
        }
    }
}
