package org.camunda.bpm.integrationtest.util;

import org.camunda.bpm.BpmPlatform;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.RejectDependenciesStrategy;



/**
 *
 * @author christian.lipphardt
 */
public class TestContainer {

  public static void addContainerSpecificResources(WebArchive webArchive) {
    addContainerSpecificResourcesWithoutWeld(webArchive);
  }

  public static void addContainerSpecificResourcesWithoutWeld(WebArchive webArchive) {
    webArchive.addAsLibraries(DeploymentHelper.getEjbClient());
  }

  public static void addContainerSpecificResourcesForNonPa(WebArchive webArchive) {
    addContainerSpecificResourcesForNonPaWithoutWeld(webArchive);
  }

  public static void addContainerSpecificResourcesForNonPaWithoutWeld(WebArchive webArchive) {
    webArchive.addAsManifestResource("jboss-deployment-structure.xml");
  }

  public static void addContainerSpecificProcessEngineConfigurationClass(WebArchive deployment) {
    // nothing to do
  }

  public static void addSpinJacksonJsonDataFormat(WebArchive webArchive) {
    webArchive.addAsManifestResource("jboss-deployment-structure-spin-json.xml", "jboss-deployment-structure.xml");
  }

  public static void addJodaTimeJacksonModule(WebArchive webArchive) {
    webArchive.addAsLibraries(getJodaTimeModule());
  }

  protected static JavaArchive[] getJodaTimeModule() {
    return Maven.resolver()
        .offline()
        .loadPomFromFile("pom.xml")
        .resolve("com.fasterxml.jackson.datatype:jackson-datatype-joda")
        .using(new RejectDependenciesStrategy(false,
            "com.fasterxml.jackson.core:jackson-annotations",
            "com.fasterxml.jackson.core:jackson-core",
            "com.fasterxml.jackson.core:jackson-databind"))
        .as(JavaArchive.class);
  }

}