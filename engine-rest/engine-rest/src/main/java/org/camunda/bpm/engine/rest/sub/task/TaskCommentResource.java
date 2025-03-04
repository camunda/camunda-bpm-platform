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
package org.camunda.bpm.engine.rest.sub.task;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.camunda.bpm.engine.rest.dto.task.CommentDto;

public interface TaskCommentResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  List<CommentDto> getComments();

  @GET
  @Path("/{commentId}")
  @Produces(MediaType.APPLICATION_JSON)
  CommentDto getComment(@PathParam("commentId") String commentId);

  @POST
  @Path("/create")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  CommentDto createComment(@Context UriInfo uriInfo, CommentDto comment);

  @DELETE
  @Path("/{commentId}")
  @Produces(MediaType.APPLICATION_JSON)
  void deleteComment(@PathParam("commentId") String commentId);

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  void updateComment(CommentDto comment);

  @DELETE
  void deleteComments();

}
