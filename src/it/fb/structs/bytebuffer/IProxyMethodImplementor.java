package it.fb.structs.bytebuffer;

import it.fb.structs.bytebuffer.ByteBufferProxyHandlerFactory.SABBDataInvocationHandler;
import java.nio.ByteBuffer;

/**
 *
 * @author Flavio
 */
interface IProxyMethodImplementor {
    public void init(SABBDataInvocationHandler ownerHandler);
    public Object run(ByteBuffer data, int baseOffset, Object[] args);
}
