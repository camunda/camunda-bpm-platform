package org.camunda.bpm.application.impl.web;

import javax.ejb.EJB;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.impl.ejb.DefaultEjbProcessApplication;


/**
 * <p>Sets the ProcessApplicationInfo.PROP_SERVLET_CONTEXT_PATH property if this is 
 * deployed as part of a WebApplication.</p>
 * 
 * @author Daniel Meyer
 * 
 */
public class ProcessArchiveServletContextListener implements ServletContextListener {
  
  @EJB
  private ProcessApplicationInterface defaultEjbProcessApplication;

  public void contextInitialized(ServletContextEvent contextEvent) {

    String contextPath = contextEvent.getServletContext().getContextPath();
    
    defaultEjbProcessApplication.getProperties().put(ProcessApplicationInfo.PROP_SERVLET_CONTEXT_PATH, contextPath);
    
  }

  public void contextDestroyed(ServletContextEvent arg0) {
  }

}
