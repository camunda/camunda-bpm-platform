package com.camunda.fox.cockpit.platform;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;

import org.activiti.engine.ProcessEngine;

import com.camunda.fox.cdi.FoxProcessEngineLookup;
import com.camunda.fox.platform.api.ProcessEngineService;

/**
 * <p>Implementation of the {@link FoxProcessEngineLookup} interface looking 
 * up the container managed process engine using the {@link ProcessEngineService}.</p>
 * 
 * @author Daniel Meyer
 */
@ApplicationScoped
public class FoxPlatformProcessEngineLookup implements FoxProcessEngineLookup {

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
}
