package it.fb.structs.apt.pattern;

import javax.lang.model.element.Element;

/**
 *
 * @author Flavio
 */
public interface IElementPattern<E extends Element> {

    public boolean matches(E element);
    
}
