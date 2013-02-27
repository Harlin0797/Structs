package it.fb.structs.apt;

import it.fb.structs.Field;
import it.fb.structs.StructPointer;
import it.fb.structs.apt.pattern.AndPattern;
import static it.fb.structs.apt.pattern.ElementPatterns.*;
import it.fb.structs.apt.pattern.ITypePattern;
import it.fb.structs.apt.pattern.NamePattern;
import it.fb.structs.apt.pattern.ParseException;
import it.fb.structs.apt.pattern.TypePatterns;
import static it.fb.structs.apt.pattern.TypePatterns.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.SimpleElementVisitor7;
import javax.lang.model.util.TypeKindVisitor7;

/**
 *
 * @author Flavio
 */
public class Parser extends AbstractParser<ExecutableElement> {

    public static PStructDesc parse(TypeElement element) {
        final Parser parser = new Parser(element);
        parser.doParse(element);
        return parser.build();
    }

    private Parser(TypeElement element) {
        super(element.getQualifiedName().toString(), PATTERNS);
    }

    private void doParse(TypeElement element) {
        for (Element enclosed : element.getEnclosedElements()) {
            enclosed.accept(new SimpleElementVisitor7<Void, TypeElement>() {
                @Override
                public Void visitExecutable(ExecutableElement method, TypeElement container) {
                    if (method.getModifiers().contains(Modifier.ABSTRACT)) {
                        addMethod(method);
                    } else if (null != method.getAnnotation(Field.class)) {
                        throw new ParseException("@Field cannot annotate implemented methods: " + method);
                    }
                    return null;
                }
            }, element);
        }
    }

    private final static List<TypeKind> PRIMITIVE_TYPES = Arrays.asList(
            TypeKind.BOOLEAN, TypeKind.BYTE, TypeKind.SHORT, TypeKind.INT, TypeKind.LONG,
            TypeKind.FLOAT, TypeKind.DOUBLE, TypeKind.CHAR);
    private final static List<MethodPattern<ExecutableElement>> PATTERNS = new ArrayList<MethodPattern<ExecutableElement>>();
    
    static {
        for (TypeKind primitiveKind : PRIMITIVE_TYPES) {
            ITypePattern<TypeMirror> primitiveKindPattern = kind(primitiveKind);
            PATTERNS.add(new Getter(primitiveKindPattern));
            PATTERNS.add(new Setter(primitiveKindPattern));
            PATTERNS.add(new ArrayGetter(primitiveKindPattern));
            PATTERNS.add(new ArraySetter(primitiveKindPattern));
        }
        ITypePattern<?> structPattern = TypePatterns.and(
                TypePatterns.kind(TypeKind.DECLARED),
                TypePatterns.typeName(StructPointer.class),
                TypePatterns.withTypeArg(kind(TypeKind.DECLARED)));
        
        PATTERNS.add(new Getter(structPattern));
        PATTERNS.add(new ArrayGetter(structPattern));
    }

    private static final class Getter implements MethodPattern<ExecutableElement> {

        private final AndPattern<ExecutableElement> elementPattern;
        
        public Getter(ITypePattern<?> typePattern) {
            elementPattern = and(nameIs("get(.*)"),
                    returns(typePattern),
                    hasNoArgs());
        }

        public ParsedField match(ExecutableElement method) {
            if (!elementPattern.matches(method)) {
                return null;
            }
            String name = elementPattern.getPattern(NamePattern.class).getMatcherGroup(method, 1);
            ParsedFieldType type = method.getReturnType().accept(ParsedFieldTypeGetter, null);
            return ParsedField.newWithGetter(name, type, method.getAnnotation(Field.class));
        }
    }

    private static final class Setter implements MethodPattern<ExecutableElement> {

        private final AndPattern<ExecutableElement> elementPattern;

        public Setter(ITypePattern<?> typePattern) {
            elementPattern = and(
                    nameIs("set(.*)"), 
                    returns(kind(TypeKind.VOID)),
                    hasArg(typePattern));
        }

