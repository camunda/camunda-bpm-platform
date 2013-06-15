/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.util;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.FileUtils;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;
import org.camunda.bpm.engine.rest.spi.impl.MockedProcessEngineProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

public class WinkTomcatServerBootstrap extends EmbeddedServerBootstrap {

  private static final String DEFAULT_REST_WEB_XML_PATH = "runtime/wink/web.xml";
  
  private Tomcat tomcat;
  private String workingDir = System.getProperty("java.io.tmpdir");
  
  private String webXmlPath;

  public WinkTomcatServerBootstrap() {
    this(DEFAULT_REST_WEB_XML_PATH);
  }
  
  public WinkTomcatServerBootstrap(String webXmlPath) {
    this.webXmlPath = webXmlPath;
  }

  public void start() {
    Properties serverProperties = readProperties();
    int port = Integer.parseInt(serverProperties.getProperty(PORT_PROPERTY));

    tomcat = new Tomcat();
    tomcat.setPort(port);
    tomcat.setBaseDir(workingDir);
    tomcat.getHost().setAppBase(workingDir);
    tomcat.getHost().setAutoDeploy(true);
    tomcat.getHost().setDeployOnStartup(true);

    String contextPath = "/" + getContextPath();
    File webApp = new File(workingDir, getContextPath());
    File oldWebApp = new File(webApp.getAbsolutePath());

    try {
      FileUtils.deleteDirectory(oldWebApp);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    PomEquippedResolveStage resolver = Maven.resolver().loadPomFromFile("pom.xml");

    WebArchive wa = ShrinkWrap.create(WebArchive.class, "rest-test.war").setWebXML(webXmlPath)
        .addAsLibraries(resolver.resolve("org.apache.wink:wink-server:1.1.1-incubating").withTransitivity().asFile())
        .addAsLibraries(resolver.resolve("org.codehaus.jackson:jackson-jaxrs:1.6.5").withTransitivity().asFile())
        .addAsLibraries(resolver.resolve("org.camunda.bpm:camunda-engine").withTransitivity().asFile())
        .addAsLibraries(resolver.resolve("org.mockito:mockito-core").withTransitivity().asFile())

        .addAsServiceProvider(ProcessEngineProvider.class, MockedProcessEngineProvider.class)
        .addPackages(true, "org.camunda.bpm.engine.rest");

    wa.as(ZipExporter.class).exportTo(
        new File(workingDir + "/" + getContextPath() + ".war"), true);

    tomcat.addWebapp(tomcat.getHost(), contextPath, webApp.getAbsolutePath());

    try {
      tomcat.start();
    } catch (LifecycleException e) {
      throw new RuntimeException(e);
    }
  }

  private String getContextPath() {
    return "rest-test";
  }

  public void stop() {
    try {
      if (tomcat.getServer() != null
          && tomcat.getServer().getState() != LifecycleState.DESTROYED) {
        if (tomcat.getServer().getState() != LifecycleState.STOPPED) {
          tomcat.stop();
        }
        tomcat.destroy();
      }
    }catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
