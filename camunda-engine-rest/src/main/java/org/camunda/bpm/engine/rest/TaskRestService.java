package org.camunda.bpm.engine.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.task.*;

@Path("/task")
@Produces(MediaType.APPLICATION_JSON)
public interface TaskRestService {

  @GET
  @Path("/")
  List<TaskDto> getTasks(TaskQueryDto query,
      @QueryParam("firstResult") Integer firstResult, @QueryParam("maxResults") Integer maxResults);

  @GET
  @Path("/{id}")
  TaskDto getTask(@PathParam("id") String id);

  @GET
  @Path("/{id}/form")
  FormDto getForm(@PathParam("id") String id);

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

  @GET
  @Path("/count")
  CountResultDto getTasksCount(TaskQueryDto query);

  @POST
  @Path("/count")
  @Consumes(MediaType.APPLICATION_JSON)
  CountResultDto queryTasksCount(TaskQueryDto query);

  @POST
  @Path("/{id}/claim")
  @Consumes(MediaType.APPLICATION_JSON)
  void claim(@PathParam("id") String taskId, UserIdDto dto);

  @POST
  @Path("/{id}/unclaim")
  @Consumes(MediaType.APPLICATION_JSON)
  void unclaim(@PathParam("id") String taskId, UserIdDto dto);

  @POST
  @Path("/{id}/complete")
  @Consumes(MediaType.APPLICATION_JSON)
  void complete(@PathParam("id") String taskId, CompleteTaskDto dto);

  @POST
  @Path("/{id}/resolve")
  @Consumes(MediaType.APPLICATION_JSON)
  void resolve(@PathParam("id") String taskId, CompleteTaskDto dto);
  
  @POST
  @Path("/{id}/delegate")
  @Consumes(MediaType.APPLICATION_JSON)
  void delegate(@PathParam("id") String taskId, UserIdDto delegatedUser);

  @GET
  @Path("/groups")
  // FIXME discussion : move this into a group resource?
  GroupInfoDto getGroupInfo(@QueryParam("userId") String userId);
}
