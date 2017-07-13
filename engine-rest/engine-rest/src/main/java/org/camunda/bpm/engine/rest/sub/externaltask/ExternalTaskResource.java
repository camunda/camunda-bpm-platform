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
package org.camunda.bpm.engine.rest.sub.externaltask;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.rest.dto.externaltask.CompleteExternalTaskDto;
import org.camunda.bpm.engine.rest.dto.externaltask.ExtendLockOnExternalTaskDto;
import org.camunda.bpm.engine.rest.dto.externaltask.ExternalTaskBpmnError;
import org.camunda.bpm.engine.rest.dto.externaltask.ExternalTaskDto;
import org.camunda.bpm.engine.rest.dto.externaltask.ExternalTaskFailureDto;
import org.camunda.bpm.engine.rest.dto.runtime.PriorityDto;
import org.camunda.bpm.engine.rest.dto.runtime.RetriesDto;

/**
 * @author Thorben Lindhauer
 * @author Askar Akhmerov
 *
 */
public interface ExternalTaskResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  ExternalTaskDto getExternalTask();

  @GET
  @Path("/errorDetails")
  @Produces(MediaType.TEXT_PLAIN)
  String getErrorDetails();

  @PUT
  @Path("/retries")
  @Consumes(MediaType.APPLICATION_JSON)
  void setRetries(RetriesDto dto);
  
  @PUT
  @Path("/priority")
  @Consumes(MediaType.APPLICATION_JSON)
  void setPriority(PriorityDto dto);

  @POST
  @Path("/complete")
  @Consumes(MediaType.APPLICATION_JSON)
  void complete(CompleteExternalTaskDto dto);

  @POST
  @Path("/failure")
  @Consumes(MediaType.APPLICATION_JSON)
  void handleFailure(ExternalTaskFailureDto dto);
  
  @POST
  @Path("/bpmnError")
  @Consumes(MediaType.APPLICATION_JSON)
  void handleBpmnError(ExternalTaskBpmnError dto);

  @POST
  @Path("/unlock")
  void unlock();

  @POST
  @Path("/extendLock")
  @Consumes(MediaType.APPLICATION_JSON)
  void extendLock(ExtendLockOnExternalTaskDto extendLockDto);
}
