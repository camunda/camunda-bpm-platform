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

import org.camunda.bpm.engine.rest.dto.task.CommentDto;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

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

}
