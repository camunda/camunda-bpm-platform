package org.camunda.bpm.engine.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.StatisticsResultDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDiagramDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.FormDto;

@Path("/process-definition")
@Produces(MediaType.APPLICATION_JSON)
public interface ProcessDefinitionRestService {

  public static final String APPLICATION_BPMN20_XML = "application/bpmn20+xml";
  public static final MediaType APPLICATION_BPMN20_XML_TYPE =
      new MediaType("application", "bpmn20+xml");

  /**
   * Exposes the {@link ProcessDefinitionQuery} interface as a REST service.
   * @param query
   * @param firstResult
   * @param maxResults
   * @return
   */
	@GET
	@Path("/")
	List<ProcessDefinitionDto> getProcessDefinitions(ProcessDefinitionQueryDto query,
	    @QueryParam("firstResult") Integer firstResult, @QueryParam("maxResults") Integer maxResults);

	@GET
  @Path("/count")
  CountResultDto getProcessDefinitionsCount(ProcessDefinitionQueryDto query);

	@GET
	@Path("/{id}")
	ProcessDefinitionDto getProcessDefinition(@PathParam("id") String processDefinitionId);

	@GET
	@Path("/{id}/xml")
	ProcessDefinitionDiagramDto getProcessDefinitionBpmn20Xml(@PathParam("id") String processDefinitionId);

	@POST
	@Path("/{id}/start")
	@Consumes(MediaType.APPLICATION_JSON)
	ProcessInstanceDto startProcessInstance(@Context UriInfo context, @PathParam("id") String processDefinitionId, StartProcessInstanceDto parameters);

	@GET
	@Path("/statistics")
	List<StatisticsResultDto> getStatistics(@QueryParam("failedJobs") Boolean includeFailedJobs);

	@GET
	@Path("/{id}/statistics")
	List<StatisticsResultDto> getActivityStatistics(@PathParam("id") String processDefinitionId, @QueryParam("failedJobs") Boolean includeFailedJobs);

  @GET
  @Path("/{id}/startForm")
  FormDto getStartForm(@PathParam("id") String processDefinitionId);
}
