package org.camunda.bpm.integrationtest.util;

import org.camunda.bpm.BpmPlatform;
import org.jboss.shrinkwrap.api.spec.WebArchive;



/**
 * 
 * @author christian.lipphardt
 */
public class TestContainer {
  
  public final static String APP_NAME = "";
  
  public final static String PROCESS_ENGINE_SERVICE_JNDI_NAME = BpmPlatform.PROCESS_ENGINE_SERVICE_JNDI_NAME;
  public final static String PROCESS_APPLICATION_SERVICE_JNDI_NAME = BpmPlatform.PROCESS_APPLICATION_SERVICE_JNDI_NAME;

  public static String getAppName() {
    return APP_NAME;
  }

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
  
}
