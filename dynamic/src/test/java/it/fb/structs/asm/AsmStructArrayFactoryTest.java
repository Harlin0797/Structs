package it.fb.structs.asm;

import it.fb.structs.test.AbstractStructArrayFactoryTest;
import org.junit.Before;

/**
 *
 * @author Flavio
 */
public class AsmStructArrayFactoryTest extends AbstractStructArrayFactoryTest {
    
    @Before
    public void setUp() {
        factory = AsmStructArrayFactory.newInstance(ByteBufferStructData.Plain.Native);
    }
}