package it.fb.structs.internal;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Flavio
 */
public class SStructDesc {
    
    private final Class<?> javaInterface;
    private final Map<String, SField> fields = new LinkedHashMap<String, SField>();

    public SStructDesc(Class<?> structClass, List<SField> fields) {
        this.javaInterface = structClass;
        for (SField field : fields) {
            this.fields.put(field.getName(), field);
        }
    }

    public Collection<SField> getFields() {
        return fields.values();
    }

    public Class<?> getJavaInterface() {
        return javaInterface;
    }
    
}
