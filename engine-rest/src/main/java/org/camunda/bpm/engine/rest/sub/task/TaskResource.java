/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.sub.task;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.rest.dto.FormVariablesDto;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.FormDto;
import org.camunda.bpm.engine.rest.dto.task.IdentityLinkDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.dto.task.UserIdDto;
import org.camunda.bpm.engine.rest.hal.task.HalTask;
import org.camunda.bpm.engine.rest.sub.VariableResource;

import static org.camunda.bpm.engine.rest.hal.Hal.MEDIA_TYPE_HAL;

public interface TaskResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  TaskDto getTask();

  @GET
  @Produces(MEDIA_TYPE_HAL)
  HalTask getHalTask();

  @GET
  @Path("/form")
  @Produces(MediaType.APPLICATION_JSON)
  FormDto getForm();

  @POST
  @Path("/submit-form")
  @Consumes(MediaType.APPLICATION_JSON)
  void submit(CompleteTaskDto dto);

  @GET
  @Path("/rendered-form")
  @Produces(MediaType.APPLICATION_XHTML_XML)
  String getRenderedForm();

  @POST
  @Path("/claim")
  @Consumes(MediaType.APPLICATION_JSON)
  void claim(UserIdDto dto);

  @POST
  @Path("/unclaim")
  void unclaim();

  @POST
  @Path("/complete")
  @Consumes(MediaType.APPLICATION_JSON)
  void complete(CompleteTaskDto dto);

  @POST
  @Path("/resolve")
  @Consumes(MediaType.APPLICATION_JSON)
  void resolve(CompleteTaskDto dto);

  @POST
  @Path("/delegate")
  @Consumes(MediaType.APPLICATION_JSON)
  void delegate(UserIdDto delegatedUser);

  @POST
  @Path("/assignee")
  @Consumes(MediaType.APPLICATION_JSON)
  void setAssignee(UserIdDto dto);

  @GET
  @Path("/identity-links")
  @Produces(MediaType.APPLICATION_JSON)
  List<IdentityLinkDto> getIdentityLinks(@QueryParam("type") String type);

  @POST
  @Path("/identity-links")
  @Consumes(MediaType.APPLICATION_JSON)
  void addIdentityLink(IdentityLinkDto identityLink);

  @POST
  @Path("/identity-links/delete")
  @Consumes(MediaType.APPLICATION_JSON)
  void deleteIdentityLink(IdentityLinkDto identityLink);

  @Path("/comment")
  TaskCommentResource getTaskCommentResource();

  @Path("/attachment")
  TaskAttachmentResource getAttachmentResource();

  @Path("/localVariables")
  VariableResource getLocalVariables();

  @GET
  @Path("/form-variables")
  @Produces(MediaType.APPLICATION_JSON)
  FormVariablesDto getFormVariables(@QueryParam("variableNames") String variableNames);

}


