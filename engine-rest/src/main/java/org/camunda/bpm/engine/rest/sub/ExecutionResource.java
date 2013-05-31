package org.camunda.bpm.engine.rest.sub;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.rest.dto.runtime.ExecutionDto;
import org.camunda.bpm.engine.rest.dto.runtime.VariableListDto;

public interface ExecutionResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  ExecutionDto getExecution();
  
  @POST
  @Path("/signal")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  void signalExecution(VariableListDto variables);
  
  @Path("/localVariables")
  VariableResource getLocalVariables();
  
  @POST
  @Path("/messageSubscriptions/{messageName}/trigger")
  @Consumes(MediaType.APPLICATION_JSON)
  void triggerMessageEvent(@PathParam("messageName") String messageName, VariableListDto variables);
  
}
