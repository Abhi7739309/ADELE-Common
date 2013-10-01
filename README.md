ADELE-Common
============

Defines set of consistent versions of ADELE frameworks and provides tools to generate a ready to use distribution
"a la carte" of adele frameworks.

License
=====

This project relies on Apache v2 license (<http://www.apache.org/licenses/LICENSE-2.0.html>).

Contributors
=====

This project has been created by the LIG laboratory ADELE Research Group of Grenoble University.

Source Organization
====

- _base_: Contain the global build configuration including licensing information.
- _deployment-package-extensions_: Contains plugin components for deployment package admin service.
- _distribution_: Contains OSGi distributions.
- _notice_: Contains files to generate notice files.
- _tests_: Contains integration tests.

Base Distribution
============

The base distribution contains following components :

<table cellpadding="2" cellspacing="0" border="1">
<tr>
  <th>Component</th>
  <th>Maven Artifact</th>
  <th>Version</th>
</tr>
<tr>
  <td>File Install</td>
  <td>org.apache.felix -- org.apache.felix.fileinstall</td>
  <td>3.1.4</td>
</tr>
<tr>
  <td>Gogo Shell</td>
  <td>org.apache.felix -- org.apache.felix.gogo.shell</td>
  <td>0.10.0</td>
</tr>
<tr>
  <td>Gogo Basic Commands</td>
  <td>org.apache.felix -- org.apache.felix.gogo.command</td>
  <td>0.12.0</td>
</tr>
<tr>
  <td>Gogo Command Runtime</td>
  <td>org.apache.felix -- org.apache.felix.gogo.runtime</td>
  <td>0.10.0</td>
</tr>
<tr>
  <td>iPOJO Component Model</td>
  <td>org.apache.felix -- org.apache.felix.ipojo</td>
  <td>1.10.1</td>
</tr>
<tr>
  <td>iPOJO Gogo Commands</td>
  <td>org.apache.felix -- org.apache.felix.ipojo.gogo</td>
  <td>1.10.1</td>
</tr>
<tr>
  <td>Felix Log Service</td>
  <td>org.apache.felix -- org.apache.felix.log</td>
  <td>1.0.1</td>
</tr>
<tr>
  <td>OSGi Compendium</td>
  <td>org.osgi -- org.osgi.compendium</td>
  <td>4.2.0</td>
</tr>
<tr>
  <td>SLF4J API</td>
  <td>org.slf4j -- slf4j-api</td>
  <td>1.6.4</td>
</tr>
<tr>
  <td>SLF4J Log4J Bridge</td>
  <td>org.slf4j -- slf4j-log4j12</td>
  <td>1.6.4</td>
</tr>
<tr>
  <td>Log4J</td>
  <td>org.apache.log4j -- com.springsource.org.apache.log4j</td>
  <td>1.2.16</td>
</tr>
<tr>
  <td>Jetty Web Server</td>
  <td>org.apache.felix -- org.apache.felix.http.jetty</td>
  <td>2.2.0</td>
</tr>
<tr>
  <td>Config Admin</td>
  <td>org.apache.felix -- org.apache.felix.configadmin</td>
  <td>1.2.8</td>
</tr>
<tr>
  <td>Felix Event Admin</td>
  <td>org.apache.felix -- org.apache.felix.eventadmin</td>
  <td>1.3.0</td>
</tr>
<tr>
  <td>IPOJO Composite</td>
  <td>org.apache.felix -- org.apache.felix.ipojo.composite</td>
  <td>1.10.1</td>
</tr>
<tr>
  <td>IPOJO Handler Extender</td>
  <td>org.apache.felix -- org.apache.felix.ipojo.handler.extender</td>
  <td>1.4.0</td>
</tr>
<tr>
  <td>iPOJO Felix Web Console</td>
  <td>org.apache.felix -- org.apache.felix.ipojo.webconsole</td>
  <td>1.7.0</td>
</tr>
<tr>
  <td>Felix Web Console Plugin Events</td>
  <td>org.apache.felix -- org.apache.felix.webconsole.plugins.event</td>
  <td>1.0.2</td>
</tr>
<tr>
  <td>Http Felix Witheboard</td>
  <td>org.apache.felix -- org.apache.felix.http.whiteboard</td>
  <td>2.0.4</td>
</tr>
<tr>
  <td>Felix Preferences</td>
  <td>org.apache.felix -- org.apache.felix.prefs</td>
  <td>1.0.4</td>
</tr>
<tr>
  <td>JSON Service</td>
  <td>org.ow2.chameleon.json -- json-service-json.org</td>
  <td>0.4.0</td>
</tr>
<tr>
  <td>Google Guava</td>
  <td>com.google.guava -- guava</td>
  <td>14.0.1</td>
</tr>
<tr>
  <td>Chameleon Shared Preferences API</td>
  <td>org.ow2.chameleon.sharedprefs -- shared-preferences-service</td>
  <td>0.2.0</td>
</tr>
<tr>
  <td>Chameleon XML Shared Preferences</td>
  <td>org.ow2.chameleon.sharedprefs -- xml-shared-preferences</td>
  <td>0.2.0</td>
</tr>
<tr>
  <td>Deployment Admin Implementation</td>
  <td>de.akquinet.gomobile -- deployment-admin-impl</td>
  <td>1.0.2</td>
</tr>
<tr>
  <td>Deployment Admin File Install Bridge</td>
  <td>de.akquinet.gomobile -- autoconf-resource-processor</td>
  <td>1.0.2</td>
</tr>
<tr>
  <td>Deployment Admin File Install</td>
  <td>fr.liglab.adele.common -- deployment.package.file.install</td>
  <td>1.0.2</td>
</tr>

</table>


Build
=====

Prerequisites
-----

- install Maven 3.x
- install jdk 6 or upper

Instructions
----

Use the following command to compile the project
> mvn clean install

Continuous Integration
----

The project is built every week on the following continuous integration server :
<https://icasa.ci.cloudbees.com/>

Maven Repositories
----

```xml
<!-- Project repositories -->
<repositories>

    <!-- ADELE repositories -->
    <repository>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
        <id>adele-central-snapshot</id>
        <name>adele-repos</name>
        <url>http://maven.dynamis-technologies.com/artifactory/adele-repos</url>
    </repository>
    <repository>
        <snapshots />
        <id>snapshots</id>
        <name>adele-central-release</name>
        <url>http://maven.dynamis-technologies.com/artifactory/adele-repos</url>
    </repository>
</repositories>
<pluginRepositories>
    <pluginRepository>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
        <id>central</id>
        <name>adele-repos</name>
        <url>http://maven.dynamis-technologies.com/artifactory/adele-repos</url>
    </pluginRepository>
    <pluginRepository>
        <snapshots />
        <id>snapshots</id>
        <name>adele-central-release</name>
        <url>http://maven.dynamis-technologies.com/artifactory/adele-repos</url>
    </pluginRepository>
</pluginRepositories>
```

Contribute to this project
====

Released Version semantic
----

 major.minor.revision

 * _major_ changed when there are modification or addition in the functionalities.
 * _minor_ changed when minor features or critical fixes have been added.
 * _revision_ changed when minor bugs are fixed.

Developer Guidelines
----

If you want to contribute to this project, you MUST follow the developper guidelines:
- Use Sun naming convention in your code.
- You should prefix private class member by an underscore (e.g. : _bundleContext).
- All project directory names must be lower case without dots (you can use - instead of underscores).
- All packages must start with fr.liglab.adele.icasa
- All Maven artifact group id must be fr.liglab.adele.icasa
- All maven artifact id must not contain fr.liglab.adele.icasa and must be lower case (cannot use underscore, prefer dots)
- All maven project pom.xml file must inherent from parent pom (group id = fr.liglab.adele.icasa and artifact id = platform-parent)
