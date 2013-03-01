package it.fb.structs.internal;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Flavio
 */
public class PStructDesc {
    
    private final Class<?> javaInterface;
    private final Map<String, ParsedField> fields = new LinkedHashMap<String, ParsedField>();

    public PStructDesc(Class<?> structClass, List<ParsedField> fields) {
        this.javaInterface = structClass;
        for (ParsedField field : fields) {
            this.fields.put(field.getName(), field);
        }
    }

    public Collection<ParsedField> getFields() {
        return fields.values();
    }

    public Class<?> getJavaInterface() {
        return javaInterface;
    }
    
}
