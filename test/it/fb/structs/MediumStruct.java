package it.fb.structs;

/**
 *
 * @author Flavio
 */
public interface MediumStruct {
    
    @Field(position=0)
    int getI();
    void setI(int value);
    
    @Field(position=1)
    float getF();
    void setF(float value);
    
    @Field(length=32, position=2)
    byte getB(int index);
    void setB(int index, byte value);
    
    @Field(position=3)
    StructPointer<SimpleStruct> getSimple();
    
}
