package com.camunda.fox.cockpit.platform;

import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;

import org.activiti.engine.ProcessEngine;

import com.camunda.fox.cockpit.api.service.CockpitProcessEngineService;
import com.camunda.fox.platform.api.ProcessEngineService;

/**
 * <p>Implementation of the {@link CockpitProcessEngineService} interface looking 
 * up the container managed process engine using the {@link ProcessEngineService}.</p>
 * 
 * @author Daniel Meyer
 * @author christian.lipphardt@camunda.com
 */
@ApplicationScoped
public class FoxPlatformProcessEngineLookup implements CockpitProcessEngineService {

	@EJB(lookup=
		      "java:global/" +
		      "camunda-fox-platform/" +
		      "process-engine/" +
		      "PlatformService!com.camunda.fox.platform.api.ProcessEngineService")
	private ProcessEngineService processEngineService;
	
	@Override
	public ProcessEngine getProcessEngine() {
		return processEngineService.getDefaultProcessEngine();
	}

	@Override
	public void ungetProcessEngine() {
		// engine managed by platform
  }

  @Override
  public ProcessEngine getDefaultProcessEngine() {
    return processEngineService.getDefaultProcessEngine();
  }

  @Override
  public List<ProcessEngine> getProcessEngines() {
    return processEngineService.getProcessEngines();
  }

  @Override
  public List<String> getProcessEngineNames() {
    return processEngineService.getProcessEngineNames();
  }

  @Override
  public ProcessEngine getProcessEngine(String name) {
    return processEngineService.getProcessEngine(name);
  }
}
