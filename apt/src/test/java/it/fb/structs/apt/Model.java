package it.fb.structs.apt;

import static java.lang.String.*;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import static java.util.Arrays.*;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import static javax.lang.model.element.ElementKind.*;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import static javax.lang.model.element.Modifier.*;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;

/**
 * Copied from 
 * https://bitbucket.org/blob79/quickcheck/src/c8e12cc026fd/quickcheck-src-generator/src/test/java/net/java/quickcheck/srcgenerator/Model.java?at=default
 * Vedi anche
 * http://openjdk.java.net/jeps/119
 * @author Flavio
 */
class Model {

    static TypeElement classElement(String name, Element... methods) {
        return typeElement(name, ElementKind.CLASS,noType(), methods);
    }
    
    static TypeElement interfaceElement(CharSequence name, Element... methods) {
        return interfaceElement(name, noType(), methods);
    }
    
    static TypeElement interfaceElement(CharSequence name, TypeMirror superInterface, Element... methods) {
        return typeElement(name, ElementKind.INTERFACE, superInterface, methods);
    }

    private static TypeElement typeElement(CharSequence name, ElementKind kind,
            TypeMirror superInterface, Element... methods) {
        return typeElement(name(name), kind, superInterface, methods);
    }
    
    static ExecutableElement methodExecutableElement(String name, TypeMirror returnType, VariableElement... parameters) {
        return methodExecutableElement(name(name), METHOD, returnType, EnumSet.of(ABSTRACT, PUBLIC), new TypeParameterElement[0], parameters, new Annotation[0] );
    }

    static ExecutableElement methodExecutableElement(String name, TypeMirror returnType, Annotation annotation,
            VariableElement... parameters) {
        return methodExecutableElement(name(name), METHOD, returnType, EnumSet.of(ABSTRACT, PUBLIC), new TypeParameterElement[0], parameters, new Annotation[] { annotation });
    }
    
    static ExecutableElement methodExecutableElement(final Name name, final ElementKind kind,
            final TypeMirror returnType, final EnumSet<Modifier> modifiers, final TypeParameterElement[] typeParameter, 
            final VariableElement[] parameters, final Annotation[] annotations) {
        return new ExecutableElement() {
            @Override public Name getSimpleName() {
                return name;
            }
            
            @Override public TypeMirror getReturnType() {
                return returnType;
            }
            
            @Override public ElementKind getKind() { 
                return kind; 
            }
            
            @Override public Set<Modifier> getModifiers() { 
                return modifiers; 
            }
            
            @Override public List<? extends VariableElement> getParameters() {
                return asList(parameters);
            }
            @Override public List<? extends TypeParameterElement> getTypeParameters() { 
                return asList(typeParameter);
            }
            @Override public <A extends Annotation> A getAnnotation( Class<A> annotationType) { 
                for (Annotation a : annotations) {
                    if (annotationType.isInstance(a)) {
                        return annotationType.cast(a);
                    }
                }
                return null;
            }
            @Override public <R, P> R accept(ElementVisitor<R, P> v, P p) { 
                return v.visitExecutable(this, p);
            }
            
            @Override
            public String toString() {
                String msg = "MethodExecutableElement[name=%s,kind=%s,returnType%s,modifiers=%s]";
                return format(msg, name, kind, returnType, modifiers);
            }
            
            @Override public boolean equals(Object obj) { throw new UnsupportedOperationException(); }
            @Override public int hashCode() { throw new UnsupportedOperationException(); }
            @Override public AnnotationValue getDefaultValue() { throw new UnsupportedOperationException(); }
            @Override public List<? extends TypeMirror> getThrownTypes() { throw new UnsupportedOperationException(); }
            @Override public boolean isVarArgs() { throw new UnsupportedOperationException(); }
            @Override public TypeMirror asType() { throw new UnsupportedOperationException(); }
            @Override public List<? extends AnnotationMirror> getAnnotationMirrors() { throw new UnsupportedOperationException(); }
            @Override public List<? extends Element> getEnclosedElements() { throw new UnsupportedOperationException(); } 
            @Override public Element getEnclosingElement() { throw new UnsupportedOperationException(); } 
        };
    }
    
