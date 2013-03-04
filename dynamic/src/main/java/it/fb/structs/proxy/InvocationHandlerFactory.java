package it.fb.structs.proxy;

import it.fb.structs.StructArray;
import it.fb.structs.StructPointer;
import it.fb.structs.asm.StructData;
import it.fb.structs.core.AbstractOffsetVisitor;
import it.fb.structs.core.PFieldTypeVisitor;
import it.fb.structs.core.PStructDesc;
import it.fb.structs.core.ParsedField;
import it.fb.structs.core.ParsedFieldType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Flavio
 */
class InvocationHandlerFactory<T, D extends StructData> {
    
    private final Class<T> structInterface;
    private final int structSize;
    private final Map<Method, MethodInvocationHandler<D>> methodHandlers;

    public InvocationHandlerFactory(Class<T> structInterface, int structSize, Map<Method, MethodInvocationHandler<D>> methodHandlers) {
        this.structInterface = structInterface;
        this.structSize = structSize;
        this.methodHandlers = methodHandlers;
    }

    public InvocationHandler newInvocationHandler(D data, int length, int index) {
        return new InvocationHandlerImpl(data, structSize, length, index, Collections.EMPTY_MAP); // TODO: Child pointers
    }

    public int getStructSize() {
        return structSize;
    }
    
    public static <T, D extends StructData> InvocationHandlerFactory<T, D> create(
            Class<T> structInterface, PStructDesc structDesc, AbstractOffsetVisitor offsetVisitor) {
        Map<Method, MethodInvocationHandler<D>> methodHandlers = new HashMap<Method, MethodInvocationHandler<D>>();
        for (ParsedField field : structDesc.getFields()) {
            Integer fieldOffset = field.accept(offsetVisitor, null);
            if (field.getType().isPrimitive()) {
                Method getter = field.findGetterOn(structInterface);
                Method setter = field.findSetterOn(structInterface);
                methodHandlers.put(getter, field.isArray() ? 
                        InvocationHandlerFactory.<D>newArrayGetterHandler(field.getType(), fieldOffset) : 
                        InvocationHandlerFactory.<D>newGetterHandler(field.getType(), fieldOffset));
                methodHandlers.put(setter, field.isArray() ? 
                    InvocationHandlerFactory.<D>newArraySetterHandler(field.getType(), fieldOffset) : 
                    InvocationHandlerFactory.<D>newSetterHandler(field.getType(), fieldOffset));
            } else {
                // TODO: Struct fields
            }
        }
        return new InvocationHandlerFactory<T, D>(structInterface, offsetVisitor.getSize(), methodHandlers);
    }

    private class InvocationHandlerImpl implements InvocationHandler, StructPointer<T> {
        
        private final D data;
        private final int structSize;
        private final int length;
        private final Map<Method, StructPointer<?>> subPointers;
        private int baseOffset;
        private int curAddress;

        public InvocationHandlerImpl(D data, int structSize, int length, int index,
                Map<Method, StructPointer<?>> subPointers) {
            this.data = data;
            this.structSize = structSize;
            this.length = length;
            this.subPointers = subPointers;
            at(index);
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == StructPointer.class) {
                Object ret = invokeSP(method, args);
                if (ret == null) {
                    return proxy;
                } else {
                    return ret;
                }
            } else {
                StructPointer<?> subPointer = subPointers.get(method);
                if (subPointer != null) {
                    return subPointer;
                } else {
                    return invokeData(method, args);
                }
            }
        }
        
        private Object invokeSP(Method method, Object[] args) {
            try {
                return getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(this, args);
            } catch (Exception ex) {
                throw new IllegalStateException("Error invoking inner method", ex);
            }
        }
        
        private Object invokeData(Method method, Object[] args) {
            return methodHandlers.get(method).invoke(data, curAddress, args);
        }

        public T get() {
            return null;
        }

