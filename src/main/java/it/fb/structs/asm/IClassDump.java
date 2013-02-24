package it.fb.structs.asm;

/**
 *
 * @author Flavio
 */
public interface IClassDump {
    
    public void dump(Class<?> structInterface, Class<?> structImplementation, byte[] data);
}
