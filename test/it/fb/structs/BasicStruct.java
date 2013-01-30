package it.fb.structs;

/**
 *
 * @author Flavio
 */
public interface BasicStruct {
    @Field
    int getI();
    void setI(int value);
    
    @Field
    long getL();
    void setL(long value);
}
