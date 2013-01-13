package it.fb.structs;

/**
 *
 * @author Flavio
 */
public interface StructArray<T> {

    int getSize();

    /**
     * Retrieves a <b>new</b> instance of <code>T</code> which refers to
     * the <i>index</i>-th element of the array.
     * @param index
     * @return 
     */
    T get(int index);
    
    /**
     * Creates a <b>new</b> StructPointer which initially refers to
     * the <i>index</i>-th element of the array.
     * @param index
     * @return 
     */
    StructPointer<T> at(int index);

}