    public static PrimitiveType primitiveType(final TypeKind kind){
        return new PrimitiveType(){

            @Override
            public <R, P> R accept(TypeVisitor<R, P> v, P p) { 
                return v.visitPrimitive(this, p);
            }

            @Override
            public TypeKind getKind() {
                return kind;
            }

            @Override
            public boolean equals(Object obj) {
                return kind.equals(((PrimitiveType)obj).getKind());
            }
        };
    }
    
    public static final PrimitiveType IntType = primitiveType(TypeKind.INT);
    public static final PrimitiveType DoubleType = primitiveType(TypeKind.DOUBLE);
    public static final NoType VoidType = new NoType() {
        public TypeKind getKind() {
            return TypeKind.VOID;
        }

        public <R, P> R accept(TypeVisitor<R, P> v, P p) {
            return v.visitNoType(this, p);
        }
        
        @Override
        public boolean equals(Object obj) {
            return TypeKind.VOID.equals(((NoType)obj).getKind());
        }
    };
    
    public static DeclaredType declaredType(final Element element, final TypeMirror... argumentTypes) {
        return new DeclaredType(){
            
            @Override public Element asElement() {
                return element;
            }
            
            @Override public List<? extends TypeMirror> getTypeArguments() {
                return Arrays.asList(argumentTypes);
            }
            
            @Override
            public String toString() {
                return format("DeclareTypeModel[element=%s, argumentTypes=%s]",
                        element, Arrays.toString(argumentTypes));
            }
            
            @Override public <R, P> R accept(TypeVisitor<R, P> v, P p) { 
                return v.visitDeclared(this, p);
            }
            
            @Override public boolean equals(Object obj) { throw new UnsupportedOperationException(); }
            @Override public int hashCode() { throw new UnsupportedOperationException(); }
            
            @Override public TypeKind getKind() { throw new UnsupportedOperationException(); }
            @Override public TypeMirror getEnclosingType() { throw new UnsupportedOperationException(); }
            
        };
    }
    
    static Name name(final CharSequence content) {
        return new Name(){
            @Override
            public boolean contentEquals(CharSequence cs) {
                return cs.toString().equals(content.toString());
            }
            @Override public String toString(){
                return content == null ? "<null>" : content.toString();
            }
            
            @Override public int length(){
                return content.length();
            }

            @Override public char charAt(int index) { 
                return content.charAt(index);
            }
            
            @Override public CharSequence subSequence(int start, int end) { 
                return content.subSequence(start, end);
            }
            
            @Override public boolean equals(Object obj) { throw new UnsupportedOperationException(); }
            @Override public int hashCode() { throw new UnsupportedOperationException(); }
            
            
        };
    }
    
    private static TypeElement typeElement( final Name qualifiedName, final ElementKind elementKind, final TypeMirror superInterface, final Element[] enclosed){
        return new TypeElement(){
            
            @Override public ElementKind getKind() {
                return elementKind;
            }
            
            @Override public List<? extends Element> getEnclosedElements() { 
                return Arrays.asList(enclosed); 
            }
            @Override public Name getQualifiedName() { 
                return qualifiedName;
            }
            
            @Override public Name getSimpleName() {
                return qualifiedName;
            }
            
            @Override public TypeMirror getSuperclass() {
                return noType();
            }

            @Override public List<? extends TypeMirror> getInterfaces(){
                return Collections.singletonList(superInterface);
            }
            
            @Override
            public String toString(){
                return format("TypeElementModel[name=[%s], kind=[%s], enclosed=[%s]]", qualifiedName, elementKind, Arrays.toString(enclosed));
            }
            
            @Override public <R, P> R accept(ElementVisitor<R, P> v, P p) {
                return v.visitType(this, p);
            }
            
            @Override public boolean equals(Object obj) { throw new UnsupportedOperationException(); }
            @Override public int hashCode() { throw new UnsupportedOperationException(); }
            @Override public NestingKind getNestingKind() { throw new UnsupportedOperationException();  }
            @Override public List<? extends TypeParameterElement> getTypeParameters() { throw new UnsupportedOperationException(); }
            @Override public TypeMirror asType() { throw new UnsupportedOperationException(); }
            @Override public <A extends Annotation> A getAnnotation(Class<A> annotationType) { throw new UnsupportedOperationException(); }
            @Override public List<? extends AnnotationMirror> getAnnotationMirrors() { throw new UnsupportedOperationException(); }
            @Override public Element getEnclosingElement() { throw new UnsupportedOperationException(); }
            @Override public Set<Modifier> getModifiers() { throw new UnsupportedOperationException(); }
            
        };
    }

