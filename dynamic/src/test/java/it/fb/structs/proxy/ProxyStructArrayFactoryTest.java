package it.fb.structs.proxy;

import it.fb.structs.asm.StructData;
import it.fb.structs.test.AbstractStructArrayFactoryTest;

/**
 *
 * @author Flavio
 */
public class ProxyStructArrayFactoryTest extends AbstractStructArrayFactoryTest {

    public ProxyStructArrayFactoryTest(StructData.Factory<?> dataFactory) {
        super(ProxyStructArrayFactory.newInstance(dataFactory));
    }
}
