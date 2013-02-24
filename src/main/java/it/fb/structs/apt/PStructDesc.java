package it.fb.structs.apt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor7;

/**
 *
 * @author Flavio
 */
public class PStructDesc {

    private final TypeElement javaInterface;
    private final List<ParsedField> fields;

    public PStructDesc(TypeElement structClass, List<ParsedField> fields) {
        this.javaInterface = structClass;
        this.fields = new ArrayList<ParsedField>(fields);
    }

    public List<ParsedField> getFields() {
        return fields;
    }

    public TypeElement getJavaInterface() {
        return javaInterface;
    }

    public Set<TypeMirror> getInnerTypes() {
        Set<TypeMirror> ret = new HashSet<TypeMirror>();
        for (ParsedField field : fields) {
            field.type.accept(new SimpleTypeVisitor7<Void, Set<TypeMirror>>() {
                @Override
                public Void visitDeclared(DeclaredType t, Set<TypeMirror> p) {
                    p.add(t.getTypeArguments().get(0));
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
