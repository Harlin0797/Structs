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
     * Retrieves the number of elements in the array this pointer points to.
     * @return The number of elements in the array; always positive.
     */
    int length();

    /**
     * Retrieves the size in bytes of each element in the array.
     * @return The size in bytes of each element in the array; always positive.
     */
    int structSize();

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
    public T pin();

    /**
     * Creates a <b>new</b> <code>StructPointer</code>, duplicate of the receiver.
     * The new pointer points to the same item as the original. The new pointer will
     * not be moved by father pointers; its position is controlled only by the user.
     * @return A new pointer to the same item as the original
     */
    public StructPointer<T> duplicate();
    
    /**
     * Returns the index of the array the receiver is currently pointing to.
     * @return The index the receiver is pointing to; between 0 (included) and length(excluded).
     */
    public int index();
}
