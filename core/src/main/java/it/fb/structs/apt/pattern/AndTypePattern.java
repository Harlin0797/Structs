package it.fb.structs.apt.pattern;

import java.util.List;
import javax.lang.model.type.TypeMirror;

/**
 *
 * @author Flavio
 */
public class AndTypePattern<M extends TypeMirror> implements ITypePattern<M> {

    private final List<ITypePattern<? super M>> patterns;
    
    public AndTypePattern(List<ITypePattern<? super M>> patterns) {
        this.patterns = patterns;
    }

    public boolean matches(M element) {
        for (ITypePattern<? super M> pattern : patterns) {
            if (!pattern.matches(element)) {
                return false;
            }
        }
        return true;
    }

    public ITypePattern<? super M> getPattern(int index) {
        return patterns.get(index);
    }

    public <N extends TypeMirror, MP extends ITypePattern<N>> MP getPattern(Class<MP> patternClass) {
        for (ITypePattern<? super M> pattern : patterns) {
            if (patternClass.isInstance(pattern)) {
                return patternClass.cast(pattern);
            }
        }
        throw new IllegalArgumentException("No " + patternClass);
    }

}
