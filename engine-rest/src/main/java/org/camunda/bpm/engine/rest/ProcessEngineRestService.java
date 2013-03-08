package org.camunda.bpm.engine.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.rest.dto.ProcessEngineDto;

@Path("/engine")
public interface ProcessEngineRestService {

  @Path("/{name}/process-definition")
  ProcessDefinitionRestService getProcessDefinitionService(@PathParam("name") String engineName);
  
  @Path("/{name}/process-instance")
  ProcessInstanceRestService getProcessInstanceService(@PathParam("name") String engineName);
  
  @Path("/{name}/task")
  TaskRestService getTaskRestService(@PathParam("name") String engineName);
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  List<ProcessEngineDto> getProcessEngineNames();
}
