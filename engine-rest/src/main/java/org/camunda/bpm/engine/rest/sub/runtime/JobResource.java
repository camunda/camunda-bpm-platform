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
package org.camunda.bpm.engine.rest.sub.runtime;

import org.camunda.bpm.engine.rest.dto.runtime.JobDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobDuedateDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobRetriesDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobSuspensionStateDto;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

public interface JobResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  JobDto getJob();

  @GET
  @Path("/stacktrace")
  @Produces(MediaType.TEXT_PLAIN)
  String getStacktrace();

  @PUT
  @Path("/retries")
  @Consumes(MediaType.APPLICATION_JSON)
  void setJobRetries(JobRetriesDto dto);

  @POST
  @Path("/execute")
  void executeJob();

  @PUT
  @Path("/duedate")
  @Consumes(MediaType.APPLICATION_JSON)
  void setJobDuedate(JobDuedateDto dto);

  @PUT
  @Path("/suspended")
  @Consumes(MediaType.APPLICATION_JSON)
  void updateSuspensionState(JobSuspensionStateDto dto);

}
