/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.util.container;

import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;
import org.camunda.bpm.engine.rest.spi.impl.MockedProcessEngineProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependencies;


public abstract class TomcatServerBootstrap extends EmbeddedServerBootstrap {

  private Tomcat tomcat;
  private String workingDir;
  private String webXmlPath;

  public TomcatServerBootstrap(String webXmlPath) {
    this.webXmlPath = webXmlPath;
  }



  public void start() {
    Properties serverProperties = readProperties();
    int port = Integer.parseInt(serverProperties.getProperty(PORT_PROPERTY));

    tomcat = new Tomcat();
    tomcat.setPort(port);
    tomcat.setBaseDir(getWorkingDir());

    tomcat.getHost().setAppBase(getWorkingDir());
    tomcat.getHost().setAutoDeploy(true);
    tomcat.getHost().setDeployOnStartup(true);

    String contextPath = "/" + getContextPath();

    // 1) Must not use shrinkwrap offline mode (see longer explanation at the end of the file)
    // 2) Must use configuration via Shrinkwrap Maven plugin (see pom.xml for plugin definition);
    //    This forwards things like the location of the settings.xml file to Shrinkwrap. We need
    //    that for our CI where settings.xml is not in the default location.
    PomEquippedResolveStage resolver = Maven.configureResolver()
      .useLegacyLocalRepo(true).configureViaPlugin();

    WebArchive wa = ShrinkWrap.create(WebArchive.class, "rest-test.war").setWebXML(webXmlPath)
        .addAsLibraries(resolver.addDependencies(
            MavenDependencies.createDependency("org.mockito:mockito-core", ScopeType.TEST, false,
            MavenDependencies.createExclusion("org.hamcrest:hamcrest-core"))).resolve()
              .withTransitivity().asFile())

        .addAsServiceProvider(ProcessEngineProvider.class, MockedProcessEngineProvider.class)
        .add(new ClassLoaderAsset("runtime/tomcat/context.xml"), "META-INF/context.xml")
        .addPackages(true, "org.camunda.bpm.engine.rest");

    addRuntimeSpecificLibraries(wa, resolver);
    wa.setWebXML(webXmlPath);

    String webAppPath = getWorkingDir() + "/" + getContextPath() + ".war";

    wa.as(ZipExporter.class).exportTo(new File(webAppPath), true);

    tomcat.addWebapp(tomcat.getHost(), contextPath, webAppPath);

    try {
      tomcat.start();
    } catch (LifecycleException e) {
      throw new RuntimeException(e);
    }
  }

  protected abstract void addRuntimeSpecificLibraries(WebArchive wa, PomEquippedResolveStage resolver);

  private String getContextPath() {
    return "rest-test";
  }

  public void stop() {
    try {
      try {
        tomcat.stop();
      } catch (Exception e) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, "Failed to stop tomcat instance", e);
      }

      try {
        tomcat.destroy();
      } catch (Exception e) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, "Failed to destroy instance", e);
      }

      tomcat = null;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String getWorkingDir() {
    if (workingDir == null) {
      workingDir = System.getProperty("java.io.tmpdir");
    }
    return workingDir;
  }

  public void setWorkingDir(String workingDir) {
    this.workingDir = workingDir;
  }

  /*
   * Must not use Shrinkwrap offline mode or else some Maven builds can fail. In particular, a
   * Maven build will fail with offline mode in the following circumstances:
   *
   * - The BOM is part of the Maven reactor (e.g. if we build the root pom)
   * - The Maven build does not install its modules (e.g. we run mvn clean verify)
   * - There is no BOM snapshot in the local repository yet (this is mostly the case in CI)
   *
   * Why?
   *
   * - The BOM is imported in the dependencyManagement section of this pom (via camunda-parent)
   * - As the BOM is part of the reactor, it will not be fetched from remote by the Maven build
   * - As we do not run "install", Maven does not put the BOM into the local repository
   * - Shrinkwrap will not be able to resolve the BOM then; note: this is different if a pom is referenced
   *   as a parent; in that case, Shrinkwrap will navigate the relativePaths towards the local pom
   *
   * Impact
   *
   * - By enabling remote fetching in Shrinkwrap, a remote snapshot BOM will be fetched in this case
   * - While counter-intuitive, we consider this an ok solution; other solution options were disallowing
   *   "mvn clean verify" in these circumstances (and changing CI builds accordingly), or reverting the
   *   changes made in CAM-11345.
   *
   * This change was made with CAM-11345. See the discussion there for more details.
   */
}
