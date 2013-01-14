package com.camunda.fox.client.impl.web;

import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.camunda.bpm.application.spi.ProcessApplication;

import com.camunda.fox.platform.spi.ProcessArchive;

/**
 * Sets the {@link ProcessArchive#PROP_SERVLET_CONTEXT_PATH} for each
 * {@link ProcessArchive} we install
 * 
 * @author Daniel Meyer
 * 
 */
public class ProcessArchiveServletContextListener implements ServletContextListener {

  @Inject
  private ProcessApplication processArchiveSupport;

  public void contextInitialized(ServletContextEvent contextEvent) {

    String contextPath = contextEvent.getServletContext().getContextPath();
    
    for (ProcessArchive pa : processArchiveSupport.getInstalledProcessArchives().keySet()) {
      Map<String, Object> properties = pa.getProperties();
      properties.put(ProcessArchive.PROP_SERVLET_CONTEXT_PATH, contextPath);
    }
    
  }

  public void contextDestroyed(ServletContextEvent arg0) {
  }

}
