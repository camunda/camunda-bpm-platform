package com.camunda.fox.cockpit.engine.spi.impl;

import com.camunda.fox.cockpit.engine.spi.ProcessEngines;
import com.camunda.fox.platform.api.ProcessEngineService;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import org.activiti.engine.ProcessEngine;

/**
 *
 * @author nico.rehwaldt
 */
@ApplicationScoped
public class PlatformProcessEnginesProvider implements ProcessEngines, Serializable {

  @EJB(lookup=
		      "java:global/" +
		      "camunda-fox-platform/" +
		      "process-engine/" +
		      "PlatformService!com.camunda.fox.platform.api.ProcessEngineService")
	private ProcessEngineService processEngineService;
  
  @Override
  public List<ProcessEngine> getProcessEngines() {
    return processEngineService.getProcessEngines();
  }

  @Override
  public ProcessEngine getProcessEngineNamed(String name) {
    return processEngineService.getProcessEngine(name);
  }

  @Override
  public ProcessEngine getDefaultProcessEngine() {
    return processEngineService.getDefaultProcessEngine();
  }
}
