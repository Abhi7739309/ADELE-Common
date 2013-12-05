/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE Research Group
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.common.dp.chameleon;


import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.*;
import org.osgi.service.deploymentadmin.BundleInfo;
import org.osgi.service.deploymentadmin.DeploymentAdmin;
import org.osgi.service.deploymentadmin.DeploymentException;
import org.osgi.service.deploymentadmin.DeploymentPackage;
import org.osgi.service.deploymentadmin.spi.ResourceProcessor;
import org.osgi.util.tracker.ServiceTracker;
import org.ow2.chameleon.core.services.AbstractDeployer;
import org.ow2.chameleon.core.services.Deployer;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Implementation of a file installer which is in charge of deployment packages.
 *
 * @author Thomas Leveque
 * 
 */
@Instantiate(name="dp-chameleon-file-installer-1")
@Component(name="dp-chameleon-file-installer")
@Provides(specifications = {Deployer.class})
public class DPFileInstaller extends AbstractDeployer implements BundleListener {

    private final BundleContext context;
    private Map<String, DeploymentPackageStatus> installedStatus = new ConcurrentHashMap<String, DeploymentPackageStatus>();

    private ReadWriteLock lock;

	@Requires
	private DeploymentAdmin _deployAdmin;
	
	private Map<String /* file name */, DeploymentPackage> _installedDps = new ConcurrentHashMap<String, DeploymentPackage>();

    public DPFileInstaller(BundleContext context) {
        lock = new ReentrantReadWriteLock();
        this.context = context;
    }

    @Validate
    public void start(){
        context.addBundleListener(this);
    }

    @Invalidate
    public void stop(){
        context.removeBundleListener(this);
    }



    /**
     * Get the list of valid/invalid DPs
     * @param valid if TRUE, the returned list are valid DPs, otherwise, the invalid DPs are returned
     * @return a list of DPs
     */
    private Set<File> getDpFiles(boolean valid){
        Set<File> invalids = new HashSet<File>();
        lock.readLock().lock();
        try{
        for(String filename: installedStatus.keySet()){
            if (installedStatus.get(filename).isValid() == valid){//
                invalids.add(installedStatus.get(filename).getFile());
            }
        }
        return invalids;
        }finally {
            lock.readLock().unlock();
        }
    }

    public void install(File file) throws Exception {
		FileInputStream fis = new FileInputStream(file);
		String fileName = file.getName();
        DeploymentPackageStatus dps = getDPS(file);
		try {
			DeploymentPackage dp = _deployAdmin.installDeploymentPackage(fis);

			if (dp != null) {
				_installedDps.put(fileName, dp);
			}
            dps.setValid(true);
		} catch (DeploymentException e) {
			e.printStackTrace();
            dps.setValid(false);
		} finally {
			fis.close();
		}
		startBundles();
   }

    private DeploymentPackageStatus getDPS(File file){
        lock.writeLock().lock();
        try{
            if (installedStatus.containsKey(file.getName())){ // if exist return the DPS
                return installedStatus.get(file.getName());
            }
            //if not, we create a new one with default invalid state
            DeploymentPackageStatus dps = new DeploymentPackageStatus(file);
            dps.setValid(false);
            installedStatus.put(file.getName(), dps);
            return dps;
        }finally{
            lock.writeLock().unlock();
        }
    }

	private void startBundles() {
		for (DeploymentPackage dp : _deployAdmin.listDeploymentPackages()) {
			for (BundleInfo bundleInfo : dp.getBundleInfos()) {
				String symbolicName = bundleInfo.getSymbolicName();
				Bundle bundle = dp.getBundle(symbolicName);
				int bundleState = bundle.getState();
				if ((bundleState == Bundle.INSTALLED) || (bundleState == Bundle.RESOLVED))
					try {
						bundle.start();
					} catch (BundleException e) {
						// ignore it
					}
			}
		}
	}

	public void uninstall(File file) throws Exception {
		String fileName = file.getName();
		DeploymentPackage dp = _installedDps.get(fileName);
		if (dp != null) {
			try {
				dp.uninstall();
			} catch (DeploymentException e) {
				e.printStackTrace();
			}
			_installedDps.remove(fileName);
            installedStatus.remove(fileName);
		}	
   }

	public void update(File file) throws Exception {
		FileInputStream fis = new FileInputStream(file);
		String fileName = file.getName();
        DeploymentPackageStatus dps = getDPS(file);
        DeploymentPackage dp = _installedDps.get(fileName);
		try {
			if (dp != null) {
				// do it to ensure that deployment package will be reinstalled if the version remains the same
				try {
					dp.uninstall();
				} catch (DeploymentException e) {
					e.printStackTrace();
				} 
			}
			
			try {
				dp = _deployAdmin.installDeploymentPackage(fis);
                dps.setValid(true);// if it didn't throw an exception, its is valid.
			} catch (DeploymentException e) {
                dps.setValid(false);// if it throw an exception, its is invalid.
                e.printStackTrace();
			}
			if (dp != null) {
				_installedDps.put(fileName, dp);
			}
		} finally {
			fis.close();
		}
		
		startBundles();
   }

    @Override
    public boolean accept(File file) {
        if (file.getName().endsWith(".dp")) {
            return true;
        }
        return false;
    }

    @Override
    public void onFileCreate(File file){
        try {
            install(file);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void onFileDelete(File file){
        try {
            uninstall(file);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void onFileChange(File file){
        try {
            update(file);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void retryInvalidDps(){
        Set<File> invalids = getDpFiles(false);
        for(File file: invalids){
            try {
                install(file);
            } catch (Exception e) {
                e.printStackTrace();
                //TODO log messages
            }
        }
    }

    private void updateValidDps(){
        Set<File> valids = getDpFiles(true);
        for(File file: valids){
            try {
                update(file);
            } catch (Exception e) {
                e.printStackTrace();
                //TODO log messages
            }
        }
    }

    /**
     * To retry invalid DPs, we listen ResourceProcessor services.
     * @param processor
     */
    @Bind
    public void bindResourceProcessor(ResourceProcessor processor){
       retryInvalidDps();
    }

    /**
     * When a resource processor disapears, we update installed DPs.
     * @param processor
     */
    @Unbind
    public void unbindResourceProcessor(ResourceProcessor processor){
     //do nothing.
    }

    /**
     * If a new bundle has passed to to a valid state we retry invalid DPs.
     * @param bundleEvent
     */
    @Override
    public void bundleChanged(BundleEvent bundleEvent) {
        if(bundleEvent.getType() == BundleEvent.STARTED){
            retryInvalidDps();
        }
    }

    /**
     * This class allows to track the validity of DPs., and re-try when.
     */
    private class DeploymentPackageStatus {

        private boolean valid;

        private File file;

        private boolean isValid() {
            return valid;
        }

        private void setValid(boolean valid) {
            this.valid = valid;
        }

        private File getFile() {
            return file;
        }

        private DeploymentPackageStatus(File file) {
            this.file = file;
            setValid(false);
        }
    }

}