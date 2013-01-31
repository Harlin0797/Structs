package it.fb.structs;

/**
 *
 * @author Flavio
 */
public interface BasicStruct {
    @Field
    byte getB();
    void setB(byte value);
    
    @Field
    char getC();
    void setC(char value);
    
    @Field
    short getS();
    void setS(short value);
    
    @Field
    int getI();
    void setI(int value);
    
    @Field
    long getL();
    void setL(long value);

    @Field
    float getF();
    void setF(float value);
    
    @Field
    double getD();
    void setD(double value);
}
