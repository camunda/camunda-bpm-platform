package org.camunda.bpm.integrationtest.util;

import org.jboss.shrinkwrap.api.spec.WebArchive;



/**
 *
 * @author christian.lipphardt
 */
public class TestContainer {

  public static void addContainerSpecificResources(WebArchive archive) {
    addContainerSpecificResourcesWithoutWeld(archive);
  }

  public static void addContainerSpecificResourcesWithoutWeld(WebArchive webArchive) {
    webArchive.addClass(TestProcessApplication.class);
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
}