        public final StructPointer<T> at(int index) {
            int _curAddress = this.curAddress = index * structSize + baseOffset;
            for (StructPointer<?> subPointer : subPointers.values()) {
                InvocationHandlerImpl invocationHandler = (InvocationHandlerImpl) Proxy.getInvocationHandler(subPointer);
                invocationHandler.baseOffset = _curAddress + 0; // TODO: POINTER FIELD OFFSET
            }
            return null;
        }

        public StructArray<T> getOwner() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int length() {
            return length;
        }

        public int structSize() {
            return structSize;
        }

        public T pin() {
            throw new UnsupportedOperationException("TODO");
        }

        public StructPointer<T> duplicate() {
            throw new UnsupportedOperationException("TODO");
        }

        public int index() {
            return (curAddress - baseOffset) / structSize;
        }
    }

    private static interface MethodInvocationHandler<D extends StructData> {
        public Object invoke(D data, int offset, Object[] args);
    }

    private static <D extends StructData> MethodInvocationHandler<D> newGetterHandler(ParsedFieldType type, final int fieldOffset) {
        return type.accept(new PFieldTypeVisitor<MethodInvocationHandler<D>, Void>() {
            public MethodInvocationHandler<D> visitBoolean(Void p) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public MethodInvocationHandler<D> visitByte(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        return data.getByte(offset + fieldOffset);
                    }
                };
            }

            public MethodInvocationHandler<D> visitChar(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        return data.getChar(offset + fieldOffset);
                    }
                };
            }

            public MethodInvocationHandler<D> visitShort(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        return data.getShort(offset + fieldOffset);
                    }
                };
            }

            public MethodInvocationHandler<D> visitInt(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        return data.getInt(offset + fieldOffset);
                    }
                };
            }

            public MethodInvocationHandler<D> visitLong(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        return data.getLong(offset + fieldOffset);
                    }
                };
            }

            public MethodInvocationHandler<D> visitFloat(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        return data.getFloat(offset + fieldOffset);
                    }
                };
            }

            public MethodInvocationHandler<D> visitDouble(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        return data.getDouble(offset + fieldOffset);
                    }
                };
            }

            public MethodInvocationHandler<D> visitStruct(String typeName, Void p) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, null);
    }

    private static <D extends StructData> MethodInvocationHandler<D> newArrayGetterHandler(ParsedFieldType type, final int fieldOffset) {
        return type.accept(new PFieldTypeVisitor<MethodInvocationHandler<D>, Void>() {
            public MethodInvocationHandler<D> visitBoolean(Void p) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public MethodInvocationHandler<D> visitByte(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        return data.getByte(offset + fieldOffset + ((Integer)args[0]) * ParsedFieldType.PFTByte.getSize());
                    }
                };
            }

            public MethodInvocationHandler<D> visitChar(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        return data.getChar(offset + fieldOffset + ((Integer)args[0]) * ParsedFieldType.PFTChar.getSize());
                    }
                };
            }

            public MethodInvocationHandler<D> visitShort(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        return data.getShort(offset + fieldOffset + ((Integer)args[0]) * ParsedFieldType.PFTShort.getSize());
                    }
                };
            }

            public MethodInvocationHandler<D> visitInt(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        return data.getInt(offset + fieldOffset + ((Integer)args[0]) * ParsedFieldType.PFTInt.getSize());
                    }
                };
            }

            public MethodInvocationHandler<D> visitLong(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        return data.getLong(offset + fieldOffset + ((Integer)args[0]) * ParsedFieldType.PFTLong.getSize());
                    }
                };
            }

            public MethodInvocationHandler<D> visitFloat(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        return data.getFloat(offset + fieldOffset + ((Integer)args[0]) * ParsedFieldType.PFTFloat.getSize());
                    }
                };
            }

            public MethodInvocationHandler<D> visitDouble(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        return data.getDouble(offset + fieldOffset + ((Integer)args[0]) * ParsedFieldType.PFTDouble.getSize());
                    }
                };
            }

            public MethodInvocationHandler<D> visitStruct(String typeName, Void p) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, null);
    }

    private static <D extends StructData> MethodInvocationHandler<D> newSetterHandler(ParsedFieldType type, final int fieldOffset) {
        return type.accept(new PFieldTypeVisitor<MethodInvocationHandler<D>, Void>() {
            public MethodInvocationHandler<D> visitBoolean(Void p) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public MethodInvocationHandler<D> visitByte(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        data.putByte(offset + fieldOffset, (Byte) args[0]);
                        return null;
                    }
                };
            }

            public MethodInvocationHandler<D> visitChar(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        data.putChar(offset + fieldOffset, (Character) args[0]);
                        return null;
                    }
                };
            }

            public MethodInvocationHandler<D> visitShort(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        data.putShort(offset + fieldOffset, (Short) args[0]);
                        return null;
                    }
                };
            }

            public MethodInvocationHandler<D> visitInt(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        data.putInt(offset + fieldOffset, (Integer) args[0]);
                        return null;
                    }
                };
            }

            public MethodInvocationHandler<D> visitLong(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        data.putLong(offset + fieldOffset, (Long) args[0]);
                        return null;
                    }
                };
            }

            public MethodInvocationHandler<D> visitFloat(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        data.putFloat(offset + fieldOffset, (Float) args[0]);
                        return null;
                    }
                };
            }

            public MethodInvocationHandler<D> visitDouble(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        data.putDouble(offset + fieldOffset, (Double) args[0]);
                        return null;
                    }
                };
            }

            public MethodInvocationHandler<D> visitStruct(String typeName, Void p) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, null);
    }

    private static <D extends StructData> MethodInvocationHandler<D> newArraySetterHandler(ParsedFieldType type, final int fieldOffset) {
        return type.accept(new PFieldTypeVisitor<MethodInvocationHandler<D>, Void>() {
            public MethodInvocationHandler<D> visitBoolean(Void p) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public MethodInvocationHandler<D> visitByte(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        data.putByte(offset + fieldOffset + ((Integer)args[0]) * ParsedFieldType.PFTByte.getSize(), (Byte) args[1]);
                        return null;
                    }
                };
            }

            public MethodInvocationHandler<D> visitChar(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        data.putChar(offset + fieldOffset + ((Integer)args[0]) * ParsedFieldType.PFTChar.getSize(), (Character) args[1]);
                        return null;
                    }
                };
            }

            public MethodInvocationHandler<D> visitShort(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        data.putShort(offset + fieldOffset + ((Integer)args[0]) * ParsedFieldType.PFTShort.getSize(), (Short) args[1]);
                        return null;
                    }
                };
            }

            public MethodInvocationHandler<D> visitInt(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        data.putInt(offset + fieldOffset + ((Integer)args[0]) * ParsedFieldType.PFTInt.getSize(), (Integer) args[1]);
                        return null;
                    }
                };
            }

            public MethodInvocationHandler<D> visitLong(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        data.putLong(offset + fieldOffset + ((Integer)args[0]) * ParsedFieldType.PFTLong.getSize(), (Long) args[1]);
                        return null;
                    }
                };
            }

            public MethodInvocationHandler<D> visitFloat(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        data.putFloat(offset + fieldOffset + ((Integer)args[0]) * ParsedFieldType.PFTFloat.getSize(), (Float) args[1]);
                        return null;
                    }
                };
            }

            public MethodInvocationHandler<D> visitDouble(Void p) {
                return new MethodInvocationHandler<D>() {
                    public Object invoke(D data, int offset, Object[] args) {
                        data.putDouble(offset + fieldOffset + ((Integer)args[0]) * ParsedFieldType.PFTDouble.getSize(), (Double) args[1]);
                        return null;
                    }
                };
            }

            public MethodInvocationHandler<D> visitStruct(String typeName, Void p) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, null);
    }

}
