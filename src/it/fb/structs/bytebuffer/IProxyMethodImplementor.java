package it.fb.structs.bytebuffer;

import java.nio.ByteBuffer;

/**
 *
 * @author Flavio
 */
public interface IProxyMethodImplementor {
    public Object run(ByteBuffer data, int baseOffset, Object[] args);
}
