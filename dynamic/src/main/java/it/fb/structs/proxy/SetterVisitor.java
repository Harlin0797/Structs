package it.fb.structs.proxy;

import it.fb.structs.asm.StructData;
import it.fb.structs.core.PFieldTypeVisitor;
import it.fb.structs.proxy.SetterVisitor.SetterParameter;

/**
 *
 * @author Flavio
 */
public class SetterVisitor<D extends StructData> implements PFieldTypeVisitor<Void, SetterParameter> {

    private final D data;

    public SetterVisitor(D data) {
        this.data = data;
    }

    public Void visitBoolean(SetterParameter parameter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Void visitByte(SetterParameter parameter) {
        data.putByte(parameter.position, (Byte) parameter.value);
        return null;
    }

    public Void visitChar(SetterParameter parameter) {
        data.putChar(parameter.position, (Character) parameter.value);
        return null;
    }

    public Void visitShort(SetterParameter parameter) {
        data.putShort(parameter.position, (Short) parameter.value);
        return null;
    }

    public Void visitInt(SetterParameter parameter) {
        data.putInt(parameter.position, (Integer) parameter.value);
        return null;
    }

    public Void visitLong(SetterParameter parameter) {
        data.putLong(parameter.position, (Long) parameter.value);
        return null;
    }

    public Void visitFloat(SetterParameter parameter) {
        data.putFloat(parameter.position, (Float) parameter.value);
        return null;
    }

    public Void visitDouble(SetterParameter parameter) {
        data.putDouble(parameter.position, (Double) parameter.value);
        return null;
    }

    public Void visitStruct(String typeName, SetterParameter parameter) {
        throw new UnsupportedOperationException();
    }

    public static final class SetterParameter {
        public final Object value;
        public final int position;

        public SetterParameter(Object value, int position) {
            this.value = value;
            this.position = position;
        }
    }
}
