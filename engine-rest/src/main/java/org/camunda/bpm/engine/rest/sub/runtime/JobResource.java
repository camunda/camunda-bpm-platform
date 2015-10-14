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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.rest.dto.runtime.JobDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobDuedateDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobPriorityDto;
import org.camunda.bpm.engine.rest.dto.runtime.RetriesDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobSuspensionStateDto;

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
  void setJobRetries(RetriesDto dto);

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

  @PUT
  @Path("/priority")
  @Consumes(MediaType.APPLICATION_JSON)
  void setJobPriority(JobPriorityDto dto);

}
