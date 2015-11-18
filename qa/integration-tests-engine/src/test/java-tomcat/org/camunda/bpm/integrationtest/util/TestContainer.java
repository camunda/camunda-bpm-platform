package org.camunda.bpm.integrationtest.util;

import org.jboss.shrinkwrap.api.spec.WebArchive;



/**
 * Tomcat test container.
 * 
 * @author Daniel Meyer
 */
public class TestContainer {
  
  public final static String APP_NAME = "";
  public final static String PROCESS_ENGINE_SERVICE_JNDI_NAME = "java:comp/env/ProcessEngineService";
  public final static String PROCESS_APPLICATION_SERVICE_JNDI_NAME = "java:comp/env/ProcessApplicationService";

  public static String getAppName() {
    return APP_NAME;
  }

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
  
}
