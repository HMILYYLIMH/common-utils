package com.hmilyylimh.cloud.common.jdk.util.clz;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * <h1>类查找器。</h1>
 *
 * @author hmilyylimh
 *         ^_^
 * @version 0.0.1
 *         ^_^
 * @date 2020-08-09
 *
 */
public class ClassFinder {

    public static List<Class> search(String pkg) {
        final List<Class> classes = new ArrayList<>();

        try {
            pkg = pkg.replace('.', '/');
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(pkg);

            for (URL url; resources.hasMoreElements() && ((url = resources.nextElement()) != null);){
                try {
                    String protocol = url.getProtocol();
                    if(protocol.equalsIgnoreCase("jar")){
                        checkJarFile(url, pkg, classes);
                    }else if(protocol.equalsIgnoreCase("file")) {
                        try {
                            checkDirectory(new File(URLDecoder.decode(url.getPath(), "UTF-8")), pkg, classes);
                        }catch (final UnsupportedEncodingException ex){
                            throw new ClassNotFoundException(pkg + " is not valid", ex);
                        }
                    }else {
                        throw new ClassNotFoundException(pkg + " is not valid");
                    }
                }catch (final IOException e){
                    throw new ClassNotFoundException("Failed to operater file.", e);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return classes;
    }

    private static void checkDirectory(File directory, String pkg, List<Class> classes) throws ClassNotFoundException {
        if(!directory.exists()){
            return;
        }
        if(!directory.isDirectory()){
            return;
        }

        File tmpDirectory;
        String[] files = directory.list();
        for (final  String file : files){
            if(file.endsWith(".class")){
                try {
                    String clzFullName = pkg.replace('/', '.') + '.' + file.substring(0, file.length()-6);
                    Class<?> clz = Thread.currentThread().getContextClassLoader().loadClass(clzFullName);
                    classes.add(clz);
                } catch (NoClassDefFoundError e) {
                    e.printStackTrace();
                }
            }else if((tmpDirectory = new File(directory, file)).isDirectory()){
                checkDirectory(tmpDirectory, pkg + File.separator + file, classes);
            }
        }
    }

    private static void checkJarFile(URL url, String pkg, List<Class> classes) throws IOException {
        JarURLConnection conn = (JarURLConnection)url.openConnection();
        JarFile jarFile = conn.getJarFile();
        Enumeration<JarEntry> entries = jarFile.entries();
        String name;

        for (JarEntry jarEntry; entries.hasMoreElements() && ((jarEntry = entries.nextElement()) != null);){
            name = jarEntry.getName();
            if(!name.endsWith(".class")){
                continue;
            }
            if(!name.startsWith(pkg)){
                continue;
            }

            try {
                name = name.replace("/", ".").replace(".class", "");
                classes.add(Class.forName(name));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
