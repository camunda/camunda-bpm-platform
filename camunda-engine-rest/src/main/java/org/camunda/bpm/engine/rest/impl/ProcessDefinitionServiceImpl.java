package org.camunda.bpm.engine.rest.impl;

import javax.ws.rs.core.Response;

import org.camunda.bpm.engine.rest.AbstractEngineService;
import org.camunda.bpm.engine.rest.ProcessDefinitionService;

public class ProcessDefinitionServiceImpl extends AbstractEngineService implements ProcessDefinitionService {

	@Override
	public Response getProcessDefinitions(String processDefinitionIdFragment) {
	  
	  
		return Response.ok("").build();
	}

}
