package it.fb.structs;

import it.fb.structs.bytebuffer.StructArrayByteBufferImpl;
import it.fb.structs.internal.Parser;
import it.fb.structs.internal.ParsedField;
import it.fb.structs.internal.ParsedField.ParsedFieldVisitor;
import it.fb.structs.internal.PStructDesc;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Flavio
 */
public class Structs {

    public static final Method POINTER_AT_METHOD;
    public static final Method POINTER_OWNER_METHOD;
    public static final Method POINTER_GET_METHOD;
    
    static {
        try {
            POINTER_AT_METHOD = StructPointer.class.getMethod("at", Integer.TYPE);
            POINTER_GET_METHOD = StructPointer.class.getMethod("get");
            POINTER_OWNER_METHOD = StructPointer.class.getMethod("getOwner");
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static <T> StructArray<T> newArray(Class<T> structInterface, int size) {
        return StructArrayByteBufferImpl.create(structInterface, size);
        //return StructArrayHashImpl.create(structInterface, size);
    }

    private static final class StructArrayHashImpl<T> implements StructArray<T> {

        private final Class<T> structInterface;
        private final Map<Method, ParsedField> getters;
        private final Map<Method, ParsedField> setters;
        private final List<Map<String, Object>> data;

        public StructArrayHashImpl(Class<T> structInterface, Map<Method, ParsedField> getters, 
                Map<Method, ParsedField> setters, List<Map<String, Object>> data) {
            this.structInterface = structInterface;
            this.getters = getters;
            this.setters = setters;
            this.data = data;
        }

        @Override
        public int getLength() {
            return data.size();
        }

        @Override
        public int getStructSize() {
            return 0;
        }

        @Override
        public StructPointer<T> at(int index) {
            return (StructPointer<T>) createProxy(index, true);
        }

        @Override
        public T get(int index) {
            return createProxy(index, false);
        }

        @Override
        public void release() {
            throw new UnsupportedOperationException("TODO");
        }

        private T createProxy(int index, boolean settable) {
            if (settable) {
                return (T) Proxy.newProxyInstance(structInterface.getClassLoader(), new Class[]{structInterface, StructPointer.class}, new StructArrayImplInvocationHandler(index));
            } else {
                return (T) Proxy.newProxyInstance(structInterface.getClassLoader(), new Class[]{structInterface}, new StructArrayImplInvocationHandler(index));
            }
        }

        private final class StructArrayImplInvocationHandler implements InvocationHandler {

            private int index;

            private StructArrayImplInvocationHandler(int index) {
                this.index = index;
            }

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                ParsedField field;
                if (method.equals(POINTER_AT_METHOD)) {
                    index = (Integer) args[0];
                    return proxy;
                } else if (method.equals(POINTER_GET_METHOD)) {
                    return proxy;
                } else if (method.equals(POINTER_OWNER_METHOD)) {
                    return StructArrayHashImpl.this;
                } else if (null != (field = getters.get(method))) {
                    if (field.isArray()) {
                        return ((List<?>)data.get(index).get(field.getName()))
                                .get((Integer)args[0]);
                    } else {
                        return data.get(index).get(field.getName());
                    }
                } else if (null != (field = setters.get(method))) {
                    if (field.isArray()) {
                        ((List<Object>)data.get(index).get(field.getName()))
                                .set((Integer)args[0], args[1]);
                    } else {
                        data.get(index).put(field.getName(), args[0]);
                    }
                    return null;
                } else {
                    throw new UnsupportedOperationException(method.toString());
                }
            }
        }

        public static <T> StructArrayHashImpl<T> create(Class<T> structInterface, int size) {
            PStructDesc desc = Parser.parse(structInterface);
            final Map<String, Object> values = new HashMap<String, Object>();
            final Map<Method, ParsedField> getters = new HashMap<Method, ParsedField>();
            final Map<Method, ParsedField> setters = new HashMap<Method, ParsedField>();

            List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
            for (int i = 0; i < size; i++) {
                for (ParsedField field : desc.getFields()) {
                    getters.put(field.getGetter(), field);
                    setters.put(field.getSetter(), field);
                    if (field.getArrayLength() <= 1) {
                        values.put(field.getName(), field.accept(FieldValueVisitor));
                    } else {
                        List<Object> list = new ArrayList<Object>(field.getArrayLength());
                        for (int j = 0; j < field.getArrayLength(); j++) {
                            list.add(field.accept(FieldValueVisitor));
                        }
                        values.put(field.getName(), list);
                    }
                }
                data.add(new HashMap<String, Object>(values));
            }

            return new StructArrayHashImpl<T>(structInterface, getters, setters, data);
        }
        
        private static ParsedFieldVisitor<Object> FieldValueVisitor = new ParsedFieldVisitor<Object>() {

            public Object visitBoolean(ParsedField field) {
                throw new UnsupportedOperationException("TODO");
            }

            @Override
            public Object visitByte(ParsedField field) {
                return Byte.valueOf((byte)0);
            }

            @Override
            public Object visitChar(ParsedField field) {
                return Character.valueOf('\0');
            }

            @Override
            public Object visitShort(ParsedField field) {
                return Short.valueOf((short) 0);
            }

            @Override
            public Object visitInt(ParsedField field) {
                return Integer.valueOf(0);
            }

            @Override
            public Object visitLong(ParsedField field) {
                return Long.valueOf(0L);
            }

            @Override
            public Object visitFloat(ParsedField field) {
                return Float.valueOf(0.0f);
            }

            @Override
            public Object visitDouble(ParsedField field) {
                return Double.valueOf(0.0);
            }

            @Override
            public Object visitStruct(ParsedField field, String className) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }
}