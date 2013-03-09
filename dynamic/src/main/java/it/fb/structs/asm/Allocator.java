package it.fb.structs.asm;

import it.fb.structs.MasterStructPointer;

/**
 *
 * @author Flavio
 */
public interface Allocator<D extends StructData> {

    <T> MasterStructPointer<T> newStructArray(Class<T> structInterface, int length);
    <T> MasterStructPointer<T> wrap(Class<T> structInterface, D data);

}
