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

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.rest.dto.task.AttachmentDto;
import org.camunda.bpm.engine.rest.mapper.MultipartFormData;

public interface TaskAttachmentResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  List<AttachmentDto> getAttachments();

  @GET
  @Path("/{attachmentId}")
  @Produces(MediaType.APPLICATION_JSON)
  AttachmentDto getAttachment(@PathParam("attachmentId") String attachmentId);

  @GET
  @Path("/{attachmentId}/data")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  InputStream getAttachmentData(@PathParam("attachmentId") String attachmentId);

  @DELETE
  @Path("/{attachmentId}")
  @Produces(MediaType.APPLICATION_JSON)
  void deleteAttachment(@PathParam("attachmentId") String attachmentId);

  @POST
  @Path("/create")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  AttachmentDto addAttachment(@Context UriInfo uriInfo, MultipartFormData multipartFormData);

}