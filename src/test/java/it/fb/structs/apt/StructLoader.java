package it.fb.structs.apt;

import it.fb.structs.StructPointer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.tools.DiagnosticListener;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

/**
 *
 * @author Flavio
 */
public class StructLoader {
    
    private static final JavaCompiler COMPILER = ToolProvider.getSystemJavaCompiler();
    
    private final ClassLoader structLoader;

    private StructLoader(ClassLoader structLoader) {
        this.structLoader = structLoader;
    }
    
    public <T> StructPointer<T> newStructArray(Class<T> struct, int length) {
        try {
            Class<?> simpleStructImpl = structLoader.loadClass(struct.getName() + "Impl");
            Method create = simpleStructImpl.getMethod("create", Integer.TYPE);
            return (StructPointer<T>) create.invoke(simpleStructImpl, length);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error creating array for " + struct, ex);
        }
    }
    
    public static StructLoader create(Class<?>... interfaces) throws IOException {
        DiagnosticListener<JavaFileObject> diagnostic = null;
        MemoryFileManager fm = new MemoryFileManager(COMPILER.getStandardFileManager(diagnostic, null, null));
        List<JavaFileObject> inputFiles = new ArrayList<JavaFileObject>();
        for (Class<?> inputInterface : interfaces) {
            inputFiles.add(fm.getJavaFileForInput(StandardLocation.CLASS_PATH, 
                inputInterface.getName(), JavaFileObject.Kind.SOURCE));
        }
        CompilationTask task = COMPILER.getTask(new PrintWriter(System.err), fm, diagnostic, 
                null, null, inputFiles);
        task.setProcessors(Arrays.asList(new StructAP()));
        if (!task.call()) {
            throw new IllegalStateException("Compilation failed");
        }
        ClassLoader compilationLoader = fm.createClassLoader(Thread.currentThread().getContextClassLoader());
        return new StructLoader(compilationLoader);
    }
    
    private static class MemoryFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

        private final Map<JavaFileObject.Kind, Map<String, MemoryFileObject>> cached 
                = new EnumMap<JavaFileObject.Kind, Map<String, MemoryFileObject>>(JavaFileObject.Kind.class);

        public MemoryFileManager(StandardJavaFileManager baseManager) {
            super(baseManager);
        }

        @Override
        public JavaFileObject getJavaFileForInput(JavaFileManager.Location location, String className, JavaFileObject.Kind kind) throws IOException {
            Map<String, MemoryFileObject> kindCache = cached.get(kind);
            if (kindCache != null) {
                MemoryFileObject ret = kindCache.get(className);
                if (ret != null) {
                    return ret;
                }
            }
            return super.getJavaFileForInput(location, className, kind);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            className = className.replaceAll("/", ".");
            String pathName = className.replaceAll("\\.", "/");
            String path = (pathName.lastIndexOf('/') == -1 ? "" : pathName.substring(0, pathName.lastIndexOf('/')));
            String name = (pathName.lastIndexOf('/') == -1 ? pathName : pathName.substring(pathName.lastIndexOf('/') + 1));
            MemoryFileObject ret = new MemoryFileObject(path, name + ".java", kind);
            Map<String, MemoryFileObject> kindCache = cached.get(kind);
            if (kindCache == null) {
                kindCache = new HashMap<String, MemoryFileObject>();
                cached.put(kind, kindCache);
            }
            kindCache.put(className, ret);
            return ret;
        }

        @Override
        public boolean isSameFile(FileObject a, FileObject b) {
            if ((a instanceof MemoryFileObject) && (b instanceof MemoryFileObject)) {
                return a.equals(b);
            } else {
                return super.isSameFile(a, b);
            }
        }
        
        public ClassLoader createClassLoader(ClassLoader parent) {
            final Map<String, byte[]> classData = new HashMap<String, byte[]>();
            if (cached.containsKey(JavaFileObject.Kind.CLASS)) {
                for (Map.Entry<String, MemoryFileObject> classFile : cached.get(JavaFileObject.Kind.CLASS).entrySet()) {
                    classData.put(classFile.getKey(), classFile.getValue().out.toByteArray());
                }
            }
            return new ClassLoader() {
                @Override
                protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                    try {
                        return super.loadClass(name, resolve);
                    } catch (ClassNotFoundException ex) {
                        byte[] data = classData.get(name);
                        if (data != null) {
                            return super.defineClass(name, data, 0, data.length);
                        } else {
                            throw ex;
                        }
                    }
                }
            };
        }
    }

    private static class MemoryFileObject extends SimpleJavaFileObject {

        private ByteArrayOutputStream out = null;

        public MemoryFileObject(String path, String name, JavaFileObject.Kind kind) {
            super(URI.create(path + "/" + name), kind);
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return new Scanner(openReader(ignoreEncodingErrors)).useDelimiter("\\Z").next();
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return new ByteArrayInputStream(out.toByteArray());
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return out = new ByteArrayOutputStream();
        }

        @Override
        public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
            return new InputStreamReader(openInputStream(), StructAP.ENCODING);
        }

        @Override
        public Writer openWriter() throws IOException {
            return new OutputStreamWriter(openOutputStream(), StructAP.ENCODING);
        }

        @Override
        public int hashCode() {
            return uri.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MemoryFileObject other = (MemoryFileObject) obj;
            return uri.equals(other.uri);
        }
    }
}
