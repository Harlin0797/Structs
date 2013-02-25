package it.fb.structs.apt;

import it.fb.structs.Field;
import it.fb.structs.apt.pattern.ParseException;
import java.util.Comparator;
import javax.lang.model.type.TypeMirror;

/**
 *
 * @author Flavio
 */
public final class ParsedField {

    public final String name;
    public final TypeMirror type;
    public final int arrayLength;
    public final int position;
    public final boolean hasGetter;
    public final boolean hasSetter;

    public ParsedField(String name, TypeMirror type, int arrayLength, int position, boolean hasGetter, boolean hasSetter) {
        this.name = name;
        this.type = type;
        this.arrayLength = arrayLength;
        this.position = position;
        this.hasGetter = hasGetter;
        this.hasSetter = hasSetter;
    }

    public boolean isArray() {
        return arrayLength > 0;
    }
    
    ParsedField mergeWith(ParsedField other) {
        if (other == null) {
            return this;
        }
        if (!this.name.equals(other.name)) {
            throw new IllegalArgumentException(String.format("Names differ: '%s' and '%s'", this.name, other.name));
        }
        if ((other.hasGetter) == (this.hasGetter)) {
            throw new IllegalStateException("Mismatched getters");
        }
        if ((other.hasSetter) == (this.hasSetter)) {
            throw new IllegalStateException("Mismatched setters");
        }
        if (!type.equals(other.type)) {
            throw new ParseException("Different types for getter and setter of field " + name);
        }
        if (this.arrayLength != 0 && other.arrayLength != 0 && this.arrayLength != other.arrayLength) {
            throw new ParseException("Array length mismatch on getter and setter of field " + name);
        }
        if (this.position != Integer.MAX_VALUE && other.arrayLength != Integer.MAX_VALUE && this.arrayLength != other.arrayLength) {
            throw new ParseException("Position length mismatch on getter and setter of field " + name);
        }
        return new ParsedField(name, type, this.arrayLength == 0 ? other.arrayLength : this.arrayLength, this.position == Integer.MAX_VALUE ? other.position : this.position, this.hasGetter || other.hasGetter, this.hasSetter || other.hasSetter);
    }

    static ParsedField newWithGetter(String name, TypeMirror type, Field annotation) {
        if (annotation == null) {
            return new ParsedField(name, type, 0, Integer.MAX_VALUE, true, false);
        } else {
            return new ParsedField(name, type, annotation.length(), annotation.position(), true, false);
        }
    }

    static ParsedField newWithSetter(String name, TypeMirror type, Field annotation) {
        if (annotation == null) {
            return new ParsedField(name, type, 0, Integer.MAX_VALUE, false, true);
        } else {
            return new ParsedField(name, type, annotation.length(), annotation.position(), false, true);
        }
    }

    @Override
    public String toString() {
        return name + " " + type + (arrayLength > 0 ? "[" + arrayLength + "]" : "");
    }

    public static final Comparator<ParsedField> PositionComparator = new Comparator<ParsedField>() {
        public final int compare(ParsedField o1, ParsedField o2) {
            return compare(o1.position, o2.position);
        }
        private int compare(int x, int y) {
            return (x < y) ? -1 : ((x == y) ? 0 : 1);
        }
    };
}
