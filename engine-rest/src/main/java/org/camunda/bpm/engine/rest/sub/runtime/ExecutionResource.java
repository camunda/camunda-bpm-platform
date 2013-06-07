package org.camunda.bpm.engine.rest.sub.runtime;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.rest.dto.runtime.ExecutionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ExecutionTriggerDto;
import org.camunda.bpm.engine.rest.sub.VariableResource;

public interface ExecutionResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  ExecutionDto getExecution();
  
  @POST
  @Path("/signal")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  void signalExecution(ExecutionTriggerDto triggerDto);
  
  @Path("/localVariables")
  VariableResource getLocalVariables();
  
  @POST
  @Path("/messageSubscriptions/{messageName}/trigger")
  @Consumes(MediaType.APPLICATION_JSON)
  void triggerMessageEvent(@PathParam("messageName") String messageName, ExecutionTriggerDto triggerDto);
  
}
