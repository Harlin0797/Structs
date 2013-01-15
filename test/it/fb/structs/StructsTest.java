package it.fb.structs;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Flavio
 */
public class StructsTest {
    
    @Test
    public void arrayStructShouldBeInstantiableAndReturnItsSize() {
        StructArray<SimpleStruct> array = Structs.newArray(SimpleStruct.class, 32);
        assertNotNull(array);
        assertEquals(32, array.getSize());
    }
    
    @Test
    public void intGettersAndSettersShouldWorkAndRememberTheValue() {
        StructArray<SimpleStruct> array = Structs.newArray(SimpleStruct.class, 32);
        for (int i = 0; i < array.getSize(); i++) {
            array.get(i).setI(i * 4);
        }
        for (int i = 0; i < array.getSize(); i++) {
            assertEquals(i * 4, array.get(i).getI());
        }
    }
    
    @Test
    public void longGettersAndSettersShouldWorkAndRememberTheValue() {
        StructArray<SimpleStruct> array = Structs.newArray(SimpleStruct.class, 32);
        for (int i = 0; i < array.getSize(); i++) {
            array.get(i).setL(i * 4L);
        }
        for (int i = 0; i < array.getSize(); i++) {
            assertEquals(i * 4L, array.get(i).getL());
        }
    }
    
    @Test
    public void byteArrayGettersAndSettersShouldWorkAndRememberTheValue() {
        StructArray<SimpleStruct> array = Structs.newArray(SimpleStruct.class, 64);
        for (int i = 0; i < array.getSize(); i++) {
            for (int j = 0; j < 32; j++) {
                array.get(i).setB(j, (byte) (i + j));
            }
        }
        for (int i = 0; i < array.getSize(); i++) {
            for (int j = 0; j < 32; j++) {
                assertEquals((byte) (i + j), array.get(i).getB(j));
            }
        }
    }
}
