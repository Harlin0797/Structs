package it.fb.structs.proxy;

import it.fb.structs.asm.DataStorage;
import it.fb.structs.test.AbstractStructArrayFactoryTest;

/**
 *
 * @author Flavio
 */
public class ProxyStructArrayFactoryTest extends AbstractStructArrayFactoryTest {

    public ProxyStructArrayFactoryTest(DataStorage<?> dataFactory) {
        super(ProxyAllocator.newInstance(dataFactory));
    }
}
