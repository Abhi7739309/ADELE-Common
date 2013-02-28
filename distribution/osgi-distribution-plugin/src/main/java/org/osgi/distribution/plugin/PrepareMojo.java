package org.osgi.distribution.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

/**
 * Goal which prepares a distribution for the zip mojo.
 * 
 * @goal prepare
 * 
 * @phase prepare-package
 * @requiresDependencyResolution
 */
public class PrepareMojo extends AbstractMojo {

	public static String defaultDistribDirectoryName = "osgi-distribution";
	
	public static String defaultDistribDirectoryPath;
	
	/**
	 * The Maven Project Object
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

	/**
	 * The Maven Session Object
	 * 
	 * @parameter expression="${session}"
	 * @required
	 * @readonly
	 */
	protected MavenSession session;

	/**
	 * The Maven PluginManager Object
	 * 
	 * @component
	 * @required
	 */
	protected BuildPluginManager pluginManager;

	/**
	 * Maven plugin output metadata
	 * 
	 * @parameter alias="outputs"
	 * @readonly
	 */
	private List<Output> outputs;

	/**
	 * Execute that mojo.
	 */
	public void execute() throws MojoExecutionException {
			
		defaultDistribDirectoryPath = this.project.getBuild().getDirectory() + File.separator + defaultDistribDirectoryName;
		manageDependencies();
		manageResources();
		giveRights();
	}

	/**
	 * Give executable rights to bat and sh files in distrib directory.
	 */
	private void giveRights() {

		File[] filesInDistrib = new File(defaultDistribDirectoryPath).listFiles(new FilenameFilter() {
			
			public boolean accept(File dir, String name) {
				return (name.endsWith(".bat") || !name.contains("."));
			}
		});
		for (File file : filesInDistrib){
			file.setExecutable(true);
		}
	}

	private void manageResources() throws MojoExecutionException {
		
		Xpp3Dom config = MojoExecutor.configuration(MojoExecutor.element("outputDirectory", defaultDistribDirectoryPath));
		
		MojoExecutor.executeMojo(
				MojoExecutor.plugin("org.apache.maven.plugins",
						"maven-resources-plugin", "2.6"), MojoExecutor
						.goal("resources"), config, MojoExecutor
						.executionEnvironment(project, session, pluginManager));
		
	}

	/**
	 * gather dependencies as dom element.
	 * 
	 * @throws MojoExecutionException
	 */
	private void manageDependencies() throws MojoExecutionException {
		List<Dependency> dependencies = project.getDependencies();

		if (dependencies != null) {
			for (Object depObj : dependencies) {
				if (!(depObj instanceof Dependency))
					continue;

				Dependency dep = (Dependency) depObj;

				if (dep.getType().equals("osgi-distribution")) {
					copyDependency(dep);
					unzipOsgiDistribution(dep);
				} else {
					copyDependency(dep);
				}
			}
		}
	}

	/**
	 * Unzip an osgi distribution dependency into distribution folder.
	 * 
	 * @param dep
	 * @throws MojoExecutionException
	 */
	private void unzipOsgiDistribution(Dependency dep)
			throws MojoExecutionException {

		String zipFinalPathName = defaultDistribDirectoryPath + File.separator
				+ dep.getArtifactId() + "-" + dep.getVersion() + ".zip";
		Xpp3Dom config = MojoExecutor.configuration(
				MojoExecutor.element("from", zipFinalPathName),
				MojoExecutor.element("to", defaultDistribDirectoryPath));

		MojoExecutor.executeMojo(MojoExecutor.plugin("org.codehaus.mojo",
				"truezip-maven-plugin", "1.1"), MojoExecutor.goal("cp"),
				config, MojoExecutor.executionEnvironment(project, session,
						pluginManager));
	}

	/**
	 * Copy the dependency given as parameter into default folder, or given
	 * output folder.
	 * 
	 * @param dep
	 * @throws MojoExecutionException
	 */
	private void copyDependency(Dependency dep) throws MojoExecutionException {

		// prepare dependency plugin config
		boolean foundMatching = false;
		Xpp3Dom items = null;
		items = new Xpp3Dom("artifactItems");
		Xpp3Dom itemAsDom = new Xpp3Dom("artifactItem");
		itemAsDom.addChild(MojoExecutor.element("groupId", dep.getGroupId())
				.toDom());
		itemAsDom.addChild(MojoExecutor.element("artifactId",
				dep.getArtifactId()).toDom());
		itemAsDom.addChild(MojoExecutor.element("version", dep.getVersion())
				.toDom());
		itemAsDom.addChild(MojoExecutor.element("type", dep.getType()).toDom());

		Xpp3Dom config = MojoExecutor.configuration(
				MojoExecutor.element("overWriteSnapshots", "true"),
				MojoExecutor.element("overWriteIfNewer", "true"));

		if (dep.getType().equals("osgi-distribution")) {
			config.addChild(MojoExecutor.element("outputDirectory",
					defaultDistribDirectoryPath).toDom());
		} else {
			if (outputs != null) {
				// check if there is an output entry for that dependency
				for (Output output : outputs) {
					if (dep.getArtifactId().equals(
							output.getIncludesArtifactId())) {
						// found a matching
						foundMatching = true;
						if (output.getOutputFileName() != null) {
							itemAsDom.addChild(MojoExecutor.element(
									"destFileName", output.getOutputFileName())
									.toDom());
						}
						if (output.getDirectory() != null) {
							config.addChild(MojoExecutor.element(
									"outputDirectory",
									defaultDistribDirectoryPath +File.separator + output.getDirectory())
									.toDom());
						}
						// since we cant have 2 outputs for the same dep, break
						// out of the loop
						break;
					}
				}
				if (!foundMatching) {
					config.addChild(MojoExecutor.element("outputDirectory",
							defaultDistribDirectoryPath + File.separator + "load").toDom());
				}
			} else {
				// default : send all dependencies to load folder
				config.addChild(MojoExecutor.element("outputDirectory",
						defaultDistribDirectoryPath + File.separator + "load").toDom());
			}
		}
		items.addChild(itemAsDom);
		config.addChild(items);

		MojoExecutor.executeMojo(
				MojoExecutor.plugin("org.apache.maven.plugins",
						"maven-dependency-plugin", "2.5.1"), MojoExecutor
						.goal("copy"), config, MojoExecutor
						.executionEnvironment(project, session, pluginManager));
	}

}