package org.ow2.chameleon.runner.test;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.apache.commons.io.FileUtils;
import org.junit.runner.Description;
import org.junit.runner.manipulation.*;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.ow2.chameleon.runner.test.internals.ChameleonExecutor;
import org.ow2.chameleon.runner.test.shared.InVivoRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * The Chameleon Test Runner.
 */
public class ChameleonRunner extends BlockJUnit4ClassRunner implements Filterable, Sortable {

    private String ROOTDIR = "target" + File.separator + "distribution" + File.separator;

    private static Logger LOGGER = LoggerFactory.getLogger(ChameleonRunner.class);
    private final ChameleonExecutor executor;
    private final InVivoRunner delegate;
    private final File basedir;

    public ChameleonRunner(Class<?> klass) throws Exception {
        super(klass);
        basedir = checkWisdomInstallation();
        File bundle = detectApplicationBundleIfExist(new File(basedir, "application"));
        if (bundle != null && bundle.exists()) {
            LOGGER.info("Application bundle found in the application directory (" + bundle.getAbsoluteFile() + "), " +
                    "deleting the file to allow test execution");
            bundle.delete();
        }
        bundle = detectApplicationBundleIfExist(new File(basedir, "runtime"));
        if (bundle != null && bundle.exists()) {
            LOGGER.info("Application bundle found in the runtime directory (" + bundle.getAbsoluteFile() + "), " +
                    "deleting the file to allow test execution");
            bundle.delete();
        }

        System.setProperty("application.configuration",
                new File(basedir, "/conf/application.conf").getAbsolutePath());
        executor = ChameleonExecutor.instance(basedir);
        executor.deployProbe();
        //executor.deployDeclaredDependencies(getArtifacts(klass));
        delegate = executor.getInVivoRunnerInstance(klass);
    }

    /**
     * Get a list of methods containing the desired annotation.
     * @param type
     * @param annotation
     * @return
     */
    public static List<Method> getMethodsAnnotatedWith(final Class<?> type, final Class<? extends Annotation> annotation) {
        final List<Method> methods = new ArrayList<Method>();
        Class<?> klass = type;
        while (klass != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
            // iterate though the list of methods declared in the class represented by klass variable, and add those annotated with the specified annotation
            final List<Method> allMethods = new ArrayList<Method>(Arrays.asList(klass.getDeclaredMethods()));
            for (final Method method : allMethods) {
                if (annotation == null || method.isAnnotationPresent(annotation)) {
                    Annotation annotInstance = method.getAnnotation(annotation);
                    methods.add(method);
                }
            }
            // move to the upper class in the hierarchy in search for more methods
            klass = klass.getSuperclass();
        }
        return methods;
    }

    /**
     * Get a list of bundles to deploy in the gateway
     * @param klass
     * @return
     */
    /*
    Artifact[] getArtifacts(Class<?> klass){
        List<Artifact> artifacts = new ArrayList<Artifact>();
        List<Method> methods = getMethodsAnnotatedWith(klass, Configuration.class);

        for(Method method: methods){
            //it must be static method to be called without creating an instance.
            if (!Modifier.isStatic(method.getModifiers())){
                LOGGER.error("Configuration method in test class must be static" + klass.getCanonicalName());
                return new Artifact[0];
            }
            //see if is an array of Artifact
            if(method.getReturnType().getComponentType() != null  && method.getReturnType().getComponentType().equals(Artifact.class)){
                try {
                    Artifact[] declaredArtifacts = (Artifact[]) method.invoke(null, null);
                    if(declaredArtifacts != null && declaredArtifacts.length>0){
                        for(Artifact nartifact : declaredArtifacts){
                            artifacts.add(nartifact);
                        }
                    }
                } catch (IllegalAccessException e) {
                    LOGGER.error("Configuration method invocation in test class throw an exception" + klass.getCanonicalName(), e);
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    LOGGER.error("Configuration method invocation in test class throw an exception" + klass.getCanonicalName(), e);
                    e.printStackTrace();
                }
            }
        }
        return artifacts.toArray( new Artifact[artifacts.size()]);
    }
    */

    /**
     * Detects if the bundle is present in the given directory.
     * The detection stops when a jar file contains a class file from target/classes and where sizes are equals.
     *
     * @param directory the directory to analyze.
     * @return the bundle file if detected.
     * @throws IOException cannot open files.
     */
    private File detectApplicationBundleIfExist(File directory) throws IOException {
        if (!directory.isDirectory()) {
            return null;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return null;
        }

        // Find one entry from classes.
        final File classes = new File("target/classes");
        if (!classes.isDirectory()){
            return null;
        }
        Collection<File> clazzes = FileUtils.listFiles(classes, new String[]{"class"}, true);
        // Transform into classnames but using / and not . as package separator.
        Collection<String> classnames = Collections2.transform(clazzes, new Function<File, String>() {
            @Override
            public String apply(File input) {
                String absolute = input.getAbsolutePath();
                return absolute.substring(classes.getAbsolutePath().length() + 1);
            }
        });

        // Iterate over the set of jar files.
        for (File file : files) {
            if (!file.getName().endsWith("jar")) {
                continue;
            }

            JarFile jar = new JarFile(file);
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    if (classnames.contains(entry.getName())) {
                        // Found !
                        return file;
                    }
                }
            }
        }

        return null;


    }

    private File getRootDir() throws IOException {
        File root = new File(ROOTDIR);
        File[] listOfFiles = root.getAbsoluteFile().listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isDirectory()) {
                return listOfFiles[i]; //it must be only one directory.
            }
        }
        throw new IOException("Unable to locate root directory in: " + root.getName());
    }

    private File checkWisdomInstallation() {
        File directory = null;
        try {
            directory = getRootDir();
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Unable to locate root directory in: " + directory.getName());
        }
        if (!directory.isDirectory()) {
            throw new ExceptionInInitializerError("Wisdom is not installed in " + directory.getAbsolutePath() + " - " +
                    "please check your execution directory, and that Wisdom is prepared correctly. To setup Wisdom, " +
                    "run 'mvn pre-integration-test' from your application directory");
        }
        return directory;
    }

    @Override
    protected Object createTest() throws Exception {
        return delegate.createTest();
    }

    @Override
    public void run(RunNotifier notifier) {
        delegate.run(notifier);
    }

    @Override
    public Description getDescription() {
        return delegate.getDescription();
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        delegate.filter(filter);
    }

    @Override
    public void sort(Sorter sorter) {
        delegate.sort(sorter);
    }
}
