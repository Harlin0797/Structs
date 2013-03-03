package it.fb.structs.apt.pattern;

import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

/**
 *
 * @author Flavio
 */
public class ArgumentsPattern implements IElementPattern<ExecutableElement> {
    
    private final List<ITypePattern> argumentPatterns;

    public ArgumentsPattern(List<ITypePattern> argumentPatterns) {
        this.argumentPatterns = argumentPatterns;
    }

    public boolean matches(ExecutableElement element) {
        if (element.getParameters().size() != argumentPatterns.size()) {
            return false;
        }
        for (int i = 0; i < argumentPatterns.size(); i++) {
            ITypePattern typePattern = argumentPatterns.get(i);
            VariableElement parameter = element.getParameters().get(i);
            if (!typePattern.matches(parameter.asType())) {
                return false;
            }
        }
        return true;
    }
    
}
