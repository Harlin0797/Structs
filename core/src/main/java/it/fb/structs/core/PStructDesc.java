package it.fb.structs.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Flavio
 */
public class PStructDesc {

    private final String javaInterface;
    private final List<ParsedField> fields;

    public PStructDesc(String javaInterface, List<ParsedField> fields) {
        this.javaInterface = javaInterface;
        this.fields = new ArrayList<ParsedField>(fields);
    }

    public List<ParsedField> getFields() {
        return fields;
    }

    public String getJavaInterface() {
        return javaInterface;
    }

    public Set<String> getInnerTypes() {
        Set<String> ret = new HashSet<String>();
        for (ParsedField field : fields) {
            field.type.accept(new SimpleFieldTypeVisitor<Void, Set<String>>() {
                @Override
                public Void visitStruct(String name, Set<String> p) {
                    p.add(name);
                    return null;
                }
            }, ret);
        }
        return ret;
    }

    @Override
    public String toString() {
        return javaInterface.toString();
    }
}
