package it.fb.structs.bytebuffer;

import it.fb.structs.StructArray;
import it.fb.structs.StructPointer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author Flavio
 */
public final class StructArrayByteBufferImpl<T> implements StructArray<T> {
    
    private final ByteBufferProxyHandlerFactory<T> handlerFactory;
    private final int length;
    private final ByteBuffer data;

    private StructArrayByteBufferImpl(ByteBufferProxyHandlerFactory<T> handlerFactory, int length, ByteBuffer data) {
        this.handlerFactory = handlerFactory;
        this.length = length;
        this.data = data;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public int getStructSize() {
        return handlerFactory.getStructSize();
    }

    @Override
    public T get(int index) {
        return handlerFactory.newDataInstance(data, 0, index);
    }

    @Override
    public StructPointer<T> at(int index) {
        return handlerFactory.newPointerInstance(this, data, 0, index);
    }

    public static <T> StructArrayByteBufferImpl<T> create(Class<T> structInterface, int size) {
        ByteBufferProxyHandlerFactory<T> handlerFactory = ByteBufferProxyHandlerFactory.newHandlerFactory(structInterface);
        ByteBuffer data = ByteBuffer.allocate(handlerFactory.getStructSize() * size).order(ByteOrder.nativeOrder());
        return new StructArrayByteBufferImpl<>(handlerFactory, size, data);
    }

}
