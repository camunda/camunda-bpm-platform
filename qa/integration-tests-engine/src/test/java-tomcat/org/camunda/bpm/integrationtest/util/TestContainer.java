package org.camunda.bpm.integrationtest.util;

import org.jboss.shrinkwrap.api.spec.WebArchive;

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
      .addClass(TestProcessApplication.class)
      .addAsWebInfResource("web.xml");
  }

  public static void addContainerSpecificResourcesWithoutWeld(WebArchive webArchive) {
    webArchive
      .addAsManifestResource("context.xml")
      .addClass(TestProcessApplication.class)
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
    webArchive.addAsLibraries(DeploymentHelper.getSpinJacksonJsonDataFormatForServer("tomcat"));
  }

  public static void addJodaTimeJacksonModule(WebArchive webArchive) {
    webArchive.addAsLibraries(DeploymentHelper.getJodaTimeModuleForServer("tomcat"));
  }

  public static void addCommonLoggingDependency(WebArchive webArchive) {
    // nothing to do
  }

}
