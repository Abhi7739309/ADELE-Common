package fr.liglab.adele.common.distributions.test;

import org.osgi.framework.*;
import org.osgi.framework.launch.Framework;
import org.ow2.chameleon.core.Chameleon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * User: garciai@imag.fr
 * Date: 11/27/13
 * Time: 12:29 PM
 */
public class ChameleonWrapper implements Framework {
    Chameleon chameleon ;

    public ChameleonWrapper(Chameleon cham){
        chameleon = cham;
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
        // Wait for bundle initialization.
        // boolean bundleStability = getBundleStability(context);
        boolean bundleStability = false;
        int count = 0;
        System.out.println("To reach stability");
        while (!bundleStability && count < 50) {
            System.out.println("Waiting for stability " + count);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // Interrupted
            }
            count++;
            bundleStability = getBundleStability(context);
        }

        if (count >= 50) {
            System.err.println("Bundle stability isn't reached after 500 tries");
            showUnstableBundles(context);
            //throw new IllegalStateException("Cannot reach the bundle stability");
        }

        boolean serviceStability = false;
        count = 0;
        int count1 = 0;
        int count2 = 0;
        while (!serviceStability && count < 500) {
            try {
                ServiceReference[] refs = context.getServiceReferences((String) null, null);
                if(refs!=null){
                    count1 = refs.length;
                }
                Thread.sleep(50);
                refs = context.getServiceReferences((String) null, null);
                if(refs!=null){
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
            showUnstableBundles(context);
            throw new IllegalStateException("Cannot reach the service stability");
        }

    }

    /**
     * Are bundle stables.
     *
     * @param bc the bundle context
     * @return <code>true</code> if every bundles are activated.
     */
    private boolean getBundleStability(BundleContext bc) {
        boolean stability = true;
        Bundle[] bundles = bc.getBundles();
        for (int i = 0; i < bundles.length; i++) {
            int state = bundles[i].getState();
            stability = stability && ((state == Bundle.ACTIVE) || (state == Bundle.RESOLVED));
        }
        System.out.println("Total charged bundles " + bundles.length);
        return stability;
    }

    /**
     * Are bundle stables.
     *
     * @param bc the bundle context
     * @return <code>true</code> if every bundles are activated.
     */
    private void showUnstableBundles(BundleContext bc) {
        Bundle[] bundles = bc.getBundles();
        for (int i = 0; i < bundles.length; i++) {
            int state = bundles[i].getState();
            System.err.println("Waiting to stability for: " + bundles[i].getSymbolicName() + " : " + state);
        }
    }

    @Override
    public void init() throws BundleException {
        chameleon.framework().init();
    }

    @Override
    public FrameworkEvent waitForStop(long l) throws InterruptedException {
        return chameleon.framework().waitForStop(l);
    }

    @Override
    public void start() throws BundleException {
        chameleon.start();
        //chameleon.framework().start();
       waitForStability(getBundleContext());
    }

    @Override
    public int getState() {
        return chameleon.framework().getState();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void start(int i) throws BundleException {
        chameleon.framework().start(i);
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void stop() throws BundleException {
        chameleon.framework().stop();//To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void stop(int i) throws BundleException {
        chameleon.framework().stop(i);//To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void uninstall() throws BundleException {
        chameleon.framework().uninstall();
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Dictionary<String, String> getHeaders() {
        return chameleon.framework().getHeaders();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void update() throws BundleException {
        chameleon.framework().update();//To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void update(InputStream inputStream) throws BundleException {
        chameleon.framework().update(inputStream);//To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getBundleId() {
        return chameleon.framework().getBundleId();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getLocation() {
        return chameleon.framework().getLocation();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ServiceReference<?>[] getRegisteredServices() {
        return chameleon.framework().getRegisteredServices();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ServiceReference<?>[] getServicesInUse() {
        return chameleon.framework().getServicesInUse();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasPermission(Object o) {
        return chameleon.framework().hasPermission(o);  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public URL getResource(String s) {
        return chameleon.framework().getResource(s);  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Dictionary<String, String> getHeaders(String s) {
        return chameleon.framework().getHeaders(s);  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getSymbolicName() {
        return chameleon.framework().getSymbolicName();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Class<?> loadClass(String s) throws ClassNotFoundException {
        return chameleon.framework().loadClass(s);  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Enumeration<URL> getResources(String s) throws IOException {
        return chameleon.framework().getResources(s);  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Enumeration<String> getEntryPaths(String s) {
        return chameleon.framework().getEntryPaths(s);  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public URL getEntry(String s) {
        return chameleon.framework().getEntry(s);  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getLastModified() {
        return chameleon.framework().getLastModified();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Enumeration<URL> findEntries(String s, String s2, boolean b) {
        return chameleon.framework().findEntries(s,s2,b);  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public BundleContext getBundleContext() {
        return chameleon.framework().getBundleContext();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<X509Certificate, List<X509Certificate>> getSignerCertificates(int i) {
        return chameleon.framework().getSignerCertificates(i);  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Version getVersion() {
        return chameleon.framework().getVersion();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <A> A adapt(Class<A> aClass) {
        return chameleon.framework().adapt(aClass);  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public File getDataFile(String s) {
        return chameleon.framework().getDataFile(s);  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int compareTo(Bundle bundle) {
        return chameleon.framework().compareTo(bundle);  //To change body of implemented methods use File | Settings | File Templates.
    }
}
