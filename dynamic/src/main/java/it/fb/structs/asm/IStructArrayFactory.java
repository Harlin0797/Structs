package it.fb.structs.asm;

import it.fb.structs.StructArray;

/**
 *
 * @author Flavio
 */
public interface IStructArrayFactory<D extends StructData> {

    <T> StructArray<T> newStructArray(Class<T> structInterface, int length);
    <T> StructArray<T> wrap(Class<T> structInterface, D data);

}
