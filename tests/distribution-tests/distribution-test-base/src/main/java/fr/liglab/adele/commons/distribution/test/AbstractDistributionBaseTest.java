package fr.liglab.adele.commons.distribution.test;

import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.repository;
import static org.ops4j.pax.exam.CoreOptions.vmOption;
import static org.ops4j.pax.exam.CoreOptions.when;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;

import org.apache.felix.ipojo.Factory;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.CompositeOption;
import org.ops4j.pax.exam.options.DefaultCompositeOption;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public abstract class AbstractDistributionBaseTest {

	protected int DEBUG_PORT = 9878;

	protected CompositeOption packDebugConfiguration() {
		CompositeOption debugConfig = new DefaultCompositeOption(when(isDebugModeOn()).useOptions(
		      vmOption(String.format("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=%d", DEBUG_PORT))));

		return debugConfig;
	}

	private static boolean isDebugModeOn() {
		RuntimeMXBean RuntimemxBean = ManagementFactory.getRuntimeMXBean();
		List<String> arguments = RuntimemxBean.getInputArguments();

		boolean debugModeOn = false;

		for (String string : arguments) {
			debugModeOn = string.indexOf("jdwp") != -1;
			if (debugModeOn)
				break;
		}

		return debugModeOn;
	}

	/**
	 * Test configuration.
	 * 
	 * @return
	 */
	public List<Option> config() {
		List<Option> config = new ArrayList<Option>();
		config.add(junitBundles());
		config.add(testBaseBundles());
		config.add(packDebugConfiguration());
		config.add(mockitoBundles());
		return config;
	}

	public CompositeOption mockitoBundles() {
		CompositeOption apamCoreConfig = new DefaultCompositeOption(
		// Repository required to load harmcrest (OSGi-fied version).
		      repository("http://repository.springsource.com/maven/bundles/external").id(
		            "com.springsource.repository.bundles.external"),

		      // Repository required to load harmcrest (OSGi-fied version).
		      repository("http://repo1.maven.org/maven2/").id("central"),
		      // Mockito without Hamcrest and Objenesis
		      mavenBundle("org.mockito", "mockito-core", "1.9.5"),
		      // Hamcrest with a version matching the range expected by Mockito
		      mavenBundle("org.hamcrest", "com.springsource.org.hamcrest.core", "1.1.0"),

		      // Objenesis with a version matching the range expected by Mockito
		      wrappedBundle(mavenBundle("org.objenesis", "objenesis", "1.2")).exports("*;version=1.2"),

		      // The default JUnit bundle also exports Hamcrest, but with an (incorrect) version of
		      // 4.9 which does not match the Mockito import. When deployed after the hamcrest bundles, it gets
		      // resolved correctly.
		      CoreOptions.junitBundles(),/*
													 * Felix has implicit boot delegation enabled by default. It conflicts with
													 * Mockito: java.lang.LinkageError: loader constraint violation in interface
													 * itable initialization: when resolving method
													 * "org.osgi.service.useradmin.User$$EnhancerByMockitoWithCGLIB$$dd2f81dc
													 * .newInstance(Lorg/mockito/cglib/proxy/Callback;)Ljava/lang/Object;" the class
													 * loader (instance of org/mockito/internal/creation/jmock/SearchingClassLoader)
													 * of the current class,
													 * org/osgi/service/useradmin/User$$EnhancerByMockitoWithCGLIB$$dd2f81dc, and the
													 * class loader (instance of
													 * org/apache/felix/framework/BundleWiringImpl$BundleClassLoaderJava5) for
													 * interface org/mockito/cglib/proxy/Factory have different Class objects for the
													 * type org/mockito/cglib/ proxy/Callback used in the signature
													 * 
													 * So we disable the bootdelegation. this property has no effect on the other
													 * OSGi implementation.
													 */
		      frameworkProperty("felix.bootdelegation.implicit").value("false"));
		return apamCoreConfig;
	}

	/**
	 * Add the needed bundles to test the platform. Mainly the bundle containing this class.
	 * 
	 * @return
	 */
	protected CompositeOption testBaseBundles() {
		CompositeOption apamCoreConfig = new DefaultCompositeOption(mavenBundle().groupId("fr.liglab.adele.common")
		      .artifactId("base.distribution.test").versionAsInProject());
		return apamCoreConfig;
	}

	@Configuration
	public Option[] configuration() {
		Option conf[] = config().toArray(new Option[0]);
		return conf;
	}

	/**
	 * Waits for stability:
	 * <ul>
	 * <li>all bundles are activated
	 * <li>service count is stable
	 * </ul>
	 * If the stability can't be reached after a specified time, the method throws a {@link IllegalStateException}.
	 * 
	 * @param context the bundle context
	 * @throws IllegalStateException when the stability can't be reach after a several attempts.
	 */
	protected void waitForStability(BundleContext context) throws IllegalStateException {
		// Bundles and service stability is handled by the Framework.
		waitForiPojoFactoriesStability(context);
	}

	/**
	 * Waits for Factories stability:
	 * <ul>
	 * <li>service factories are valid
	 * </ul>
	 * If the stability can't be reached after a specified time, the method throws a {@link IllegalStateException}.
	 * 
	 * @param context the bundle context
	 * @throws IllegalStateException when the stability can't be reach after a several attempts.
	 */
	protected void waitForiPojoFactoriesStability(BundleContext context) throws IllegalStateException {

		int count = 0;
		boolean serviceStability = false;
		int count1 = 0;
		int count2 = 0;
		while (!serviceStability && count < 500) {
			try {
				ServiceReference[] refs = context.getServiceReferences(Factory.class.getName(), null);
                if(refs != null){
				    count1 = refs.length;
                }
				Thread.sleep(50);
				refs = context.getServiceReferences(Factory.class.getName(), "(factory.state=" + Factory.VALID + ")");
                if(refs != null){
				    count2 = refs.length;
                }
				serviceStability = count1 == count2;
			} catch (Exception e) {
				e.printStackTrace();
				serviceStability = false;
				// Nothing to do, while recheck the condition
			}
			count++;
		}

		if (count >= 500) {
			System.err.println("Service stability isn't reached after 500 tries (" + count1 + " != " + count2+")");
			throw new IllegalStateException("Cannot reach the service stability");
		}
	}

	protected Object getService(BundleContext context, Class clazz) {
		ServiceReference sr = context.getServiceReference(clazz.getName());

		if (sr == null) {
			return null;
		}
		Object service = context.getService(sr);
		return service;
	}

	protected Object getService(BundleContext context, Class clazz, String filter) {
		ServiceReference[] references;
		try {
			references = context.getServiceReferences(clazz.getName(), filter);
			if (references == null) {
				return null;
			}
			return context.getService(references[0]);
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

}