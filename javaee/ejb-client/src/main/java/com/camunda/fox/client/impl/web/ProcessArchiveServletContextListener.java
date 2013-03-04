package com.camunda.fox.client.impl.web;

import javax.ejb.EJB;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.camunda.bpm.application.ProcessApplicationInfo;

import com.camunda.fox.client.impl.ProcessArchiveSupport;


/**
 * <p>Sets the ProcessApplicationInfo.PROP_SERVLET_CONTEXT_PATH property if this is 
 * deployed as part of a WebApplication.</p>
 * 
 * @author Daniel Meyer
 * 
 */
public class ProcessArchiveServletContextListener implements ServletContextListener {
  
  @EJB
  private ProcessArchiveSupport processArchiveSupport;

  public void contextInitialized(ServletContextEvent contextEvent) {

    String contextPath = contextEvent.getServletContext().getContextPath();
    
    processArchiveSupport.getProperties().put(ProcessApplicationInfo.PROP_SERVLET_CONTEXT_PATH, contextPath);
    
  }

  public void contextDestroyed(ServletContextEvent arg0) {
  }

}
