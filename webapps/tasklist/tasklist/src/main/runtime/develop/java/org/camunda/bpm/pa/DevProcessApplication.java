
package org.camunda.bpm.pa;


import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.ServletProcessApplication;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.tasklist.TasklistDemoData;

/**
 *
 * @author nico.rehwaldt
 */
@ProcessApplication("tasklist-test-processes")
public class DevProcessApplication extends ServletProcessApplication {

  @PostDeploy
  public void startProcesses(ProcessEngine engine) {

    new TasklistDemoData().createDemoData();
  }
}
