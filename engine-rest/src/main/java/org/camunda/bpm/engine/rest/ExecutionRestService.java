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

import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.runtime.ExecutionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ExecutionQueryDto;

@Path(ExecutionRestService.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface ExecutionRestService {
  
  public static final String PATH = "/execution";

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  List<ExecutionDto> getExecutions(@Context UriInfo uriInfo,
      @QueryParam("firstResult") Integer firstResult,
      @QueryParam("maxResults") Integer maxResults);
  
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  List<ExecutionDto> queryExecutions(ExecutionQueryDto query,
      @QueryParam("firstResult") Integer firstResult,
      @QueryParam("maxResults") Integer maxResults);
  
  @GET
  @Path("/count")
  CountResultDto getExecutionsCount(@Context UriInfo uriInfo);
  
  @POST
  @Path("/count")
  @Consumes(MediaType.APPLICATION_JSON)
  CountResultDto queryExecutionsCount(ExecutionQueryDto query);
}
