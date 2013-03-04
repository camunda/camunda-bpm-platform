package com.camunda.fox.platform.test.util;

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
      .addAsResource("context.xml", "META-INF/context.xml")
      .addAsLibraries(com.camunda.fox.platform.test.util.DeploymentHelper.getWeld())
      .addClass(IntegrationTestProcessApplication.class);
      
        
  }
  
}
