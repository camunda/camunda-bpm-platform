package org.camunda.bpm.tasklist;

import org.camunda.bpm.application.impl.ServletProcessApplication;

import javax.servlet.ServletContextEvent;

/**
 * @author: drobisch
 */
public class TasklistProcessApplication extends ServletProcessApplication {

  @Override
  public void postDeploy() {
    TasklistDemoData.createDemoData();
  }

}
