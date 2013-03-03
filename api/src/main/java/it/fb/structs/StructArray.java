package it.fb.structs;

/**
 *
 * @author Flavio
 */
public interface StructArray<T> {

    /**
     * Retrieves the number of elements in the array.
     * @return The number of elements in the array; always positive.
     */
    int getLength();

    /**
     * Retrieves the size of each element in the array.
     * @return The size of each element in the array; always positive.
     */
    int getStructSize();

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

    /**
     * Destroys the structure, releasing all associated resources.
     * All the pointers and instances obtained through this object
     * are invalid and must be discarded.
     */
    void release();
}
