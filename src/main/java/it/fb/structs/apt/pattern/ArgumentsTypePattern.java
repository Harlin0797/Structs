package it.fb.structs.apt.pattern;

import java.util.List;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 *
 * @author Flavio
 */
public class ArgumentsTypePattern implements ITypePattern<DeclaredType> {

    private final List<? extends ITypePattern<?>> argumentPatterns;

    public ArgumentsTypePattern(List<? extends ITypePattern<?>> argumentPatterns) {
        this.argumentPatterns = argumentPatterns;
    }
    
    public boolean matches(DeclaredType type) {
        if (type.getTypeArguments().size() != argumentPatterns.size()) {
            return false;
        }
        for (int i = 0; i < argumentPatterns.size(); i++) {
            ITypePattern paramPattern = argumentPatterns.get(i);
            TypeMirror paramType = type.getTypeArguments().get(i);
            if (!paramPattern.matches(paramType)) {
                return false;
            }
        }
        return true;
    }
    
}
