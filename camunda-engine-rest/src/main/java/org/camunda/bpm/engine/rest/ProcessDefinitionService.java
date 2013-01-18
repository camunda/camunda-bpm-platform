package org.camunda.bpm.engine.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.rest.dto.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.ProcessDefinitionQueryDto;

@Path("/process-definition")
@Produces(MediaType.APPLICATION_JSON)
public interface ProcessDefinitionService {

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
}
