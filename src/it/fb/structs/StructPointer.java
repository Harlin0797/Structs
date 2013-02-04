package it.fb.structs;

/**
 *
 * @author Flavio
 */
public interface StructPointer<T> {

    /**
     * Retrieves the item this pointer currently is pointing at.
     * This method does not create a new object; in fact, it always 
     * returns the same instance.
     * @return 
     */
    public T get();

    /**
     * Moves the pointer to another index.
     * @param index
     * @return The receiver
     */
    public StructPointer<T> at(int index);

    /**
     * TODO: REMOVE.
     * Retrieves the StructArray this pointer belongs to.
     * @return 
     */
    public StructArray<T> getOwner();

    /**
     * Retrieves the number of elements in the array this pointer points to.
     * @return The number of elements in the array; always positive.
     */
    int getLength();

    /**
     * Retrieves the size in bytes of each element in the array.
     * @return The size in bytes of each element in the array; always positive.
     */
    int getStructSize();

    /**
     * Copies the data pointer by the <i>source</i> pointer into the
     * data pointer by the receiver.
     * @param source Pointer pointing to the data to be copied
     * @return The receiver
     */
    //public StructPointer<T> copy(StructPointer<T> source);
    
    /**
     * Creates a <b>new</b> instance of <code>T</code>, fixed on the
     * data currently pointed by the receiver.
     * @return A new long-lived instance of T
     */
    //public T pin();

    //public StructPointer<T> duplicate();
}
