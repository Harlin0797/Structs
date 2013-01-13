package it.fb.structs;

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

    private static final Method INDEXED_SETTER_METHOD;
    
    static {
        try {
            INDEXED_SETTER_METHOD = Indexed.class.getMethod("setIndex", Integer.TYPE);
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static <T> StructArray<T> newArray(Class<T> structInterface, int size) {
        return StructArrayImpl.create(structInterface, size);
    }

    private static final class StructArrayImpl<T> implements StructArray<T> {

        private final Class<T> structInterface;
        private final Map<Method, SField> getters;
        private final Map<Method, SField> setters;
        private final List<Map<String, Object>> data;

        public StructArrayImpl(Class<T> structInterface, Map<Method, SField> getters, 
                Map<Method, SField> setters, List<Map<String, Object>> data) {
            this.structInterface = structInterface;
            this.getters = getters;
            this.setters = setters;
            this.data = data;
        }

        @Override
        public int getSize() {
            return data.size();
        }

        @Override
        public StructPointer<T> at(int index) {
            return new StructPointerImpl(createProxy(index, true));
        }

        @Override
        public T get(int index) {
            return createProxy(index, false);
        }

        private T createProxy(int index, boolean settable) {
            if (settable) {
                return (T) Proxy.newProxyInstance(structInterface.getClassLoader(), new Class[]{structInterface, Indexed.class}, new StructArrayImplInvocationHandler(index));
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
                if (method.equals(INDEXED_SETTER_METHOD)) {
                    index = (Integer) args[0];
                    return null;
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
        
        private final class StructPointerImpl implements StructPointer<T> {
            
            private final T proxy;
            private final Indexed proxy2;

            public StructPointerImpl(T proxy) {
                this.proxy = proxy;
                this.proxy2 = (Indexed) proxy;
            }

            @Override
            public T get() {
                return proxy;
            }

            @Override
            public StructPointer<T> at(int index) {
                proxy2.setIndex(index);
                return this;
            }

            @Override
            public StructArray<T> getOwner() {
                return StructArrayImpl.this;
            }
            
        }

        public static <T> StructArrayImpl<T> create(Class<T> structInterface, int size) {
            SStructDesc desc = Parser.parse(structInterface);
            final Map<String, Object> values = new HashMap<>();
            final Map<Method, SField> getters = new HashMap<>();
            final Map<Method, SField> setters = new HashMap<>();

            List<Map<String, Object>> data = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                for (SField field : desc.getFields()) {
                    getters.put(field.getGetter(), field);
                    setters.put(field.getSetter(), field);
                    if (field.getArrayLength() <= 1) {
                        values.put(field.getName(), field.accept(FieldValueVisitor));
                    } else {
                        List<Object> list = new ArrayList<>(field.getArrayLength());
                        for (int j = 0; j < field.getArrayLength(); j++) {
                            list.add(field.accept(FieldValueVisitor));
                        }
                        values.put(field.getName(), list);
                    }
                }
                data.add(new HashMap<>(values));
            }

            return new StructArrayImpl<>(structInterface, getters, setters, data);
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
            public Object visitStruct(SField field) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    private interface Indexed {
        void setIndex(int index);
    }
}