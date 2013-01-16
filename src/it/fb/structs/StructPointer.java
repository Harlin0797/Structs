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
     * Retrieves the StructArray this pointer belongs to.
     * @return 
     */
    public StructArray<T> getOwner();

}
