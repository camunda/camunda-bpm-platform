package org.camunda.bpm.tasklist;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author : drobisch
 */
public class StartUpListener implements ServletContextListener {

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    TasklistProcessEngineProvider.createProcessEngine();
    TasklistProcessEngineProvider.createDemoData();
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
  }

}
