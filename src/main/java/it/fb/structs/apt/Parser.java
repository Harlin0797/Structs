package it.fb.structs.apt;

import it.fb.structs.Field;
import it.fb.structs.StructPointer;
import it.fb.structs.apt.pattern.AndPattern;
import static it.fb.structs.apt.pattern.ElementPatterns.*;
import it.fb.structs.apt.pattern.IElementPattern;
import it.fb.structs.apt.pattern.ITypePattern;
import it.fb.structs.apt.pattern.NamePattern;
import it.fb.structs.apt.pattern.ParseException;
import it.fb.structs.apt.pattern.TypePatterns;
import static it.fb.structs.apt.pattern.TypePatterns.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleElementVisitor7;

/**
 *
 * @author Flavio
 */
public class Parser {
    
    public static PStructDesc parse(TypeElement element) {
        final Parser parser = new Parser(element);
        return parser.buildDesc();
    }
    
    private final Map<String, ParsedField> fields = new HashMap<String, ParsedField>();
    private final Map<String, ParsedField> arrayFields = new HashMap<String, ParsedField>();
    private final TypeElement element;

    private Parser(TypeElement element) {
        this.element = element;
    }

    private PStructDesc buildDesc() {
        for (Element enclosed : element.getEnclosedElements()) {
            enclosed.accept(new SimpleElementVisitor7<Void, TypeElement>() {
                @Override
                public Void visitExecutable(ExecutableElement method, TypeElement container) {
                    addMethod(method, container);
                    return null;
                }
            }, element);
        }
        List<ParsedField> allFields = new ArrayList<ParsedField>();
        allFields.addAll(fields.values());
        allFields.addAll(arrayFields.values());
        Collections.sort(allFields, ParsedField.PositionComparator);
        return new PStructDesc(element, allFields);
    }

    private void addMethod(ExecutableElement method, TypeElement container) {
        Field fieldAnnotation = method.getAnnotation(Field.class);
        if (!method.getModifiers().contains(Modifier.ABSTRACT)) {
            if (fieldAnnotation != null) {
                throw new ParseException("@Field cannot annotate implemented methods: " + method);
            }
            return;
        }

        for (NamedPattern pattern : PATTERNS) {
            if (pattern.matches(method)) {
                try {
                    pattern.callAddProperty(this, method, fieldAnnotation);
                } catch (RuntimeException ex) {
                    throw new IllegalStateException("Error with pattern " + pattern + " on method " + method, ex);
                }
                return;
            }
        }
        
        throw new ParseException("Unrecognized method signature: " + method);
    }

    private void mergeField(Map<String, ParsedField> mergeOnMap, ParsedField newField) {
        ParsedField curField = mergeOnMap.get(newField.name);
        mergeOnMap.put(newField.name, newField.mergeWith(curField));
    }
    
    private void addPropertyGetter(String name, TypeMirror type, ExecutableElement method, Field fieldAnnotation) {
        mergeField(fields, ParsedField.newWithGetter(name, type, fieldAnnotation, method));
    }

    private void addPropertySetter(String name, TypeMirror type, ExecutableElement method, Field fieldAnnotation) {
        mergeField(fields, ParsedField.newWithSetter(name, type, fieldAnnotation, method));
    }

    private void addPropertyArrayGetter(String name, TypeMirror type, ExecutableElement method, Field fieldAnnotation) {
        mergeField(arrayFields, ParsedField.newWithGetter(name, type, fieldAnnotation, method));
    }

    private void addPropertyArraySetter(String name, TypeMirror type, ExecutableElement method, Field fieldAnnotation) {
        mergeField(arrayFields, ParsedField.newWithSetter(name, type, fieldAnnotation, method));
    }
    
    private final static List<TypeKind> PRIMITIVE_TYPES = Arrays.asList(
            TypeKind.BYTE, TypeKind.SHORT, TypeKind.INT, TypeKind.LONG,
            TypeKind.FLOAT, TypeKind.DOUBLE, TypeKind.CHAR);
    private final static List<NamedPattern> PATTERNS = new ArrayList<NamedPattern>();
    
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
                TypePatterns.typeName(StructPointer.class.getPackage().getName(), StructPointer.class.getSimpleName()),
                TypePatterns.withTypeArg(kind(TypeKind.DECLARED)));
        
        PATTERNS.add(new Getter(structPattern));
        PATTERNS.add(new ArrayGetter(structPattern));
    }

    private static abstract class NamedPattern implements IElementPattern<ExecutableElement> {
        
        protected final AndPattern<ExecutableElement> basePattern;

        public NamedPattern(AndPattern<ExecutableElement> basePattern) {
            this.basePattern = basePattern;
        }
        
        public boolean matches(ExecutableElement element) {
            return basePattern.matches(element);
        }
        
        public String getPropertyName(ExecutableElement element) {
            Matcher matcher = basePattern.getPattern(NamePattern.class).matcher(element);
            if (!matcher.matches()) {
                throw new IllegalStateException("Matcher now fails but previously succeded");
            }
            return matcher.group(1);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }

        public abstract void callAddProperty(Parser target, ExecutableElement element, Field fieldAnnotation);
    }
    
    private static final class Getter extends NamedPattern {
        public Getter(ITypePattern<?> typePattern) {
            super(and(
                nameIs("get(.*)"), 
                returns(typePattern),
                hasNoArgs()));
        }

        public void callAddProperty(Parser target, ExecutableElement element,
                Field fieldAnnotation) {
            target.addPropertyGetter(getPropertyName(element), element.getReturnType(), element, fieldAnnotation);
        }
    }

    private static final class Setter extends NamedPattern {
        public Setter(ITypePattern<?> typePattern) {
            super(and(
                nameIs("set(.*)"), 
                returns(kind(TypeKind.VOID)),
                hasArg(typePattern)));
        }

        public void callAddProperty(Parser target, ExecutableElement element,
                Field fieldAnnotation) {
            target.addPropertySetter(getPropertyName(element), element.getParameters().get(0).asType(), element, fieldAnnotation);
        }
    }

    private static final class ArrayGetter extends NamedPattern {
        public ArrayGetter(ITypePattern<?> typePattern) {
            super(and(
                nameIs("get(.*)"), 
                returns(typePattern),
                hasArg(kind(TypeKind.INT))));
        }

        public void callAddProperty(Parser target, ExecutableElement element,
                Field fieldAnnotation) {
            target.addPropertyArrayGetter(getPropertyName(element), element.getReturnType(), element, fieldAnnotation);
        }
    }

    private static final class ArraySetter extends NamedPattern {
        public ArraySetter(ITypePattern<?> typePattern) {
            super(and(
                nameIs("set(.*)"), 
                returns(kind(TypeKind.VOID)),
                hasArgs(kind(TypeKind.INT), typePattern)));
        }

        public void callAddProperty(Parser target, ExecutableElement element,
                Field fieldAnnotation) {
            target.addPropertyArraySetter(getPropertyName(element), element.getParameters().get(1).asType(), element, fieldAnnotation);
        }
    }
}
