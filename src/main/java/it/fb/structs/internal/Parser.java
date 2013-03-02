package it.fb.structs.internal;

import it.fb.structs.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Flavio
 */
public class Parser {
    
    public static PStructDesc parse(Type structInterface) {
        return parse((Class<?>) structInterface);
    }

    public static PStructDesc parse(Class<?> structInterface) {
        if (!structInterface.isInterface()) {
            throw new IllegalArgumentException(structInterface + " is not an interface");
        }
        Map<String, ParsedField> fields = new LinkedHashMap<String, ParsedField>();
        for (Method m : structInterface.getDeclaredMethods()) {
            Field fAnn = m.getAnnotation(Field.class);
            int length = fAnn == null ? 0 : fAnn.length();
            int position = fAnn == null ? Integer.MAX_VALUE : fAnn.position();
            if (isGetter(m) || isArrayGetter(m)) {
                String propName = m.getName().substring(3);
                if (fields.containsKey(propName)) {
                    ParsedField f = fields.get(propName);
                    fields.put(propName, new ParsedField(f.getType(),
                            Math.max(length, f.getArrayLength()), propName, position, true, f.hasSetter()));
                } else {
                    fields.put(propName, new ParsedField(ParsedFieldType.typeOf(m.getGenericReturnType()), 
                            length, propName, position, true, false));
                }
            } else if (isSetter(m) || isArraySetter(m)) {
                String propName = m.getName().substring(3);
                if (fields.containsKey(propName)) {
                    ParsedField f = fields.get(propName);
                    fields.put(propName, new ParsedField(f.getType(),
                            Math.max(length, f.getArrayLength()), propName, f.getPosition(), f.hasSetter(), true));
                } else {
                    fields.put(propName, new ParsedField(ParsedFieldType.typeOf(m.getParameterTypes()[m.getParameterTypes().length - 1]), 
                            length, propName, position, false, true));
                }
            }
        }
        if (fields.isEmpty()) {
            throw new IllegalArgumentException("No valid struct fields in " + structInterface);
        }
        List<ParsedField> sortedFields = new ArrayList<ParsedField>(fields.values());
        Collections.sort(sortedFields, new Comparator<ParsedField>() {
            @Override
            public int compare(ParsedField o1, ParsedField o2) {
                return Integer.compare(o1.getPosition(), o2.getPosition());
            }
        });
        return new PStructDesc(structInterface, sortedFields);
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
