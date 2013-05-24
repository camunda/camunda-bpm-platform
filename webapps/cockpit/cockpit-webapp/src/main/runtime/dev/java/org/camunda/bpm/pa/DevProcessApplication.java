package org.camunda.bpm.pa;

import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.ServletProcessApplication;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;

/**
 *
 * @author nico.rehwaldt
 */
@ProcessApplication("cockpit-test-processes")
public class DevProcessApplication extends ServletProcessApplication {

  @PostDeploy
  public void startProcesses(ProcessEngine engine) {

    RuntimeService runtimeService = engine.getRuntimeService();

    runtimeService.startProcessInstanceByKey("OrderProcess");
    runtimeService.startProcessInstanceByKey("FailingProcess");
    runtimeService.startProcessInstanceByKey("CallActivity");
  }
}
