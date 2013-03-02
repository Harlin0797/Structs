package it.fb.structs.internal;

import it.fb.structs.Field;
import it.fb.structs.StructPointer;
import it.fb.structs.apt.AbstractParser;
import it.fb.structs.apt.PStructDesc;
import it.fb.structs.apt.ParsedField;
import it.fb.structs.apt.ParsedFieldType;
import it.fb.structs.apt.pattern.ParseException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Flavio
 */
public class Parser extends AbstractParser<Method> {

    public static PStructDesc parse(Type structInterface) {
        Parser parser = new Parser((Class<?>) structInterface);
        parser.doParse((Class<?>) structInterface);
        return parser.build();
    }

    private Parser(Class<?> structInterface) {
        super(structInterface.getName(), PATTERNS);
    }

    private void doParse(Class<?> structInterface) {
        if (!structInterface.isInterface()) {
            throw new IllegalArgumentException(structInterface + " is not an interface");
        }
        for (Method m : structInterface.getDeclaredMethods()) {
            if ((m.getModifiers() & Modifier.ABSTRACT) != 0) {
                addMethod(m);
            } else if (null != m.getAnnotation(Field.class)) {
                throw new ParseException("@Field cannot annotate implemented methods: " + m);
            }
        }
    }

    private static final MethodPattern<Method> Getter = new MethodPattern<Method>() {
        public ParsedField match(Method m) {
            if (m.getName().startsWith("get") && m.getParameterTypes().length == 0
                    && m.getReturnType() != Void.TYPE
                    && m.getReturnType().isPrimitive()) {
                return ParsedField.newWithGetter(m.getName().substring(3), 
                        ParsedFieldType.typeOf(m.getReturnType()),
                        m.getAnnotation(Field.class));
            } else {
                return null;
            }
        }
    };

    private static final MethodPattern<Method> ArrayGetter = new MethodPattern<Method>() {
        public ParsedField match(Method m) {
            if (m.getName().startsWith("get") && m.getParameterTypes().length == 1
                && m.getReturnType() != Void.TYPE
                && m.getReturnType().isPrimitive()
                && m.getParameterTypes()[0] == Integer.TYPE) {
                return ParsedField.newWithGetter(m.getName().substring(3), 
                        ParsedFieldType.typeOf(m.getReturnType()),
                        m.getAnnotation(Field.class));
            } else {
                return null;
            }
        }
    };

    private static final MethodPattern<Method> StructGetter = new MethodPattern<Method>() {
        public ParsedField match(Method m) {
            if (m.getName().startsWith("get") && m.getParameterTypes().length == 0
                    && m.getReturnType().equals(StructPointer.class)) {
                return ParsedField.newWithGetter(m.getName().substring(3), 
                        ParsedFieldType.typeOf(((ParameterizedType)m.getGenericReturnType()).getActualTypeArguments()[0]),
                        m.getAnnotation(Field.class));
            } else {
                return null;
            }
        }
    };

    private static final MethodPattern<Method> StructArrayGetter = new MethodPattern<Method>() {
        public ParsedField match(Method m) {
            if (m.getName().startsWith("get") && m.getParameterTypes().length == 1
                    && m.getReturnType().equals(StructPointer.class)
                    && m.getParameterTypes()[0] == Integer.TYPE) {
                return ParsedField.newWithGetter(m.getName().substring(3), 
                        ParsedFieldType.typeOf(((ParameterizedType)m.getGenericReturnType()).getActualTypeArguments()[0]),
                        m.getAnnotation(Field.class));
            } else {
                return null;
            }
        }
    };

    private static final MethodPattern<Method> Setter = new MethodPattern<Method>() {
        public ParsedField match(Method m) {
            if (m.getName().startsWith("set")
                    && m.getParameterTypes().length == 1
                    && m.getParameterTypes()[0].isPrimitive()
                    && m.getReturnType() == Void.TYPE) {
                return ParsedField.newWithSetter(m.getName().substring(3), 
                        ParsedFieldType.typeOf(m.getParameterTypes()[0]),
                        m.getAnnotation(Field.class));
            } else {
                return null;
            }
        }
    };

    private static final MethodPattern<Method> ArraySetter = new MethodPattern<Method>() {
        public ParsedField match(Method m) {
            if (m.getName().startsWith("set")
                    && m.getParameterTypes().length == 2
                    && m.getParameterTypes()[0] == Integer.TYPE
                    && m.getParameterTypes()[1].isPrimitive()
                    && m.getReturnType() == Void.TYPE) {
                return ParsedField.newWithSetter(m.getName().substring(3), 
                        ParsedFieldType.typeOf(m.getParameterTypes()[1]),
                        m.getAnnotation(Field.class));
            } else {
                return null;
            }
        }
    };

    private static final List<MethodPattern<Method>> PATTERNS =
            Arrays.asList(Getter, Setter, ArrayGetter, ArraySetter, StructGetter, StructArrayGetter);

}
