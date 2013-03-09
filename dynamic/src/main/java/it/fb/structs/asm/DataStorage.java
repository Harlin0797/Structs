package it.fb.structs.asm;

/**
 *
 * @author Flavio
 */
public interface DataStorage<D extends StructData> {

    public Class<D> getBufferClass();

    public D newBuffer(int size);
    
}
