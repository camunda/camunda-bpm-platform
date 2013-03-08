package org.camunda.bpm.engine.rest.impl;

import org.camunda.bpm.engine.rest.EngineService;
import org.camunda.bpm.engine.rest.ProcessDefinitionService;
import org.camunda.bpm.engine.rest.ProcessInstanceService;
import org.camunda.bpm.engine.rest.TaskRestService;

public class EngineServiceImpl implements EngineService {

  @Override
  public ProcessDefinitionService getProcessDefinitionService(String engineName) {
    return new ProcessDefinitionServiceImpl(engineName);
  }

  @Override
  public ProcessInstanceService getProcessInstanceService(String engineName) {
    return new ProcessInstanceServiceImpl(engineName);
  }

  @Override
  public TaskRestService getTaskRestService(String engineName) {
    return new TaskRestServiceImpl(engineName);
  }

}
