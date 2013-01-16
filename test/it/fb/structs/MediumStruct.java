package it.fb.structs;

/**
 *
 * @author Flavio
 */
public interface MediumStruct {
    
    @Field
    int getI();
    void setI(int value);
    
    @Field
    float getF();
    void setF(float value);
    
    @Field(length=32)
    byte getB(int index);
    void setB(int index, byte value);
    
    @Field
    SimpleStruct getSimple();
    
}
