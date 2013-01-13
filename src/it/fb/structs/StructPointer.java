package it.fb.structs;

/**
 *
 * @author Flavio
 */
public interface StructPointer<T> {

    public T get();
    public StructPointer<T> at(int index);
    public StructArray<T> getOwner();

}
