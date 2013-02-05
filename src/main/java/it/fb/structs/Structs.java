package it.fb.structs;

import it.fb.structs.bytebuffer.StructArrayByteBufferImpl;
import it.fb.structs.internal.Parser;
import it.fb.structs.internal.SField;
import it.fb.structs.internal.SField.SFieldVisitor;
import it.fb.structs.internal.SStructDesc;
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
        private final Map<Method, SField> getters;
        private final Map<Method, SField> setters;
        private final List<Map<String, Object>> data;

        public StructArrayHashImpl(Class<T> structInterface, Map<Method, SField> getters, 
                Map<Method, SField> setters, List<Map<String, Object>> data) {
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
                SField field;
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
            SStructDesc desc = Parser.parse(structInterface);
            final Map<String, Object> values = new HashMap<String, Object>();
            final Map<Method, SField> getters = new HashMap<Method, SField>();
            final Map<Method, SField> setters = new HashMap<Method, SField>();

            List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
            for (int i = 0; i < size; i++) {
                for (SField field : desc.getFields()) {
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
        
        private static SFieldVisitor<Object> FieldValueVisitor = new SFieldVisitor<Object>() {
            
            @Override
            public Object visitByte(SField field) {
                return Byte.valueOf((byte)0);
            }

            @Override
            public Object visitChar(SField field) {
                return Character.valueOf('\0');
            }

            @Override
            public Object visitShort(SField field) {
                return Short.valueOf((short) 0);
            }

            @Override
            public Object visitInt(SField field) {
                return Integer.valueOf(0);
            }

            @Override
            public Object visitLong(SField field) {
                return Long.valueOf(0L);
            }

            @Override
            public Object visitFloat(SField field) {
                return Float.valueOf(0.0f);
            }

            @Override
            public Object visitDouble(SField field) {
                return Double.valueOf(0.0);
            }

            @Override
            public Object visitStruct(SField field, SStructDesc structDesc) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }
}