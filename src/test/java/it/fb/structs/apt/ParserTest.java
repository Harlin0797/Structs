package it.fb.structs.apt;

import it.fb.structs.Field;
import static it.fb.structs.apt.Model.*;
import javax.lang.model.element.TypeElement;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Flavio
 */
public class ParserTest {
    
    private Field faDefault;
    private Field faLength16;
    
    
    @Before
    public void setUp() throws NoSuchMethodException {
        faDefault = ParserTest.class.getDeclaredMethod("faDefault").getAnnotation(Field.class);
        faLength16 = ParserTest.class.getDeclaredMethod("faLength16").getAnnotation(Field.class);
    }
    
    @Field
    void faDefault() { }
    
    @Field(length=16)
    void faLength16() { }

    @Test
    public void testParsePrimitiveData() {
        TypeElement element = Model.interfaceElement("SimpleInterface", 
                Model.methodExecutableElement("getI", IntType, faDefault),
                Model.methodExecutableElement("setI", VoidType, var("value", IntType)),
                Model.methodExecutableElement("getD", DoubleType, faLength16, var("index", IntType)),
                Model.methodExecutableElement("setD", VoidType, faLength16, var("index", IntType), var("value", DoubleType))
        );
        PStructDesc result = Parser.parse(element);
        assertEquals("SimpleInterface", result.getJavaInterface());
        assertEquals(2, result.getFields().size());
        assertEquals("I", result.getFields().get(0).name);
        assertEquals(0, result.getFields().get(0).arrayLength);
        assertEquals("D", result.getFields().get(1).name);
        assertEquals(16, result.getFields().get(1).arrayLength);
        assertTrue(result.getFields().get(1).isArray());
    }

}
