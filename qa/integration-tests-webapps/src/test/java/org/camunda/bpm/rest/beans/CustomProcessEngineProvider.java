package org.camunda.bpm.rest.beans;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;

import java.util.Set;

public class CustomProcessEngineProvider implements ProcessEngineProvider {

  @Override
  public ProcessEngine getDefaultProcessEngine() {
    return BpmPlatform.getDefaultProcessEngine();
  }

  @Override
  public ProcessEngine getProcessEngine(String name) {
    return BpmPlatform.getProcessEngineService().getProcessEngine(name);
  }

  @Override
  public Set<String> getProcessEngineNames() {
    return BpmPlatform.getProcessEngineService().getProcessEngineNames();
  }

}
