package org.camunda.bpm.engine.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.rest.dto.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.ProcessInstanceQueryDto;

@Path("/process-instance")
@Produces(MediaType.APPLICATION_JSON)
public interface ProcessInstanceService {

  @GET
  @Path("/")
  List<ProcessInstanceDto> getProcessInstances(ProcessInstanceQueryDto query, 
      @QueryParam("firstResult") Integer firstResult, @QueryParam("maxResults") Integer maxResults);
}
