package it.fb.structs.test;

import it.fb.structs.Field;
import it.fb.structs.Struct;

/**
 *
 * @author Flavio
 */
@Struct
public interface ArrayStruct {
    @Field(length=32)
    byte getB(int index);
    void setB(int index, byte value);
    
    @Field(length=32)
    char getC(int index);
    void setC(int index, char value);
    
    @Field(length=32)
    short getS(int index);
    void setS(int index, short value);
    
    @Field(length=32)
    int getI(int index);
    void setI(int index, int value);
    
    @Field(length=32)
    long getL(int index);
    void setL(int index, long value);

    @Field(length=32)
    float getF(int index);
    void setF(int index, float value);
    
    @Field(length=32)
    double getD(int index);
    void setD(int index, double value);
}
