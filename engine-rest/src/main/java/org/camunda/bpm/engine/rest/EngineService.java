package org.camunda.bpm.engine.rest;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/engine/{name}")
public interface EngineService {

  @Path("/process-definition")
  ProcessDefinitionService getProcessDefinitionService(@PathParam("name") String engineName);
  
  @Path("/process-instance")
  ProcessInstanceService getProcessInstanceService(@PathParam("name") String engineName);
  
  @Path("/process-task")
  TaskRestService getTaskRestService(@PathParam("name") String engineName);
}
