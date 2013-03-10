package org.camunda.bpm.integrationtest.util;

import org.jboss.shrinkwrap.api.spec.WebArchive;



/**
 * Tomcat test container.
 * 
 * @author Daniel Meyer
 */
public class TestContainer {
  
  public final static String APP_NAME = "";
  
  public static void addContainerSpecificResources(WebArchive archive) {
    
    archive
      .addAsManifestResource("context.xml")
      .addAsLibraries(DeploymentHelper.getWeld())
      .addClass(IntegrationTestProcessApplication.class)
      .addAsWebInfResource("web.xml");
  }

  public static void addContainerSpecificResourcesForNonPa(WebArchive deployment) {
    
    deployment
    .addAsManifestResource("context.xml")
    .addAsLibraries(DeploymentHelper.getWeld())
    .addAsWebInfResource("web.xml");
    
  }
  
}
