package org.camunda.bpm.pa;

import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.ServletProcessApplication;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;

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
    runtimeService.startProcessInstanceByKey("FailingProcess");
    runtimeService.startProcessInstanceByKey("CallActivity");


    runtimeService.startProcessInstanceByKey("OrderProcess");
    runtimeService.startProcessInstanceByKey("OrderProcess");
    runtimeService.startProcessInstanceByKey("OrderProcess");
    runtimeService.startProcessInstanceByKey("OrderProcess");
    runtimeService.startProcessInstanceByKey("FailingProcess");
    runtimeService.startProcessInstanceByKey("FailingProcess");
    runtimeService.startProcessInstanceByKey("FailingProcess");
    runtimeService.startProcessInstanceByKey("FailingProcess");
    runtimeService.startProcessInstanceByKey("FailingProcess");
    runtimeService.startProcessInstanceByKey("FailingProcess");
    runtimeService.startProcessInstanceByKey("CallActivity");
    runtimeService.startProcessInstanceByKey("CallActivity");
    runtimeService.startProcessInstanceByKey("CallActivity");
    runtimeService.startProcessInstanceByKey("CallActivity");
    runtimeService.startProcessInstanceByKey("CallActivity");

    ((ProcessEngineImpl) engine).getProcessEngineConfiguration().getJobExecutor().start();

  }
}
