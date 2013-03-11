package org.camunda.bpm.tasklist;

import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.ServletProcessApplication;
import org.camunda.bpm.tasklist.TasklistDemoData;


/**
 * @author: drobisch
 */
@ProcessApplication
public class TasklistProcessApplication extends ServletProcessApplication {

  @PostDeploy
  public void postDeploy() {
    new TasklistDemoData().createDemoData();
  }

}
