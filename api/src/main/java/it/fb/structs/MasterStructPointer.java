package it.fb.structs;

/**
 *
 * @author Flavio
 */
public interface MasterStructPointer<T> extends StructPointer<T> {
    /**
     * Destroys the structure, releasing all associated resources.
     * All the pointers and instances obtained through this object
     * are invalid and must be discarded.
     */
    public void release();
}
