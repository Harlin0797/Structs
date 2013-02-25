package it.fb.structs.apt.pattern;

import java.util.Arrays;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 *
 * @author Flavio
 */
public class TypePatterns {

    public static <M extends TypeMirror> AndTypePattern<M> and(
            ITypePattern<? super M> pattern1,
            ITypePattern<? super M> pattern2) {
        return new AndTypePattern<M>(Arrays.<ITypePattern<? super M>>asList(pattern1, pattern2));
    }

    public static <M extends TypeMirror> AndTypePattern<M> and(
            ITypePattern<? super M> pattern1,
            ITypePattern<? super M> pattern2,
            ITypePattern<? super M> pattern3) {
        return new AndTypePattern<M>(Arrays.<ITypePattern<? super M>>asList(pattern1, pattern2, pattern3));
    }

    public static <M extends TypeMirror> ITypePattern<M> kind(TypeKind kind) {
        return new TypeKindPattern<M>(kind);
    }

    public static TypeNamePattern typeName(Class<?> matchClass) {
        return new TypeNamePattern(matchClass.getPackage().getName(), matchClass.getSimpleName());
    }

    public static TypeNamePattern typeName(String enclosingName, String name) {
        return new TypeNamePattern(enclosingName, name);
    }

    public static <M extends TypeMirror> ArgumentsTypePattern withTypeArg(ITypePattern<M> argPattern) {
        return new ArgumentsTypePattern(Arrays.asList(argPattern));
    }
}
