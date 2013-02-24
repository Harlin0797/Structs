package it.fb.structs.apt.pattern;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.SimpleElementVisitor7;

/**
 *
 * @author Flavio
 */
public class TypeNamePattern implements ITypePattern<DeclaredType> {

    private final String packageName;
    private final String name;

    public TypeNamePattern(String packageName, String name) {
        this.packageName = packageName;
        this.name = name;
    }

    public boolean matches(DeclaredType type) {
        if (!type.asElement().getSimpleName().contentEquals(name)) {
            return false;
        }
        return type.asElement().getEnclosingElement().accept(PackageNameEquals, packageName);
    }

    private static final ElementVisitor<Boolean, String> PackageNameEquals = new SimpleElementVisitor7<Boolean, String>() {
        @Override
        protected Boolean defaultAction(Element e, String name) {
            return false;
        }
        @Override
        public Boolean visitPackage(PackageElement e, String name) {
            return e.getQualifiedName().contentEquals(name);
        }
    };
}
