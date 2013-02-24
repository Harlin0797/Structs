package it.fb.structs.apt.pattern;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;

/**
 *
 * @author Flavio
 */
public class NamePattern<E extends Element> implements IElementPattern<E> {
    private final Pattern namePattern;

    public NamePattern(Pattern namePattern) {
        this.namePattern = namePattern;
    }

    public boolean matches(E element) {
        return namePattern.matcher(element.getSimpleName()).matches();
    }

    public Matcher matcher(E element) {
        return namePattern.matcher(element.getSimpleName());
    }
}
