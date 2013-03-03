package it.fb.structs.test;

import it.fb.structs.Field;
import it.fb.structs.Struct;
import it.fb.structs.StructPointer;

/**
 *
 * @author Flavio
 */
@Struct
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
