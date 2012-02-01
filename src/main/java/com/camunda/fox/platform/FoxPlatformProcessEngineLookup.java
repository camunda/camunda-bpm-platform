package com.camunda.fox.platform;

import javax.ejb.EJB;

import org.activiti.engine.ProcessEngine;

import com.camunda.fox.cdi.FoxProcessEngineLookup;
import com.camunda.fox.platform.api.ProcessEngineService;

/**
 * <p>Implementation of the {@link FoxProcessEngineLookup} interface looking 
 * up the container managed process engine using the {@link ProcessEngineService}.</p>
 * 
 * @author Daniel Meyer
 */
public class FoxPlatformProcessEngineLookup implements FoxProcessEngineLookup {

	@EJB(lookup=
		      "java:global/" +
		      "camunda-fox-platform/" +
		      "process-engine/" +
		      "ProcessEngineService!com.camunda.fox.platform.api.ProcessEngineService")
	private ProcessEngineService processEngineService;
	
	@Override
	public ProcessEngine getProcessEngine() {
		return processEngineService.getProcessEngine();
	}

	@Override
	public void ungetProcessEngine() {
		// engine managed by platform
	}
	
}
