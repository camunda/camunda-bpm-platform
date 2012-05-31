package com.camunda.fox.client.impl.web;

import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.camunda.fox.client.impl.ProcessArchiveSupport;
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
  private ProcessArchiveSupport processArchiveSupport;

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
