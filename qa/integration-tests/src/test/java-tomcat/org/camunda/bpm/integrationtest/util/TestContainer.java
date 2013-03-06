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
    
    archive.addAsWebInfResource("web.xml")
      .addAsManifestResource("context.xml")
      .addAsLibraries(DeploymentHelper.getWeld())
      .addClass(IntegrationTestProcessApplication.class);
  }
  
}
