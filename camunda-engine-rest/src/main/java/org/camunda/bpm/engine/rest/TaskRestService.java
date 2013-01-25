package org.camunda.bpm.engine.rest;

import java.util.List;

import javax.ws.rs.GET;
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
}
