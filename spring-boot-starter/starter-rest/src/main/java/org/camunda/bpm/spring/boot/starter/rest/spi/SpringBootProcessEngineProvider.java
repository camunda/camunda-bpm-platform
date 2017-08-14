package org.camunda.bpm.spring.boot.starter.rest.spi;

import java.util.Set;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;

public class SpringBootProcessEngineProvider implements ProcessEngineProvider {

  @Override
  public ProcessEngine getDefaultProcessEngine() {
    return ProcessEngines.getDefaultProcessEngine();
  }

  @Override
  public ProcessEngine getProcessEngine(String name) {
    return ProcessEngines.getProcessEngine(name);
  }

  @Override
  public Set<String> getProcessEngineNames() {
    return ProcessEngines.getProcessEngines().keySet();
  }

}
