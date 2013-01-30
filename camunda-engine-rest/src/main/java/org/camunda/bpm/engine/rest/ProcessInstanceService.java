package org.camunda.bpm.engine.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.rest.dto.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.ProcessInstanceQueryDto;

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
}
