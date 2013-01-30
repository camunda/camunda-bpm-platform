package org.camunda.bpm.engine.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.rest.dto.TaskDto;
import org.camunda.bpm.engine.rest.dto.TaskQueryDto;

@Path("/task")
@Produces(MediaType.APPLICATION_JSON)
public interface TaskRestService {

  @GET
  @Path("/")
  List<TaskDto> getTasks(TaskQueryDto query,
      @QueryParam("firstResult") Integer firstResult, @QueryParam("maxResults") Integer maxResults);
  
  /**
   * Expects the same parameters as {@link TaskRestService#getTasks(TaskQueryDto, Integer, Integer)} (as 
   * JSON message body) and allows more than one variable check.
   * @param query
   * @param firstResult
   * @param maxResults
   * @return
   */
  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  List<TaskDto> queryTasks(TaskQueryDto query,
      @QueryParam("firstResult") Integer firstResult, @QueryParam("maxResults") Integer maxResults);
}
