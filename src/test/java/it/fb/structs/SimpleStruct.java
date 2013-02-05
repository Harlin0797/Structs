package it.fb.structs;

/**
 *
 * @author Flavio
 */
public interface SimpleStruct {
    @Field(position=0)
    int getI();
    void setI(int value);
    
    @Field(position=1)
    long getL();
    void setL(long value);
    
    @Field(length=32, position=2)
    byte getB(int index);
    void setB(int index, byte value);
}
