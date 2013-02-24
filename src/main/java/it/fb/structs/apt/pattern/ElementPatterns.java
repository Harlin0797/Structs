package it.fb.structs.apt.pattern;

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;

/**
 *
 * @author Flavio
 */
public class ElementPatterns {
    
    public static <E extends Element> AndPattern<E> and(
            IElementPattern<? super E> pattern1,
            IElementPattern<? super E> pattern2) {
        return new AndPattern<E>(Arrays.<IElementPattern<? super E>>asList(pattern1, pattern2));
    }

    public static <E extends Element> AndPattern<E> and(
            IElementPattern<? super E> pattern1,
            IElementPattern<? super E> pattern2,
            IElementPattern<? super E> pattern3) {
        return new AndPattern<E>(Arrays.<IElementPattern<? super E>>asList(pattern1, pattern2, pattern3));
    }

    public static <E extends Element> NamePattern<E> nameIs(String nameRegex) {
        return new NamePattern<E>(Pattern.compile(nameRegex));
    }

    public static ReturnTypePattern returns(ITypePattern returnType) {
        return new ReturnTypePattern(returnType);
    }

    public static ArgumentsPattern hasArg(ITypePattern argType) {
        return new ArgumentsPattern(Arrays.asList(argType));
    }

    public static ArgumentsPattern hasArgs(ITypePattern arg1Type, ITypePattern arg2Type) {
        return new ArgumentsPattern(Arrays.asList(arg1Type, arg2Type));
    }

    public static ArgumentsPattern hasArgs(ITypePattern arg1Type, ITypePattern arg2Type, ITypePattern arg3Type) {
        return new ArgumentsPattern(Arrays.asList(arg1Type, arg2Type, arg3Type));
    }

    public static ArgumentsPattern hasNoArgs() {
        return new ArgumentsPattern(Collections.<ITypePattern>emptyList());
    }
    
}