    private static TypeMirror noType(){
        return new NoType() {
            @Override public <R, P> R accept(TypeVisitor<R, P> v, P p) { throw new UnsupportedOperationException(); }
            @Override public TypeKind getKind() { throw new UnsupportedOperationException(); }
        };
    }

    static ArrayType arrayType(final TypeMirror type){
        return new ArrayType(){
            @Override public <R, P> R accept(TypeVisitor<R, P> v, P p) { return v.visitArray(this, p); }
            @Override public TypeMirror getComponentType() { return type; }

            @Override public TypeKind getKind() { throw new UnsupportedOperationException(); } };
    }
    
    static TypeVariable typeVariable(){
        return new TypeVariable() {
            @Override public TypeKind getKind() { throw new UnsupportedOperationException(); }
            @Override public <R, P> R accept(TypeVisitor<R, P> v, P p) { return v.visitTypeVariable(this, p);}
            @Override public TypeMirror getUpperBound() { throw new UnsupportedOperationException(); }
            @Override public TypeMirror getLowerBound() { throw new UnsupportedOperationException(); }
            @Override public Element asElement() { throw new UnsupportedOperationException(); }
        };
    }
    
    static VariableElement var(final CharSequence name, final TypeMirror type){
        return new VariableElement() {
            
            @Override
            public Name getSimpleName() {
                return name(name);
            }
            
            @Override
            public TypeMirror asType() {
                return type;
            }
            
            @Override public Set<Modifier> getModifiers() { throw new UnsupportedOperationException(); }
            @Override public ElementKind getKind() { throw new UnsupportedOperationException(); }
            @Override public Element getEnclosingElement() { throw new UnsupportedOperationException(); } 
            @Override public List<? extends Element> getEnclosedElements() { throw new UnsupportedOperationException(); }
            @Override public List<? extends AnnotationMirror> getAnnotationMirrors() { throw new UnsupportedOperationException(); }
            @Override public <A extends Annotation> A getAnnotation(Class<A> annotationType) { throw new UnsupportedOperationException(); }
            @Override public <R, P> R accept(ElementVisitor<R, P> v, P p) { throw new UnsupportedOperationException(); }
            @Override public Object getConstantValue() { throw new UnsupportedOperationException(); }
        };
    }
    
    static TypeParameterElement typeParameterElement() {
        return typeParameterElement(Collections.<TypeMirror> emptyList());
    }
    
    static TypeParameterElement typeParameterElement(final List<TypeMirror> bounds){
        return new TypeParameterElement(){
            @Override public List<? extends TypeMirror> getBounds() { 
                return bounds;
            } 
            
            @Override public Element getGenericElement() { throw new UnsupportedOperationException(); } 
            @Override public <R, P> R accept(ElementVisitor<R, P> v, P p) { throw new UnsupportedOperationException(); }
            @Override public TypeMirror asType() { throw new UnsupportedOperationException(); }
            @Override public <A extends Annotation> A getAnnotation(Class<A> annotationType) { throw new UnsupportedOperationException(); }
            @Override public List<? extends AnnotationMirror> getAnnotationMirrors() { throw new UnsupportedOperationException(); }
            @Override public List<? extends Element> getEnclosedElements() { throw new UnsupportedOperationException(); }
            @Override public Element getEnclosingElement() { throw new UnsupportedOperationException(); }
            @Override public ElementKind getKind() { throw new UnsupportedOperationException(); }
            @Override public Set<Modifier> getModifiers() { throw new UnsupportedOperationException(); }
            @Override public Name getSimpleName() { throw new UnsupportedOperationException(); } 
        };
    }
}
