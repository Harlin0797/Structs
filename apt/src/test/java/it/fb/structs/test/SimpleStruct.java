package it.fb.structs.test;

import it.fb.structs.Field;
import it.fb.structs.Struct;

/**
 *
 * @author Flavio
 */
@Struct
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
