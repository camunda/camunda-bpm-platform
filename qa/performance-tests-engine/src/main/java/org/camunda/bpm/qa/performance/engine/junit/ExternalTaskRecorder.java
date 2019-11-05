package org.camunda.bpm.qa.performance.engine.junit;

import static org.camunda.bpm.qa.performance.engine.steps.PerfTestConstants.TASK_ID;

import org.camunda.bpm.consulting.snippet.engine_plugin_external_task_listener.ExternalTaskListener;
import org.camunda.bpm.consulting.snippet.engine_plugin_external_task_listener.LockingExternalTaskListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestRunContext;

public class ExternalTaskRecorder extends LockingExternalTaskListener implements ExternalTaskListener {

  @Override
  public void workOnTask(String externalTaskId, String workerId, VariableMap variables, VariableMap localVariables,
      ProcessEngineConfigurationImpl processEngineConfiguration) {
    PerfTestRunContext perfTestRunContext = PerfTestRunContext.currentContext.get();
    if(perfTestRunContext != null) {
      perfTestRunContext.setVariable(TASK_ID, externalTaskId);
    }

  }
  
}
