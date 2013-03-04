package it.fb.structs.apt;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JVar;
import it.fb.structs.Struct;
import it.fb.structs.StructArray;
import it.fb.structs.StructPointer;
import it.fb.structs.core.PFieldTypeVisitor;
import it.fb.structs.core.PStructDesc;
import it.fb.structs.core.ParseException;
import it.fb.structs.core.ParsedField;
import it.fb.structs.core.Tarjan;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.SimpleElementVisitor7;
import javax.tools.Diagnostic;

/**
 *
 * @author Flavio
 */
@SupportedAnnotationTypes({"it.fb.structs.Struct"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class StructAP extends AbstractProcessor {
    
    public  final static Charset ENCODING = Charset.forName("UTF-8");
    private final int alignment = 4;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Map<String, PStructDesc> structDescriptors = new HashMap<String, PStructDesc>();
        for (Element elem : roundEnv.getElementsAnnotatedWith(Struct.class)) {
            try {
                process(elem, structDescriptors);
            } catch (IOException ex) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ex.toString(), elem);
            }
        }
        
        List<PStructDesc> sortedStructs = Tarjan.topologicalSort(structDescriptors.values(), new Tarjan.Dependents<PStructDesc>() {
            public Iterable<PStructDesc> getDependents(PStructDesc node) {
                List<PStructDesc> ret = new ArrayList<PStructDesc>();
                for (String innerType : node.getInnerTypes()) {
                    PStructDesc innerDesc = structDescriptors.get(innerType);
                    if (innerDesc == null) {
                        throw new ParseException("Unknown type " + innerType + " referred by " + node.getJavaInterface());
                    }
                    ret.add(innerDesc);
                }
                return ret;
            }
        });
        
        Map<String, Integer> structSizes = new HashMap<String, Integer>();
        JCodeModel cm = new JCodeModel();
        for (PStructDesc structDesc : sortedStructs) {
            try {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generating implementation: " + structDesc.getJavaInterface());
                Generator generator = new Generator(structDesc, cm, alignment, structSizes);
                generator.generate();
                structSizes.put(structDesc.getJavaInterface(), generator.getStructSize());
            } catch (JClassAlreadyExistsException ex) {
                throw new IllegalStateException(ex);
            }
        }

        writeClasses(cm);

        return (annotations.size() == 1 && annotations.iterator().next().getQualifiedName().contentEquals(Struct.class.getName()));
    }
    
    private PStructDesc process(Element annotatedElement, Map<String, PStructDesc> map) throws IOException {
        return annotatedElement.accept(new SimpleElementVisitor7<PStructDesc, Map<String, PStructDesc>>() {
            @Override
            protected PStructDesc defaultAction(Element e, Map<String, PStructDesc> map) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Can not have @Struct annotation", e);
                return null;
            }
            
            @Override
            public PStructDesc visitType(TypeElement e, Map<String, PStructDesc> map) {
                try {
                    PStructDesc desc = Parser.parse(e);
                    map.put(e.getQualifiedName().toString(), desc);
                    return desc;
                } catch (ParseException ex) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ex.getMessage(), e);
                    return null;
                }
            }
        }, map);
    }
    
    private void writeClasses(JCodeModel cm) {
        try {
            cm.build(new CodeWriter() {
                {
                    this.encoding = ENCODING.name();
                }
                @Override
                public OutputStream openBinary(JPackage pkg, String fileName) throws IOException {
                    if (fileName.endsWith(".java")) {
                        fileName = fileName.substring(0, fileName.length() - 5);
                    }
                    if (pkg.isUnnamed()) {
                        return processingEnv.getFiler()
                                .createSourceFile(fileName)
                                .openOutputStream();
                    } else {
                        return processingEnv.getFiler()
                                .createSourceFile(pkg.name() + "." + fileName)
                                .openOutputStream();
                    }
                }

                @Override
                public void close() {
                }
            });
        } catch (IOException ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ex.toString());
        }
    }

    private static class Generator implements PFieldTypeVisitor<Void, ParsedField> {
        
        private final PStructDesc desc;
        private final JCodeModel cm;
        private final int alignment;
        private final Map<String, Integer> structSizes;
        
        private JClass intfClass;
        private JDefinedClass implClass;
        private JFieldVar size;
        private JFieldVar baseAddress;
        private JFieldVar length;
        private JFieldVar baseOffset;
        private JFieldVar curAddress;
        private JBlock isConstructorBlock;
        private JBlock isAtBlock;
        private JVar atCurOffset;
        private int fieldOffset;

        public Generator(PStructDesc desc, JCodeModel cm, int alignment, Map<String, Integer> structSizes) {
            this.desc = desc;
            this.cm = cm;
            this.alignment = alignment;
            this.structSizes = structSizes;
        }

        private void generate() throws JClassAlreadyExistsException {
            String intfName = desc.getJavaInterface();
            String implName = intfName + "Impl";
            intfClass = cm.ref(intfName);
            implClass = cm._class(JMod.PUBLIC, implName, ClassType.CLASS);
            implClass._implements(cm.ref(StructPointer.class).narrow(intfClass));
            implClass._implements(intfClass);
            generateUnsafe();
            generateVariables();
            generateConstructor();
            generateGet();
            generateAt();
            generateLength();
            generateStructSize();
            generateIndex();
            generatePin();
            generateDuplicate();
            generateSetBaseOffset();
            generateGetOwner();
            generateRelease();
            
            for (ParsedField field : desc.getFields()) {
                field.type.accept(this, field);
            }

            size.init(JExpr.lit(fieldOffset));
            
            generateCreate();
        }
        
        public int getStructSize() {
            return fieldOffset;
        }
        
        private int align(int fieldLen) {
            if (fieldLen % alignment != 0) {
                return fieldLen + alignment - (fieldLen % alignment);
            } else {
                return fieldLen;
            }
        }

        public Void visitStruct(String typeName, ParsedField p) {
            JClass innerIntfClass = cm.ref(typeName);
            JClass innerImplClass = cm.ref(typeName + "Impl");
            
            // Field generation
            JFieldVar ptrField = implClass.field(JMod.PRIVATE + JMod.FINAL, innerImplClass, "_" + p.name);
            
            // Constructor initialization
            isConstructorBlock.assign(ptrField, JExpr._new(innerImplClass)
                    .arg(baseAddress)
                    .arg(JExpr.lit((p.arrayLength == 0 ? 1 : p.arrayLength)))
                    .arg(JExpr.lit(fieldOffset))
                    .arg(JExpr.lit(0)));
            
            // Getter
            if (p.isArray()) {
                JMethod getter = implClass.method(JMod.PUBLIC, cm.ref(StructPointer.class).narrow(innerIntfClass), "get" + p.name);
                JVar index = getter.param(cm.INT, "index");
                getter.body().invoke(ptrField, "at").arg(index);
                getter.body()._return(ptrField);
            } else {
                JMethod getter = implClass.method(JMod.PUBLIC, cm.ref(StructPointer.class).narrow(innerIntfClass), "get" + p.name);
                getter.body()._return(ptrField);
            }
            
            // At initialization
            isAtBlock.invoke(ptrField, "setBaseOffset")
                    .arg(atCurOffset.plus(JExpr.lit(fieldOffset)));
            
            if (!structSizes.containsKey(typeName)) {
                throw new IllegalStateException("Unknown interface: " + typeName);
            }
            fieldOffset += structSizes.get(typeName) * (p.isArray() ? p.arrayLength : 1);
            return null;
        }

        public Void visitBoolean(ParsedField parameter) {
            throw new UnsupportedOperationException("TODO: boolean");
        }

        @Override
        public Void visitByte(ParsedField field) {
            return generatePrimitiveAccessors(field, cm.BYTE, "getByte", "putByte");
        }

        @Override
        public Void visitShort(ParsedField field) {
            return generatePrimitiveAccessors(field, cm.SHORT, "getShort", "putShort");
        }

        @Override
        public Void visitInt(ParsedField field) {
            return generatePrimitiveAccessors(field, cm.INT, "getInt", "putInt");
        }

        @Override
        public Void visitLong(ParsedField field) {
            return generatePrimitiveAccessors(field, cm.LONG, "getLong", "putLong");
        }

        @Override
        public Void visitChar(ParsedField field) {
            return generatePrimitiveAccessors(field, cm.CHAR, "getChar", "putChar");
        }

        @Override
        public Void visitFloat(ParsedField field) {
            return generatePrimitiveAccessors(field, cm.FLOAT, "getFloat", "putFloat");
        }

        @Override
        public Void visitDouble(ParsedField field) {
            return generatePrimitiveAccessors(field, cm.DOUBLE, "getDouble", "putDouble");
        }

        private Void generatePrimitiveAccessors(ParsedField field, JPrimitiveType type,
                String unsafeGetterName, String unsafeSetterName) {
            if (field.isArray()) {
                generateArrayGetter(field.name, type, unsafeGetterName, field.type.getSize());
                generateArraySetter(field.name, type, unsafeSetterName, field.type.getSize());
                fieldOffset += align(field.type.getSize() * field.arrayLength);
            } else {
                generateGetter(field.name, type, unsafeGetterName);
                generateSetter(field.name, type, unsafeSetterName);
                fieldOffset += align(field.type.getSize());
            }
            return null;
        }
        
        private void generateGetter(String fieldName, JPrimitiveType type, String unsafeGetterName) {
            JMethod getter = implClass.method(JMod.PUBLIC, type, "get" + fieldName);
            getter.body()._return(JExpr.ref("TheUnsafe")
                    .invoke(unsafeGetterName)
                    .arg(curAddress.plus(JExpr.lit(fieldOffset))));
        }

        private void generateSetter(String fieldName, JPrimitiveType type, String unsafeSetterName) {
            JMethod setter = implClass.method(JMod.PUBLIC, cm.VOID, "set" + fieldName);
            JVar value = setter.param(type, "value");
            setter.body().invoke(JExpr.ref("TheUnsafe"), unsafeSetterName)
                    .arg(curAddress.plus(JExpr.lit(fieldOffset)))
                    .arg(value);
        }
        
        private void generateArrayGetter(String fieldName, JPrimitiveType type, String unsafeGetterName, int typeSize) {
            JMethod getter = implClass.method(JMod.PUBLIC, type, "get" + fieldName);
            JVar index = getter.param(cm.INT, "index");
            getter.body()._return(JExpr.ref("TheUnsafe")
                    .invoke(unsafeGetterName)
                    .arg(curAddress
                        .plus(JExpr.lit(fieldOffset))
                        .plus(index.mul(JExpr.lit(typeSize)))));
        }

        private void generateArraySetter(String fieldName, JPrimitiveType type, String unsafeSetterName, int typeSize) {
            JMethod setter = implClass.method(JMod.PUBLIC, cm.VOID, "set" + fieldName);
            JVar index = setter.param(cm.INT, "index");
            JVar value = setter.param(type, "value");
            setter.body().invoke(JExpr.ref("TheUnsafe"), unsafeSetterName)
                    .arg(curAddress
                        .plus(JExpr.lit(fieldOffset))
                        .plus(index.mul(JExpr.lit(typeSize))))
                    .arg(value);
        }

        //<editor-fold defaultstate="collapsed" desc="Generazione metodi accessori">
        
        private void generateUnsafe() {
            implClass.direct(
                    "    protected final static sun.misc.Unsafe TheUnsafe; \n" +
                    "    static {\n" +
                    "        try {\n" +
                    "            java.lang.reflect.Field field = sun.misc.Unsafe.class.getDeclaredField(\"theUnsafe\");\n" +
                    "            field.setAccessible(true);\n" +
                    "            TheUnsafe = (sun.misc.Unsafe) field.get(null);\n" +
                    "        } catch (Exception e) {\n" +
                    "            throw new ExceptionInInitializerError(e);\n" +
                    "        }\n" +
                    "    }\n");
        }
        
        private void generateVariables() {
            size = implClass.field(JMod.PUBLIC + JMod.STATIC + JMod.FINAL, cm.INT, "SIZE");
            baseAddress = implClass.field(JMod.PRIVATE + JMod.FINAL, cm.LONG, "baseAddress");
            length = implClass.field(JMod.PRIVATE + JMod.FINAL, cm.INT, "length");
            baseOffset = implClass.field(JMod.PRIVATE, cm.INT, "baseOffset");
            curAddress = implClass.field(JMod.PRIVATE, cm.LONG, "curAddress");
        }
        
        private JMethod generateConstructor() {
            JMethod constructor = implClass.constructor(JMod.PUBLIC);
            JVar vBaseAddress = constructor.param(cm.LONG, "_baseAddress");
            JVar vLength = constructor.param(cm.INT, "_length");
            JVar vBaseOffset = constructor.param(cm.INT, "_baseOffset");
            JVar vIndex = constructor.param(cm.INT, "_index");
            
            constructor.body().assign(baseAddress, vBaseAddress);
            constructor.body().assign(length, vLength);
            constructor.body().assign(baseOffset, vBaseOffset);
            isConstructorBlock = constructor.body().block();
            constructor.body().invoke("at").arg(vIndex);
            
            return constructor;
        }
        
        private void generateGet() {
            JMethod at = implClass.method(JMod.PUBLIC + JMod.FINAL, intfClass, "get");
            at.body()._return(JExpr._this());
        }
        
        private void generateAt() {
            JMethod at = implClass.method(JMod.PUBLIC + JMod.FINAL, implClass, "at");
            JVar index = at.param(cm.INT, "index");
            atCurOffset = at.body().decl(cm.INT, "curOffset", index.mul(size).plus(baseOffset));
            at.body().assign(curAddress, baseAddress.plus(atCurOffset));
            isAtBlock = at.body().block();
            at.body()._return(JExpr._this());
        }
        
        private void generateLength() {
            implClass.method(JMod.PUBLIC + JMod.FINAL, cm.INT, "length").body()._return(length);
        }
        
        private void generateStructSize() {
            implClass.method(JMod.PUBLIC + JMod.FINAL, cm.INT, "structSize").body()._return(size);
        }
        
        private void generateIndex() {
            implClass.method(JMod.PUBLIC + JMod.FINAL, cm.INT, "index").body()._return(
                    JExpr.cast(cm.INT,
                    curAddress.minus(baseAddress).minus(baseOffset).div(size)));
        }
        
        private void generatePin() {
            implClass.method(JMod.PUBLIC + JMod.FINAL, intfClass, "pin").body()._return(
                    JExpr.invoke("duplicate"));
        }
        
        private void generateDuplicate() {
            implClass.method(JMod.PUBLIC + JMod.FINAL, implClass, "duplicate").body()._return(
                    JExpr._new(implClass)
                    .arg(baseAddress)
                    .arg(length)
                    .arg(baseOffset)
                    .arg(JExpr.invoke("index")));
        }
        
        private void generateSetBaseOffset() {
            JMethod setBaseOffset = implClass.method(JMod.PUBLIC + JMod.FINAL, cm.VOID, "setBaseOffset");
            JVar vBaseOffset = setBaseOffset.param(cm.INT, "_baseOffset");
            setBaseOffset.body().assign(baseOffset, vBaseOffset);
            setBaseOffset.body().invoke("at").arg(JExpr.lit(0));
        }
        
        private void generateGetOwner() {
            JMethod getOwner = implClass.method(JMod.PUBLIC + JMod.FINAL, cm.ref(StructArray.class).narrow(intfClass), "getOwner");
            getOwner.body()._throw(JExpr._new(cm.ref(UnsupportedOperationException.class)));
        }
        
        private void generateRelease() {
            JMethod release = implClass.method(JMod.PUBLIC + JMod.FINAL, cm.VOID, "release");
            release.body().invoke(JExpr.ref("TheUnsafe"), "freeMemory")
                    .arg(baseAddress);
        }

        private void generateCreate() {
            JMethod create = implClass.method(JMod.PUBLIC + JMod.STATIC, cm.ref(StructPointer.class).narrow(intfClass), "create");
            JVar vLength = create.param(cm.INT, "length");
            JVar vBaseAddress = create.body().decl(cm.LONG, "baseAddress",
                    JExpr.ref("TheUnsafe").invoke("allocateMemory")
                    .arg(size.mul(vLength)));
            create.body()._return(JExpr._new(implClass)
                    .arg(vBaseAddress)
                    .arg(vLength)
                    .arg(JExpr.lit(0))
                    .arg(JExpr.lit(0)));
        }
        //</editor-fold>
    }
}
