package it.fb.structs.proxy;

import it.fb.structs.asm.ByteBufferStructData;
import it.fb.structs.test.AbstractStructArrayFactoryTest;
import org.junit.Before;

/**
 *
 * @author Flavio
 */
public class ProxyStructArrayFactoryTest extends AbstractStructArrayFactoryTest {
    
    @Before
    public void setUp() {
        factory = ProxyStructArrayFactory.newInstance(ByteBufferStructData.Plain.Native);
    }
}
