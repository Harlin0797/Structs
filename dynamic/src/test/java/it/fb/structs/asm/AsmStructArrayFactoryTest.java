package it.fb.structs.asm;

import it.fb.structs.test.AbstractStructArrayFactoryTest;

/**
 *
 * @author Flavio
 */
public class AsmStructArrayFactoryTest extends AbstractStructArrayFactoryTest {

    public AsmStructArrayFactoryTest(StructData.Factory<?> dataFactory) {
        super(AsmStructArrayFactory.newInstance(dataFactory));
    }

}