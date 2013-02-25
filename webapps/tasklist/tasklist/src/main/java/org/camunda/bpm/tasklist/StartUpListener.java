package org.camunda.bpm.tasklist;

import com.camunda.fox.platform.spi.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.StrongUuidGenerator;

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
