package it.fb.structs;

import it.fb.structs.bytebuffer.OffsetVisitor;
import it.fb.structs.internal.IStructArrayFactory;
import it.fb.structs.internal.IStructArrayFactory.Builder;
import it.fb.structs.internal.Parser;
import it.fb.structs.internal.SField;
import it.fb.structs.internal.SStructDesc;

/**
 *
 * @author Flavio
 */
public class StructArrayRepository<D extends StructData> {

    private final StructData.Factory<D> dataFactory;
    private final IStructArrayFactory.Factory classFactory;

    public StructArrayRepository(StructData.Factory<D> dataFactory, IStructArrayFactory.Factory classFactory) {
        this.dataFactory = dataFactory;
        this.classFactory = classFactory;
    }

    public <T> StructArray<T> newStructArray(Class<T> structInterface, int length) {
        Builder<T, D> builder = classFactory.newBuilder(dataFactory, structInterface);
        SStructDesc desc = Parser.parse(structInterface);
        OffsetVisitor ov = new OffsetVisitor(4);
        
        for (SField field : desc.getFields()) {
            int fieldOffset = field.accept(ov);
            builder.addGetter(field.getGetter(), field, fieldOffset);
            if (field.getSetter() != null) {
                builder.addSetter(field.getSetter(), field, fieldOffset);
            }
        }
        
        IStructArrayFactory<T, D> factory = builder.build(ov.getSize());
        return factory.newStructArray(length);
    }
    
}
