package org.camunda.bpm.engine.rest;

import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.VariableListDto;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

@Path("/process-instance")
@Produces(MediaType.APPLICATION_JSON)
public interface ProcessInstanceService {

  /**
   * Exposes the {@link ProcessInstanceQuery} interface as a REST service.
   * 
   * @param query
   * @param firstResult
   * @param maxResults
   * @return
   */
  @GET
  @Path("/")
  List<ProcessInstanceDto> getProcessInstances(ProcessInstanceQueryDto query,
      @QueryParam("firstResult") Integer firstResult,
      @QueryParam("maxResults") Integer maxResults);

  /**
   * Expects the same parameters as
   * {@link ProcessInstanceService#getProcessInstances(ProcessInstanceQueryDto, Integer, Integer)} (as a JSON message body)
   * and allows for any number of variable checks.
   * 
   * @param query
   * @param firstResult
   * @param maxResults
   * @return
   */
  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  List<ProcessInstanceDto> queryProcessInstances(ProcessInstanceQueryDto query,
      @QueryParam("firstResult") Integer firstResult,
      @QueryParam("maxResults") Integer maxResults);

  @GET
  @Path("/count")
  CountResultDto getProcessInstancesCount(ProcessInstanceQueryDto query);
  
  @POST
  @Path("/count")
  @Consumes(MediaType.APPLICATION_JSON)
  CountResultDto queryProcessInstancesCount(ProcessInstanceQueryDto query);

  @GET
  @Path("/{id}/variables")
  VariableListDto getVariables(@PathParam("id") String processInstanceId);
}
