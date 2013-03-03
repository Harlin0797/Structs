package it.fb.structs.apt.pattern;

import java.util.List;
import javax.lang.model.element.Element;

/**
 *
 * @author Flavio
 */
public class AndPattern<E extends Element> implements IElementPattern<E> {

    private final List<IElementPattern<? super E>> patterns;
    
    public AndPattern(List<IElementPattern<? super E>> patterns) {
        this.patterns = patterns;
    }

    public boolean matches(E element) {
        for (IElementPattern<? super E> pattern : patterns) {
            if (!pattern.matches(element)) {
                return false;
            }
        }
        return true;
    }

    public IElementPattern<? super E> getPattern(int index) {
        return patterns.get(index);
    }

    public <F extends Element, EP extends IElementPattern<F>> EP getPattern(Class<EP> patternClass) {
        for (IElementPattern<? super E> pattern : patterns) {
            if (patternClass.isInstance(pattern)) {
                return patternClass.cast(pattern);
            }
        }
        throw new IllegalArgumentException("No " + patternClass);
    }

}
