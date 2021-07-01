package me.realized.duels.extension;

import com.google.common.io.ByteStreams;
import lombok.Getter;
import me.realized.duels.api.extension.DuelsExtension;
import sun.misc.CompoundEnumeration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class ExtensionClassLoader extends URLClassLoader {

    private final JarFile jar;
    private final Manifest manifest;
    private final URL url;
    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();
    @Getter
    private final DuelsExtension extension;

    ExtensionClassLoader(final File file, final ExtensionInfo info, final ClassLoader parent) throws Exception {
        super(new URL[]{file.toURI().toURL()}, parent);
        this.jar = new JarFile(file);
        this.manifest = jar.getManifest();
        this.url = file.toURI().toURL();

        final Class<?> mainClass = Class.forName(info.getMain(), true, this);

        if (!DuelsExtension.class.isAssignableFrom(mainClass)) {
            throw new RuntimeException(mainClass.getName() + " does not extend DuelsExtension");
        }

        this.extension = mainClass.asSubclass(DuelsExtension.class).newInstance();
    }

    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        Class<?> result = this.classes.get(name);

        if (result == null) {
            final String path = name.replace('.', '/').concat(".class");
            final JarEntry entry = jar.getJarEntry(path);

            if (entry != null) {
                final byte[] classBytes;

                try (InputStream inputStream = jar.getInputStream(entry)) {
                    classBytes = ByteStreams.toByteArray(inputStream);
                } catch (IOException ex) {
                    throw new ClassNotFoundException(name, ex);
                }

                final int dot = name.lastIndexOf('.');

                if (dot != -1) {
                    final String pkgName = name.substring(0, dot);

                    if (getPackage(pkgName) == null) {
                        try {
                            if (manifest != null) {
                                definePackage(pkgName, manifest, url);
                            } else {
                                definePackage(pkgName, null, null, null, null, null, null, null);
                            }
                        } catch (IllegalArgumentException ex) {
                            if (getPackage(pkgName) == null) {
                                throw new IllegalStateException("Cannot find package " + pkgName);
                            }
                        }
                    }
                }

                final CodeSigner[] signers = entry.getCodeSigners();
                final CodeSource source = new CodeSource(url, signers);
                result = defineClass(name, classBytes, 0, classBytes.length, source);

                if (result == null) {
                    result = super.findClass(name);
                }

                classes.put(name, result);
            }
        }

        return result;
    }

    @Override
    public URL getResource(final String name) {
        return findResource(name);
    }

    @Override
    public Enumeration<URL> getResources(final String name) throws IOException {
        @SuppressWarnings("unchecked") final Enumeration<URL>[] tmp = (Enumeration<URL>[]) new Enumeration<?>[2];
        tmp[1] = findResources(name);
        return new CompoundEnumeration<>(tmp);
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            jar.close();
        }
    }
}
