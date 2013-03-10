package org.camunda.bpm.integrationtest.util;

import org.jboss.shrinkwrap.api.spec.WebArchive;



/**
 * 
 * @author christian.lipphardt
 */
public class TestContainer {
  
  public final static String APP_NAME = "";
  
  public static void addContainerSpecificResources(WebArchive archive) {
    
    archive.addAsLibraries(DeploymentHelper.getEjbClient());
        
  }
  public static void addContainerSpecificResourcesForNonPa(WebArchive webArchive) {
    webArchive.addAsManifestResource("jboss-deployment-structure.xml");
    
  }
  
}
