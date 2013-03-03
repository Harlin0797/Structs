package it.fb.structs.test;

import it.fb.structs.Field;
import it.fb.structs.Struct;
import it.fb.structs.StructPointer;

/**
 *
 * @author Flavio
 */
@Struct
public interface ComplexStruct {
    
    @Field(position=0)
    int getI();
    void setI(int value);
    
    @Field(position=1, length=8)
    StructPointer<MediumStruct> getMedium(int index);
    
    @Field(position=2, length=16)
    double getD(int index);
    void setD(int index, double value);
}
