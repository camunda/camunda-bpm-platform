package org.camunda.bpm.engine.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.rest.dto.history.HistoricActivityInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricActivityInstanceQueryDto;

@Path(HistoricActivityInstanceRestService.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface HistoricActivityInstanceRestService {

	  public static final String PATH = "/historic-activity-instance"; 
	  
	  /**
	   * Exposes the {@link HistoricActivityInstanceQuery} interface as a REST service.
	   * 
	   * @param query
	   * @param firstResult
	   * @param maxResults
	   * @return
	   */
	  @GET
	  @Produces(MediaType.APPLICATION_JSON)
	  List<HistoricActivityInstanceDto> getHistoricActivityInstances(@Context UriInfo uriInfo,
	      @QueryParam("firstResult") Integer firstResult,
	      @QueryParam("maxResults") Integer maxResults);

	  /**
	   * @param query
	   * @param firstResult
	   * @param maxResults
	   * @return
	   */
	  @POST
	  @Consumes(MediaType.APPLICATION_JSON)
	  @Produces(MediaType.APPLICATION_JSON)
	  List<HistoricActivityInstanceDto> queryHistoricActivityInstances(HistoricActivityInstanceQueryDto query,
	      @QueryParam("firstResult") Integer firstResult,
	      @QueryParam("maxResults") Integer maxResults);
	
}
