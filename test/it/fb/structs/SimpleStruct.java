package it.fb.structs;

/**
 *
 * @author Flavio
 */
public interface SimpleStruct {
    @Field
    int getI();
    void setI(int value);
    
    @Field
    long getL();
    void setL(long value);
    
    @Field(length=32)
    byte getB(int index);
    void setB(int index, byte value);
}