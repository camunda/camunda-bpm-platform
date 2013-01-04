package org.camunda.bpm.engine.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/process-definition")
@Produces("application/json")
public interface ProcessDefinitionService {

	@GET
	@Path("/query")
	Response getProcessDefinitions(@QueryParam("pid") String processDefinitionIdFragment);
}