        public ParsedField match(ExecutableElement method) {
            if (!elementPattern.matches(method)) {
                return null;
            }
            String name = elementPattern.getPattern(NamePattern.class).getMatcherGroup(method, 1);
            ParsedFieldType type = method.getParameters().get(0).asType().accept(ParsedFieldTypeGetter, null);
            return ParsedField.newWithSetter(name, type, method.getAnnotation(Field.class));
        }
    }

    private static final class ArrayGetter implements MethodPattern<ExecutableElement> {

        private final AndPattern<ExecutableElement> elementPattern;
        
        public ArrayGetter(ITypePattern<?> typePattern) {
            elementPattern = and(
                    nameIs("get(.*)"), 
                    returns(typePattern),
                    hasArg(kind(TypeKind.INT)));
        }

        public ParsedField match(ExecutableElement method) {
            if (!elementPattern.matches(method)) {
                return null;
            }
            String name = elementPattern.getPattern(NamePattern.class).getMatcherGroup(method, 1);
            ParsedFieldType type = method.getReturnType().accept(ParsedFieldTypeGetter, null);
            return ParsedField.newWithGetter(name, type, method.getAnnotation(Field.class));
        }
    }

    private static final class ArraySetter implements MethodPattern<ExecutableElement> {

        private final AndPattern<ExecutableElement> elementPattern;

        public ArraySetter(ITypePattern<?> typePattern) {
            elementPattern = and(
                    nameIs("set(.*)"), 
                    returns(kind(TypeKind.VOID)),
                    hasArgs(kind(TypeKind.INT), typePattern));
        }

        public ParsedField match(ExecutableElement method) {
            if (!elementPattern.matches(method)) {
                return null;
            }
            String name = elementPattern.getPattern(NamePattern.class).getMatcherGroup(method, 1);
            ParsedFieldType type = method.getParameters().get(1).asType().accept(ParsedFieldTypeGetter, null);
            return ParsedField.newWithSetter(name, type, method.getAnnotation(Field.class));
        }
    }

    private static final TypeVisitor<ParsedFieldType, Void> ParsedFieldTypeGetter = new TypeKindVisitor7<ParsedFieldType, Void>() {

        @Override
        public ParsedFieldType visitPrimitiveAsBoolean(PrimitiveType t, Void p) {
            return ParsedFieldType.PFTBoolean;
        }

        @Override
        public ParsedFieldType visitPrimitiveAsByte(PrimitiveType t, Void p) {
            return ParsedFieldType.PFTByte;
        }

        @Override
        public ParsedFieldType visitPrimitiveAsShort(PrimitiveType t, Void p) {
            return ParsedFieldType.PFTShort;
        }

        @Override
        public ParsedFieldType visitPrimitiveAsInt(PrimitiveType t, Void p) {
            return ParsedFieldType.PFTInt;
        }

        @Override
        public ParsedFieldType visitPrimitiveAsLong(PrimitiveType t, Void p) {
            return ParsedFieldType.PFTLong;
        }

        @Override
        public ParsedFieldType visitPrimitiveAsChar(PrimitiveType t, Void p) {
            return ParsedFieldType.PFTChar;
        }

        @Override
        public ParsedFieldType visitPrimitiveAsFloat(PrimitiveType t, Void p) {
            return ParsedFieldType.PFTFloat;
        }

        @Override
        public ParsedFieldType visitPrimitiveAsDouble(PrimitiveType t, Void p) {
            return ParsedFieldType.PFTDouble;
        }

        @Override
        public ParsedFieldType visitDeclared(DeclaredType t, Void p) {
            if (TypePatterns.typeName(StructPointer.class).matches(t)) {
                DeclaredType innerType = (DeclaredType) t.getTypeArguments().get(0);
                return ParsedFieldType.typeOf(((TypeElement)innerType.asElement()).getQualifiedName().toString());
            } else {
                return ParsedFieldType.typeOf(((TypeElement)t.asElement()).getQualifiedName().toString());
            }
        }
    };
}
