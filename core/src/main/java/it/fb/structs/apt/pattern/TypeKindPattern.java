package it.fb.structs.apt.pattern;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 *
 * @author Flavio
 */
public class TypeKindPattern<M extends TypeMirror> implements ITypePattern<M> {
    
    private final TypeKind typeKind;

    public TypeKindPattern(TypeKind typeKind) {
        this.typeKind = typeKind;
    }

    public boolean matches(M type) {
        return type.getKind().equals(typeKind);
    }
}
