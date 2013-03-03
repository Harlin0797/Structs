package it.fb.structs.apt.pattern;

import javax.lang.model.element.ExecutableElement;

/**
 *
 * @author Flavio
 */
public class ReturnTypePattern implements IElementPattern<ExecutableElement> {
    
    private final ITypePattern returnTypePattern;

    public ReturnTypePattern(ITypePattern returnTypePattern) {
        this.returnTypePattern = returnTypePattern;
    }

    public boolean matches(ExecutableElement element) {
        return returnTypePattern.matches(element.getReturnType());
    }
    
}
