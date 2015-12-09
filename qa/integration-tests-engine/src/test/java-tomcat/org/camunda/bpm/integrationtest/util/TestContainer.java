package org.camunda.bpm.integrationtest.util;

import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.RejectDependenciesStrategy;



/**
 * Tomcat test container.
 *
 * @author Daniel Meyer
 */
public class TestContainer {

  public static void addContainerSpecificResources(WebArchive webArchive) {
    webArchive
      .addAsManifestResource("context.xml")
      .addAsLibraries(DeploymentHelper.getWeld())
      .addClass(IntegrationTestProcessApplication.class)
      .addAsWebInfResource("web.xml");
  }

  public static void addContainerSpecificResourcesWithoutWeld(WebArchive webArchive) {
    webArchive
      .addAsManifestResource("context.xml")
      .addClass(IntegrationTestProcessApplication.class)
      .addAsWebInfResource("web-without-weld.xml", "web.xml");
  }

  public static void addContainerSpecificResourcesForNonPa(WebArchive webArchive) {
    webArchive
      .addAsManifestResource("context.xml")
      .addAsLibraries(DeploymentHelper.getWeld())
      .addAsWebInfResource("web.xml");
  }

  public static void addContainerSpecificResourcesForNonPaWithoutWeld(WebArchive webArchive) {
    webArchive
      .addAsManifestResource("context.xml")
      .addAsWebInfResource("web-without-weld.xml", "web.xml");
  }

  public static void addContainerSpecificProcessEngineConfigurationClass(WebArchive deployment) {
    // nothing to do
  }

  public static void addSpinJacksonJsonDataFormat(WebArchive webArchive) {
    webArchive.addAsLibraries(getSpinJacksonJsonDataFormat());
  }

  protected static JavaArchive[] getSpinJacksonJsonDataFormat() {
    return Maven.resolver()
      .offline()
      .loadPomFromFile("pom.xml")
      .resolve("org.camunda.spin:camunda-spin-dataformat-json-jackson")
      .using(new RejectDependenciesStrategy(false,
          "org.camunda.spin:camunda-spin-core",
          "org.camunda.commons:camunda-commons-logging",
          "org.camunda.commons:camunda-commons-utils"))
      .as(JavaArchive.class);
  }


}
