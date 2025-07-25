/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest;

import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.rest.hal.Hal;
import org.camunda.bpm.engine.rest.sub.task.TaskReportResource;
import org.camunda.bpm.engine.rest.sub.task.TaskResource;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Produces(MediaType.APPLICATION_JSON)
public interface TaskRestService {

  public static final String PATH = "/task";

  @Path("/{id}")
  TaskResource getTask(@PathParam("id") String id,
                       @QueryParam("withCommentAttachmentInfo") boolean withCommentAttachmentInfo,
                       @QueryParam("withTaskVariablesInReturn") boolean withTaskVariablesInReturn,
                       @QueryParam("withTaskLocalVariablesInReturn") boolean withTaskLocalVariablesInReturn);

  @GET
  @Produces({MediaType.APPLICATION_JSON, Hal.APPLICATION_HAL_JSON})
  Object getTasks(@Context Request request, @Context UriInfo uriInfo,
                  @QueryParam("firstResult") Integer firstResult, @QueryParam("maxResults") Integer maxResults);

  /**
   * Expects the same parameters as {@link TaskRestService#getTasks(UriInfo, Integer, Integer)} (as
   * JSON message body) and allows more than one variable check.
   * @param query
   * @param firstResult
   * @param maxResults
   * @return
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  List<TaskDto> queryTasks(TaskQueryDto query,
      @QueryParam("firstResult") Integer firstResult, @QueryParam("maxResults") Integer maxResults);

  @GET
  @Path("/count")
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto getTasksCount(@Context UriInfo uriInfo);

  @POST
  @Path("/count")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto queryTasksCount(TaskQueryDto query);

  @POST
  @Path("/create")
  @Consumes(MediaType.APPLICATION_JSON)
  void createTask(TaskDto taskDto);

  @Path("/report")
  TaskReportResource getTaskReportResource();


}
