package org.camunda.bpm.engine.rest.util;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;
import org.camunda.bpm.engine.rest.spi.impl.MockedProcessEngineProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    File webApp = new File(getWorkingDir(), getContextPath());

    PomEquippedResolveStage resolver = Maven.resolver().loadPomFromFile("pom.xml");

    WebArchive wa = ShrinkWrap.create(WebArchive.class, "rest-test.war").setWebXML(webXmlPath)
        .addAsLibraries(resolver.resolve("org.codehaus.jackson:jackson-jaxrs:1.6.5").withTransitivity().asFile())
        .addAsLibraries(resolver.resolve("org.camunda.bpm:camunda-engine").withTransitivity().asFile())
        .addAsLibraries(resolver.resolve("org.mockito:mockito-core").withTransitivity().asFile())

        .addAsServiceProvider(ProcessEngineProvider.class, MockedProcessEngineProvider.class)
        .addPackages(true, "org.camunda.bpm.engine.rest");

    addRuntimeSpecificLibraries(wa, resolver);
    wa.setWebXML(webXmlPath);

    wa.as(ZipExporter.class).exportTo(
        new File(getWorkingDir() + "/" + getContextPath() + ".war"), true);

    Context ctx = tomcat.addWebapp(tomcat.getHost(), contextPath, webApp.getAbsolutePath());

    // add anti-locking config to avoid locked files
    // on windows systems
    try {
      ctx.setConfigFile(new File("src/test/resources/runtime/tomcat/context.xml").toURI().toURL());
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }

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

}
