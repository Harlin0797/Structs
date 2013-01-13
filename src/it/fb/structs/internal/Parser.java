package it.fb.structs.internal;

import it.fb.structs.Field;
import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Flavio
 */
public class Parser {
    
    public static SStructDesc parse(Class<?> structInterface) {
        Map<String, SField> fields = new LinkedHashMap<>();
        for (Method m : structInterface.getDeclaredMethods()) {
            Field fAnn = m.getAnnotation(Field.class);
            int length = fAnn == null ? 0 : fAnn.length();
            if (isGetter(m) || isArrayGetter(m)) {
                String propName = Introspector.decapitalize(m.getName().substring(3));
                if (fields.containsKey(propName)) {
                    SField f = fields.get(propName);
                    fields.put(propName, new SField(f.getType(),
                            Math.max(length, f.getArrayLength()), propName, m, f.getSetter()));
                } else {
                    fields.put(propName, new SField(SFieldType.typeOf(m.getReturnType()), 
                            length, propName, m, null));
                }
            } else if (isSetter(m) || isArraySetter(m)) {
                String propName = Introspector.decapitalize(m.getName().substring(3));
                if (fields.containsKey(propName)) {
                    SField f = fields.get(propName);
                    fields.put(propName, new SField(f.getType(),
                            Math.max(length, f.getArrayLength()), propName, f.getGetter(), m));
                } else {
                    fields.put(propName, new SField(SFieldType.typeOf(m.getParameterTypes()[m.getParameterTypes().length - 1]), 
                            length, propName, null, m));
                }
            }
        }
        return new SStructDesc(structInterface, new ArrayList<>(fields.values()));
    }

    private static boolean isGetter(Method m) {
        return m.getName().startsWith("get") && m.getParameterTypes().length == 0
                && m.getReturnType() != Void.TYPE;
    }
    
    private static boolean isArrayGetter(Method m) {
        return m.getName().startsWith("get") && m.getParameterTypes().length == 1
                && m.getReturnType() != Void.TYPE
                && m.getParameterTypes()[0] == Integer.TYPE;
    }

    private static boolean isSetter(Method m) {
        return m.getName().startsWith("set") 
            && m.getParameterTypes().length == 1
            && m.getReturnType() == Void.TYPE;
    }
    
    private static boolean isArraySetter(Method m) {
        return m.getName().startsWith("set") 
            && m.getParameterTypes().length == 2
            && m.getParameterTypes()[0] == Integer.TYPE
            && m.getReturnType() == Void.TYPE;
    }
}
