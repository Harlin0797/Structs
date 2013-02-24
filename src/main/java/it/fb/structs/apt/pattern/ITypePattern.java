package it.fb.structs.apt.pattern;

import javax.lang.model.type.TypeMirror;

/**
 *
 * @author Flavio
 */
public interface ITypePattern<M extends TypeMirror> {

    public boolean matches(M type);
    
}
