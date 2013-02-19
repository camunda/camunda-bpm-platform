package com.camunda.fox.client.impl.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.camunda.bpm.application.ProcessApplicationDeployment;
import org.camunda.bpm.application.impl.deployment.metadata.ProcessesXmlParser;


/**
 * Sets the {@link ProcessesXmlParser#PROP_SERVLET_CONTEXT_PATH} for each
 * {@link ProcessApplicationDeployment} we install
 * 
 * @author Daniel Meyer
 * 
 */
public class ProcessArchiveServletContextListener implements ServletContextListener {

  public void contextInitialized(ServletContextEvent contextEvent) {

    String contextPath = contextEvent.getServletContext().getContextPath();
//    
//    for (ProcessApplicationDeployment pa : processArchiveSupport.getInstalledProcessArchives().keySet()) {
//      Map<String, Object> properties = pa.getProperties();
//      properties.put(ProcessesXmlParser.PROP_SERVLET_CONTEXT_PATH, contextPath);
//    }
    
  }

  public void contextDestroyed(ServletContextEvent arg0) {
  }

}
